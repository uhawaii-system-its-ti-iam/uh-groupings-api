#!/usr/bin/env bash
# aws/setup.sh - AWS setup script for UH Groupings API.
#
# This script is non-interactive end to end. All inputs come from files; the
# script never prompts. If any required input is missing the script exits
# before any AWS API call.
#
# Sources of input:
#   - aws/.env                                   non-secret deployment configuration
#                                                (required: AWS_PROJECT_ID, VPC_ID, SUBNET_IDS)
#   - $HOME/.$USER-conf/uh-groupings-api-overrides.properties
#                                                Grouper service-account password
#                                                (`grouperClient.webService.password`);
#                                                bind-mounted into the AWS CLI container
#                                                at /overrides/
#
# JWT signing key:
#   Generated here with `openssl rand -base64 32` and written to
#   groupings/api/jwt-secret. The API project owns this value; companion UI
#   projects reference the same Secrets Manager entry from their own task
#   definitions. Re-running setup preserves the existing JWT secret to avoid
#   silently invalidating UI tokens; rotate it explicitly via the CLI command
#   documented in docs/SECRETS.md (Secrets Manager Integration → Rotate the
#   JWT key).

set -euo pipefail

#
# Variables
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
ECR_TEMPLATE_PATH="${SCRIPT_DIR}/cloudformation/ecr-repository.yml"
ECS_TEMPLATE_PATH="${SCRIPT_DIR}/cloudformation/ecs-cluster.yml"

# Application secrets are read from the developer's overrides file, which
# docker-compose.aws.yml bind-mounts into the AWS CLI container at /overrides/.
# The path is overridable for running the script outside the container.
OVERRIDES_FILE="${OVERRIDES_FILE:-/overrides/uh-groupings-api-overrides.properties}"

AWS_ACCOUNT_ID=""
ECR_REPOSITORY_URI=""
ALB_URL=""
GROUPER_PASSWORD=""

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
    ECS_TASK_COUNT="${ECS_TASK_COUNT:-2}"
    VPC_ID="${VPC_ID:-}"
    SUBNET_IDS="${SUBNET_IDS:-}"
}

validate_config() {
    if [[ -z "${AWS_PROJECT_ID}" ]]; then
        error "AWS_PROJECT_ID must be set in aws/.env."
        exit 1
    fi
}

validate_network_configuration() {
    log "Validating network configuration..."

    if [[ -z "${VPC_ID}" || "${VPC_ID}" == *xxxxx* ]]; then
        error "VPC_ID is not set in aws/.env (current value: '${VPC_ID:-<unset>}')."
        error "Set it to a real VPC ID (e.g., vpc-0a1b2c3d4e5f6789a) and re-run."
        exit 1
    fi

    if [[ -z "${SUBNET_IDS}" || "${SUBNET_IDS}" == *xxxxx* || "${SUBNET_IDS}" == *yyyyy* ]]; then
        error "SUBNET_IDS is not set in aws/.env (current value: '${SUBNET_IDS:-<unset>}')."
        error "Set it to a comma-separated list of real subnet IDs in at least 2 AZs and re-run."
        exit 1
    fi

    log "  VPC ID:     ${VPC_ID}"
    log "  Subnet IDs: ${SUBNET_IDS}"
    log "✓ Network configuration validated"
    log ""
}

