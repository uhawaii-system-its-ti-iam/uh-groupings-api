# AWS Quick Start — Initial Infrastructure Setup

## Deploy to AWS in About 60 Minutes

**Purpose:** Stand up the AWS infrastructure for the first time.

**Already have infrastructure?** See [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) for ongoing operations.

**Want to run locally first?** See [DEV_QUICKSTART.md](../../docs/DEV_QUICKSTART.md).

---

## What You'll Create

- Two **private-posture** subnets (different AZs) inside your existing VPC
- VPC endpoints (ECR api/dkr, S3 gateway, Secrets Manager, CloudWatch Logs) + `sg-vpce`
- ECR repository for Docker images
- ECS Fargate cluster, Service Connect namespace, and the API service
- `sg-api-backend` — the API task security group, created with **no ingress rule**
- Two AWS Secrets Manager entries (`groupings/api/grouper-password`, `groupings/api/jwt-secret`)
- CloudWatch log group
- Optionally, a CodePipeline that auto-deploys on `git push`

**No load balancer and no public endpoint.** The API is deployed as a private service. The companion UI reaches it over ECS Service Connect, and the UI stack adds the one ingress rule that opens port 8080 to `sg-ui-apps`. Until the UI exists the API has no inbound client and is not functionally exercised — Step 4 verifies provisioning only.

This is a one-time setup. After completion, all ongoing operations are documented in [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md).

---

## Prerequisites (5 min)

You need:

