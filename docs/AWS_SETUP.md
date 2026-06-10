# AWS Setup Guide for UH Groupings API

This guide walks you through setting up the complete CI/CD pipeline for your first AWS project.

## Prerequisites

- AWS Account with appropriate permissions
- AWS CLI installed and configured (`aws configure`)
- Docker installed locally
- GitHub Enterprise repository access
- Basic understanding of terminal commands

## Architecture Overview

```
GitHub Enterprise → CodePipeline → CodeBuild → ECR → ECS/Fargate
                                      ↓
                              Secrets Manager
                                      ↓
                               CloudWatch Logs
```

## Phase 1: Initial AWS Setup (15-20 minutes)

### Step 1: Set Environment Variables

```bash
export AWS_REGION="us-west-2"
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export ENVIRONMENT="sandbox"
export PROJECT_NAME="uh-groupings-api"
```

### Step 2: Create ECR Repository

```bash
# Create ECR repository using CloudFormation
aws cloudformation create-stack \
  --stack-name uh-groupings-ecr-${ENVIRONMENT} \
  --template-body file://aws/cloudformation/ecr-repository.yml \
  --parameters \
    ParameterKey=RepositoryName,ParameterValue=${PROJECT_NAME} \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT}

# Wait for stack creation
aws cloudformation wait stack-create-complete \
  --stack-name uh-groupings-ecr-${ENVIRONMENT}

# Get the ECR repository URI
export ECR_REPOSITORY_URI=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecr-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`RepositoryUri`].OutputValue' \
  --output text)

echo "ECR Repository URI: $ECR_REPOSITORY_URI"
```

### Step 3: Test Local Docker Build

Before deploying to AWS, test your Docker setup locally:

```bash
# Build the Docker image
docker build -t ${PROJECT_NAME}:local .

# Run locally
docker-compose up

# Test the health endpoint
curl http://localhost:8080/actuator/health

# Stop when done
docker-compose down
```

### Step 4: Push Initial Image to ECR

```bash
# Login to ECR
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin ${ECR_REPOSITORY_URI}

# Tag the image
docker tag ${PROJECT_NAME}:local ${ECR_REPOSITORY_URI}:latest

# Push to ECR
docker push ${ECR_REPOSITORY_URI}:latest
```

## Phase 2: Secrets Management (10 minutes)

### Step 5: Store Secrets in AWS Secrets Manager

```bash
# Create secrets for the application
aws secretsmanager create-secret \
  --name groupings/api/grouper-url \
  --description "Grouper API URL" \
  --secret-string "https://your-grouper-server.example.com/grouper-ws"

aws secretsmanager create-secret \
  --name groupings/api/grouper-username \
  --secret-string "your-grouper-username"

aws secretsmanager create-secret \
  --name groupings/api/grouper-password \
  --secret-string "your-grouper-password"

aws secretsmanager create-secret \
  --name groupings/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)"

aws secretsmanager create-secret \
  --name groupings/api/db-password \
  --secret-string "your-database-password"
```

**Important:** Replace placeholder values with your actual credentials!

### Step 6: Verify Secrets

```bash
# List all secrets
aws secretsmanager list-secrets --query "SecretList[?starts_with(Name, 'groupings/')].Name"

# Test retrieving a secret (optional)
aws secretsmanager get-secret-value --secret-id groupings/api/grouper-url --query SecretString --output text
```

## Phase 3: VPC and Networking (15 minutes)

### Step 7: Identify or Create VPC

```bash
# Check existing VPCs
aws ec2 describe-vpcs --query "Vpcs[*].[VpcId,Tags[?Key=='Name'].Value|[0],CidrBlock]" --output table

# Set VPC ID (use existing or create new)
export VPC_ID="vpc-xxxxxxxxx"  # Replace with your VPC ID

# Get subnet IDs (need at least 2 in different AZs for ALB)
aws ec2 describe-subnets --filters "Name=vpc-id,Values=${VPC_ID}" \
  --query "Subnets[*].[SubnetId,AvailabilityZone,CidrBlock]" --output table

# Set subnet IDs (comma-separated, at least 2)
export SUBNET_IDS="subnet-xxxxx,subnet-yyyyy"
```

**Note:** If you don't have a VPC, create one using the AWS Console or CLI. You need:
- 1 VPC
- At least 2 public subnets in different Availability Zones

## Phase 4: ECS Cluster and Service (20 minutes)

### Step 8: Deploy ECS Infrastructure

