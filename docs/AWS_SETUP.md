# AWS Setup Guide for UH Groupings API

This guide walks you through setting up the complete AWS infrastructure and CI/CD pipeline.

## Prerequisites

- AWS account with IAM permissions for ECR, ECS, CloudFormation, IAM, and Secrets Manager
- Docker Desktop installed and running (the AWS CLI runs inside a project-provided container; you do **not** need it on your host)
- `aws-vault` configured via `make aws-vault-setup` (one time)
- Access to the project's GitHub repository
- An existing VPC with at least 2 subnets in different Availability Zones

## Architecture Overview

```
GitHub → CodePipeline → CodeBuild → ECR → ECS/Fargate
                                      ↓
                              Secrets Manager
                                      ↓
                               CloudWatch Logs
```

## Configuration & Secrets Model — at a glance

The full model is documented in [SECRETS.md](SECRETS.md). The short version, repeated here so the AWS workflow makes sense in isolation:

- The deployed API has exactly **two secrets** at runtime: `grouperClient.webService.password` and `jwt.secret.key`. Both live in **AWS Secrets Manager** under `groupings/api/grouper-password` and `groupings/api/jwt-secret`.
- All other configuration (Grouper URL, username, email flags, etc.) is non-secret and travels in the ECS task definition's `environment[]` array.
- **AWS account credentials** (the IAM access key a developer uses to run `make aws-*` commands) are unrelated to application secrets. They are stored separately in the developer's OS keychain via `aws-vault`. See [SECRETS.md](SECRETS.md#aws-account-credentials-developer-side).
- `aws/.env` is **not** a secrets store; it carries non-sensitive deployment parameters.

