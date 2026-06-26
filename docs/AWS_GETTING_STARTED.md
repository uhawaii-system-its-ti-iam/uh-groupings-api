# AWS Getting Started Checklist

A printable checklist for deploying the UH Groupings Spring API to AWS for the first time.

**Read this only if you're deploying to AWS.** For local development, see the project [README](../README.md).

**Other AWS docs:**
- [AWS_QUICKSTART.md](AWS_QUICKSTART.md) — Narrative walkthrough of the same setup (longer, more context)
- [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md) — Ongoing operations after the first deployment (scaling, rollback, troubleshooting)

---

## Pre-flight (one time per developer)

- [ ] **Docker Desktop installed and running**
  ```bash
  docker --version
  docker ps   # should not error
  ```

- [ ] **Make installed** (standard on macOS and Linux)
  ```bash
  make --version
  ```

You don't need the AWS CLI installed locally — `make aws-*` runs it inside a Docker container.

---

## Phase 1 — Provision AWS Credentials (~5 min)

- [ ] **Create or retrieve an IAM Access Key ID and Secret Access Key**
  - **Personal sandbox:** AWS Console → IAM → **Users** → your user → **Security credentials** → **Create access key** → use case "Command Line Interface (CLI)". Attach `AdministratorAccess` to the user first if it doesn't already have permissions.
  - **Team account:** ask your AWS admin for credentials, or use IAM Identity Center (`aws sso login --profile <name>` works with `aws-vault exec`).
  - **Save both values immediately** — the Secret Access Key is shown only at creation. If lost, you must delete the key and create a new one.

  See [AWS_QUICKSTART.md → Step 1](AWS_QUICKSTART.md#step-1-configure-credentials-510-min) for the full walkthrough and required IAM permissions.

- [ ] **Run the vault setup**
  ```bash
  make aws-vault-setup
  ```
  The script installs `aws-vault` if needed (Homebrew on macOS), then prompts you for the access key pair and stores them in your OS keychain under the profile name `uh-groupings`. Idempotent — re-runs do nothing if the profile already exists.

- [ ] **(Optional) Create a shell alias** to save typing
  ```bash
  alias avx='aws-vault exec uh-groupings --'
  ```

---

## Phase 2 — Configure `aws/.env` (~5 min)

- [ ] **Edit `aws/.env`** with your deployment parameters:
  ```bash
  AWS_REGION=us-west-2
  AWS_ENV=sandbx
  AWS_PROJECT_ID=groupings-api
  AWS_OWNER=mhodges      # or your username
  PROJECT_NAME="UH Groupings API"
  VPC_ID=vpc-xxxxx       # required — replace with a real VPC ID
  SUBNET_IDS=subnet-xxxxx,subnet-yyyyy   # required — 2 public subnet IDs in different AZs (AWS ALB constraint)
  ECS_TASK_COUNT=1
  ```

  Setup is non-interactive and rejects the placeholder values shown above. Replace them with real IDs before running `make aws-setup`. See [AWS_QUICKSTART.md → Step 2](AWS_QUICKSTART.md#step-2-configure-awsenv-515-min) for how to find or create your VPC and subnets.

- [ ] **Confirm the project ID is short** (≤10 chars). The companion projects use `groupings-aui` and `groupings-rui`. See [AWS_NAMING_CONVENTIONS.md](AWS_NAMING_CONVENTIONS.md) for why.

---

## Phase 3 — Provision AWS Infrastructure (~30 min)

- [ ] **Run setup**
  ```bash
  aws-vault exec uh-groupings -- make aws-setup
  # Or with the alias:
  # avx make aws-setup
  ```
  The script will:
  - Create the ECR repository
  - Build and push the initial Docker image
  - Prompt for the **Grouper Password** (silent input)
  - Generate a JWT signing key
  - Write both to AWS Secrets Manager (`groupings/api/grouper-password`, `groupings/api/jwt-secret`)
  - Create the ECS cluster, service, ALB, target group, and IAM roles

- [ ] **Verify the deployment**
  ```bash
  avx make aws-logs
  ```

- [ ] **Hit the load balancer**
  ```bash
  source aws/.env
  ALB_URL=$(avx aws cloudformation describe-stacks \
    --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
    --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
    --output text \
    --region "${AWS_REGION}")
  curl -f "${ALB_URL}/actuator/health"
  # Expected: {"status":"UP"}
  ```

---

## Phase 4 — CI/CD Pipeline (Optional, ~15 min)

The AWS CodeConnections handshake to GitHub requires a manual OAuth approval in the AWS Console; the rest is automated. See [AWS_DEPLOYMENT.md → CodePipeline Setup (Manual)](AWS_DEPLOYMENT.md#codepipeline-setup-manual) for details.

- [ ] **Create the AWS CodeConnections GitHub connection** in the AWS Console; complete OAuth.
- [ ] **Note the connection ARN.**
- [ ] **Deploy the pipeline stack** with `aws/cloudformation/codepipeline.yml`, supplying the connection ARN, repo owner/branch, and the ECS cluster+service names from Phase 3.
- [ ] **Test by pushing a commit** to the configured branch (canonical: `main`).

---

## Daily Operations Checklist

```bash
# Tail logs
avx make aws-logs

# Check service status
avx make aws-service-events

# Check why a task stopped
avx make aws-task-status

# Force a redeploy after a manual ECR push
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"
avx aws ecs update-service --cluster "${CLUSTER}" --service "${SERVICE}" --force-new-deployment
```

For the full operations playbook, see [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md).

---

## Cost Awareness

Sandbox approximate monthly cost: **$50–70** (ECS Fargate ~$30–40, ALB ~$20, ECR + CloudWatch + CodeBuild ~$5).

To save money when not actively testing, scale the service to zero:

```bash
source aws/.env
avx aws ecs update-service \
  --cluster "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster" \
  --service "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service" \
  --desired-count 0
```

To delete everything for the environment:

```bash
avx make aws-teardown
```

---

## Success Criteria

You're done when:

- [x] `aws-vault list` shows the `uh-groupings` profile
- [x] AWS deployment's ALB returns `{"status":"UP"}`
- [x] `avx make aws-logs` streams the deployed application's output
- [x] (Optional) A `git push` to the configured branch triggers a pipeline build and ECS rolling deploy
