# 🚀 Getting Started Checklist - AWS CI/CD Pipeline

## Quick Reference Card for Your First AWS Project

---

## ✅ Pre-Deployment Checklist

### Local Environment Setup
- [ ] **AWS CLI installed and configured**
  ```bash
  aws --version
  aws configure  # Enter your credentials
  aws sts get-caller-identity  # Verify it works
  ```

- [ ] **Docker Desktop installed and running**
  ```bash
  docker --version
  docker ps  # Should not error
  ```

- [ ] **Git configured with GitHub Enterprise**
  ```bash
  git config --global user.name "Your Name"
  git config --global user.email "your.email@hawaii.edu"
  ```

- [ ] **Credentials ready**
  - [ ] AWS Access Key ID and Secret
  - [ ] Grouper API URL
  - [ ] Grouper username and password
  - [ ] Database credentials (if applicable)

---

## 🎯 Choose Your Path

### Option A: Quick Automated Setup (Recommended) ⭐

**Time: ~60 minutes**

1. [ ] **Set up local development environment**
   ```bash
   # Create properties file
   mkdir -p ~/.$(whoami)-conf
   nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   # Add your Grouper credentials (see docker/README.md)
   ```

2. [ ] **Test locally first**
   ```bash
   docker-compose up
   # Open http://localhost:8080/actuator/health
   # Press Ctrl+C when done
   docker-compose down
   ```

3. [ ] **Run automated AWS setup**
   ```bash
   chmod +x aws/setup.sh
   ./aws/setup.sh
   ```
   **The script will prompt you for:**
   - Grouper API URL
   - Grouper username/password
   - Database password
   - VPC and Subnet IDs

