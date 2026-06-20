# AWS Infrastructure Documentation

This directory contains all AWS-related configuration and infrastructure-as-code files for the UH Groupings API.

## Directory Structure

```
aws/
├── README.md                          # This file
├── setup.sh                           # Automated setup script
├── buildspec.yml                      # CodeBuild specification
├── deployment.json                    # Deployment configuration
├── task-definition.json               # ECS task definition template
├── appspec.yml                        # CodeDeploy specification
└── cloudformation/                    # CloudFormation templates
    ├── ecr-repository.yml             # ECR repository setup
    ├── ecs-cluster.yml                # ECS cluster and service
    └── codepipeline.yml               # CI/CD pipeline
```

## Quick Start

### Configure

1. **Review and edit `aws/.env` with your configuration:**

2. **Ensure AWS credentials are configured:**
   ```bash
   # If not already done
   aws configure
   ```

### Option 1: Using Make (Recommended)

From the repository root:

```bash
# Interactive setup
make aws-setup

# Non-interactive (CI/CD)
make aws-setup-ci

# Setup via Docker AWS CLI container
make aws-docker-setup
```

### Option 2: Direct Execution

```bash
cd aws/
./setup.sh

# Override variables inline
AWS_REGION=us-east-1 AWS_ENV=production ./setup.sh

# Non-interactive mode (for CI/CD)
NON_INTERACTIVE=true ./setup.sh
```

**Available Environment Variables:**
- `AWS_REGION` - AWS region to deploy to (default: `us-west-2`)
- `AWS_ENV` - Deployment environment (default: `sandbox`)
  - Options: `sandbox`, `development`, `test`, `production`
- `AWS_PROJECT_ID` - AWS project identifier (required)
- `PROJECT_NAME` - Project display name used in script output (defaults to `AWS_PROJECT_ID`)
- `NON_INTERACTIVE` - Skip confirmation prompts
- `VPC_ID` - VPC ID to use (prompts if not set)
- `SUBNET_IDS` - Comma-separated subnet IDs (prompts if not set)
- `DESIRED_COUNT` - Number of ECS tasks (default: `2`)

This will create all necessary AWS resources in about 30 minutes.

### Manual Setup

Follow the detailed guide in [docs/AWS_SETUP.md](../docs/AWS_SETUP.md)

## CloudFormation Templates

### 1. ECR Repository (`ecr-repository.yml`)

Creates and configures Amazon ECR repository for Docker images.

**Features:**
- Image scanning on push
- Lifecycle policies for image retention
- Encryption at rest

**Deploy:**
```bash
aws cloudformation create-stack \
  --stack-name uh-groupings-ecr-sandbox \
  --template-body file://cloudformation/ecr-repository.yml \
  --parameters \
    ParameterKey=RepositoryName,ParameterValue=uh-groupings-api \
    ParameterKey=Environment,ParameterValue=sandbox
```

### 2. ECS Cluster (`ecs-cluster.yml`)

Creates complete ECS infrastructure including:
- Fargate cluster
- ECS service
- Application Load Balancer
- Target groups
- Security groups
- IAM roles

**Deploy:**
```bash
aws cloudformation create-stack \
  --stack-name uh-groupings-ecs-sandbox \
  --template-body file://cloudformation/ecs-cluster.yml \
  --parameters \
    ParameterKey=Environment,ParameterValue=sandbox \
    ParameterKey=VpcId,ParameterValue=vpc-xxxxx \
    ParameterKey=SubnetIds,ParameterValue="subnet-xxxxx,subnet-yyyyy" \
    ParameterKey=ContainerImage,ParameterValue=123456789012.dkr.ecr.us-west-2.amazonaws.com/uh-groupings-api:latest \
    ParameterKey=DesiredCount,ParameterValue=2 \
  --capabilities CAPABILITY_NAMED_IAM
```

### 3. CodePipeline (`codepipeline.yml`)