check_prerequisites() {
    log "Checking prerequisites..."

    if ! command -v aws >/dev/null 2>&1; then
        error "AWS CLI not installed"
        exit 1
    fi

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

fetch_aws_account_id() {
    AWS_ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
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
        error "Inside the AWS CLI container the file is bind-mounted from the host:"
        error "  \$HOME/.\$USER-conf/uh-groupings-api-overrides.properties"
        error ""
        error "Create that file (see docs/DEV_QUICKSTART.md for the template) and re-run."
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

create_ecr_repository() {
    log "Step 1: Creating ECR Repository..."
    aws cloudformation create-stack \
      --stack-name "${AWS_PROJECT_ID}-ecr-${AWS_ENV}" \
      --template-body "file://${ECR_TEMPLATE_PATH}" \
      --parameters \
        "ParameterKey=Owner,ParameterValue=${AWS_OWNER}" \
        "ParameterKey=Project,ParameterValue=${AWS_PROJECT_ID}" \
        "ParameterKey=Environment,ParameterValue=${AWS_ENV}" \
      --region "${AWS_REGION}"

    log "Waiting for ECR stack creation..."
    aws cloudformation wait stack-create-complete \
      --stack-name "${AWS_PROJECT_ID}-ecr-${AWS_ENV}" \
      --region "${AWS_REGION}"

    ECR_REPOSITORY_URI="$(aws cloudformation describe-stacks \
      --stack-name "${AWS_PROJECT_ID}-ecr-${AWS_ENV}" \
      --query 'Stacks[0].Outputs[?OutputKey==`RepositoryUri`].OutputValue' \
      --output text \
      --region "${AWS_REGION}")"

    log "✓ ECR Repository created: ${ECR_REPOSITORY_URI}"
    log ""
}

build_and_push_image() {
    log "Step 2: Building and pushing initial Docker image..."

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
    log "Step 3: Configuring secrets in AWS Secrets Manager..."
    log ""

    # Grouper password — overwrite from the overrides file, which is the
    # canonical source for this value (it's owned by the upstream IdP).
    create_or_update_secret "groupings/api/grouper-password" "${GROUPER_PASSWORD}"
    log "✓ groupings/api/grouper-password (from overrides file)"

    # JWT key — generated here once and preserved on re-run. The API project
    # owns this value; companion UI projects reference the same Secrets Manager
    # entry. Overwriting it on re-run would silently invalidate every active UI
    # token, so we only create it if it doesn't already exist. To rotate
    # explicitly, use the manual CLI command in docs/SECRETS.md
    # (Secrets Manager Integration → Rotate the JWT key).
    if jwt_secret_exists_in_aws; then
        log "✓ groupings/api/jwt-secret already exists; preserving existing value"
        log "  (rotate explicitly via the CLI command in docs/SECRETS.md;"
        log "  rotation requires redeploying every UI consumer)"
    else
        local generated_jwt
        generated_jwt="$(openssl rand -base64 32)"
        create_or_update_secret "groupings/api/jwt-secret" "${generated_jwt}"
        log "✓ groupings/api/jwt-secret generated and stored"
        log "  (UI projects must reference this same secret; they do not generate their own)"
    fi
    log ""
}

deploy_ecs_infrastructure() {
    log "Step 4: Creating ECS cluster and service..."

    aws cloudformation create-stack \
      --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
      --template-body "file://${ECS_TEMPLATE_PATH}" \
      --parameters \
        "ParameterKey=Owner,ParameterValue=${AWS_OWNER}" \
        "ParameterKey=Project,ParameterValue=${AWS_PROJECT_ID}" \
        "ParameterKey=Environment,ParameterValue=${AWS_ENV}" \
        "ParameterKey=VpcId,ParameterValue=${VPC_ID}" \
        "ParameterKey=SubnetIds,ParameterValue=${SUBNET_IDS}" \
        "ParameterKey=ContainerImage,ParameterValue=${ECR_REPOSITORY_URI}:latest" \
        "ParameterKey=DesiredCount,ParameterValue=${ECS_TASK_COUNT}" \
      --capabilities CAPABILITY_NAMED_IAM \
      --region "${AWS_REGION}"

    log "Waiting for ECS stack creation (this may take 10 minutes)..."
    aws cloudformation wait stack-create-complete \
      --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
      --region "${AWS_REGION}"

    ALB_URL="$(aws cloudformation describe-stacks \
      --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
      --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
      --output text \
      --region "${AWS_REGION}")"

    log "✓ ECS cluster created"
    log "Application URL: ${ALB_URL}"
    log ""
}

print_summary() {
    log "=== Setup Complete ==="
    log ""
    log "Resources created:"
    log "  - ECR Repository: ${ECR_REPOSITORY_URI}"
    log "  - ECS Cluster:    ${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
    log "  - ECS Service:    ${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"
    log "  - Application URL: ${ALB_URL}"
    log ""
    log "Next steps:"
    log "  1. Configure GitHub Enterprise connection in AWS Console"
    log "  2. Deploy CodePipeline stack"
    log "  3. Test the application: curl ${ALB_URL}/actuator/health"
    log ""
    log "For detailed instructions, see docs/AWS_QUICKSTART.md and docs/AWS_DEPLOYMENT.md"
}

#
# Main
#

# Phase 1 - load and validate every input before any AWS API call
load_env_file
apply_defaults
validate_config
validate_network_configuration
load_overrides_file
print_configuration
check_prerequisites
fetch_aws_account_id

# Phase 2 - provision AWS resources
create_ecr_repository
build_and_push_image
configure_secrets
deploy_ecs_infrastructure
print_summary
