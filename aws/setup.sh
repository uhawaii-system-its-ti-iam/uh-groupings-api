#!/bin/bash
# AWS Setup Script for UH Groupings API
# This script automates the AWS infrastructure setup

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
AWS_REGION="${AWS_REGION:-us-west-2}"
ENVIRONMENT="${ENVIRONMENT:-sandbox}"
PROJECT_NAME="uh-groupings-api"

echo -e "${GREEN}=== UH Groupings API - AWS Setup ===${NC}"
echo ""

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI not installed${NC}"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Prerequisites met${NC}"
echo ""

# Get AWS Account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "AWS Account ID: $AWS_ACCOUNT_ID"
echo "AWS Region: $AWS_REGION"
echo "Environment: $ENVIRONMENT"
echo ""

# Prompt for confirmation
read -p "Continue with setup? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
fi

# Step 1: Create ECR Repository
echo -e "${YELLOW}Step 1: Creating ECR Repository...${NC}"
aws cloudformation create-stack \
  --stack-name uh-groupings-ecr-${ENVIRONMENT} \
  --template-body file://aws/cloudformation/ecr-repository.yml \
  --parameters \
    ParameterKey=RepositoryName,ParameterValue=${PROJECT_NAME} \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
  --region ${AWS_REGION}

echo "Waiting for ECR stack creation..."
aws cloudformation wait stack-create-complete \
  --stack-name uh-groupings-ecr-${ENVIRONMENT} \
  --region ${AWS_REGION}

ECR_REPOSITORY_URI=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecr-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`RepositoryUri`].OutputValue' \
  --output text \
  --region ${AWS_REGION})

echo -e "${GREEN}✓ ECR Repository created: ${ECR_REPOSITORY_URI}${NC}"
echo ""

# Step 2: Build and push initial image
echo -e "${YELLOW}Step 2: Building and pushing initial Docker image...${NC}"

# Login to ECR
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin ${ECR_REPOSITORY_URI}

# Build image
docker build -t ${PROJECT_NAME}:latest .

# Tag and push
docker tag ${PROJECT_NAME}:latest ${ECR_REPOSITORY_URI}:latest
docker push ${ECR_REPOSITORY_URI}:latest

echo -e "${GREEN}✓ Image pushed to ECR${NC}"
echo ""

# Step 3: Create secrets
echo -e "${YELLOW}Step 3: Setting up AWS Secrets Manager...${NC}"
echo "Please enter your configuration values:"

read -p "Grouper API URL: " GROUPER_URL
read -p "Grouper Username: " GROUPER_USERNAME
read -s -p "Grouper Password: " GROUPER_PASSWORD
echo ""
read -s -p "Database Password: " DB_PASSWORD
echo ""

# Generate JWT secret
JWT_SECRET=$(openssl rand -base64 32)

# Create secrets
aws secretsmanager create-secret \
  --name groupings/api/grouper-url \
  --secret-string "${GROUPER_URL}" \
  --region ${AWS_REGION} 2>/dev/null || \
  aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-url \
  --secret-string "${GROUPER_URL}" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name groupings/api/grouper-username \
  --secret-string "${GROUPER_USERNAME}" \
  --region ${AWS_REGION} 2>/dev/null || \
  aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-username \
  --secret-string "${GROUPER_USERNAME}" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name groupings/api/grouper-password \
  --secret-string "${GROUPER_PASSWORD}" \
  --region ${AWS_REGION} 2>/dev/null || \
  aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-password \
  --secret-string "${GROUPER_PASSWORD}" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name groupings/api/jwt-secret \
  --secret-string "${JWT_SECRET}" \
  --region ${AWS_REGION} 2>/dev/null || \
  aws secretsmanager update-secret \
  --secret-id groupings/api/jwt-secret \
  --secret-string "${JWT_SECRET}" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name groupings/api/db-password \
  --secret-string "${DB_PASSWORD}" \
  --region ${AWS_REGION} 2>/dev/null || \
  aws secretsmanager update-secret \
  --secret-id groupings/api/db-password \
  --secret-string "${DB_PASSWORD}" \
  --region ${AWS_REGION}

echo -e "${GREEN}✓ Secrets configured${NC}"
echo ""

# Step 4: Get VPC information
echo -e "${YELLOW}Step 4: Checking VPC configuration...${NC}"
echo "Available VPCs:"
aws ec2 describe-vpcs --query "Vpcs[*].[VpcId,Tags[?Key=='Name'].Value|[0],CidrBlock]" --output table --region ${AWS_REGION}

read -p "Enter VPC ID to use: " VPC_ID

echo "Available subnets in VPC ${VPC_ID}:"
aws ec2 describe-subnets --filters "Name=vpc-id,Values=${VPC_ID}" \
  --query "Subnets[*].[SubnetId,AvailabilityZone,CidrBlock]" --output table --region ${AWS_REGION}

read -p "Enter Subnet IDs (comma-separated, at least 2): " SUBNET_IDS

echo ""

# Step 5: Deploy ECS infrastructure
echo -e "${YELLOW}Step 5: Creating ECS cluster and service...${NC}"
aws cloudformation create-stack \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --template-body file://aws/cloudformation/ecs-cluster.yml \
  --parameters \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=VpcId,ParameterValue=${VPC_ID} \
    ParameterKey=SubnetIds,ParameterValue=\"${SUBNET_IDS}\" \
    ParameterKey=ContainerImage,ParameterValue=${ECR_REPOSITORY_URI}:latest \
    ParameterKey=DesiredCount,ParameterValue=2 \
  --capabilities CAPABILITY_NAMED_IAM \
  --region ${AWS_REGION}

echo "Waiting for ECS stack creation (this may take 10 minutes)..."
aws cloudformation wait stack-create-complete \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --region ${AWS_REGION}

ALB_URL=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text \
  --region ${AWS_REGION})

echo -e "${GREEN}✓ ECS cluster created${NC}"
echo -e "${GREEN}Application URL: ${ALB_URL}${NC}"
echo ""

# Summary
echo -e "${GREEN}=== Setup Complete ===${NC}"
echo ""
echo "Resources created:"
echo "  - ECR Repository: ${ECR_REPOSITORY_URI}"
echo "  - ECS Cluster: uh-groupings-${ENVIRONMENT}"
echo "  - Application URL: ${ALB_URL}"
echo ""
echo "Next steps:"
echo "  1. Configure GitHub Enterprise connection in AWS Console"
echo "  2. Deploy CodePipeline stack"
echo "  3. Test the application: curl ${ALB_URL}/actuator/health"
echo ""
echo "For detailed instructions, see docs/AWS_SETUP.md"
