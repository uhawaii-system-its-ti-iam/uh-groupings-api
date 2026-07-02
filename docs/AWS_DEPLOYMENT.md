# Deployment Operations Guide

## Day-to-Day Deployment and Operations

**Purpose:** Operating and deploying to existing AWS infrastructure.

**Need to create infrastructure first?** See [AWS_QUICKSTART.md](./AWS_QUICKSTART.md).

**This guide assumes:** You have already deployed the AWS infrastructure (subnets, ECR, ECS, ALB, Pipeline) via `make aws-setup`.

---

## Quick Reference

The Makefile wraps every AWS operation and runs the AWS CLI directly on your host (AWS CLI v2 required), with credentials supplied by IAM Identity Center (SSO). See [AWS_QUICKSTART.md → Step 1](AWS_QUICKSTART.md#step-1-configure-credentials-510-min) for setup.

```bash
# Sign in (optional — any aws-* target below does this automatically on demand)
make aws-sso-setup        # writes the profile + browser login
make aws-sso-login        # force a fresh login when a session expires

# Operations
make aws-logs             # tail ECS logs
make aws-service-events   # recent ECS service events
make aws-task-status      # why the most recent task stopped
make aws-stack-events     # CloudFormation CREATE_FAILED events
```

For ad-hoc operations not covered by Make targets, the same pattern applies:

```bash
aws ecs ...
```

The examples below use the resource names produced by the project's naming convention. If you've changed `AWS_OWNER` or `AWS_PROJECT_ID` in `aws/.env`, substitute accordingly.

---

## CodePipeline Setup (Manual)

`make aws-setup` creates the subnets, ECR, ECS, ALB, and Secrets Manager resources, but it does **not** create the CI/CD pipeline. The pipeline requires an AWS CodeConnections connection to GitHub that is authorized through the AWS Console via OAuth — that handshake cannot be automated. Once the connection exists, the pipeline stack itself can be created from the CLI.

This is a one-time setup per environment. After the pipeline exists, ongoing deploys are handled by the [Deployment Methods](#deployment-methods) section below.

### Automation boundary (what scripts can and cannot do)

Automated by project scripts:

- Validate AWS auth/session.
- Find/reuse an existing `AVAILABLE` GitHub connection.
- Create a new `PENDING` connection if none exists.
- Print the resolved connection ARN for `aws/.env`.

Manual by design (AWS/GitHub security boundary):

- The browser OAuth approval that turns a `PENDING` connection into `AVAILABLE`.

Use the helper command first:

```bash
make aws-github-connect
```

Recommended connection metadata (if you create it manually in the console):

- **Connection name:** `${AWS_OWNER}-${AWS_PROJECT_ID}-github` (example: `mhodges-groupings-api-github`)
- **Tags (optional):** `Owner=${AWS_OWNER}`, `Project=${AWS_PROJECT_ID}`, `Environment=${AWS_ENV}`

### Step 1: Create GitHub Connection

1. Go to **AWS Console → Developer Tools → Settings → Connections**.
2. Click **Create connection**.
3. Select **GitHub** as the provider.
4. Complete the OAuth authorization flow (authorizes AWS to pull from your GitHub org).
5. Copy the **Connection ARN**.
6. Paste the ARN into `aws/.env` as `GITHUB_CONNECTION_ARN`.

The connection starts in `PENDING` status and becomes `AVAILABLE` after authorization. You only need one connection per AWS account — it works for any repo on github.com.

After it is `AVAILABLE`, store its ARN in `aws/.env`:

```bash
sed -i '' 's|^GITHUB_CONNECTION_ARN=.*|GITHUB_CONNECTION_ARN=arn:aws:codeconnections:us-west-2:123456789012:connection/REPLACE_ME|' aws/.env
```

### Step 2: Deploy the CodePipeline Stack

```bash
cd aws/

# Source your environment
source .env

# Resolve ECS resource names from the stack created by setup.sh
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
aws cloudformation deploy \
  --stack-name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}" \
  --template-file cloudformation/codepipeline.yml \
  --parameter-overrides \
    "Owner=${AWS_OWNER}" \
    "Project=${AWS_PROJECT_ID}" \
    "Environment=${AWS_ENV}" \
    "GitHubOwner=${GITHUB_ORG}" \
    "GitHubRepo=${GITHUB_REPO}" \
    "GitHubBranch=${GITHUB_BRANCH}" \
    "ConnectionArn=${GITHUB_CONNECTION_ARN}" \
    "ECSClusterName=${ECS_CLUSTER}" \
    "ECSServiceName=${ECS_SERVICE}" \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-fail-on-empty-changeset \
  --region "${AWS_REGION}"
```

**Important:** `buildspec.yml` lives in the `aws/` subdirectory. Ensure the CodeBuild project references `aws/buildspec.yml` as its buildspec path.

### Step 3: Verify the Pipeline

```bash
# Trigger the pipeline manually
aws codepipeline start-pipeline-execution \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"

# Watch its state
aws codepipeline get-pipeline-state \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"
```

The canonical branch is `main`. For pilot work, you can temporarily point the pipeline at a feature branch by re-deploying with a different `GitHubBranch` parameter.

If the pipeline doesn't trigger on push, verify the CodeConnections connection status is `Available` (not `Pending`). The OAuth handshake from Step 1 must be completed in the AWS Console.

---

## Deployment Methods

### 1. Pipeline-Triggered Deployment (default)

The CodePipeline created by `aws/cloudformation/codepipeline.yml` automatically deploys to ECS when code is pushed to the configured branch (canonical: `main`).

**Flow:**

```
Push to GitHub → CodePipeline → CodeBuild → ECR → ECS Rolling Update
```

**Deployment settings (from `ecs-service.yml`):**
- `MaximumPercent`: 200% (can run 2× desired tasks during deployment)
- `MinimumHealthyPercent`: 100% (maintains full capacity during deployment)
- Health check grace period: 60 seconds

### 2. Manual Deployment

Force a new deployment without code changes:

```bash
# Resolve cluster and service names from your aws/.env
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"

aws ecs update-service \
  --cluster "${CLUSTER}" \
  --service "${SERVICE}" \
  --force-new-deployment
```

Deploy a specific image tag:

```bash
aws ecs register-task-definition \
  --cli-input-json file://aws/task-definition.json

aws ecs update-service \
  --cluster "${CLUSTER}" \
  --service "${SERVICE}" \
  --task-definition "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}:LATEST_REVISION"
```

### 3. Blue/Green Deployment (advanced)

For zero-downtime deployments with instant rollback. Requires:
- CodeDeploy application and deployment group
- Two target groups (blue and green)

Steps to enable:
1. Modify the ECS service to use the `CODE_DEPLOY` deployment controller
2. Create a CodeDeploy application:
   ```bash
   aws deploy create-application \
     --application-name "${AWS_PROJECT_ID}" \
     --compute-platform ECS
   ```
3. Create a deployment group with blue/green configuration
4. Add `appspec.yml` to the repository (already present in `aws/`)

---

## Rollback Procedures

### Option 1 — Revert via Git

```bash
git revert <commit-hash>
git push origin main
```

The pipeline automatically deploys the reverted state.

### Option 2 — Roll back the ECS service to a previous task definition

```bash
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"
TASK_FAMILY="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}"

# List recent task definition revisions
aws ecs list-task-definitions \
  --family-prefix "${TASK_FAMILY}" \
  --sort DESC \
  --max-items 5

# Update service to a previous revision
aws ecs update-service \
  --cluster "${CLUSTER}" \
  --service "${SERVICE}" \
  --task-definition "${TASK_FAMILY}:PREVIOUS_REVISION"
```

### Option 3 — Re-tag a previous ECR image as `latest`

```bash
ECR_REPO="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}"

aws ecr describe-images \
  --repository-name "${ECR_REPO}" \
  --query 'sort_by(imageDetails,& imagePushedAt)[-5:].[imageTags[0],imagePushedAt]' \
  --output table
```

Then re-push or re-tag the desired image and force a new deployment as in Option 2.

---

## Deployment Verification

### Pre-deployment checklist

- [ ] All tests passing locally (`make test`)
- [ ] Secrets present in Secrets Manager (`groupings/api/grouper-password`, `groupings/api/jwt-secret`)
- [ ] Task-definition CPU/memory limits appropriate
- [ ] Health check endpoint reachable
- [ ] Stakeholders notified for production

### Post-deployment verification

```bash
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"

# Service status
aws ecs describe-services \
  --cluster "${CLUSTER}" --services "${SERVICE}" \
  --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount,Deployments:deployments}'

# Tail logs
make aws-logs

# Test the load balancer
ALB_URL=$(aws cloudformation describe-stacks \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text \
  --region "${AWS_REGION}")

curl -f "${ALB_URL}/actuator/health"
```

---

## Environment-specific Deployments

| Environment  | Branch                               | Approval                 | Frequency              | Rollback risk  |
|--------------|--------------------------------------|--------------------------|------------------------|----------------|
| Sandbox      | `main` (or temporary feature branch) | Auto                     | Multiple times per day | Low            |
| Dev          | `develop` or `main`                  | Auto                     | Daily                  | Low            |
| Test/Staging | `release/*` or `test`                | Auto with approval gate  | Daily/per sprint       | Medium         |
| Production   | `main` or tagged release             | Manual approval required | Weekly/bi-weekly       | High           |

Production deployments additionally require:
- Change management ticket
- Deployment window scheduled
- Documented rollback plan
- Stakeholder notification

---

## AWS State Configuration Normalization

To keep `make aws-*` behavior predictable, the auth helpers normalize SSO-related values from `aws/.env` into a concrete AWS CLI profile configuration.

### Source of truth

- `aws/.env` provides raw inputs (`SSO_START_URL`, `AWS_REGION`, `AWS_ACCOUNT_ID`, `SSO_ROLE_NAME`).
- `aws/lib-auth.sh` treats these as required when it needs to write a profile.

### Normalization and write path

When `ensure_aws_session` runs (directly or through any `make aws-*` target):

1. It resolves the profile name from `AWS_PROFILE`, then `AWS_SSO_PROFILE`, then default `uh-groupings`.
2. If no valid session exists, it ensures a matching profile block exists.
3. Profile content is written by `write_sso_profile` to:
   - `${AWS_CONFIG_FILE}` when set, or
   - `~/.aws/config` by default.

In environments that export `AWS_CONFIG_FILE=aws/.aws-state/config`, this is the mechanism that populates `aws/.aws-state/config`.

### How downstream commands use normalized state

- `ensure_aws_session` exports `AWS_PROFILE`, so subsequent `aws` CLI calls use the normalized profile.
- The AWS CLI reads the selected profile from the configured config file path (`AWS_CONFIG_FILE` or default `~/.aws/config`).
- SSO tokens remain in the standard AWS CLI cache (`~/.aws/sso/cache/`); only profile metadata is written to the config file.

### Operational notes

- Changing SSO values in `aws/.env` requires rerunning an auth-backed target (for example `make aws-sso-setup`) to refresh profile configuration.
- If profile state appears stale, remove the generated config file and rerun setup.

---

## Pipeline Configuration

### CodeBuild environment variables

Set these in the CodeBuild project (created by `codepipeline.yml`):

| Variable             | Description         | Example                        |
|----------------------|---------------------|--------------------------------|
| `AWS_ACCOUNT_ID`     | AWS Account ID      | `123456789012`                 |
| `AWS_DEFAULT_REGION` | AWS Region          | `us-west-2`                    |
| `IMAGE_REPO_NAME`    | ECR repository name | `mhodges-groupings-api-sandbx` |
| `IMAGE_TAG`          | Docker image tag    | `latest` or commit SHA         |

### Adding a manual approval stage

For production pipelines:

```bash
aws codepipeline get-pipeline \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}" > pipeline.json
# Edit pipeline.json to add an Approval stage between Build and Deploy
aws codepipeline update-pipeline \
  --cli-input-json file://pipeline.json
```

---

## Scaling Operations

### Manual scaling

```bash
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"

# Scale up
aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 4

# Scale down (or to zero to save sandbox cost)
aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 0
```

### Auto-scaling (target tracking)

```bash
SERVICE_NAMESPACE=ecs
RESOURCE_ID="service/${CLUSTER}/${SERVICE}"

aws application-autoscaling \
  register-scalable-target \
  --service-namespace "${SERVICE_NAMESPACE}" \
  --resource-id "${RESOURCE_ID}" \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 --max-capacity 10

aws application-autoscaling \
  put-scaling-policy \
  --service-namespace "${SERVICE_NAMESPACE}" \
  --resource-id "${RESOURCE_ID}" \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration file://scaling-policy.json
```

`scaling-policy.json`:
```json
{
  "TargetValue": 75.0,
  "PredefinedMetricSpecification": {
    "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
  },
  "ScaleOutCooldown": 60,
  "ScaleInCooldown": 300
}
```

---

## CloudWatch Monitoring

CloudWatch metrics for ECS are emitted automatically. To get paged on resource pressure, attach alarms to the standard CPU and memory metrics. Both alarms below assume the cluster and service names from the project's [naming convention](AWS_NAMING_CONVENTIONS.md).

```bash
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"

# CPU above 80% sustained for 10 minutes
aws cloudwatch put-metric-alarm \
  --alarm-name "${AWS_PROJECT_ID}-high-cpu-${AWS_ENV}" \
  --alarm-description "Alert when CPU exceeds 80%" \
  --metric-name CPUUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2 \
  --dimensions "Name=ClusterName,Value=${CLUSTER}" "Name=ServiceName,Value=${SERVICE}"

# Memory above 80% sustained for 10 minutes
aws cloudwatch put-metric-alarm \
  --alarm-name "${AWS_PROJECT_ID}-high-memory-${AWS_ENV}" \
  --alarm-description "Alert when memory exceeds 80%" \
  --metric-name MemoryUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2 \
  --dimensions "Name=ClusterName,Value=${CLUSTER}" "Name=ServiceName,Value=${SERVICE}"
```

Add `--alarm-actions <SNS_TOPIC_ARN>` to route alarm state changes to email, Slack, PagerDuty, etc.

---

## Troubleshooting Deployments

### Deployment stuck

```bash
make aws-service-events
```

Common causes:
1. Health checks failing
2. Resource constraints (CPU/memory)
3. Port conflicts
4. Security group blocking traffic

If everything is stuck, force-stop running tasks to trigger fresh placement:

```bash
TASK_ARNS=$(aws ecs list-tasks \
  --cluster "${CLUSTER}" --service-name "${SERVICE}" \
  --query 'taskArns' --output text)

for task in $TASK_ARNS; do
  aws ecs stop-task \
    --cluster "${CLUSTER}" --task "$task"
done
```

### Tasks failing to start

```bash
make aws-task-status
```

### Image pull errors

```bash
aws ecr get-repository-policy \
  --repository-name "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}"
```

---

## Maintenance Windows

```bash
# Scale to zero before maintenance
aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 0

# (perform maintenance)

# Scale back up
aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 2
```

---

## Deployment Checklist

### Before every deployment

- [ ] Review changes in pull request
- [ ] All CI checks passing
- [ ] Environment secrets verified (`groupings/api/grouper-password`, `groupings/api/jwt-secret`)
- [ ] Capacity planning reviewed
- [ ] Rollback plan documented

### After every deployment

- [ ] Monitor logs for 15 minutes (`make aws-logs`)
- [ ] Verify health check passing
- [ ] Test critical user flows
- [ ] Check CloudWatch metrics
- [ ] Update deployment log
- [ ] Notify stakeholders of completion

---

## Useful Commands

A quick reference for ad-hoc operations. All commands assume `source aws/.env` has set `AWS_REGION`, `AWS_OWNER`, `AWS_PROJECT_ID`, and `AWS_ENV`, and that `CLUSTER` and `SERVICE` are derived as `${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-{cluster|service}`.

```bash
# Pipeline status
aws codepipeline get-pipeline-state \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"

# Manually start the pipeline
aws codepipeline start-pipeline-execution \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}"

# Recent ECS service events (the 5 most recent)
aws ecs describe-services \
  --cluster "${CLUSTER}" --services "${SERVICE}" \
  --query 'services[0].events[0:5]'

# Scale ECS service
aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 3

# Tail recent logs (or use `make aws-logs`)
aws logs tail \
  "/ecs/${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}" --since 1h --follow

# Force a new deployment without a code change
aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --force-new-deployment
```

---

## Related Documentation

- [AWS_QUICKSTART.md](AWS_QUICKSTART.md) — initial provisioning
- [AWS_NAMING_CONVENTIONS.md](AWS_NAMING_CONVENTIONS.md) — how the resource names above are derived
- [SECRETS.md](SECRETS.md) — secrets model (IAM Identity Center for developer credentials, Secrets Manager for app runtime)
