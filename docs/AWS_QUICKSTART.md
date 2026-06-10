# AWS Quick Start - Initial Infrastructure Setup

## Deploy to AWS in 60 Minutes

**Purpose:** Set up AWS infrastructure for the first time.

**Already have infrastructure?** See [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) for ongoing operations.

**Want to run locally first?** See [DEV_QUICKSTART.md](./DEV_QUICKSTART.md)

---

## What You'll Create

- ECR repository for Docker images
- ECS Fargate cluster with 2 tasks
- Application Load Balancer
- CodePipeline (GitHub → Build → Deploy)
- Secrets Manager for credentials
- CloudWatch logging

**One-time setup.** After this, use [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) for deployments.

---

## Prerequisites (5 min)

```bash
# Install AWS CLI
brew install awscli

# Configure credentials
aws configure
# Enter: Access Key ID, Secret Key, Region (us-west-2)

# Verify
aws sts get-caller-identity
```

**Also need:**
- Docker Desktop running
- GitHub Enterprise access
- VPC with 2+ subnets (or create new)

---

## Option A: Automated Setup (30 min) ⭐

```bash
# Set your owner (mhodges or its-iam)
export OWNER="mhodges"
export ENVIRONMENT="sandbox"

# Run setup script
./aws/setup.sh
```

**Script will prompt for:**
- Grouper credentials
- Database password
- VPC and subnet IDs

**Then automatically:**
1. Creates ECR repository
2. Builds and pushes Docker image
3. Creates secrets in Secrets Manager
4. Deploys ECS cluster with ALB
5. Starts application

**Time:** ~30 minutes (including wait times)

---

## Option B: Manual CloudFormation (45 min)

### Step 1: Deploy ECR

```bash
export OWNER="mhodges"
export PROJECT="groupings"
export ENVIRONMENT="sandbox"
export COMPONENT="api"

aws cloudformation create-stack \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecr \
  --template-body file://aws/cloudformation/ecr-repository.yml \
  --parameters \
    ParameterKey=Owner,ParameterValue=${OWNER} \
    ParameterKey=Project,ParameterValue=${PROJECT} \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=Component,ParameterValue=${COMPONENT}

# Wait for completion
aws cloudformation wait stack-create-complete \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecr
```

### Step 2: Build and Push Image

```bash
# Get ECR URI
ECR_URI=$(aws cloudformation describe-stacks \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecr \
  --query 'Stacks[0].Outputs[?OutputKey==`RepositoryUri`].OutputValue' \
  --output text)

# Login to ECR
aws ecr get-login-password --region us-west-2 | \
  docker login --username AWS --password-stdin ${ECR_URI}

# Build and push
docker build -t ${OWNER}-${PROJECT}-${ENVIRONMENT}-${COMPONENT} .
docker tag ${OWNER}-${PROJECT}-${ENVIRONMENT}-${COMPONENT}:latest ${ECR_URI}:latest
docker push ${ECR_URI}:latest
```

### Step 3: Create Secrets

```bash
# Create secrets in Secrets Manager
aws secretsmanager create-secret \
  --name groupings/${ENVIRONMENT}/api/grouper-password \
  --secret-string "YOUR_PASSWORD"

aws secretsmanager create-secret \
  --name groupings/${ENVIRONMENT}/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)"

# Create remaining secrets (see SECRETS.md for list)
```

### Step 4: Deploy ECS Cluster

```bash
# Get your VPC and subnet IDs
aws ec2 describe-vpcs
aws ec2 describe-subnets --filters "Name=vpc-id,Values=vpc-xxxxx"

# Deploy ECS
aws cloudformation create-stack \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecs \
  --template-body file://aws/cloudformation/ecs-cluster.yml \
  --parameters \
    ParameterKey=Owner,ParameterValue=${OWNER} \
    ParameterKey=Project,ParameterValue=${PROJECT} \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=Component,ParameterValue=${COMPONENT} \
    ParameterKey=VpcId,ParameterValue=vpc-xxxxx \
    ParameterKey=SubnetIds,ParameterValue=\"subnet-xxx,subnet-yyy\" \
    ParameterKey=ContainerImage,ParameterValue=${ECR_URI}:latest \
  --capabilities CAPABILITY_NAMED_IAM

# Wait (5-10 minutes)
aws cloudformation wait stack-create-complete \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecs
```

