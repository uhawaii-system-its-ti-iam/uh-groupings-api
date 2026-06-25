# AWS Quick Start — Initial Infrastructure Setup

## Deploy to AWS in About 60 Minutes

**Purpose:** Stand up the AWS infrastructure for the first time.

**Already have infrastructure?** See [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) for ongoing operations.

**Want to run locally first?** See [DEV_QUICKSTART.md](./DEV_QUICKSTART.md).

---

## What You'll Create

- ECR repository for Docker images
- ECS Fargate cluster + service
- Application Load Balancer
- Two AWS Secrets Manager entries (`groupings/api/grouper-password`, `groupings/api/jwt-secret`)
- CloudWatch log group
- Optionally, a CodePipeline that auto-deploys on `git push`

This is a one-time setup. After completion, all ongoing operations are documented in [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md).

---

## Prerequisites (5 min)

You need:

- **Docker Desktop** running locally (the AWS CLI is invoked inside a Docker container; you do **not** need it installed on your host)
- **Make** (standard on macOS and Linux)
- **`aws-vault`** for credential storage — installed automatically by `make aws-vault-setup`
- An IAM access key pair from your AWS account
- VPC and at least 2 subnets in different Availability Zones

---

## Step 1: Configure Credentials (5 min)

`aws-vault` stores your IAM access key in your operating system's keychain rather than in a plaintext file on disk. From the repository root:

```bash
make aws-vault-setup
```

The script will:
1. Install `aws-vault` via Homebrew if it's not already present (macOS).
2. Prompt for your **AWS Access Key ID** and **Secret Access Key**.
3. Store them under a profile named `uh-groupings` in your OS keychain.

The script is idempotent — re-running it skips both steps if everything is already configured.

Every subsequent AWS command is wrapped with `aws-vault exec`, which releases the credentials only as ephemeral environment variables for the duration of one command. They never touch disk.

---

## Step 2: Configure `aws/.env` (5 min)

Edit `aws/.env` to set deployment parameters. The defaults work for a personal sandbox:

```bash
AWS_REGION=us-west-2
AWS_ENV=sandbx

# Project identifier — must be ≤10 chars (AWS naming-length limit on ALB/TG).
# Companion projects: groupings-aui (Angular UI), groupings-rui (React UI).
AWS_PROJECT_ID=groupings-api
AWS_OWNER=mhodges

# Display name shown in setup script output
PROJECT_NAME="UH Groupings API"

# Network — required (real values, not placeholders)
VPC_ID=vpc-xxxxx
SUBNET_IDS=subnet-xxxxx,subnet-yyyyy

# ECS task count
ECS_TASK_COUNT=1
```

The script reads only from `aws/.env`. Inline overrides and command-line flags are not supported.

See [AWS_NAMING_CONVENTIONS.md](./AWS_NAMING_CONVENTIONS.md) for why `AWS_PROJECT_ID` must be short and how the values combine into resource names.

---

## Step 3: Run the Automated Setup (~30 min)

```bash
aws-vault exec uh-groupings -- make aws-setup
```

The script (`aws/setup.sh`) runs inside the AWS CLI Docker container and is **non-interactive end to end** — it never prompts. The flow is:

1. Loads `aws/.env`.
2. Validates that `AWS_PROJECT_ID`, `VPC_ID`, and `SUBNET_IDS` are set to real values (placeholders like `vpc-xxxxx` are rejected). Setup exits before any AWS API call if any is missing.
3. Validates the developer's overrides file (`~/.$(whoami)-conf/uh-groupings-api-overrides.properties`); exits if `grouperClient.webService.password` is missing or empty.
4. Verifies prerequisites and your AWS account ID.
5. **Step 1 — ECR:** creates the repository via `aws/cloudformation/ecr-repository.yml`.
6. **Step 2 — Image:** builds and pushes the initial Docker image to the new ECR repo.
7. **Step 3 — Secrets:** writes `groupings/api/grouper-password` from your overrides file. Generates a fresh JWT signing key with `openssl rand -base64 32` and writes it to `groupings/api/jwt-secret`, *unless that secret already exists* — in which case the existing value is preserved so re-running setup does not invalidate UI tokens.
8. **Step 4 — ECS:** creates the Fargate cluster, service, ALB, target group, and IAM roles via `aws/cloudformation/ecs-cluster.yml`.
9. Prints the ECR URI, cluster/service names, and ALB URL.