Creates CI/CD pipeline with:
- GitHub Enterprise source integration
- CodeBuild for building Docker images
- Automatic deployment to ECS

**Deploy:**
```bash
aws cloudformation create-stack \
  --stack-name uh-groupings-pipeline-sandbox \
  --template-body file://cloudformation/codepipeline.yml \
  --parameters \
    ParameterKey=Environment,ParameterValue=sandbox \
    ParameterKey=GitHubEnterpriseUrl,ParameterValue="https://github.your-company.com" \
    ParameterKey=GitHubOwner,ParameterValue="uhawaii-system-its-ti-iam" \
    ParameterKey=GitHubRepo,ParameterValue="uh-groupings-api" \
    ParameterKey=GitHubBranch,ParameterValue="main" \
    ParameterKey=ECSClusterName,ParameterValue="uh-groupings-sandbox" \
    ParameterKey=ECSServiceName,ParameterValue="uh-groupings-api-service" \
  --capabilities CAPABILITY_NAMED_IAM
```

## Configuration Files

### deployment.json

Project metadata and deployment configuration. Used for documentation and tooling.

### task-definition.json

ECS task definition template with:
- Resource limits (CPU/memory)
- Environment variables
- Secrets Manager integration
- Health check configuration
- Logging configuration

**Register task definition:**
```bash
# Replace placeholders first
sed -i '' "s/{AWS_ACCOUNT_ID}/${AWS_ACCOUNT_ID}/g" task-definition.json
sed -i '' "s/{AWS_REGION}/${AWS_REGION}/g" task-definition.json

# Register
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

### appspec.yml

CodeDeploy specification for blue/green deployments (advanced).

### buildspec.yml

CodeBuild specification for CI/CD pipeline Docker image building.

**Important:** Since `buildspec.yml` is located in the `aws/` subdirectory, configure your CodeBuild project to use the custom buildspec path:

```bash
aws codebuild create-project \
  --name uh-groupings-api-build \
  --buildspec aws/buildspec.yml \
  # ... other parameters ...
```

Or in the AWS Console:
- **Build > Buildspec name:** `aws/buildspec.yml`

## Environment Variables

Configure deployment by setting variables in `aws/.env` or passing them inline:

| Variable          | Description                | Default          | Example             |
|-------------------|----------------------------|------------------|---------------------|
| `AWS_REGION`      | AWS region                 | `us-west-2`      | `us-east-1`         |
| `AWS_ENV`         | Environment name           | `sandbox`        | `production`        |
| `AWS_PROJECT_ID`  | Project identifier         | (required)       | `uh-groupings-api`  |
| `PROJECT_NAME`    | Display name               | `AWS_PROJECT_ID` | `UH Groupings API`  |
| `NON_INTERACTIVE` | Skip prompts               | (unset)          | `true`              |
| `VPC_ID`          | Existing VPC ID            | (prompted)       | `vpc-12345678`      |
| `SUBNET_IDS`      | Comma-separated subnet IDs | (prompted)       | `subnet-a,subnet-b` |
| `DESIRED_COUNT`   | ECS task count             | `2`              | `1`                 |

**Usage (from repo root via Make):**
```bash
# Interactive mode (loads aws/.env automatically)
make aws-setup

# Non-interactive mode (CI/CD)
make aws-setup-ci

# Override variables
AWS_REGION=us-east-1 make aws-setup

# Override multiple variables
AWS_REGION=eu-west-1 AWS_ENV=production make aws-setup-ci
```

**Usage (direct, from aws/ directory):**
```bash
cd aws/

# Interactive mode
./setup.sh

# Override region
AWS_REGION=us-east-1 ./setup.sh

# Override environment
AWS_ENV=production ./setup.sh

# Non-interactive
NON_INTERACTIVE=true ./setup.sh

# Set for session (persists across commands)
export AWS_REGION=us-east-1
export AWS_ENV=production
./setup.sh
```

**Note:** These are deployment script variables. AWS credentials should be configured via `aws configure` or AWS environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`).