### Step 5: Get Application URL

```bash
ALB_URL=$(aws cloudformation describe-stacks \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecs \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text)

echo "Application: ${ALB_URL}"

# Test
curl ${ALB_URL}/actuator/health
```

---

## Verify Setup

```bash
# Check ECS service
aws ecs describe-services \
  --cluster ${OWNER}-${PROJECT}-${ENVIRONMENT}-cluster \
  --services ${OWNER}-${PROJECT}-${ENVIRONMENT}-service

# View logs
aws logs tail /ecs/${OWNER}-${PROJECT}-${ENVIRONMENT}-${COMPONENT} --follow

# Test application
curl ${ALB_URL}/actuator/health
```

**Expected:** `{"status":"UP"}`

---

## Setup Complete! ✅

Your infrastructure is deployed. For ongoing operations (deployments, scaling, troubleshooting):

**→ Go to [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md)**

---

## What's Next?

### Configure CI/CD Pipeline (Optional - 20 min)

1. **Create GitHub Connection** (in AWS Console)
   - CodeStar Connections → Create connection
   - Connect to GitHub Enterprise
   - Note the Connection ARN

2. **Deploy Pipeline**
   ```bash
   aws cloudformation create-stack \
     --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-pipeline \
     --template-body file://aws/cloudformation/codepipeline.yml \
     --parameters \
       ParameterKey=Owner,ParameterValue=${OWNER} \
       ParameterKey=ECSClusterName,ParameterValue=${OWNER}-${PROJECT}-${ENVIRONMENT}-cluster \
       ParameterKey=ECSServiceName,ParameterValue=${OWNER}-${PROJECT}-${ENVIRONMENT}-service \
     --capabilities CAPABILITY_NAMED_IAM
   ```

3. **Test Pipeline**
   ```bash
   # Push code to trigger deployment
   git push origin main
   ```

---

## Cost Estimate

**Sandbox:** ~$50-70/month
- ECS Fargate: ~$30-40/month
- ALB: ~$20/month
- Other: ~$5/month

**Save money:** Scale to 0 when not in use (see AWS_DEPLOYMENT.md)

---

## Detailed Guides

- **Naming conventions:** [NAMING_CONVENTIONS.md](./AWS_NAMING_CONVENTIONS.md)
- **Secrets management:** [SECRETS.md](./SECRETS.md)
- **Full AWS setup:** [AWS_SETUP.md](./AWS_SETUP.md) (detailed step-by-step)
- **Architecture:** [ARCHITECTURE.md](./ARCHITECTURE.md)

---

**Time:** ~60 minutes | **Last Updated:** 2026-06-09

---

## Prerequisites (5 minutes)

Install required tools:

```bash
# Check if AWS CLI is installed
aws --version

# If not installed, install AWS CLI
# macOS:
brew install awscli

# Configure AWS CLI (you'll need your access key ID and secret)
aws configure
```

You'll also need:
- Docker Desktop installed and running
- Access to GitHub Enterprise repository
- AWS account credentials

## Automated Setup (30 minutes)

We've created a script to automate most of the setup:

Navigate to project root directory.

```bash
./aws/setup.sh
```

