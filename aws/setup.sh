#!/usr/bin/env bash
# aws/setup.sh - AWS setup script for UH Groupings API.
# All configuration settings are read from aws/.env.

set -euo pipefail

#
# Variables
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
ECR_TEMPLATE_PATH="${SCRIPT_DIR}/cloudformation/ecr-repository.yml"
ECS_TEMPLATE_PATH="${SCRIPT_DIR}/cloudformation/ecs-cluster.yml"

AWS_ACCOUNT_ID=""
ECR_REPOSITORY_URI=""
ALB_URL=""

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
    NON_INTERACTIVE="${NON_INTERACTIVE:-}"
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
        error "OpenSSL not installed"
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

confirm_setup() {
    if [[ -n "${NON_INTERACTIVE}" ]]; then
        return
    fi

    local reply
    read -r -p "Continue with setup? (y/n) " -n 1 reply
    echo

    if [[ ! "${reply}" =~ ^[Yy]$ ]]; then
        log "Setup cancelled."
        exit 1
    fi

    log ""
}

create_ecr_repository() {
    log "Step 3: Creating ECR Repository..."
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
    log "Step 4: Building and pushing initial Docker image..."

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

configure_secrets() {
    local grouper_password
    local jwt_secret

    log "Step 2: Setting up AWS Secrets Manager..."
    log "Storing only the two truly sensitive runtime values."
    log "Non-secret values (Grouper URL, username, etc.) are configured in the"
    log "ECS task definition environment[] array, not here."
    log ""

    read -r -s -p "Grouper Password: " grouper_password
    echo

    jwt_secret="$(openssl rand -base64 32)"

    create_or_update_secret "groupings/api/grouper-password" "${grouper_password}"
    create_or_update_secret "groupings/api/jwt-secret" "${jwt_secret}"

    log "✓ Secrets configured: grouper-password, jwt-secret"
    log ""
}

collect_network_configuration() {
    log "Step 1: Checking VPC configuration..."

    if [[ -z "${VPC_ID}" ]]; then
        log "Available VPCs:"
        aws ec2 describe-vpcs \
          --query "Vpcs[*].[VpcId,Tags[?Key=='Name'].Value|[0],CidrBlock]" \
          --output table \
          --region "${AWS_REGION}"
        read -r -p "Enter VPC ID to use: " VPC_ID
    else
        log "Using VPC ID from aws/.env: ${VPC_ID}"
    fi

    if [[ -z "${SUBNET_IDS}" ]]; then
        log "Available subnets in VPC ${VPC_ID}:"
        aws ec2 describe-subnets \
          --filters "Name=vpc-id,Values=${VPC_ID}" \
          --query "Subnets[*].[SubnetId,AvailabilityZone,CidrBlock]" \
          --output table \
          --region "${AWS_REGION}"
        read -r -p "Enter Subnet IDs (comma-separated, at least 2): " SUBNET_IDS
    else
        log "Using Subnet IDs from aws/.env: ${SUBNET_IDS}"
    fi

    log ""
}

deploy_ecs_infrastructure() {
    log "Step 5: Creating ECS cluster and service..."

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
    log "For detailed instructions, see docs/AWS_SETUP.md"
}

#
# Main
#

# Phase 1 - load and verify configuration
load_env_file
apply_defaults
validate_config
print_configuration
check_prerequisites
fetch_aws_account_id
confirm_setup

# Phase 2 - collect all interactive input up front (network, secrets)
collect_network_configuration
configure_secrets

# Phase 3 - provision AWS resources
create_ecr_repository
build_and_push_image
deploy_ecs_infrastructure
print_summary