```bash
# Deploy ECS cluster with ALB
aws cloudformation create-stack \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --template-body file://aws/cloudformation/ecs-cluster.yml \
  --parameters \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=VpcId,ParameterValue=${VPC_ID} \
    ParameterKey=SubnetIds,ParameterValue=\"${SUBNET_IDS}\" \
    ParameterKey=ContainerImage,ParameterValue=${ECR_REPOSITORY_URI}:latest \
    ParameterKey=DesiredCount,ParameterValue=2 \
  --capabilities CAPABILITY_NAMED_IAM

# Wait for stack creation (this may take 5-10 minutes)
aws cloudformation wait stack-create-complete \
  --stack-name uh-groupings-ecs-${ENVIRONMENT}

# Get the Load Balancer URL
export ALB_URL=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text)

echo "Application URL: ${ALB_URL}"

# Test the application
curl ${ALB_URL}/actuator/health
```

### Step 9: Get ECS Resource Names

```bash
# Get cluster and service names for CodePipeline
export ECS_CLUSTER=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`ClusterName`].OutputValue' \
  --output text)

export ECS_SERVICE=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`ServiceName`].OutputValue' \
  --output text)

echo "ECS Cluster: ${ECS_CLUSTER}"
echo "ECS Service: ${ECS_SERVICE}"
```

## Phase 5: CI/CD Pipeline Setup (25 minutes)

### Step 10: Configure GitHub Enterprise Connection

**Important:** This requires AWS Console access for the first setup.

1. Go to AWS Console → Developer Tools → Settings → Connections
2. Click "Create connection"
3. Select "GitHub Enterprise Server"
4. Enter your GitHub Enterprise URL
5. Follow the wizard to complete the OAuth handshake
6. Copy the Connection ARN

```bash
# Set the GitHub connection ARN from Console
export GITHUB_CONNECTION_ARN="arn:aws:codestar-connections:${AWS_REGION}:${AWS_ACCOUNT_ID}:connection/xxxxxx"
```

### Step 11: Create CodePipeline

**Option A: Using CloudFormation (Recommended)**

```bash
aws cloudformation create-stack \
  --stack-name uh-groupings-pipeline-${ENVIRONMENT} \
  --template-body file://aws/cloudformation/codepipeline.yml \
  --parameters \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=GitHubEnterpriseUrl,ParameterValue="https://github.your-company.com" \
    ParameterKey=GitHubOwner,ParameterValue="uhawaii-system-its-ti-iam" \
    ParameterKey=GitHubRepo,ParameterValue="uh-groupings-api" \
    ParameterKey=GitHubBranch,ParameterValue="main" \
    ParameterKey=ECSClusterName,ParameterValue=${ECS_CLUSTER} \
    ParameterKey=ECSServiceName,ParameterValue=${ECS_SERVICE} \
  --capabilities CAPABILITY_NAMED_IAM

# Wait for completion
aws cloudformation wait stack-create-complete \
  --stack-name uh-groupings-pipeline-${ENVIRONMENT}
```

**Option B: Using AWS Console (Easier for First Time)**

1. Go to CodePipeline → Create pipeline
2. Pipeline name: `uh-groupings-api-pipeline-sandbox`
3. Service role: Create new role
4. Source: GitHub Enterprise, select your connection
5. Repository: uhawaii-system-its-ti-iam/uh-groupings-api
6. Branch: main
7. Build provider: AWS CodeBuild
8. Create new build project: uh-groupings-api-build-sandbox
9. Environment: Managed image, Standard, Ubuntu, standard:7.0
10. Enable privileged mode (for Docker)
11. Use buildspec.yml from repository
12. Deploy provider: Amazon ECS
13. Cluster: Select your cluster
14. Service: Select your service
15. Create pipeline

## Phase 6: Testing and Verification (10 minutes)

### Step 12: Trigger First Deployment

```bash
# Commit and push a small change to trigger pipeline
git add .
git commit -m "Setup AWS CI/CD pipeline"
git push origin main

# Monitor pipeline execution
aws codepipeline get-pipeline-state --name uh-groupings-api-pipeline-${ENVIRONMENT}

# Or watch in the Console:
# https://console.aws.amazon.com/codesuite/codepipeline/pipelines
```

### Step 13: Monitor Deployment

```bash
# Watch ECS service deployment
aws ecs describe-services \
  --cluster ${ECS_CLUSTER} \
  --services ${ECS_SERVICE} \
  --query 'services[0].deployments'

# Check CloudWatch logs
aws logs tail /ecs/uh-groupings-api --follow

# Test the deployed application
curl ${ALB_URL}/actuator/health
curl ${ALB_URL}/actuator/info
```

### Step 14: Verify Complete Setup

```bash
# Check all resources
echo "=== Resource Summary ==="
echo "ECR Repository: ${ECR_REPOSITORY_URI}"
echo "ECS Cluster: ${ECS_CLUSTER}"
echo "ECS Service: ${ECS_SERVICE}"
echo "Load Balancer: ${ALB_URL}"
echo "Region: ${AWS_REGION}"
```