The Grouper URL and username are **not** read by `setup.sh` — they are non-secret values that belong in the ECS task definition `environment[]` array (currently in `aws/task-definition.json`).

The script is idempotent for the Grouper password (`create-or-update`) and the JWT key (preserved if already present), but not for stack creation. If a run fails partway through, see "Recovery" below.

---

## Step 4: Verify (2 min)

```bash
# Tail the application's CloudWatch logs
aws-vault exec uh-groupings -- make aws-logs

# Test the load balancer
curl "$(aws-vault exec uh-groupings -- aws cloudformation describe-stacks \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text \
  --region "${AWS_REGION}")/actuator/health"
```

Expected: `{"status":"UP"}` once the ECS task has finished its first health check (~1–2 minutes after the script completes).

If anything fails, troubleshoot with:

```bash
aws-vault exec uh-groupings -- make aws-service-events
aws-vault exec uh-groupings -- make aws-task-status
aws-vault exec uh-groupings -- make aws-stack-events
```

---

## Step 5: CodePipeline (Optional, ~15 min)

The pipeline cannot be fully automated because the GitHub CodeConnections handshake requires a manual OAuth approval. See [AWS_DEPLOYMENT.md → CodePipeline Setup (Manual)](./AWS_DEPLOYMENT.md#codepipeline-setup-manual) for the full procedure. Summary:

1. Create an AWS CodeConnections entry in the AWS Console; authorize via GitHub OAuth.
2. Note the connection ARN.
3. Deploy `aws/cloudformation/codepipeline.yml` with the connection ARN, GitHub owner/repo/branch, and the ECS cluster + service names from Step 4.

The canonical branch is `main`. For pilot work, you can temporarily point the pipeline at a feature branch.

---

## Recovery and Teardown

### A failed setup run

If `make aws-setup` fails partway through, identify the cause:

```bash
aws-vault exec uh-groupings -- make aws-stack-events
```

Then either fix the input and re-run (CloudFormation will reject duplicate stacks) or tear down and start over:

```bash
aws-vault exec uh-groupings -- make aws-teardown
```

### Tear everything down

```bash
aws-vault exec uh-groupings -- make aws-teardown
```

This deletes the ECR, ECS, and pipeline CloudFormation stacks but **not** the Secrets Manager entries. To remove the secrets too:

```bash
aws-vault exec uh-groupings -- aws secretsmanager delete-secret \
  --secret-id groupings/api/grouper-password --force-delete-without-recovery

aws-vault exec uh-groupings -- aws secretsmanager delete-secret \
  --secret-id groupings/api/jwt-secret --force-delete-without-recovery
```

---

## Cost Estimate

Per environment (sandbox):

| Resource | Approx. monthly cost |
|----------|----------------------|
| ECS Fargate (1–2 tasks, 0.5 vCPU, 1 GB RAM) | $30–40 |
| Application Load Balancer | $20 |
| ECR + CloudWatch + CodeBuild | $5 |
| **Total** | **$50–70** |

To save money in a sandbox, scale the service to 0 when not in use:

```bash
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"

aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 0
```

---

## What's Next

- **Ongoing operations:** [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md)
- **Naming conventions:** [AWS_NAMING_CONVENTIONS.md](./AWS_NAMING_CONVENTIONS.md)
- **Secrets model:** [SECRETS.md](./SECRETS.md)
- **Architecture overview:** [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## Common Issues

**"Cannot connect to Docker daemon"**
Start Docker Desktop and retry. The Make targets check for Docker before invoking the AWS CLI container.

**"Stack already exists"**
A previous run left resources behind. Use `make aws-teardown` and start fresh, or import the existing stacks into your local state.

**"AccessDenied" calling AWS APIs**
Your IAM user/role lacks permissions. Check what `aws-vault exec uh-groupings -- aws sts get-caller-identity` returns and verify the IAM policies attached to that identity cover ECR, ECS, CloudFormation, IAM, and Secrets Manager.

**Pipeline not triggering on push**
Verify the CodeConnections connection status is `Available` (not `Pending`). The OAuth handshake from Step 5 must be completed in the AWS Console.

---

**Time:** ~60 minutes including verification.
