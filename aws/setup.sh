#!/usr/bin/env bash
# aws/setup.sh - AWS setup script for UH Groupings API.
#
# This script is non-interactive end to end. All inputs come from files; the
# script never prompts. If any required input is missing the script exits
# before any AWS API call.
#
# Sources of input:
#   - aws/.env                                   non-secret deployment configuration
#                                                (required: AWS_PROJECT_ID, VPC_ID, AWS_ACCOUNT_ID)
#   - $HOME/.$USER-conf/uh-groupings-api-overrides.properties
#                                                Grouper service-account password
#                                                (`grouperClient.webService.password`);
#                                                read directly from the developer's home dir
#
# JWT signing key:
#   Generated here with `openssl rand -base64 32` and written to
#   groupings/api/jwt-secret. The API project owns this value; companion UI
#   projects reference the same Secrets Manager entry from their own task
#   definitions. Re-running setup preserves the existing JWT secret to avoid
#   silently invalidating UI tokens; rotate it explicitly via the CLI command
#   documented in aws/docs/SECRETS.md (Secrets Manager Integration → Rotate the
#   JWT key).

set -euo pipefail

#
# Variables
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
VPC_TEMPLATE_PATH="${SCRIPT_DIR}/cloudformation/vpc.yml"
ECR_TEMPLATE_PATH="${SCRIPT_DIR}/cloudformation/ecr-repository.yml"
ECS_TEMPLATE_PATH="${SCRIPT_DIR}/cloudformation/ecs-service.yml"

# Shared IAM Identity Center (SSO) auth helpers (ensure_aws_session).
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib-auth.sh"

# Application secrets are read from the developer's overrides file in their
# home directory. The path is overridable for non-standard setups.
OVERRIDES_FILE="${OVERRIDES_FILE:-${HOME}/.$(id -un)-conf/uh-groupings-api-overrides.properties}"

AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-}"
ECR_REPOSITORY_URI=""
GROUPER_PASSWORD=""
# Populated from the vpc stack outputs by create_vpc_stack().
SUBNET_ID=""
# Populated from Secrets Manager by configure_secrets(); passed to the ECS stack.
GROUPER_PASSWORD_SECRET_ARN=""
JWT_SECRET_ARN=""

#
# Functions
#

log() {
    printf '%s\n' "$1"
}

error() {
    printf 'Error: %s\n' "$1" >&2
}

load_env_file() {
    if [[ ! -f "${ENV_FILE}" ]]; then
        error "Configuration file not found: ${ENV_FILE}"
        exit 1
    fi

    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a
}

apply_defaults() {
    AWS_REGION="${AWS_REGION:-us-west-2}"
    AWS_ENV="${AWS_ENV:-sandbox}"
    AWS_PROJECT_ID="${AWS_PROJECT_ID:-}"
    PROJECT_NAME="${PROJECT_NAME:-${AWS_PROJECT_ID}}"
    AWS_OWNER="${AWS_OWNER:-mhodges}"
    # Default 1, matching the single-AZ test posture and the DesiredCount
    # default in ecs-service.yml. Multi-AZ / higher counts are a prod concern.
    ECS_TASK_COUNT="${ECS_TASK_COUNT:-1}"
    VPC_ID="${VPC_ID:-}"
    APP_TIER="${APP_TIER:-test}"
    # NOTE: API_HOSTNAME / API_CERTIFICATE_ARN / API_HOSTED_ZONE_ID are
    # deprecated and deliberately not read. The API is private (ECS Service
    # Connect + sg-api-backend); public DNS, TLS, and the certificate belong to
    # the companion UI stack. See aws/.env and aws/AGENTS.md.
}

validate_config() {
    if [[ -z "${AWS_PROJECT_ID}" ]]; then
        error "AWS_PROJECT_ID must be set in aws/.env."
        exit 1
    fi
}