## Secrets Management

Store secrets in AWS Secrets Manager (never in Git):

```bash
# Grouper configuration
aws secretsmanager create-secret \
  --name groupings/api/grouper-url \
  --secret-string "https://grouper.example.com/grouper-ws"

aws secretsmanager create-secret \
  --name groupings/api/grouper-username \
  --secret-string "your-username"

aws secretsmanager create-secret \
  --name groupings/api/grouper-password \
  --secret-string "your-password"

# JWT configuration
aws secretsmanager create-secret \
  --name groupings/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)"

# Database configuration
aws secretsmanager create-secret \
  --name groupings/api/db-password \
  --secret-string "your-db-password"
```

## Common Operations

### Update ECS Service

```bash
# Force new deployment
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --force-new-deployment

# Scale service
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 4
```

### View Logs

```bash
# Tail logs in real-time
aws logs tail /ecs/uh-groupings-api --follow

# Filter for errors
aws logs filter-log-events \
  --log-group-name /ecs/uh-groupings-api \
  --filter-pattern "ERROR"
```

### Trigger Pipeline

```bash
# Manually start pipeline
aws codepipeline start-pipeline-execution \
  --name uh-groupings-api-pipeline-sandbox
```

## Cost Optimization

### Sandbox/Development
- Use FARGATE_SPOT for cost savings
- Scale down to 1 task
- Stop when not in use (scale to 0)

### Production
- Use standard FARGATE
- Enable autoscaling
- Review instance sizing regularly

### Clean Up Resources

```bash
# Delete all stacks (WARNING: This deletes everything!)
aws cloudformation delete-stack --stack-name uh-groupings-pipeline-sandbox
aws cloudformation delete-stack --stack-name uh-groupings-ecs-sandbox
aws cloudformation delete-stack --stack-name uh-groupings-ecr-sandbox

# Delete secrets
aws secretsmanager delete-secret --secret-id groupings/api/grouper-url --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/grouper-username --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/grouper-password --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/jwt-secret --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id groupings/api/db-password --force-delete-without-recovery
```

## Troubleshooting

### Stack Creation Failed

```bash
# View stack events to identify the issue
aws cloudformation describe-stack-events \
  --stack-name uh-groupings-ecs-sandbox \
  --query 'StackEvents[?ResourceStatus==`CREATE_FAILED`]'
```

### Service Won't Start

```bash
# Check service events
aws ecs describe-services \
  --cluster uh-groupings-sandbox \
  --services uh-groupings-api-service \
  --query 'services[0].events[0:10]'

# Check task stopped reason
aws ecs describe-tasks \
  --cluster uh-groupings-sandbox \
  --tasks $(aws ecs list-tasks --cluster uh-groupings-sandbox --query 'taskArns[0]' --output text) \
  --query 'tasks[0].{StoppedReason:stoppedReason,Containers:containers[*].{Name:name,Reason:reason}}'
```

## Security Best Practices

1. Use IAM roles with least privilege
2. Store all secrets in Secrets Manager
3. Enable ECR image scanning
4. Use VPC endpoints to avoid public internet
5. Enable CloudTrail for audit logging
6. Regularly update container images
7. Review security group rules
8. Enable Container Insights
9. Use encryption at rest and in transit

## Additional Resources

- [AWS Quick Start Guide](../docs/AWS_QUICKSTART.md)
- [AWS Detailed Setup Guide](../docs/AWS_SETUP.md)
- [AWS Deployment Guide](../docs/AWS_DEPLOYMENT.md)
- [Architecture Documentation](../docs/ARCHITECTURE.md)
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS CodePipeline Documentation](https://docs.aws.amazon.com/codepipeline/)

## Support

For issues or questions:
- Internal: #groupings-dev Slack channel
- AWS Support: Through AWS Console

---
