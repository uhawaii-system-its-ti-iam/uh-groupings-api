# Deployment Operations Guide

## Day-to-Day Deployment and Operations

**Purpose:** Operating and deploying to existing AWS infrastructure.

**Need to create infrastructure first?** See [AWS_QUICKSTART.md](./AWS_QUICKSTART.md)

**This guide assumes:** You have already deployed the AWS infrastructure (ECR, ECS, ALB, Pipeline).

---

## Quick Reference

```bash
# Common deployment operations
export OWNER="mhodges"
export PROJECT="groupings"
export ENVIRONMENT="sandbox"
export CLUSTER="${OWNER}-${PROJECT}-${ENVIRONMENT}-cluster"
export SERVICE="${OWNER}-${PROJECT}-${ENVIRONMENT}-service"

# Force new deployment
aws ecs update-service --cluster ${CLUSTER} --service ${SERVICE} --force-new-deployment

# Scale up/down
aws ecs update-service --cluster ${CLUSTER} --service ${SERVICE} --desired-count 4

# View logs
aws logs tail /ecs/${OWNER}-${PROJECT}-${ENVIRONMENT}-api --follow

# Check service status
aws ecs describe-services --cluster ${CLUSTER} --services ${SERVICE}
```

---

The pipeline automatically deploys to ECS when code is pushed to the main branch.

**Flow:**
```
Push to GitHub → CodePipeline → CodeBuild → ECR → ECS Rolling Update
```

**Deployment Settings:**
- **MaximumPercent:** 200% (can run 2x desired tasks during deployment)
- **MinimumHealthyPercent:** 100% (maintains full capacity during deployment)
- **Health Check Grace Period:** 60 seconds

### 2. Manual Deployment

**Force new deployment without code changes:**
```bash
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --force-new-deployment
```

**Deploy specific image tag:**
```bash
# Update task definition with new image
aws ecs register-task-definition \
  --cli-input-json file://aws/task-definition.json

# Update service
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --task-definition uh-groupings-api:LATEST_VERSION
```

### 3. Blue/Green Deployment (Advanced)

For zero-downtime deployments with instant rollback capability.

**Prerequisites:**
- CodeDeploy application and deployment group
- Two target groups (blue and green)

**Steps to enable:**
1. Modify ECS service to use CODE_DEPLOY deployment controller
2. Create CodeDeploy application:
```bash
aws deploy create-application \
  --application-name uh-groupings-api \
  --compute-platform ECS
```

3. Create deployment group with blue/green configuration
4. Add `appspec.yml` to repository

## Rollback Procedures

### Option 1: Rollback via Pipeline

```bash
# Revert Git commit
git revert <commit-hash>
git push origin main

# Pipeline will automatically redeploy previous version
```

### Option 2: Rollback via ECS Service

```bash
# Get previous task definition
aws ecs list-task-definitions \
  --family-prefix uh-groupings-api \
  --sort DESC \
  --max-items 5

# Update service to previous task definition
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --task-definition uh-groupings-api:PREVIOUS_REVISION
```

### Option 3: Deploy Previous ECR Image

```bash
# List recent images
aws ecr describe-images \
  --repository-name uh-groupings-api \
  --query 'sort_by(imageDetails,& imagePushedAt)[-5:].[imageTags[0],imagePushedAt]' \
  --output table

# Tag and deploy previous image as 'latest'
aws ecr batch-get-image \
  --repository-name uh-groupings-api \
  --image-ids imageTag=<PREVIOUS_TAG> \
  --query 'images[].imageManifest' \
  --output text | \
aws ecr put-image \
  --repository-name uh-groupings-api \
  --image-tag latest \
  --image-manifest -

# Force new deployment
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --force-new-deployment
```

## Deployment Verification

### Pre-Deployment Checklist

- [ ] All tests passing locally
- [ ] Environment variables configured in Secrets Manager
- [ ] Task definition CPU/memory limits appropriate
- [ ] Health check endpoint responding
- [ ] Database migrations completed (if applicable)
- [ ] Deployment window communicated to stakeholders

### Post-Deployment Verification

**1. Check Service Status:**
```bash
aws ecs describe-services \
  --cluster uh-groupings-sandbox \
  --services uh-groupings-api-service \
  --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount,Deployments:deployments}'
```

**2. Monitor Logs:**
```bash
# Real-time logs
aws logs tail /ecs/uh-groupings-api --follow

# Check for errors
aws logs filter-log-events \
  --log-group-name /ecs/uh-groupings-api \
  --filter-pattern "ERROR" \
  --start-time $(date -u -d '5 minutes ago' +%s)000
```

**3. Test Endpoints:**
```bash
# Get ALB URL
ALB_URL=$(aws elbv2 describe-load-balancers \
  --names uh-groupings-alb-sandbox \
  --query 'LoadBalancers[0].DNSName' \
  --output text)

# Health check
curl -f http://${ALB_URL}/actuator/health

# API test
curl -H "Authorization: Bearer <token>" http://${ALB_URL}/api/v2.1/groupings

# Load test (optional)
ab -n 100 -c 10 http://${ALB_URL}/actuator/health
```

**4. Verify Container Health:**
```bash
# List running tasks
aws ecs list-tasks \
  --cluster uh-groupings-sandbox \
  --service-name uh-groupings-api-service

# Check task health
TASK_ARN=$(aws ecs list-tasks \
  --cluster uh-groupings-sandbox \
  --service-name uh-groupings-api-service \
  --query 'taskArns[0]' --output text)

aws ecs describe-tasks \
  --cluster uh-groupings-sandbox \
  --tasks ${TASK_ARN} \
  --query 'tasks[0].{Health:healthStatus,Status:lastStatus,Started:startedAt}'
```