# APP_TIER is the single source of truth for the deployment tier: it selects the
# Spring profile (aws-<tier>) in the ECS stack, which in turn selects the Grouper
# backend and the app.environment label. This guard keeps it valid and consistent
# with AWS_ENV — before any AWS API call. There is no hostname/certificate
# invariant to enforce: the API has no public endpoint of its own.
validate_tier() {
    log "Validating deployment tier..."

    case "${APP_TIER}" in
        test|prod) ;;
        *)
            error "APP_TIER must be 'test' or 'prod' (got '${APP_TIER}')."
            exit 1
            ;;
    esac

    # A prod tier must only run in the prod environment.
    if [[ "${APP_TIER}" == "prod" && "${AWS_ENV}" != "prod" ]]; then
        error "APP_TIER=prod is only allowed when AWS_ENV=prod (got AWS_ENV=${AWS_ENV})."
        error "Set APP_TIER=test for non-prod environments."
        exit 1
    fi

    log "  Tier:        ${APP_TIER} (Spring profile aws-${APP_TIER})"
    log "  Ingress:     none — API is private (ECS Service Connect + sg-api-backend, no ALB)"
    log "✓ Deployment tier validated"
    log ""
}

validate_network_configuration() {
    log "Validating network configuration..."

    if [[ -z "${VPC_ID}" || "${VPC_ID}" == *xxxxx* ]]; then
        error "VPC_ID is not set in aws/.env (current value: '${VPC_ID:-<unset>}')."
        error "Set it to a real VPC ID (e.g., vpc-0a1b2c3d4e5f6789a) and re-run."
        exit 1
    fi

    log "  VPC ID:     ${VPC_ID}"
    log "  Subnet:     created by the vpc stack (aws/cloudformation/vpc.yml), single-AZ"
    log "✓ Network configuration validated"
    log ""
}

check_prerequisites() {
    log "Checking prerequisites..."

    # The CLI is required for enabling Browser-based authentication.
    if ! command -v aws >/dev/null 2>&1; then
        error "AWS CLI not installed"
        exit 1
    fi

    # Docker is required for building the image before it gets pushed.
    if ! command -v docker >/dev/null 2>&1; then
        error "Docker not installed"
        exit 1
    fi

    if ! command -v openssl >/dev/null 2>&1; then
        error "OpenSSL not installed (required to generate the JWT signing key)"
        exit 1
    fi

    log "✓ Prerequisites met"
    log ""
}

print_configuration() {
    log "=== ${PROJECT_NAME} - AWS Setup ==="
    log ""
    log "Configuration:"
    log "  AWS Region:    ${AWS_REGION}"
    log "  Environment:   ${AWS_ENV}"
    log "  Project ID:    ${AWS_PROJECT_ID}    (used in stack and resource names)"
    log "  CFN Owner:     ${AWS_OWNER}"
    log ""
}

validate_aws_account_id() {
    if [[ -z "${AWS_ACCOUNT_ID}" ]]; then
        error "AWS_ACCOUNT_ID is not set in aws/.env."
        error "Set it to your 12-digit AWS account ID and re-run."
        exit 1
    fi
    log "AWS Account ID: ${AWS_ACCOUNT_ID}"
    log ""
}

# Read a single property value from a Java-style properties file.
#   - Lines starting with '#' are treated as comments.
#   - The first '=' on a line separates key from value.
#   - Whitespace around the key, around the value, and a trailing CR are stripped
#     (matching the Java properties loader's behavior).
#   - Prints the value to stdout, or nothing if the key is absent.
read_property() {
    local file="$1"
    local key="$2"

    awk -v key="${key}" '
        /^[[:space:]]*#/ { next }
        /^[[:space:]]*$/ { next }
        {
            idx = index($0, "=")
            if (idx == 0) next
            k = substr($0, 1, idx - 1)
            v = substr($0, idx + 1)
            gsub(/\r$/, "", v)
            gsub(/^[[:space:]]+|[[:space:]]+$/, "", k)
            gsub(/^[[:space:]]+|[[:space:]]+$/, "", v)
            if (k == key) {
                print v
                exit
            }
        }
    ' "${file}"
}

load_overrides_file() {
    log "Validating overrides file..."

    if [[ ! -f "${OVERRIDES_FILE}" ]]; then
        error "Overrides file not found: ${OVERRIDES_FILE}"
        error ""
        error "The setup script reads the Grouper service-account password from this file."
        error "It lives in your home directory at:"
        error "  \$HOME/.\$(id -un)-conf/uh-groupings-api-overrides.properties"
        error ""
        error "Create that file (see docs/DEV_QUICKSTART.md in the repo root for the template) and re-run."
        exit 1
    fi

    GROUPER_PASSWORD="$(read_property "${OVERRIDES_FILE}" "grouperClient.webService.password")"

    if [[ -z "${GROUPER_PASSWORD}" ]]; then
        error "Required property missing or empty in ${OVERRIDES_FILE}:"
        error "  grouperClient.webService.password"
        error ""
        error "Add the Grouper service-account password and re-run."
        exit 1
    fi

    log "✓ Overrides file present with Grouper password"
    log ""
}