4. [ ] **Configure GitHub Enterprise Connection**
   - Open [AWS CodeStar Connections](https://console.aws.amazon.com/codesuite/settings/connections)
   - Create new connection
   - Connect to your GitHub Enterprise server
   - Note the Connection ARN

5. [ ] **Deploy CodePipeline**
   ```bash
   # See docs/AWS_QUICKSTART.md for exact commands
   ```

6. [ ] **Test your deployment**
   ```bash
   curl http://your-alb-url/actuator/health
   ```

✅ **Done!** Go to: [docs/AWS_QUICKSTART.md](docs/AWS_QUICKSTART.md)

---

### Option B: Manual Step-by-Step Setup

**Time: ~90 minutes | Learn every detail**

- [ ] **Phase 1: ECR Repository (5 min)**
  - Deploy `aws/cloudformation/ecr-repository.yml`
  - Build and push Docker image

- [ ] **Phase 2: Secrets Manager (10 min)**
  - Create 5 secrets for application config

- [ ] **Phase 3: VPC & Networking (15 min)**
  - Identify VPC and subnets
  - Verify network configuration

- [ ] **Phase 4: ECS Cluster (20 min)**
  - Deploy `aws/cloudformation/ecs-cluster.yml`
  - Wait for ALB and ECS service

- [ ] **Phase 5: CodePipeline (25 min)**
  - Configure GitHub connection
  - Deploy `aws/cloudformation/codepipeline.yml`
  - Test the pipeline

- [ ] **Phase 6: Verification (10 min)**
  - Test endpoints
  - Check logs
  - Monitor metrics

✅ **Done!** Go to: [docs/AWS_SETUP.md](docs/AWS_SETUP.md)

---

## 📋 Post-Deployment Checklist

### Immediate Verification

- [ ] **Health endpoint responding**
  ```bash
  curl http://your-alb-url/actuator/health
  # Expected: {"status":"UP"}
  ```

- [ ] **Logs are flowing to CloudWatch**
  ```bash
  aws logs tail /ecs/uh-groupings-api --follow
  ```

- [ ] **ECS service running**
  ```bash
  aws ecs describe-services \
    --cluster uh-groupings-sandbox \
    --services uh-groupings-api-service \
    --query 'services[0].{Running:runningCount,Desired:desiredCount}'
  ```

- [ ] **Pipeline triggered on push**
  ```bash
  # Make a test commit
  echo "# Test" >> README.md
  git add README.md
  git commit -m "Test CI/CD pipeline"
  git push origin main
  
  # Check pipeline status
  aws codepipeline get-pipeline-state \
    --name uh-groupings-api-pipeline-sandbox
  ```

### Security & Monitoring Setup

- [ ] **Verify secrets are not in Git**
  ```bash
  git status  # dev.env should not appear
  grep -r "password" .git/  # Should find nothing
  ```

- [ ] **CloudWatch alarms configured** (optional but recommended)
  - CPU utilization > 80%
  - Memory utilization > 80%
  - ALB 5xx errors
  - ECS task failures

- [ ] **IAM roles reviewed**
  - Task execution role has minimal permissions
  - Task role has application permissions only

### Documentation & Team

- [ ] **Document your AWS account details**
  - AWS Account ID: _______________
  - Region: _______________
  - VPC ID: _______________
  - Subnet IDs: _______________

- [ ] **Share access with team**
  - Add team members to AWS IAM
  - Share documentation links
  - Schedule knowledge transfer session

- [ ] **Update project README**
  - Add deployment instructions
  - Link to this documentation
  - Add application URL

---

## 🔧 Essential Commands Reference

### Daily Operations

```bash
# View logs
aws logs tail /ecs/uh-groupings-api --follow

# Check service status
aws ecs describe-services \
  --cluster uh-groupings-sandbox \
  --services uh-groupings-api-service

# Force new deployment
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --force-new-deployment

# Scale up
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 4

# Scale down (save costs)
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 0
```

### Troubleshooting

```bash
# Check recent errors in logs
aws logs filter-log-events \
  --log-group-name /ecs/uh-groupings-api \
  --filter-pattern "ERROR" \
  --start-time $(date -u -d '30 minutes ago' +%s)000

# Check why task stopped
aws ecs describe-tasks \
  --cluster uh-groupings-sandbox \
  --tasks $(aws ecs list-tasks --cluster uh-groupings-sandbox --query 'taskArns[0]' --output text) \
  --query 'tasks[0].stoppedReason'

# View pipeline execution
aws codepipeline get-pipeline-state \
  --name uh-groupings-api-pipeline-sandbox
```

---

## 📚 Documentation Map

### Read These First
1. **[AWS_QUICKSTART.md](docs/AWS_QUICKSTART.md)** - 60-minute automated setup
2. **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Understand what you built

### Deep Dives
3. **[AWS_SETUP.md](docs/AWS_SETUP.md)** - Detailed manual setup
4. **[AWS_DEPLOYMENT.md](docs/AWS_DEPLOYMENT.md)** - Deployment operations
5. **[aws/README.md](aws/README.md)** - AWS infrastructure details

### Quick Reference
- **This file** - Your checklist and command reference
- **[docs/README.md](docs/README.md)** - Documentation hub

---

## 🆘 Common Issues & Solutions

### Issue: "Cannot connect to Docker daemon"
**Solution:**
```bash
open -a Docker  # Start Docker Desktop
# Wait 30 seconds, then retry
```

### Issue: "AWS credentials not found"
**Solution:**
```bash
aws configure
# Enter: Access Key ID, Secret, Region (us-west-2), Format (json)
aws sts get-caller-identity  # Verify
```

### Issue: "Health checks failing in ECS"
**Solution:**
```bash
# Check application logs
aws logs tail /ecs/uh-groupings-api --follow

# Check if app is listening on port 8080
# Check if /actuator/health endpoint exists
# Verify security groups allow traffic
```

### Issue: "Pipeline not triggering"
**Solution:**
- Verify GitHub webhook exists in repo settings
- Check CodeStar connection status is "Available"
- Ensure branch name matches pipeline config

### Issue: "Task keeps restarting"
**Solution:**
```bash
# Check stopped task reason
aws ecs describe-tasks \
  --cluster uh-groupings-sandbox \
  --tasks <task-id> \
  --query 'tasks[0].{Reason:stoppedReason,Containers:containers[*].reason}'

# Common causes:
# - Secrets not found (check Secrets Manager)
# - Health check failing (check /actuator/health)
# - Out of memory (increase task memory)
```

---

## 💰 Cost Management

### Current Monthly Estimate
- **Sandbox:** ~$50-70/month
- **Production:** ~$100-150/month (with more capacity)

### Save Money
```bash
# Stop when not in use
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 0

# Start again when needed
aws ecs update-service \
  --cluster uh-groupings-sandbox \
  --service uh-groupings-api-service \
  --desired-count 2
```

### Clean Up Everything (when done testing)
```bash
# WARNING: This deletes all resources!
aws cloudformation delete-stack --stack-name uh-groupings-pipeline-sandbox
aws cloudformation delete-stack --stack-name uh-groupings-ecs-sandbox
aws cloudformation delete-stack --stack-name uh-groupings-ecr-sandbox

# Delete secrets
aws secretsmanager delete-secret \
  --secret-id groupings/api/grouper-url \
  --force-delete-without-recovery
# (repeat for all secrets)
```

---

## 🎉 Success Criteria

You're successful when:

✅ Local Docker container runs without errors  
✅ Application accessible via ALB URL  
✅ Health endpoint returns `{"status":"UP"}`  
✅ Logs visible in CloudWatch  
✅ Git push triggers pipeline automatically  
✅ Deployment completes without errors  
✅ Application responds to API requests  

---

## 🚀 Next Level

After basic setup works:

- [ ] **Add SSL certificate** (AWS Certificate Manager + ALB)
- [ ] **Configure custom domain** (Route 53)
- [ ] **Set up autoscaling** (target tracking)
- [ ] **Add monitoring dashboard** (CloudWatch)
- [ ] **Configure alarms** (email/Slack notifications)
- [ ] **Deploy to production** (separate environment)
- [ ] **Implement blue/green deployments** (CodeDeploy)

---

## 📞 Getting Help

1. **Check docs:** Start with [docs/AWS_QUICKSTART.md](docs/AWS_QUICKSTART.md)
2. **Search logs:** `aws logs tail /ecs/uh-groupings-api --follow`
3. **Troubleshooting:** [docs/AWS_SETUP.md#troubleshooting](docs/AWS_SETUP.md#troubleshooting)
4. **Internal support:** #groupings-dev Slack channel
5. **AWS support:** Through AWS Console

---

**You've got this!** 💪 This is your first AWS project, not your last.

**Quick Start:** Open [docs/AWS_QUICKSTART.md](docs/AWS_QUICKSTART.md) and begin! 🚀

---
