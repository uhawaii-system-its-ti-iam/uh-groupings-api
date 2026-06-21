# AWS Infrastructure Documentation

This directory contains all AWS-related configuration and infrastructure-as-code files for the UH Groupings API.

## Directory Structure

```
aws/
├── README.md                          # This file
├── setup.sh                           # Automated infrastructure setup script
├── setup-vault.sh                     # Idempotent aws-vault installer/profile setup
├── buildspec.yml                      # CodeBuild specification
├── task-definition.json               # ECS task definition template
├── appspec.yml                        # CodeDeploy specification
└── cloudformation/                    # CloudFormation templates
    ├── ecr-repository.yml             # ECR repository setup
    ├── ecs-cluster.yml                # ECS cluster and service
    └── codepipeline.yml               # CI/CD pipeline
```

## Quick Start

All AWS commands run inside a Docker container that has the AWS CLI installed, so you do **not** need the AWS CLI on your host. You only need Docker Desktop, GNU Make, and `aws-vault` for credential management.

### Step 1: Edit `aws/.env`

Set deployment parameters (region, environment name, project ID, VPC, subnets):

```bash
AWS_REGION=us-west-2
AWS_ENV=sandbox
AWS_PROJECT_ID=uh-groupings-api
VPC_ID=vpc-xxxxx
SUBNET_IDS=subnet-xxxxx,subnet-yyyyy
ECS_TASK_COUNT=1
```

This file holds **non-secret** deployment configuration only. Secrets are prompted at runtime by `setup.sh` and written to AWS Secrets Manager.

### Step 2: Provision AWS credentials with `aws-vault`

[`aws-vault`](https://github.com/99designs/aws-vault) stores your AWS access keys in your operating system's keychain (macOS Keychain, Windows Credential Manager, Linux Secret Service) instead of leaving them in plaintext on disk. It releases credentials only for the duration of a single command, as ephemeral environment variables.

Run the one-time setup target:

```bash
make aws-vault-setup
```

This invokes `aws/setup-vault.sh`, which is idempotent:

1. Verifies `aws-vault` is installed; on macOS it offers to install it via Homebrew if missing.
2. Checks whether a profile named `uh-groupings` already has credentials in your keychain. If yes, it prints a confirmation and exits. If not, it runs `aws-vault add uh-groupings` and prompts you for the **Access Key ID** and **Secret Access Key**.

You can re-run `make aws-vault-setup` any time; it will not overwrite existing credentials. To use a different profile name, set `AWS_VAULT_PROFILE`:

```bash
AWS_VAULT_PROFILE=uh-groupings-prod make aws-vault-setup
```

The Docker container in this project reads `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_SESSION_TOKEN` from environment variables, which `aws-vault` injects automatically.

If you cannot install `aws-vault`, see [Credential alternatives](#credential-alternatives) below.


### Step 3: Run setup

Wrap any `make aws-*` command with `aws-vault exec`:

```bash
aws-vault exec uh-groupings -- make aws-setup
```

This launches `setup.sh` inside the AWS CLI container with credentials supplied as session-scoped environment variables. The credentials live only inside that one container invocation; they are not written to disk and disappear when the command finishes.

You will be prompted for the Grouper URL, Grouper service account credentials, and the database password. The script generates a JWT signing key and stores all secrets in AWS Secrets Manager.

### Make Targets

| Target | Description |
|--------|-------------|
| `make aws-setup` | Run interactive infrastructure setup (creates ECR, ECS, ALB, secrets) |
| `make aws-teardown` | Delete all CloudFormation stacks (prompts for confirmation) |
| `make aws-stack-events` | Show CloudFormation `CREATE_FAILED` events |
| `make aws-service-events` | Show recent ECS service events |
| `make aws-task-status` | Show why the most recent ECS task stopped |
| `make aws-logs` | Tail CloudWatch logs for the API |

Every target above must be wrapped with `aws-vault exec uh-groupings --` to receive credentials.

**Tip:** Create a shell alias to avoid retyping the wrapper:

```bash
alias avx='aws-vault exec uh-groupings --'
# Then:
avx make aws-setup
avx make aws-logs
```

### Available `aws/.env` Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | Target AWS region | `us-west-2` |
| `AWS_ENV` | Deployment environment (`sandbox`, `development`, `test`, `production`) | `sandbox` |
| `AWS_PROJECT_ID` | Project identifier (required) | _none_ |
| `PROJECT_NAME` | Display name used in setup-script output | `AWS_PROJECT_ID` |
| `NON_INTERACTIVE` | Skip confirmation prompts | unset |
| `VPC_ID` | Existing VPC ID (prompts if blank) | _prompted_ |
| `SUBNET_IDS` | Comma-separated subnet IDs (prompts if blank) | _prompted_ |
| `ECS_TASK_COUNT` | Number of ECS tasks to run | `2` |

### Credential Alternatives

`aws-vault` is recommended, but the docker-compose passes through any `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_SESSION_TOKEN` already present in the shell environment, so the following also work:

- **AWS IAM Identity Center (SSO):** `aws sso login --profile <profile>` followed by `aws-vault exec` or by exporting credentials yourself.
- **Direct environment variables:** `export AWS_ACCESS_KEY_ID=...` etc. before running `make`. Useful in CI/CD where the runner already provides credentials.
- **Static `~/.aws/credentials`:** Re-add `${HOME}/.aws:/root/.aws:ro` to `docker-compose.aws.yml` and run `aws configure` on the host. This is the least secure option and is **not recommended**.

### Manual Setup

If you prefer not to use the script, follow the step-by-step guide in [../docs/AWS_SETUP.md](../docs/AWS_SETUP.md). The same `aws-vault exec` wrapper applies to any `aws` CLI commands you issue manually.

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

Configure deployment by setting variables in `aws/.env`. The script reads only from this file — environment-variable overrides and command-line arguments are not supported. To change region, environment, or any other parameter, edit `aws/.env`.

**Usage:**

From the repository root, with credentials supplied via `aws-vault`:

```bash
aws-vault exec uh-groupings -- make aws-setup
```

To run non-interactively (skips confirmation prompts), set `NON_INTERACTIVE=true` in `aws/.env` before running.

**Note:** AWS credentials are managed by `aws-vault` (see Step 2 above). Do not commit credentials to `aws/.env` — only deployment parameters belong there.

## Secrets Management

Store secrets in AWS Secrets Manager:

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

All troubleshooting commands run via the project's `Makefile`, which executes the AWS CLI inside the Docker container so you don't need it installed locally.

### Stack Creation Failed

Show the CloudFormation events that failed during ECS stack creation:

```bash
make aws-stack-events
```

### Service Won't Start

Show recent ECS service events:

```bash
make aws-service-events
```

Show the stopped reason for the most recent task:

```bash
make aws-task-status
```

### View Application Logs

Tail the API's CloudWatch logs:

```bash
make aws-logs
```

Each target sources `aws/.env` for region and project naming, then queries the relevant CloudFormation stack outputs to discover the cluster and service names automatically.

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

- [Project docs directory
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS CodePipeline Documentation](https://docs.aws.amazon.com/codepipeline/)

---
