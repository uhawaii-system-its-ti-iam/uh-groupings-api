# AWS Quick Start — Initial Infrastructure Setup

## Deploy to AWS in About 60 Minutes

**Purpose:** Stand up the AWS infrastructure for the first time.

**Already have infrastructure?** See [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) for ongoing operations.

**Want to run locally first?** See [DEV_QUICKSTART.md](./DEV_QUICKSTART.md).

---

## What You'll Create

- Two public subnets (in different AZs) inside your existing VPC
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

- **AWS CLI v2** installed on your host (macOS: `brew install awscli`; Linux: [AWS's instructions](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)). All `make aws-*` targets call it directly.
- **Docker Desktop** running locally — needed by `make aws-setup` to build and push the application image to ECR.
- **Make** (standard on macOS and Linux)
- An existing AWS VPC with public internet egress — its main route table routes `0.0.0.0/0` to an Internet Gateway (Step 2 helps you confirm this). `make aws-setup` creates the two subnets.

---

## Step 1: Configure Credentials (5–10 min)

This project uses **IAM Identity Center (SSO) temporary credentials** exclusively. Long-lived IAM access keys are not supported.

Authentication is **automatic**: every `make aws-*` target signs you in on demand. When there's no valid session, the target writes the `uh-groupings` SSO profile to your `~/.aws/config` (from the `aws/.env` values) and opens your browser to authorize — then continues.

### Configure the four SSO values in `aws/.env`

These drive the profile the tooling writes (no prompting):

- `SSO_START_URL`  — e.g., `https://d-9267e44193.awsapps.com/start`
- `AWS_REGION`     — e.g., `us-west-2` (also used as the SSO region)
- `AWS_ACCOUNT_ID` — e.g., `610572473041`
- `SSO_ROLE_NAME`  — e.g., `AWSAdministratorAccess`

### Sign in

You can authorize up front:

```bash
make aws-sso-setup
```

…or simply run the target you actually want (e.g., `make aws-check-vpc`) and let it open the browser for you. Both write the profile if it's missing and skip the login if a valid session already exists.

### Refreshing an expired session

Sessions expire (typically 1–12 h, set by your org). Any target re-authenticates automatically when that happens, or you can refresh proactively:

```bash
make aws-sso-login
```

---

## Step 2: Configure `aws/.env` (5–15 min)

### Prepare your VPC first

`make aws-setup` creates the two public subnets (via `aws/cloudformation/vpc.yml`), only an existing VPC is required. Before editing `aws/.env`, you need:

- A VPC in your target region.
- That VPC to provide public internet egress: its main route table routes `0.0.0.0/0` to an Internet Gateway. Subnets created by the stack inherit this route table, which is what makes them public.

The setup provisions two `/28` subnets in different Availability Zones. The 2-AZ minimum is an AWS-side constraint on Application Load Balancers.

**Personal sandbox :** Use your account's default VPC. Each region has one whose main route table already routes to an Internet Gateway.

```bash
aws ec2 describe-vpcs \
  --filters "Name=isDefault,Values=true" \
  --query 'Vpcs[].{Id:VpcId,CIDR:CidrBlock}' \
  --output table --region us-west-2
```

**Custom VPC (e.g., a `sandbox-vpc-01` you manage yourself):** Confirm its main route table has a `0.0.0.0/0 → igw-...` route. See the [AWS Internet Gateway guide](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Internet_Gateway.html) if you need to add one.

**Enterprise or team accounts:** The VPC is probably owned by your network team. Ask them for a VPC ID with public egress and confirm two free `/28` CIDR ranges you can use (or supply your own via the template parameters).

Confirm your VPC has a public default route before continuing:

```bash
aws ec2 describe-route-tables \
  --filters "Name=vpc-id,Values=vpc-xxxxx" "Name=association.main,Values=true" \
  --query 'RouteTables[].Routes[?DestinationCidrBlock==`0.0.0.0/0`]' \
  --output table --region us-west-2
```

A row with a `GatewayId` starting `igw-` means the VPC provides public egress.

### Edit `aws/.env`

Edit `aws/.env` to set deployment parameters. The defaults work for a personal sandbox:

The script reads only from `aws/.env`.

See [AWS_NAMING_CONVENTIONS.md](./AWS_NAMING_CONVENTIONS.md) for why `AWS_PROJECT_ID` must be short and how the values combine into resource names.

Authentication-backed commands normalize your SSO inputs into an AWS profile configuration. By default, that profile is written to `~/.aws/config`; if your shell exports `AWS_CONFIG_FILE=aws/.aws-state/config`, the same normalized profile is written to `aws/.aws-state/config` instead. For the full lifecycle, see [AWS_DEPLOYMENT.md → AWS State Configuration Normalization](./AWS_DEPLOYMENT.md#aws-state-configuration-normalization).

---

## Step 3: Run the Automated Setup (~30 min)

```bash
make aws-setup
```

All `make aws-*` commands assume `export AWS_PROFILE=uh-groupings` is set in your shell (see Step 1).

The script (`aws/setup.sh`) runs on your host and is **non-interactive end to end** — it never prompts. The flow is:

1. Loads `aws/.env`.
2. Validates that `AWS_PROJECT_ID` and `VPC_ID` are set to real values (placeholders like `vpc-xxxxx` are rejected). Setup exits before any AWS API call if either is missing.
3. Validates the developer's overrides file (`~/.$(whoami)-conf/uh-groupings-api-overrides.properties`); exits if `grouperClient.webService.password` is missing or empty.
4. Verifies prerequisites and your AWS account ID.
5. **Step 1 — VPC:** creates two public subnets in your VPC via `aws/cloudformation/vpc.yml` and reads their IDs from the stack outputs.
6. **Step 2 — ECR:** creates the repository via `aws/cloudformation/ecr-repository.yml`.
7. **Step 3 — Image:** builds and pushes the initial Docker image to the new ECR repo.
8. **Step 4 — Secrets:** writes `groupings/api/grouper-password` from your overrides file. Generates a fresh JWT signing key with `openssl rand -base64 32` and writes it to `groupings/api/jwt-secret`, *unless that secret already exists* — in which case the existing value is preserved so re-running setup does not invalidate UI tokens.
9. **Step 5 — ECS:** creates the Fargate cluster, service, ALB, target group, and IAM roles via `aws/cloudformation/ecs-service.yml`, using the subnet IDs from Step 1.
10. Prints the ECR URI, cluster/service names, and ALB URL.

The Grouper URL and username are **not** read by `setup.sh` — they are non-secret values that belong in the ECS task definition `environment[]` array (currently in `aws/task-definition.json`).

The whole script is now idempotent. Secrets use create-or-update (and the JWT key is preserved if it already exists), and each CloudFormation stack is applied with `aws cloudformation deploy`, which creates the stack on first run and updates it (or no-ops) on subsequent runs. A stack left in `ROLLBACK_COMPLETE` by a failed first create is deleted automatically before redeploying. So you can safely re-run `make aws-setup` to resume after a partial failure or to pick up template changes. If a run fails, see "Recovery" below.

---

## Step 4: Verify (2 min)

```bash
# Tail the application's CloudWatch logs
make aws-logs

# Test the load balancer
curl "$(aws cloudformation describe-stacks \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text \
  --region "${AWS_REGION}")/actuator/health"
```

Expected: `{"status":"UP"}` once the ECS task has finished its first health check (~1–2 minutes after the script completes).

If anything fails, troubleshoot with:

```bash
make aws-service-events
make aws-task-status
make aws-stack-events
```

---

## Step 5: CodePipeline (Optional, ~15 min)

The pipeline cannot be fully automated because the GitHub CodeConnections handshake requires a manual OAuth approval in the browser. The project automates everything *around* that boundary.

Use the helper first:

```bash
make aws-github-connect
```

What this helper does:

- Ensures your AWS CLI session is valid.
- Reuses an existing `AVAILABLE` GitHub connection when present.
- Otherwise, creates a `PENDING` connection and opens browser pages for completion.
- Polls until the connection becomes `AVAILABLE` and prints the exact ARN to put in `aws/.env` (`GITHUB_CONNECTION_ARN=...`).

What still must be done manually:

- In AWS Console → Connections, click **Update pending connection**.
- Complete the GitHub OAuth consent flow.

Recommended metadata when creating a connection manually:

- **Connection name:** `${AWS_OWNER}-${AWS_PROJECT_ID}-github` (example: `mhodges-groupings-api-github`)
- **Tags (optional):** `Owner=${AWS_OWNER}`, `Project=${AWS_PROJECT_ID}`, `Environment=${AWS_ENV}`

See [AWS_DEPLOYMENT.md → CodePipeline Setup (Manual)](./AWS_DEPLOYMENT.md#codepipeline-setup-manual) for the full procedure. Summary:

1. Create an AWS CodeConnections entry in the AWS Console; authorize via GitHub OAuth.
2. Note the connection ARN.
3. Deploy `aws/cloudformation/codepipeline.yml` with the connection ARN, GitHub owner/repo/branch, and the ECS cluster + service names from Step 4.

The canonical branch is `main`. For pilot work, you can temporarily point the pipeline at a feature branch.

---

## Recovery and Teardown

### A failed setup run

If `make aws-setup` fails partway through, identify the cause:

```bash
make aws-stack-events
```

Then just re-run setup — `aws cloudformation deploy` updates the existing stacks (and clears any `ROLLBACK_COMPLETE` stack from a failed first create) rather than failing on them:

```bash
make aws-setup
```

If the infrastructure is beyond repair, tear it down and start over:

```bash
make aws-teardown
```

### Tear everything down

```bash
make aws-teardown
```

This deletes the ECR, ECS, VPC (subnets), and pipeline CloudFormation stacks but **not** the Secrets Manager entries. To remove the secrets too:

```bash
aws secretsmanager delete-secret \
  --secret-id groupings/api/grouper-password --force-delete-without-recovery

aws secretsmanager delete-secret \
  --secret-id groupings/api/jwt-secret --force-delete-without-recovery
```

---

## Cost Estimate

Per environment (sandbox):

| Resource                                    | Approx. monthly cost  |
|---------------------------------------------|-----------------------|
| ECS Fargate (1–2 tasks, 0.5 vCPU, 1 GB RAM) | $30–40                |
| Application Load Balancer                   | $20                   |
| ECR + CloudWatch + CodeBuild                | $5                    |
| **Total**                                   | **$50–70**            |

To save money in a sandbox, scale the service to 0 when not in use:

```bash
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"

aws ecs update-service \
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
Only `make aws-setup` needs Docker (to build and push the application image). Start Docker Desktop and retry.

**"Stack already exists" or `ROLLBACK_COMPLETE`**
Re-running `make aws-setup` handles both: `aws cloudformation deploy` updates an existing stack, and a stack stuck in `ROLLBACK_COMPLETE` from a failed first create is deleted and recreated automatically. If a stack is otherwise wedged, `make aws-teardown` and start fresh.

**"AccessDenied" calling AWS APIs**
Your IAM Identity Center role lacks permissions. Run `aws sts get-caller-identity` and verify the role/permission set attached to your profile covers ECR, ECS, CloudFormation, IAM, and Secrets Manager. If the session has expired, run `make aws-sso-login` and retry.

**Pipeline not triggering on push**
Verify the CodeConnections connection status is `Available` (not `Pending`). The OAuth handshake from Step 5 must be completed in the AWS Console.

---

**Time:** ~60 minutes including verification.