# CloudFormation's create-stack fails outright if the stack already exists, so
# re-running setup would abort. `aws cloudformation deploy` instead does a
# create-or-update via a change set and waits for it to finish. The one state
# deploy cannot recover from is ROLLBACK_COMPLETE (left by a failed *first*
# create): such a stack can be neither updated nor re-created, only deleted.
# This helper clears that state so the deploy that follows can proceed, which
# is what makes the whole script safely re-runnable.
delete_stack_if_rollback_complete() {
    local stack_name="$1"
    local status
    status="$(aws cloudformation describe-stacks \
      --stack-name "${stack_name}" \
      --query 'Stacks[0].StackStatus' \
      --output text \
      --region "${AWS_REGION}" 2>/dev/null || true)"

    if [[ "${status}" == "ROLLBACK_COMPLETE" ]]; then
        log "  ${stack_name} is in ROLLBACK_COMPLETE (failed prior create); deleting before redeploy..."
        aws cloudformation delete-stack \
          --stack-name "${stack_name}" \
          --region "${AWS_REGION}"
        aws cloudformation wait stack-delete-complete \
          --stack-name "${stack_name}" \
          --region "${AWS_REGION}"
    fi
}

# When `aws cloudformation deploy` fails, the CLI only reports that the stack
# operation failed — the actual reason lives in the stack events. Surface the
# FAILED events so the cause is visible without opening the AWS console.
report_stack_failure() {
    local stack_name="$1"
    error ""
    error "Stack '${stack_name}' failed to deploy. Failure reason(s):"
    aws cloudformation describe-stack-events \
      --stack-name "${stack_name}" \
      --query "reverse(StackEvents[?contains(ResourceStatus, 'FAILED')].[LogicalResourceId, ResourceStatusReason])" \
      --output text \
      --region "${AWS_REGION}" 2>/dev/null | sed 's/^/    /' || true
    error ""
    error "The stack will roll back automatically. Fix the cause above, then"
    error "re-run 'make aws-setup' — a ROLLBACK_COMPLETE stack is deleted and"
    error "recreated automatically. For subnet CIDR errors, adjust SubnetCidr"
    error "(or the VPC_ID) and validate first with 'make aws-check-vpc'."
}

create_vpc_stack() {
    log "Step 1: Creating VPC networking (subnet + endpoints)..."
    delete_stack_if_rollback_complete "${AWS_PROJECT_ID}-vpc-${AWS_ENV}"
    if ! aws cloudformation deploy \
      --stack-name "${AWS_PROJECT_ID}-vpc-${AWS_ENV}" \
      --template-file "${VPC_TEMPLATE_PATH}" \
      --parameter-overrides \
        "Owner=${AWS_OWNER}" \
        "Project=${AWS_PROJECT_ID}" \
        "Environment=${AWS_ENV}" \
        "VpcId=${VPC_ID}" \
        "MainRouteTableId=${MAIN_ROUTE_TABLE_ID}" \
      --no-fail-on-empty-changeset \
      --region "${AWS_REGION}"; then
        report_stack_failure "${AWS_PROJECT_ID}-vpc-${AWS_ENV}"
        exit 1
    fi

    SUBNET_ID="$(aws cloudformation describe-stacks \
      --stack-name "${AWS_PROJECT_ID}-vpc-${AWS_ENV}" \
      --query 'Stacks[0].Outputs[?OutputKey==`SubnetId`].OutputValue' \
      --output text \
      --region "${AWS_REGION}")"

    log "✓ VPC networking created"
    log "  Subnet ID:  ${SUBNET_ID}"
    log ""
}

