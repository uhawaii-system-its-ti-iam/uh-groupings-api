# AWS Setup Guide for UH Groupings API

This guide walks you through setting up the complete AWS infrastructure and CI/CD pipeline.

## Prerequisites

- AWS Account with appropriate permissions
- AWS CLI installed and configured (`aws configure`)
- Docker Desktop installed and running
- GitHub repository access
- Basic understanding of terminal commands

## Architecture Overview

```
GitHub → CodePipeline → CodeBuild → ECR → ECS/Fargate
                                      ↓
                              Secrets Manager
                                      ↓
                               CloudWatch Logs
```

## Phase 1: Automated Setup (ECR + ECS)

The `aws/setup.sh` script automates the following:

1. Creates an ECR repository for Docker images
2. Builds and pushes the initial Docker image
3. Configures secrets in AWS Secrets Manager (interactive prompts)
4. Collects VPC/subnet configuration
5. Deploys the ECS cluster, service, and load balancer

### Running the Setup Script

**From the repository root (recommended):**

```bash
make aws-setup
```

**Or directly:**

```bash
cd aws/
./setup.sh
```

**Non-interactive (CI/CD):**

```bash
make aws-setup-ci
```

### What You'll Need During Setup

The script will prompt for:
- **Grouper API URL** — your Grouper WS endpoint
- **Grouper Username/Password** — service account credentials
- **Database Password** — application database password
- **VPC ID** — an existing VPC (the script lists available VPCs)
- **Subnet IDs** — at least 2 subnets in different Availability Zones (the script lists available subnets)

A JWT secret is auto-generated via `openssl rand -base64 32`.

### Configuration

Edit `aws/.env` before running:

```bash
# Required
AWS_REGION=us-west-2
AWS_ENV=sandbox
AWS_PROJECT_ID=uh-groupings-api

# Display name
PROJECT_NAME="UH Groupings API"

# Network (leave as placeholders to be prompted)
VPC_ID=vpc-xxxxx
SUBNET_IDS=subnet-xxxxx,subnet-yyyyy

# ECS
DESIRED_COUNT=1
```

Environment variables passed inline take precedence over `.env` values:

```bash
AWS_REGION=us-east-1 AWS_ENV=production make aws-setup
```

### What the Script Creates

| Resource | Stack Name | Description |
|----------|-----------|-------------|
| ECR Repository | `{project}-ecr-{env}` | Docker image registry with scanning and lifecycle policies |
| ECS Cluster + ALB | `{project}-ecs-{env}` | Fargate cluster, service, load balancer, security groups, IAM roles |
| Secrets | `groupings/api/*` | Grouper URL, credentials, JWT secret, DB password |

### After Setup Completes

The script prints:
- ECR Repository URI
- ECS Cluster name
- Application URL (ALB endpoint)

Verify the deployment:

```bash
curl <ALB_URL>/actuator/health
```

## Phase 2: CodePipeline Setup (Manual)

The CI/CD pipeline requires a manual step — the GitHub CodeStar connection must be authorized through the AWS Console via OAuth. This cannot be automated.

### Step 1: Create GitHub Connection

1. Go to **AWS Console → Developer Tools → Settings → Connections**
2. Click **Create connection**
3. Select **GitHub** (or GitHub Enterprise Server)
4. Enter your GitHub URL
5. Complete the OAuth authorization flow
6. Copy the **Connection ARN**

The connection starts in `PENDING` status and becomes `AVAILABLE` after authorization.

### Step 2: Deploy CodePipeline Stack

```bash
cd aws/

# Source your environment
source .env

# Get ECS resource names from the stack created by setup.sh
ECS_CLUSTER=$(aws cloudformation describe-stacks \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'Stacks[0].Outputs[?OutputKey==`ClusterName`].OutputValue' \
  --output text \
  --region "${AWS_REGION}")

ECS_SERVICE=$(aws cloudformation describe-stacks \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'Stacks[0].Outputs[?OutputKey==`ServiceName`].OutputValue' \
  --output text \
  --region "${AWS_REGION}")

# Deploy the pipeline
aws cloudformation create-stack \
  --stack-name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}" \
  --template-body file://cloudformation/codepipeline.yml \
  --parameters \
    ParameterKey=Environment,ParameterValue="${AWS_ENV}" \
    ParameterKey=GitHubEnterpriseUrl,ParameterValue="https://github.com" \
    ParameterKey=GitHubOwner,ParameterValue="${GITHUB_ORG}" \
    ParameterKey=GitHubRepo,ParameterValue="${GITHUB_REPO}" \
    ParameterKey=GitHubBranch,ParameterValue="${GITHUB_BRANCH}" \
    ParameterKey=ECSClusterName,ParameterValue="${ECS_CLUSTER}" \
    ParameterKey=ECSServiceName,ParameterValue="${ECS_SERVICE}" \
  --capabilities CAPABILITY_NAMED_IAM \
  --region "${AWS_REGION}"

# Wait for completion
aws cloudformation wait stack-create-complete \
  --stack-name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}" \
  --region "${AWS_REGION}"
```