- **AWS CLI v2** installed on your host (macOS: `brew install awscli`; Linux: [AWS's instructions](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)). All `make aws-*` targets call it directly.
- **Docker Desktop** running locally — needed by `make aws-setup` to build and push the application image to ECR.
- **Make** (standard on macOS and Linux)
- An existing AWS VPC in your target region with one free `/28` CIDR range. `make aws-setup` creates the subnet. **No Internet Gateway, default route, NAT gateway, or second Availability Zone is required** — the API needs no internet egress and is single-AZ.

## Step 1: Configure Credentials (5–10 min)

This project authenticates with **IAM Identity Center (SSO) temporary credentials**. The built-in auto-login only knows how to create **SSO** sessions — it does not configure long-lived IAM access keys.

Authentication is **automatic**: every `make aws-*` target ensures a usable session on demand. Two things can happen:

- **A valid session already exists** for the resolved profile (default `uh-groupings`, or whatever you set via `AWS_PROFILE`) → the tooling reuses it as-is, regardless of how you obtained it.
- **No valid session** → the target writes the SSO profile to your `~/.aws/config` from the four `aws/.env` values below and opens your browser to authorize, then continues.

### Configure the four SSO values in `aws/.env`

These drive the profile the tooling writes (no prompting). They point at the shared ITS sandbox account and are already set in the committed `aws/.env`:

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

`make aws-setup` creates the subnet (via `aws/cloudformation/vpc.yml`), so only an existing VPC is required. You need:

- A VPC in your target region, with one free `/28` CIDR range that doesn't overlap an existing subnet.
- The VPC's main route table ID (`MAIN_ROUTE_TABLE_ID`) — the S3 gateway endpoint attaches to it.

The deployment is **single-subnet, single-AZ**: one subnet holding the API task ENI and the four interface-endpoint ENIs. That matches the single task (`ECS_TASK_COUNT=1`) and keeps all endpoint traffic within one AZ. Multi-AZ is a deliberate later change — add a second subnet in `vpc.yml` first; raising the task count alone would just stack tasks in the same AZ.

You do **not** need an Internet Gateway, a `0.0.0.0/0` route, or a NAT gateway. The API creates no load balancer and needs no internet egress: tasks run with `AssignPublicIp DISABLED` and reach AWS services through the VPC endpoints this setup creates.

List the VPCs in the account and note the ID designated for this project:

```bash
make aws-list-vpcs
```

> The ITS sandbox has no default VPC, so a `Default` column showing `False` is expected — use the VPC ID your team designates for this work.

Validate the VPC before continuing:

```bash
make aws-check-vpc
```

This hard-fails only on things that would actually break the deploy: a missing VPC, an unusable subnet CIDR, or a missing main route table. Lines prefixed `·` are informational — including Internet Gateway presence, AZ count, and whether a data-center route exists. A missing IGW is fine and will not block setup.

If you re-run this after the stack is already deployed, the CIDR check will report an overlap against this project's own subnet. That's expected; the script says so explicitly when it detects the existing vpc stack.

One informational line worth reading: if no VGW or Transit Gateway route is reported, Grouper WS will be unreachable once the app starts. That path is pending infrastructure-team confirmation and does not block provisioning — see [AWS_ARCHITECTURE.md](AWS_ARCHITECTURE.md#grouper-ws-connectivity-pending-infrastructure-confirmation).

### Edit `aws/.env`

Edit `aws/.env` to set deployment parameters. The committed defaults already target the shared ITS sandbox; set `AWS_OWNER` to your own short identifier (e.g., your username, as in the default `mhodges`) so the resources you create are named and tagged distinctly — for example `mhodges-groupings-api-sandbx-cluster`.

The script reads only from `aws/.env`.

Two values control tiering, and they are deliberately separate:

- `AWS_ENV` (e.g. `sandbx`) names and tags your resources.
- `APP_TIER` (`test` | `prod`) selects the Spring profile `aws-${APP_TIER}`, which selects the Grouper backend. `setup.sh` rejects `APP_TIER=prod` unless `AWS_ENV=prod`.

`API_HOSTNAME`, `API_CERTIFICATE_ARN`, and `API_HOSTED_ZONE_ID` are **deprecated and unused** — the API has no public endpoint, hostname, or certificate. Leave them blank.

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
5. **Step 1 — VPC:** creates the subnet plus `sg-vpce` and the VPC endpoints via `aws/cloudformation/vpc.yml`, then reads the subnet id from the stack outputs.
6. **Step 2 — ECR:** creates the repository via `aws/cloudformation/ecr-repository.yml`.
7. **Step 3 — Image:** builds and pushes the initial Docker image to the new ECR repo.
8. **Step 4 — Secrets:** writes `groupings/api/grouper-password` from your overrides file. Generates a fresh JWT signing key with `openssl rand -base64 32` and writes it to `groupings/api/jwt-secret`, *unless that secret already exists* — in which case the existing value is preserved so re-running setup does not invalidate UI tokens.
9. **Step 5 — ECS:** creates the Fargate cluster, the Service Connect namespace, the API service and task definition, `sg-api-backend`, and the IAM roles via `aws/cloudformation/ecs-service.yml`, using the subnet IDs from Step 1. It also ensures the account-wide ECS service-linked role exists.
10. Prints the ECR URI, cluster/service names, and the exported `sg-api-backend` name.

There is no ALB, target group, or URL in the output — the API is private.

The Grouper URL and username are **not** read by `setup.sh`. They are non-secret values supplied by the `aws-test` / `aws-prod` Spring profile in the image (see `aws/task-definition.json` for the equivalent `environment[]` shape).

The whole script is now idempotent. Secrets use create-or-update (and the JWT key is preserved if it already exists), and each CloudFormation stack is applied with `aws cloudformation deploy`, which creates the stack on first run and updates it (or no-ops) on subsequent runs. A stack left in `ROLLBACK_COMPLETE` by a failed first create is deleted automatically before redeploying. So you can safely re-run `make aws-setup` to resume after a partial failure or to pick up template changes. If a run fails, see "Recovery" below.

---

## Step 4: Verify (5 min)

The API has no public endpoint, so there is nothing to `curl` from your laptop. Verify in three steps instead.

**1. Confirm the task reached RUNNING:**

```bash
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"

aws ecs describe-services --cluster "${CLUSTER}" --services "${SERVICE}" \
  --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount}'
```

Expect `Running` to reach `1`. Because there is no load balancer, nothing gates the task on an HTTP check — a task that pulls its image and starts the JVM will report RUNNING.

**2. Confirm Spring started, in the logs:**

```bash
make aws-logs
```

Look for `Started SpringBootWebApplication`. Grouper-related errors at this stage are **expected** — see the note below.

**3. Confirm the connection values the UI project will need.** These are CloudFormation outputs the API publishes so the UI's stack can reference them instead of hardcoding ids:

```bash
aws cloudformation describe-stacks \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'Stacks[0].Outputs' --output table --region "${AWS_REGION}"
```

Expect `ApiTaskSecurityGroupId`, `ServiceConnectNamespaceArn`, `ServiceConnectDnsName`, `ClusterName`, and `ServiceName`. These form the interface the UI stack imports.

**That is the whole verification.** The API is **not** functionally exercised until the companion UI is deployed — that is a deliberate project decision, not a gap. There is no ALB to curl, no inbound client until the UI adds its ingress rule, and `aws ecs execute-command` is intentionally not enabled (it would require widening the ECS task role for a shell we have decided we don't need). See [`../AGENTS.md`](../AGENTS.md) → "Verification scope".

Two consequences worth internalizing:

- **`runningCount: 1` is the meaningful signal.** It proves the image pulled through the ECR endpoints and both secrets resolved through the Secrets Manager endpoint — i.e. the no-NAT networking works. That is what this setup is actually proving.
- **Grouper errors in the logs are expected.** The health endpoint depends on on-prem Grouper WS, whose connectivity mechanism is still pending infrastructure-team confirmation. This is also why the container health check is disabled: with it enabled, ECS would restart-loop the task. Do not expect `{"status":"UP"}`. See [AWS_ARCHITECTURE.md](AWS_ARCHITECTURE.md#grouper-ws-connectivity-pending-infrastructure-confirmation).

If anything fails, troubleshoot with:

```bash
make aws-service-events
make aws-task-status
make aws-stack-events
```

A task stuck in `PENDING` with `ResourceInitializationError` almost always means a VPC endpoint problem (image pull or secret fetch), since there is no NAT fallback path.

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

On this sandbox branch, teardown and re-setup is a normal iteration loop rather than a last resort. The target deletes the stacks in dependency order — pipeline, then ecs, then ecr, then vpc — waiting for each to finish before starting the next, and skipping any that aren't deployed. Stacks that fail to delete stop the run and point you at `make aws-stack-events`.

Two things are deliberately **not** deleted:

- **The VPC**, which is owned by the VPC team and only referenced.
- **The Secrets Manager entries.** Preserving `groupings/api/jwt-secret` means re-provisioning does not invalidate tokens held by UI consumers.

The ECR repository is emptied automatically (`EmptyOnDelete: true` in `ecr-repository.yml`). Without that, CloudFormation would refuse to delete a repository still holding the pushed image, and teardown would fail at that stack.

> If the companion UI stack has already been deployed, tear it down **first**. It adds an ingress rule to `sg-api-backend` and joins the API's Service Connect namespace; those dependencies will block deletion of the API's ECS stack.

To remove the secrets too:

```bash
aws secretsmanager delete-secret \
  --secret-id groupings/api/grouper-password --force-delete-without-recovery

aws secretsmanager delete-secret \
  --secret-id groupings/api/jwt-secret --force-delete-without-recovery
```

---

## Cost Estimate

Per environment (sandbox, single task):

| Resource                                  | Approx. monthly cost |
|-------------------------------------------|----------------------|
| ECS Fargate (1 task, 0.5 vCPU, 1 GB RAM)  | $15–20               |
| Interface VPC endpoints (4 × 1 ENI)       | $30                  |
| ECR + CloudWatch Logs                     | $2–7                 |
| CodeBuild                                 | ~$0.005/min, only while building |
| **Total**                                 | **~$50–60**          |

The four interface endpoints are the largest line item. They replace both an ALB (~$20) and a NAT gateway (~$32 plus data processing) while keeping all AWS traffic off the internet. The S3 gateway endpoint is free.

To save money in a sandbox, scale the service to 0 when not in use. Note that endpoint charges are hourly per ENI and continue regardless of task count:

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
- **Architecture overview:** [AWS_ARCHITECTURE.md](./AWS_ARCHITECTURE.md)

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