create_ecr_repository() {
    log "Step 2: Creating ECR Repository..."
    delete_stack_if_rollback_complete "${AWS_PROJECT_ID}-ecr-${AWS_ENV}"
    if ! aws cloudformation deploy \
      --stack-name "${AWS_PROJECT_ID}-ecr-${AWS_ENV}" \
      --template-file "${ECR_TEMPLATE_PATH}" \
      --parameter-overrides \
        "Owner=${AWS_OWNER}" \
        "Project=${AWS_PROJECT_ID}" \
        "Environment=${AWS_ENV}" \
      --no-fail-on-empty-changeset \
      --region "${AWS_REGION}"; then
        report_stack_failure "${AWS_PROJECT_ID}-ecr-${AWS_ENV}"
        exit 1
    fi

    ECR_REPOSITORY_URI="$(aws cloudformation describe-stacks \
      --stack-name "${AWS_PROJECT_ID}-ecr-${AWS_ENV}" \
      --query 'Stacks[0].Outputs[?OutputKey==`RepositoryUri`].OutputValue' \
      --output text \
      --region "${AWS_REGION}")"

    log "✓ ECR Repository created: ${ECR_REPOSITORY_URI}"
    log ""
}

build_and_push_image() {
    log "Step 3: Building and pushing initial Docker image..."

    aws ecr get-login-password --region "${AWS_REGION}" | \
      docker login --username AWS --password-stdin "${ECR_REPOSITORY_URI}"

    docker build -t "${AWS_PROJECT_ID}:latest" "${REPO_ROOT}"
    docker tag "${AWS_PROJECT_ID}:latest" "${ECR_REPOSITORY_URI}:latest"
    docker push "${ECR_REPOSITORY_URI}:latest"

    log "✓ Image pushed to ECR"
    log ""
}

create_or_update_secret() {
    local secret_name="$1"
    local secret_value="$2"

    aws secretsmanager create-secret \
      --name "${secret_name}" \
      --secret-string "${secret_value}" \
      --region "${AWS_REGION}" 2>/dev/null || \
      aws secretsmanager update-secret \
        --secret-id "${secret_name}" \
        --secret-string "${secret_value}" \
        --region "${AWS_REGION}"
}

jwt_secret_exists_in_aws() {
    aws secretsmanager describe-secret \
        --secret-id "groupings/api/jwt-secret" \
        --region "${AWS_REGION}" \
        >/dev/null 2>&1
}

configure_secrets() {
    log "Step 4: Configuring secrets in AWS Secrets Manager..."
    log ""

    # Grouper password — overwrite from the overrides file, which is the
    # canonical source for this value (it's owned by the upstream IdP).
    create_or_update_secret "groupings/api/grouper-password" "${GROUPER_PASSWORD}"
    log "✓ groupings/api/grouper-password (from overrides file)"

    # JWT key — generated here once and preserved on re-run. The API project
    # owns this value; companion UI projects reference the same Secrets Manager
    # entry. Overwriting it on re-run would silently invalidate every active UI
    # token, so we only create it if it doesn't already exist. To rotate
    # explicitly, use the manual CLI command in aws/docs/SECRETS.md
    # (Secrets Manager Integration → Rotate the JWT key).
    if jwt_secret_exists_in_aws; then
        log "✓ groupings/api/jwt-secret already exists; preserving existing value"
        log "  (rotate explicitly via the CLI command in aws/docs/SECRETS.md;"
        log "  rotation requires redeploying every UI consumer)"
    else
        local generated_jwt
        generated_jwt="$(openssl rand -base64 32)"
        create_or_update_secret "groupings/api/jwt-secret" "${generated_jwt}"
        log "✓ groupings/api/jwt-secret generated and stored"
        log "  (UI projects must reference this same secret; they do not generate their own)"
    fi

    # Capture the full secret ARNs (they include a random 6-char suffix) so the
    # ECS task definition can reference them in its secrets[] block.
    GROUPER_PASSWORD_SECRET_ARN="$(aws secretsmanager describe-secret \
      --secret-id "groupings/api/grouper-password" \
      --query 'ARN' --output text --region "${AWS_REGION}")"
    JWT_SECRET_ARN="$(aws secretsmanager describe-secret \
      --secret-id "groupings/api/jwt-secret" \
      --query 'ARN' --output text --region "${AWS_REGION}")"
    log ""
}