**Important:** Since `buildspec.yml` is in the `aws/` subdirectory, ensure the CodeBuild project references `aws/buildspec.yml` as the buildspec path.

### Step 3: Verify Pipeline

```bash
# Trigger the pipeline
aws codepipeline start-pipeline-execution \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"

# Monitor status
aws codepipeline get-pipeline-state \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"
```

## Phase 3: CloudWatch Monitoring (Optional)

### Set Up Alarms

```bash
# CPU Utilization Alarm
aws cloudwatch put-metric-alarm \
  --alarm-name "${AWS_PROJECT_ID}-high-cpu-${AWS_ENV}" \
  --alarm-description "Alert when CPU exceeds 80%" \
  --metric-name CPUUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2

# Memory Utilization Alarm
aws cloudwatch put-metric-alarm \
  --alarm-name "${AWS_PROJECT_ID}-high-memory-${AWS_ENV}" \
  --alarm-description "Alert when memory exceeds 80%" \
  --metric-name MemoryUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2
```

## Troubleshooting

### Common Issues

**ECS tasks fail to start**
```bash
# Check task stopped reason
aws ecs describe-tasks \
  --cluster "${ECS_CLUSTER}" \
  --tasks $(aws ecs list-tasks --cluster "${ECS_CLUSTER}" --query 'taskArns[0]' --output text) \
  --query 'tasks[0].stoppedReason'

# Check CloudWatch logs
aws logs tail /ecs/uh-groupings-api --since 30m
```

**Health checks failing**
- Verify Spring Boot is listening on the expected port
- Check security group allows inbound traffic
- Ensure `/actuator/health` endpoint is accessible

**Secrets not loading**
```bash
# Verify task execution role has Secrets Manager permissions
aws iam get-role-policy \
  --role-name "uh-groupings-ecs-execution-${AWS_ENV}" \
  --policy-name SecretsManagerAccess
```

**Pipeline not triggering on push**
- Verify the CodeStar connection status is `Available` (not `Pending`)
- Check CodePipeline execution history for errors

**CodeBuild fails with "Cannot connect to Docker daemon"**
- Ensure "Privileged mode" is enabled in the CodeBuild project

### Stack Creation Failed

```bash
aws cloudformation describe-stack-events \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'StackEvents[?ResourceStatus==`CREATE_FAILED`]'
```

## Teardown

Remove all AWS resources:

```bash
make aws-teardown
```

Or manually:

```bash
cd aws/
source .env

aws cloudformation delete-stack --stack-name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"
aws cloudformation delete-stack --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}"
aws cloudformation delete-stack --stack-name "${AWS_PROJECT_ID}-ecr-${AWS_ENV}"

# Delete secrets
aws secretsmanager delete-secret --secret-id groupings/api/grouper-url --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/grouper-username --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/grouper-password --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/jwt-secret --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/db-password --force-delete-without-recovery
```

## Cost Estimation (Sandbox)

| Resource | Monthly Cost |
|----------|-------------|
| ECR | ~$0.10/GB |
| ECS Fargate (2 tasks, 0.5 vCPU, 1GB RAM) | ~$30-40 |
| Application Load Balancer | ~$20 |
| CloudWatch Logs | ~$1-5 |
| CodeBuild | ~$0.005/minute (build time only) |
| **Total** | **~$50-70** |

## Useful Commands

```bash
# View pipeline status
aws codepipeline get-pipeline-state --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"

# Manually start pipeline
aws codepipeline start-pipeline-execution --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"

# View ECS service events
aws ecs describe-services --cluster "${ECS_CLUSTER}" --services "${ECS_SERVICE}" \
  --query 'services[0].events[0:5]'

# Scale ECS service
aws ecs update-service --cluster "${ECS_CLUSTER}" --service "${ECS_SERVICE}" --desired-count 3

# View recent logs
aws logs tail /ecs/uh-groupings-api --since 1h --follow

# Force new deployment (without code change)
aws ecs update-service --cluster "${ECS_CLUSTER}" --service "${ECS_SERVICE}" --force-new-deployment
```

## Next Steps

1. Set up production environment with similar configuration
2. Add custom domain and SSL certificate (ACM + Route 53)
3. Configure autoscaling policies
4. Set up CloudWatch alerts to email/Slack
5. Add security scanning (ECR image scanning is enabled by default)
6. Implement blue/green deployments (see [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md))

## Resources

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS CodePipeline Documentation](https://docs.aws.amazon.com/codepipeline/)
- [AWS Quick Start Guide](AWS_QUICKSTART.md)
- [AWS Deployment Guide](AWS_DEPLOYMENT.md)
- Internal: #groupings-dev Slack channel