The script will:
1. Create ECR repository for Docker images
2. Build and push your first Docker image
3. Create secrets in AWS Secrets Manager (you'll be prompted for values)
4. Deploy ECS cluster with Application Load Balancer
5. Start your application on Fargate

**You'll be prompted for:**
- Grouper API URL
- Grouper username/password
- Database password
- VPC and subnet IDs (select from displayed options)

## Manual Setup (if you prefer step-by-step)

Follow the detailed guide: [AWS_SETUP.md](./AWS_SETUP.md)

## Set Up CI/CD Pipeline (20 minutes)

After the automated setup completes:

### 1. Create GitHub Enterprise Connection

1. Go to [AWS Console → Developer Tools → Connections](https://console.aws.amazon.com/codesuite/settings/connections)
2. Click "Create connection"
3. Choose "GitHub Enterprise Server"
4. Enter your GitHub Enterprise URL
5. Complete the OAuth flow
6. Note the Connection ARN

### 2. Deploy CodePipeline

```bash
# Set your environment variables
export AWS_REGION="us-west-2"
export ENVIRONMENT="sandbox"
export GITHUB_CONNECTION_ARN="arn:aws:codestar-connections:us-west-2:123456789012:connection/xxxxx"

# Get ECS cluster and service names
export ECS_CLUSTER=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`ClusterName`].OutputValue' \
  --output text)

export ECS_SERVICE=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`ServiceName`].OutputValue' \
  --output text)

# Deploy pipeline
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
```

### 3. Activate GitHub Connection

1. Go to AWS Console → CodeStar Connections
2. Find your connection (it will be "Pending")
3. Click "Update pending connection"
4. Complete the authorization in GitHub

## Test Your Deployment (5 minutes)

```bash
# Get your application URL
ALB_URL=$(aws cloudformation describe-stacks \
  --stack-name uh-groupings-ecs-${ENVIRONMENT} \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text)

echo "Your application: ${ALB_URL}"

# Test health endpoint
curl ${ALB_URL}/actuator/health

# Expected response: {"status":"UP"}
```

## Test CI/CD Pipeline

```bash
# Make a small change and push
echo "# Testing CI/CD" >> README.md
git add README.md
git commit -m "Test CI/CD pipeline"
git push origin main

# Watch the pipeline
aws codepipeline get-pipeline-state \
  --name uh-groupings-api-pipeline-${ENVIRONMENT}

# Or view in console:
# https://console.aws.amazon.com/codesuite/codepipeline/pipelines
```

## Common Issues & Solutions

### "Docker daemon not running"
```bash
# Make sure Docker Desktop is running
open -a Docker
```

### "Access Denied" errors
```bash
# Check your AWS credentials
aws sts get-caller-identity

# Make sure your IAM user/role has necessary permissions
```

### "Health checks failing"
```bash
# Check the logs
aws logs tail /ecs/uh-groupings-api --follow

# Check ECS service events
aws ecs describe-services \
  --cluster uh-groupings-${ENVIRONMENT} \
  --services uh-groupings-api-service \
  --query 'services[0].events[0:5]'
```

### "Pipeline not triggering"
- Verify GitHub webhook is created
- Check CodeStar connection status is "Available"
- Ensure branch name matches pipeline configuration

## What Just Happened?

You've created:
1. **ECR Repository** - Stores your Docker images
2. **ECS Cluster** - Runs your containerized application on Fargate
3. **Application Load Balancer** - Routes traffic to your containers
4. **Secrets Manager** - Stores sensitive configuration
5. **CodePipeline** - Automates build and deployment
6. **CloudWatch Logs** - Captures application logs

## Next Steps

1. ✅ **Add Custom Domain** - Configure Route 53 and SSL certificate
2. ✅ **Set Up Monitoring** - Configure CloudWatch dashboards and alarms
3. ✅ **Enable Autoscaling** - Scale based on CPU/memory usage
4. ✅ **Deploy to Production** - Repeat setup with `ENVIRONMENT=production`
5. ✅ **Review Security** - Audit IAM roles and security groups

## Learning Resources

- [Architecture Overview](./ARCHITECTURE.md) - Understand the system design
- [AWS Deployment Guide](./AWS_DEPLOYMENT.md) - Learn deployment strategies
- [AWS Setup Guide](./AWS_SETUP.md) - Detailed step-by-step instructions

## Cost Estimate

Your sandbox environment will cost approximately:
- **$50-70/month** for continuous running
- **$0.005/minute** during builds (only when deploying)

To minimize costs:
- Scale down when not in use: `aws ecs update-service --desired-count 0`
- Use FARGATE_SPOT for non-production environments
- Delete resources when done testing

## Getting Help

If you encounter issues:
1. Check CloudWatch logs: `aws logs tail /ecs/uh-groupings-api --follow`
2. Review [Troubleshooting section in AWS_SETUP.md](./AWS_SETUP.md#troubleshooting)
3. Ask for help in #groupings-dev Slack channel

---