# ECS requires an account-wide service-linked role (AWSServiceRoleForECS).
# AWS usually auto-creates it on first ECS use, but in restricted/shared
# accounts that doesn't always happen, and cluster creation then fails with
# "Unable to assume the service linked role." Create it once, idempotently.
ensure_ecs_service_linked_role() {
    if aws iam get-role --role-name AWSServiceRoleForECS >/dev/null 2>&1; then
        return 0
    fi
    log "  Creating the ECS service-linked role (one-time per account)..."
    aws iam create-service-linked-role --aws-service-name ecs.amazonaws.com >/dev/null 2>&1 || true
    if aws iam get-role --role-name AWSServiceRoleForECS >/dev/null 2>&1; then
        log "  ✓ ECS service-linked role present"
    else
        error "Could not create the ECS service-linked role (AWSServiceRoleForECS)."
        error "Create it once with:"
        error "  aws iam create-service-linked-role --aws-service-name ecs.amazonaws.com"
        error "then re-run 'make aws-setup'."
        exit 1
    fi
}

deploy_ecs_infrastructure() {
    log "Step 5: Creating ECS cluster and service (this may take 10 minutes)..."
    ensure_ecs_service_linked_role
    delete_stack_if_rollback_complete "${AWS_PROJECT_ID}-ecs-${AWS_ENV}"
    if ! aws cloudformation deploy \
      --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
      --template-file "${ECS_TEMPLATE_PATH}" \
      --parameter-overrides \
        "Owner=${AWS_OWNER}" \
        "Project=${AWS_PROJECT_ID}" \
        "Environment=${AWS_ENV}" \
        "Tier=${APP_TIER}" \
        "VpcId=${VPC_ID}" \
        "SubnetIds=${SUBNET_ID}" \
        "ContainerImage=${ECR_REPOSITORY_URI}:latest" \
        "DesiredCount=${ECS_TASK_COUNT}" \
        "GrouperPasswordSecretArn=${GROUPER_PASSWORD_SECRET_ARN}" \
        "JwtSecretArn=${JWT_SECRET_ARN}" \
      --capabilities CAPABILITY_NAMED_IAM \
      --no-fail-on-empty-changeset \
      --region "${AWS_REGION}"; then
        report_stack_failure "${AWS_PROJECT_ID}-ecs-${AWS_ENV}"
        exit 1
    fi

    log "✓ ECS cluster and API service created"
    log ""
}

print_summary() {
    log "=== Setup Complete ==="
    log ""
    log "Resources created:"
    log "  - VPC Subnet:     ${SUBNET_ID} (in ${VPC_ID}, single-AZ)"
    log "  - ECR Repository: ${ECR_REPOSITORY_URI}"
    log "  - ECS Cluster:    ${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
    log "  - ECS Service:    ${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"
    log "  - API task SG:    ${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-sg-api-backend (exported for the UI stack)"
    log ""
    log "The API is private: no load balancer, no public endpoint, no inbound client"
    log "until the companion UI adds its ingress rule. By project decision the API is"
    log "NOT functionally exercised until the UI is deployed, so there is nothing to"
    log "curl and 'execute-command' is intentionally disabled."
    log ""
    log "Provisioning is verified at the infrastructure level: the stacks completed,"
    log "and a runningCount of ${ECS_TASK_COUNT} proves the image pulled through the ECR"
    log "endpoints and both secrets resolved through the Secrets Manager endpoint."
    log "Check with 'make aws-service-events' and 'make aws-logs'."
    log ""
    log "NOTE: the API health endpoint depends on on-prem Grouper WS. Grouper is a"
    log "live, required dependency, but the data-center connectivity mechanism is"
    log "still pending infra-team confirmation, so Grouper calls will fail for now."
    log "The container health check is intentionally disabled so the task is not"
    log "restart-looped. See aws/AGENTS.md, 'Grouper WS connectivity'."
    log ""
    log "Next steps:"
    log "  1. Create a GitHub connection in the AWS Console (see aws/docs/AWS_DEPLOYMENT.md)"
    log "  2. Deploy the CodePipeline stack"
    log ""
    log "For detailed instructions, see aws/docs/AWS_QUICKSTART.md and aws/docs/AWS_DEPLOYMENT.md"
}

#
# Main
#

# Phase 1 - load and validate every input before any AWS API call
load_env_file
apply_defaults
validate_config
validate_tier
validate_network_configuration
load_overrides_file
print_configuration
check_prerequisites
ensure_aws_session || { error "AWS authentication failed."; exit 1; }
validate_aws_account_id

# Phase 2 - provision AWS resources
create_vpc_stack
create_ecr_repository
build_and_push_image
configure_secrets
deploy_ecs_infrastructure
print_summary