## Environment-Specific Deployments

### Sandbox (Development)
- **Branch:** `main` or `develop`
- **Approval:** Auto-deploy on merge
- **Frequency:** Multiple times per day
- **Rollback Risk:** Low (non-production)

### Test/Staging
- **Branch:** `release/*` or `test`
- **Approval:** Auto-deploy with manual approval gate
- **Frequency:** Daily or per sprint
- **Rollback Risk:** Medium

### Production
- **Branch:** `production` or tagged releases
- **Approval:** Manual approval required
- **Frequency:** Weekly or bi-weekly
- **Rollback Risk:** High (plan carefully)
- **Requirements:**
  - Change management ticket
  - Deployment window scheduled
  - Rollback plan documented
  - Stakeholder notification
  - Database backup confirmed

## Pipeline Configuration

### Environment Variables in CodeBuild

Set these in your CodeBuild project:

| Variable             | Description         | Example                |
|----------------------|---------------------|------------------------|
| `AWS_ACCOUNT_ID`     | AWS Account ID      | `123456789012`         |
| `AWS_DEFAULT_REGION` | AWS Region          | `us-west-2`            |
| `IMAGE_REPO_NAME`    | ECR repository name | `uh-groupings-api`     |
| `IMAGE_TAG`          | Docker image tag    | `latest` or commit SHA |

### Adding Manual Approval Stage

To add manual approval before production deployment:

```bash
# Edit CodePipeline
aws codepipeline get-pipeline --name uh-groupings-api-pipeline-production > pipeline.json

# Add approval stage (edit pipeline.json)
{
  "name": "Approval",
  "actions": [{
    "name": "ManualApproval",
    "actionTypeId": {
      "category": "Approval",
      "owner": "AWS",
      "provider": "Manual",
      "version": "1"
    },
    "configuration": {
      "CustomData": "Please review and approve production deployment",
      "NotificationArn": "arn:aws:sns:us-west-2:123456789012:deployment-approvals"
    }
  }]
}

# Update pipeline
aws codepipeline update-pipeline --cli-input-json file://pipeline.json
```

## Scaling Operations

### Manual Scaling

```bash
# Scale up
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 4

# Scale down
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 1
```

### Auto Scaling (Target Tracking)

```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/uh-groupings-sandbox/uh-groupings-api-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10

# CPU-based scaling policy
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/uh-groupings-sandbox/uh-groupings-api-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration file://scaling-policy.json
```

**scaling-policy.json:**
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

## Troubleshooting Deployments

### Deployment Stuck/Not Progressing

**Check deployment status:**
```bash
aws ecs describe-services \
  --cluster uh-groupings-sandbox \
  --services uh-groupings-api-service \
  --query 'services[0].deployments'
```

**Common causes:**
1. Health checks failing
2. Resource constraints (CPU/memory)
3. Port conflicts
4. Security group blocking traffic

**Force stop stuck deployment:**
```bash
# Stop all tasks to force fresh deployment
TASK_ARNS=$(aws ecs list-tasks \
  --cluster uh-groupings-sandbox \
  --service-name uh-groupings-api-service \
  --query 'taskArns' --output text)

for task in $TASK_ARNS; do
  aws ecs stop-task --cluster uh-groupings-sandbox --task $task
done
```

### Tasks Failing to Start

**View stopped task reason:**
```bash
# Get most recent stopped task
STOPPED_TASK=$(aws ecs list-tasks \
  --cluster uh-groupings-sandbox \
  --desired-status STOPPED \
  --query 'taskArns[0]' --output text)

aws ecs describe-tasks \
  --cluster uh-groupings-sandbox \
  --tasks $STOPPED_TASK \
  --query 'tasks[0].{StoppedReason:stoppedReason,StoppedAt:stoppedAt,Containers:containers[0].reason}'
```

### Image Pull Errors

```bash
# Verify ECR permissions
aws ecr get-repository-policy --repository-name uh-groupings-api

# Test ECR login manually
aws ecr get-login-password --region us-west-2 | \
  docker login --username AWS --password-stdin \
  ${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com
```

## Maintenance Windows

### Scheduled Maintenance

1. **Announce maintenance window**
2. **Scale down to 0 or enable maintenance mode**
```bash
# Option 1: Scale to 0
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 0

# Option 2: Update ALB to return maintenance page
# (requires custom target group with maintenance page)
```

3. **Perform maintenance (DB updates, etc.)**
4. **Scale back up**
```bash
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 2
```
5. **Verify functionality**
6. **Announce maintenance complete**

## Deployment Checklist

### Before Every Deployment

- [ ] Review changes in pull request
- [ ] All CI checks passing
- [ ] Database migrations tested
- [ ] Environment secrets verified
- [ ] Capacity planning reviewed
- [ ] Rollback plan documented

### After Every Deployment

- [ ] Monitor logs for errors (15 minutes)
- [ ] Verify health check passing
- [ ] Test critical user flows
- [ ] Check CloudWatch metrics
- [ ] Update deployment log/wiki
- [ ] Notify stakeholders of completion

---

**Last Updated:** 2026-06-09  
**Maintained by:** UH ITS DevOps Team