For technical specifics — task-definition wiring, IAM, manual CLI for secret manipulation — see the [Secrets Manager Integration](#secrets-manager-integration) section below.

## Phase 1: Automated Setup (ECR + ECS)

The `aws/setup.sh` script automates the following:

1. Creates an ECR repository for Docker images
2. Builds and pushes the initial Docker image
3. Configures secrets in AWS Secrets Manager (interactive prompts)
4. Collects VPC/subnet configuration
5. Deploys the ECS cluster, service, and load balancer

### Running the Setup Script

**From the repository root:**

```bash
make aws-setup
```

This runs `aws/setup.sh` inside the AWS CLI Docker container.

### What You'll Need During Setup

The script will prompt for:
- **Grouper Password** (silent input) — service account password
- **VPC ID** — an existing VPC (the script lists available VPCs)
- **Subnet IDs** — at least 2 subnets in different Availability Zones (the script lists available subnets)

A JWT secret is auto-generated via `openssl rand -base64 32` and stored in Secrets Manager. This key is used by `JwtService` to sign and verify authentication tokens shared between the API and UI. It is generated once during initial setup and must remain stable — regenerating it invalidates all active user sessions. Treat it as a secret (never commit it to the repository); rotate it only with a coordinated redeployment of both the API and UI.

The script does **not** prompt for the Grouper URL or username — those are non-secret values that belong in the ECS task definition `environment[]` array, not Secrets Manager.

### Configuration

Edit `aws/.env` before running.

The script reads configuration only from `aws/.env`. To change any parameter, edit the file before running. 

### What the Script Creates

| Resource | Stack Name | Description |
|----------|-----------|-------------|
| ECR Repository | `${AWS_PROJECT_ID}-ecr-${AWS_ENV}` | Docker image registry with scanning and lifecycle policies |
| ECS Cluster + ALB | `${AWS_PROJECT_ID}-ecs-${AWS_ENV}` | Fargate cluster, service, load balancer, security groups, IAM roles |
| Secrets | `groupings/api/grouper-password`, `groupings/api/jwt-secret` | The two sensitive values the deployed API reads at startup |

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

## Secrets Manager Integration

This section is the technical reference for how the two AWS Secrets Manager entries (`groupings/api/grouper-password` and `groupings/api/jwt-secret`) are wired into the deployed ECS task. For the conceptual overview of secrets in this project, see [SECRETS.md](SECRETS.md).

### How `setup.sh` provisions the two secrets

During `make aws-setup`, the script's `configure_secrets` step:

- Prompts the developer (silent input) for the **Grouper Password**.
- Generates the **JWT signing key** with `openssl rand -base64 32`.
- Writes both to AWS Secrets Manager via a create-or-update helper:
  ```bash
  aws secretsmanager create-secret --name "$NAME" --secret-string "$VALUE" \
    || aws secretsmanager update-secret --secret-id "$NAME" --secret-string "$VALUE"
  ```
- Re-running the setup is therefore safe: existing secrets are updated in place rather than duplicated.

### ECS task definition wiring

`aws/task-definition.json` (and the equivalent CloudFormation in `aws/cloudformation/ecs-cluster.yml`) splits values into two arrays — `secrets[]` for sensitive values pulled from Secrets Manager, `environment[]` for everything else:

```json
{
  "containerDefinitions": [
    {
      "name": "uh-groupings-api",
      "secrets": [
        {
          "name": "GROUPERCLIENT_WEBSERVICE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-password"
        },
        {
          "name": "JWT_SECRET_KEY",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/jwt-secret"
        }
      ],
      "environment": [
        { "name": "GROUPERCLIENT_WEBSERVICE_URL",   "value": "https://grouper-prod.its.hawaii.edu/grouper-ws/servicesRest/" },
        { "name": "GROUPERCLIENT_WEBSERVICE_LOGIN", "value": "_groupings_api_2" },
        { "name": "GROUPINGS_API_LOCALHOST_USER",   "value": "service_account_user" },
        { "name": "GROUPINGS_API_TEST_ADMIN_USER",  "value": "service_account_user" },
        { "name": "EMAIL_IS_ENABLED",               "value": "false" },
        { "name": "EMAIL_SEND_RECIPIENT",           "value": "groupings-alerts@hawaii.edu" },
        { "name": "PROPERTIES_OVERRIDE_RESULT",     "value": "OVERRIDDEN" }
      ]
    }
  ]
}
```

How each array behaves at runtime:

- **`secrets[]`** — ECS calls Secrets Manager at container startup, decrypts each value, and exposes it as the named environment variable inside the container. The plaintext never appears in the task definition, the ECS console, or CloudWatch logs.
- **`environment[]`** — values are baked into the task definition as plain text. They appear in the ECS console and may show up in logs if the application echoes them. Use this only for non-sensitive settings.

The Spring application binds environment variables to property names automatically — `GROUPERCLIENT_WEBSERVICE_PASSWORD` becomes `grouperClient.webService.password`, etc.

### IAM permissions

The ECS task **execution** role needs read access to the two secrets so ECS can fetch them at container start. The CloudFormation in `aws/cloudformation/ecs-cluster.yml` creates a role named `${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-role-ecs-execution` (e.g., `mhodges-groupings-api-sandbx-role-ecs-execution`) with the following inline policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [ "secretsmanager:GetSecretValue" ],
      "Resource": [
        "arn:aws:secretsmanager:us-west-2:*:secret:groupings/api/*"
      ]
    }
  ]
}
```

The wildcard `groupings/api/*` covers both current secrets and any future ones added under the same prefix without requiring an IAM policy update.

### Manual operations

These commands are for ad-hoc work (rotation, inspection). All run inside the AWS CLI Docker container with `aws-vault` providing credentials.

#### Update a secret

```bash
aws-vault exec uh-groupings -- aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-password \
  --secret-string "NEW_GROUPER_PASSWORD" \
  --region us-west-2

# Force ECS to restart tasks with the new value
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"
aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --force-new-deployment
```

#### Rotate the JWT key

The JWT key is shared between the API and any UI consumer. Rotation requires redeploying every consumer to pick up the new value at the same time; otherwise tokens issued by one and validated by the other will fail.

```bash
aws-vault exec uh-groupings -- aws secretsmanager update-secret \
  --secret-id groupings/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)" \
  --region us-west-2
```

After updating, redeploy the API and every UI service.

#### Inspect a secret value (carefully — prints in plaintext)

```bash
aws-vault exec uh-groupings -- aws secretsmanager get-secret-value \
  --secret-id groupings/api/grouper-password \
  --query SecretString \
  --output text \
  --region us-west-2
```

#### List the project's secrets

```bash
aws-vault exec uh-groupings -- aws secretsmanager list-secrets \
  --filters Key=name,Values=groupings/ \
  --query 'SecretList[*].[Name,CreatedDate]' \
  --output table \
  --region us-west-2
```

### Auditing

CloudTrail records every Secrets Manager API call. To view recent `GetSecretValue` events:

```bash
aws-vault exec uh-groupings -- aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=EventName,AttributeValue=GetSecretValue \
  --max-results 10 \
  --region us-west-2
```

### Cost

AWS Secrets Manager pricing (as of writing): $0.40 per secret per month + $0.05 per 10,000 API calls. With two secrets, the project pays roughly $0.80/month for secret storage. ECS retrieves each secret once at task start, so retrieval costs are negligible.

### Automated rotation (advanced)

AWS Secrets Manager supports Lambda-driven automatic rotation. This project does not currently use it because:

- The Grouper password is owned by an upstream team's identity provider, not by AWS, so rotation must be coordinated externally.
- The JWT key is shared across the API and UI services; rotation requires a coordinated multi-service redeploy.

If automated rotation becomes appropriate later, the entry point is `aws secretsmanager rotate-secret --rotation-lambda-arn ... --rotation-rules AutomaticallyAfterDays=30`. See AWS's [rotating secrets](https://docs.aws.amazon.com/secretsmanager/latest/userguide/rotating-secrets.html) documentation.

## Troubleshooting

### Common Issues

**ECS tasks fail to start**

Use the Make targets that wrap the AWS CLI in Docker (so you don't need it on your host):

```bash
aws-vault exec uh-groupings -- make aws-task-status   # why the most recent task stopped
aws-vault exec uh-groupings -- make aws-logs          # tail CloudWatch logs
```

**Health checks failing**
- Verify the deployed Spring Boot is listening on the expected port (8080 in the `prod`/`test` profiles)
- Check security group allows inbound traffic
- Ensure `/actuator/health` endpoint is accessible

**Secrets not loading**

The IAM execution role created by `setup.sh` is named `${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-role-ecs-execution` (e.g., `mhodges-groupings-api-sandbx-role-ecs-execution`). Verify it has the `SecretsManagerAccess` inline policy attached.

**Pipeline not triggering on push**
- Verify the CodeStar connection status is `Available` (not `Pending`)
- Check CodePipeline execution history for errors

**CodeBuild fails with "Cannot connect to Docker daemon"**
- Ensure "Privileged mode" is enabled in the CodeBuild project

### Stack Creation Failed

```bash
aws-vault exec uh-groupings -- make aws-stack-events
```

## Teardown

Remove all AWS resources via the Makefile (runs inside the AWS CLI Docker container):

```bash
aws-vault exec uh-groupings -- make aws-teardown
```

This deletes the pipeline, ECS, and ECR CloudFormation stacks. To also remove the two secrets:

```bash
aws-vault exec uh-groupings -- aws secretsmanager delete-secret \
  --secret-id groupings/api/grouper-password --force-delete-without-recovery
aws-vault exec uh-groupings -- aws secretsmanager delete-secret \
  --secret-id groupings/api/jwt-secret --force-delete-without-recovery
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

# View recent logs (use `make aws-logs` for the convenience target)
aws logs tail "/ecs/${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}" --since 1h --follow

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