## Phase 7: CloudWatch Monitoring (Optional, 10 minutes)

### Step 15: Create CloudWatch Dashboard

```bash
# Create a simple dashboard
aws cloudwatch put-dashboard \
  --dashboard-name uh-groupings-api-${ENVIRONMENT} \
  --dashboard-body file://aws/cloudwatch-dashboard.json
```

### Step 16: Set Up Alarms

```bash
# CPU Utilization Alarm
aws cloudwatch put-metric-alarm \
  --alarm-name uh-groupings-api-high-cpu-${ENVIRONMENT} \
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
  --alarm-name uh-groupings-api-high-memory-${ENVIRONMENT} \
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

**1. CodeBuild fails with "Cannot connect to Docker daemon"**
- Ensure "Privileged mode" is enabled in CodeBuild project
- Check that the build image supports Docker

**2. ECS tasks fail to start**
```bash
# Check task stopped reason
aws ecs describe-tasks \
  --cluster ${ECS_CLUSTER} \
  --tasks $(aws ecs list-tasks --cluster ${ECS_CLUSTER} --query 'taskArns[0]' --output text) \
  --query 'tasks[0].stoppedReason'

# Check CloudWatch logs
aws logs tail /ecs/uh-groupings-api --since 30m
```

**3. Health checks failing**
- Verify Spring Boot is listening on port 8080
- Check security group allows inbound traffic on 8080
- Ensure `/actuator/health` endpoint is accessible

**4. Secrets not loading**
```bash
# Verify task execution role has Secrets Manager permissions
aws iam get-role-policy \
  --role-name uh-groupings-ecs-execution-${ENVIRONMENT} \
  --policy-name SecretsManagerAccess
```

**5. Pipeline not triggering on push**
- Verify GitHub webhook is active
- Check CodeStar connection status is "Available"
- Review CodePipeline execution history for errors

## Cost Estimation (sandbox environment)

- ECR: ~$0.10/GB/month (minimal for images)
- ECS Fargate: ~$30-40/month (2 tasks, 0.5 vCPU, 1GB RAM)
- ALB: ~$20/month
- CloudWatch Logs: ~$1-5/month
- CodeBuild: ~$0.005/minute (only during builds)
- **Total: ~$50-70/month** for sandbox environment

## Next Steps

1. ✅ Set up production environment with similar configuration
2. ✅ Implement blue/green deployments (see AWS_DEPLOYMENT.md)
3. ✅ Add custom domain and SSL certificate
4. ✅ Configure autoscaling policies
5. ✅ Set up CloudWatch alerts to email/Slack
6. ✅ Implement automated testing in pipeline
7. ✅ Add security scanning (container vulnerability scanning)

## Useful Commands Reference

```bash
# View pipeline status
aws codepipeline get-pipeline-state --name uh-groupings-api-pipeline-${ENVIRONMENT}

# Manually start pipeline
aws codepipeline start-pipeline-execution --name uh-groupings-api-pipeline-${ENVIRONMENT}

# View ECS service events
aws ecs describe-services --cluster ${ECS_CLUSTER} --services ${ECS_SERVICE} \
  --query 'services[0].events[0:5]'

# Scale ECS service
aws ecs update-service --cluster ${ECS_CLUSTER} --service ${ECS_SERVICE} --desired-count 3

# View recent logs
aws logs tail /ecs/uh-groupings-api --since 1h --follow

# List running tasks
aws ecs list-tasks --cluster ${ECS_CLUSTER} --service-name ${ECS_SERVICE}

# Force new deployment (without code change)
aws ecs update-service --cluster ${ECS_CLUSTER} --service ${ECS_SERVICE} --force-new-deployment
```

## Security Best Practices

1. ✅ Never commit secrets to Git (use Secrets Manager)
2. ✅ Enable ECR image scanning
3. ✅ Use least-privilege IAM roles
4. ✅ Enable CloudTrail for audit logging
5. ✅ Restrict security group rules
6. ✅ Use VPC endpoints for AWS services (optional, reduces costs)
7. ✅ Enable MFA for AWS Console access
8. ✅ Regularly rotate secrets in Secrets Manager
9. ✅ Review and update dependencies regularly

## Support and Resources

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS CodePipeline Documentation](https://docs.aws.amazon.com/codepipeline/)
- [Spring Boot on AWS](https://spring.io/guides/gs/spring-boot-docker/)
- Internal: #groupings-dev Slack channel

---

**Created:** 2026-06-09  
**Last Updated:** 2026-06-09  
**Author:** UH ITS DevOps Team
