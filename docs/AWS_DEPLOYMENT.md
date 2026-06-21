# Deployment Operations Guide

## Day-to-Day Deployment and Operations

**Purpose:** Operating and deploying to existing AWS infrastructure.

**Need to create infrastructure first?** See [AWS_QUICKSTART.md](./AWS_QUICKSTART.md).

**This guide assumes:** You have already deployed the AWS infrastructure (ECR, ECS, ALB, Pipeline) via `make aws-setup`.

---

## Quick Reference

The Makefile wraps every AWS operation so you don't need the AWS CLI installed locally — it runs inside the project's AWS CLI Docker container, with credentials supplied by `aws-vault`.

```bash
# One-time per developer
make aws-vault-setup

# Operations (all wrapped with aws-vault exec)
aws-vault exec uh-groupings -- make aws-logs            # tail CloudWatch logs
aws-vault exec uh-groupings -- make aws-service-events  # recent ECS service events
aws-vault exec uh-groupings -- make aws-task-status     # why the most recent task stopped
aws-vault exec uh-groupings -- make aws-stack-events    # CloudFormation CREATE_FAILED events
```

For ad-hoc operations not covered by Make targets, the same pattern applies:

```bash
aws-vault exec uh-groupings -- aws ecs ...
```

The examples below use the resource names produced by the project's naming convention. If you've changed `AWS_OWNER` or `AWS_PROJECT_ID` in `aws/.env`, substitute accordingly.

---

## Deployment Methods

### 1. Pipeline-Triggered Deployment (default)

The CodePipeline created by `aws/cloudformation/codepipeline.yml` automatically deploys to ECS when code is pushed to the configured branch (canonical: `main`).

**Flow:**

```
Push to GitHub → CodePipeline → CodeBuild → ECR → ECS Rolling Update
```

**Deployment settings (from `ecs-cluster.yml`):**
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

aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" \
  --service "${SERVICE}" \
  --force-new-deployment
```

Deploy a specific image tag:

```bash
aws-vault exec uh-groupings -- aws ecs register-task-definition \
  --cli-input-json file://aws/task-definition.json

aws-vault exec uh-groupings -- aws ecs update-service \
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
   aws-vault exec uh-groupings -- aws deploy create-application \
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
aws-vault exec uh-groupings -- aws ecs list-task-definitions \
  --family-prefix "${TASK_FAMILY}" \
  --sort DESC \
  --max-items 5

# Update service to a previous revision
aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" \
  --service "${SERVICE}" \
  --task-definition "${TASK_FAMILY}:PREVIOUS_REVISION"
```

### Option 3 — Re-tag a previous ECR image as `latest`

```bash
ECR_REPO="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}"

aws-vault exec uh-groupings -- aws ecr describe-images \
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
aws-vault exec uh-groupings -- aws ecs describe-services \
  --cluster "${CLUSTER}" --services "${SERVICE}" \
  --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount,Deployments:deployments}'

# Tail logs
aws-vault exec uh-groupings -- make aws-logs

# Test the load balancer
ALB_URL=$(aws-vault exec uh-groupings -- aws cloudformation describe-stacks \
  --stack-name "${AWS_PROJECT_ID}-ecs-${AWS_ENV}" \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerUrl`].OutputValue' \
  --output text \
  --region "${AWS_REGION}")

curl -f "${ALB_URL}/actuator/health"
```

---

## Environment-specific Deployments

| Environment | Branch | Approval | Frequency | Rollback risk |
|-------------|--------|----------|-----------|---------------|
| Sandbox | `main` (or temporary feature branch) | Auto | Multiple times per day | Low |
| Dev | `develop` or `main` | Auto | Daily | Low |
| Test/Staging | `release/*` or `test` | Auto with approval gate | Daily/per sprint | Medium |
| Production | `main` or tagged release | Manual approval required | Weekly/bi-weekly | High |

Production deployments additionally require:
- Change management ticket
- Deployment window scheduled
- Documented rollback plan
- Stakeholder notification

---

## Pipeline Configuration

### CodeBuild environment variables

Set these in the CodeBuild project (created by `codepipeline.yml`):

| Variable | Description | Example |
|----------|-------------|---------|
| `AWS_ACCOUNT_ID` | AWS Account ID | `123456789012` |
| `AWS_DEFAULT_REGION` | AWS Region | `us-west-2` |
| `IMAGE_REPO_NAME` | ECR repository name | `mhodges-groupings-api-sandbx` |
| `IMAGE_TAG` | Docker image tag | `latest` or commit SHA |

### Adding a manual approval stage

For production pipelines:

```bash
aws-vault exec uh-groupings -- aws codepipeline get-pipeline \
  --name "${AWS_PROJECT_ID}-pipeline-${AWS_ENV}" > pipeline.json
# Edit pipeline.json to add an Approval stage between Build and Deploy
aws-vault exec uh-groupings -- aws codepipeline update-pipeline \
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
aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 4

# Scale down (or to zero to save sandbox cost)
aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 0
```

### Auto-scaling (target tracking)

```bash
SERVICE_NAMESPACE=ecs
RESOURCE_ID="service/${CLUSTER}/${SERVICE}"

aws-vault exec uh-groupings -- aws application-autoscaling \
  register-scalable-target \
  --service-namespace "${SERVICE_NAMESPACE}" \
  --resource-id "${RESOURCE_ID}" \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 --max-capacity 10

aws-vault exec uh-groupings -- aws application-autoscaling \
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

## Troubleshooting Deployments

### Deployment stuck

```bash
aws-vault exec uh-groupings -- make aws-service-events
```

Common causes:
1. Health checks failing
2. Resource constraints (CPU/memory)
3. Port conflicts
4. Security group blocking traffic

If everything is stuck, force-stop running tasks to trigger fresh placement:

```bash
TASK_ARNS=$(aws-vault exec uh-groupings -- aws ecs list-tasks \
  --cluster "${CLUSTER}" --service-name "${SERVICE}" \
  --query 'taskArns' --output text)

for task in $TASK_ARNS; do
  aws-vault exec uh-groupings -- aws ecs stop-task \
    --cluster "${CLUSTER}" --task "$task"
done
```

### Tasks failing to start

```bash
aws-vault exec uh-groupings -- make aws-task-status
```

### Image pull errors

```bash
aws-vault exec uh-groupings -- aws ecr get-repository-policy \
  --repository-name "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}"
```

---

## Maintenance Windows

```bash
# Scale to zero before maintenance
aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --desired-count 0

# (perform maintenance)

# Scale back up
aws-vault exec uh-groupings -- aws ecs update-service \
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

## Related Documentation

- [AWS_QUICKSTART.md](AWS_QUICKSTART.md) — initial provisioning
- [AWS_SETUP.md](AWS_SETUP.md) — detailed setup
- [AWS_NAMING_CONVENTIONS.md](AWS_NAMING_CONVENTIONS.md) — how the resource names above are derived
- [SECRETS.md](SECRETS.md) — secrets model (aws-vault for developer credentials, Secrets Manager for app runtime)
