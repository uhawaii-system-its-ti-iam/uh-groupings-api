# Architecture Documentation - UH Groupings API

## System Overview

The UH Groupings API is a Spring Boot application deployed on AWS using a modern, cloud-native architecture with full CI/CD automation.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        GitHub Enterprise                        │
│                     (Source Code Repository)                    │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 │ Push/PR
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AWS CodePipeline                           │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐  │
│   │  Source  │───▶│  Build   │───▶│ Deploy   │───▶│ Monitor  │  │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘  │
└─────────────────────────────────────────────────────────────────┘
                       │                    │
                       │ CodeBuild          │ ECS API
                       ▼                    ▼
         ┌──────────────────────┐  ┌──────────────────────┐
         │   AWS CodeBuild      │  │   Amazon ECS         │
         │  ┌────────────────┐  │  │  ┌────────────────┐  │
         │  │ Maven Build    │  │  │  │ Fargate Tasks  │  │
         │  │ Docker Build   │  │  │  │  (Containers)  │  │
         │  │ Push to ECR    │  │  │  └────────────────┘  │
         │  └────────────────┘  │  └──────────────────────┘
         └──────────────────────┘            │
                    │                        │
                    ▼                        ▼
         ┌──────────────────────┐  ┌──────────────────────┐
         │   Amazon ECR         │  │  Application Load    │
         │ (Container Images)   │  │     Balancer         │
         └──────────────────────┘  └──────────────────────┘
                                             │
                                             │ HTTPS
                                             ▼
                                    ┌──────────────────┐
                                    │    End Users     │
                                    └──────────────────┘
```

## Component Details

### 1. Source Control
- **Service:** GitHub Enterprise
- **Repository:** uhawaii-system-its-ti-iam/uh-groupings-api
- **Branching Strategy:**
  - **Sandbox (Personal):** Any branch (e.g., `dev-mhodges-2203`, `feature/*`, `main`)
  - **Dev (Shared):** `develop` or `main`
  - **Test/Staging:** `release/*` or `test`
  - **Production:** `main` or `production`
- **Webhook:** Triggers CodePipeline on push/merge to configured branch
- **Branch Flexibility:** Sandbox pipelines can watch any branch for isolated feature development

#### Changing Deployment Branch

Sandbox environments support deploying from any branch:

```bash
# Update pipeline to watch a different branch
aws cloudformation update-stack \
  --stack-name mhodges-groupings-sandbox-pipeline \
  --use-previous-template \
  --parameters \
    ParameterKey=GitHubBranch,ParameterValue=dev-mhodges-2203 \
    ParameterKey=Owner,UsePreviousValue=true \
  --capabilities CAPABILITY_NAMED_IAM

# Or via AWS Console:
# CodePipeline → Your Pipeline → Edit → Source Stage → Change Branch
```

This allows you to deploy `dev-mhodges-2203` → sandbox, while team environments typically deploy from standard branches.

### 2. CI/CD Pipeline (AWS CodePipeline)

#### Stage 1: Source
- **Provider:** GitHub Enterprise (via CodeStar Connection)
- **Trigger:** Automatic on commit to monitored branch
- **Output:** Source code ZIP artifact

#### Stage 2: Build (AWS CodeBuild)
- **Image:** aws/codebuild/standard:7.0
- **Runtime:** Ubuntu with Docker support
- **Steps:**
  1. Maven build (compiles Java, runs unit tests)
  2. Docker multi-stage build
  3. Docker image push to ECR
  4. Generate `imagedefinitions.json`
- **Environment Variables:**
  - AWS_ACCOUNT_ID
  - AWS_DEFAULT_REGION
  - IMAGE_REPO_NAME
  - IMAGE_TAG
- **Artifacts:** imagedefinitions.json, task-definition.json
- **Cache:** Maven dependencies cached in S3

#### Stage 3: Deploy (ECS)
- **Target:** ECS Fargate Service
- **Strategy:** Rolling update (MinimumHealthyPercent: 100%)
- **Health Check:** ALB monitors `/actuator/health`
- **Rollback:** Automatic on deployment failure

### 3. Container Registry (Amazon ECR)

- **Repository:** uh-groupings-api
- **Image Scanning:** Enabled on push
- **Lifecycle Policy:**
  - Keep last 10 production images
  - Keep last 5 non-production images
  - Expire untagged images after 7 days
- **Encryption:** AES-256

### 4. Compute (Amazon ECS Fargate)

#### Cluster Configuration
- **Name:** uh-groupings-{environment}
- **Capacity Provider:** FARGATE (with FARGATE_SPOT fallback)
- **Container Insights:** Enabled

#### Service Configuration
- **Launch Type:** Fargate
- **Desired Count:** 2 (adjustable per environment)
- **Task Definition:**
  - CPU: 512 (0.5 vCPU)
  - Memory: 1024 MB
  - Port: 8080
- **Deployment:**
  - MaximumPercent: 200%
  - MinimumHealthyPercent: 100%
  - Health Check Grace: 60s

#### Task IAM Roles
- **Execution Role:** Pulls images, reads secrets, writes logs
- **Task Role:** Application runtime permissions

### 5. Load Balancing (Application Load Balancer)

- **Type:** Application Load Balancer (Layer 7)
- **Scheme:** Internet-facing
- **Listeners:**
  - HTTP:80 → Forward to target group
  - (Future) HTTPS:443 → SSL termination
- **Target Group:**
  - Protocol: HTTP
  - Port: 8080
  - Health Check: `/actuator/health`
  - Health Check Interval: 30s
  - Healthy Threshold: 2
  - Unhealthy Threshold: 3

### 6. Secrets Management (AWS Secrets Manager)

Secrets stored (never in code):
- `groupings/api/grouper-url`
- `groupings/api/grouper-username`
- `groupings/api/grouper-password`
- `groupings/api/jwt-secret`
- `groupings/api/db-password`

**Access:** Task execution role injects secrets as environment variables at container startup.

### 7. Monitoring & Logging

#### CloudWatch Logs
- **Log Group:** `/ecs/uh-groupings-api`
- **Retention:** 30 days
- **Stream Prefix:** `ecs/{task-id}`

#### CloudWatch Metrics
- ECS Service CPU/Memory utilization
- ALB request count, latency, error rates
- CodeBuild success/failure rates
- Custom application metrics (via Spring Boot Actuator)

#### Alarms (Recommended)
- High CPU utilization (>80%)
- High memory utilization (>80%)
- ALB 5xx errors
- ECS task failures
- CodePipeline execution failures

### 8. Networking

#### VPC Configuration
- **Subnets:** Minimum 2 public subnets in different AZs
- **Security Groups:**
  - ALB SG: Inbound 80/443 from Internet
  - ECS SG: Inbound 8080 from ALB SG only

#### Network Flow
```
Internet → ALB (public subnets) → ECS Tasks (public subnets) → Grouper API
```

## Data Flow

### Request Flow
```
1. User → HTTPS → ALB
2. ALB → Health Check → ECS Task:8080/actuator/health
3. ALB → Route Request → ECS Task:8080/api/v2.1/*
4. ECS Task → Authenticate (JWT) → Process Request
5. ECS Task → Query Grouper API (external)
6. ECS Task → Response → ALB → User
```

### Deployment Flow
```
1. Developer → Git Push → GitHub Enterprise (any branch for sandbox)
2. GitHub → Webhook → CodePipeline (watches configured branch)
3. CodePipeline → Trigger → CodeBuild
4. CodeBuild → Maven Build → Docker Build → ECR Push
5. CodePipeline → Update ECS Service
6. ECS → Pull new image from ECR
7. ECS → Start new tasks (rolling deployment)
8. ALB → Health check new tasks
9. ECS → Drain old tasks
10. Deployment complete
```

**Note:** Sandbox pipelines can watch any branch (e.g., `dev-mhodges-2203`), while team environments typically watch standard branches (`develop`, `test`, `main`).

## Technology Stack

### Application Layer
- **Language:** Java 21
- **Framework:** Spring Boot 4.0.6
- **Packaging:** WAR (deployed as standalone)
- **Dependencies:**
  - Spring Web MVC
  - Spring Security
  - Spring Actuator
  - Grouper Client 4.23.0
  - JWT (Java Jason Web Token)
  - Spring Cloud Vault

### Infrastructure Layer
- **Container Runtime:** Docker
- **Orchestration:** ECS Fargate
- **Build Tool:** Maven 3.9
- **Base Image:** Eclipse Temurin 21 JRE

### DevOps Tools
- **Version Control:** Git (GitHub Enterprise)
- **CI/CD:** AWS CodePipeline + CodeBuild
- **IaC:** AWS CloudFormation
- **Monitoring:** CloudWatch

## Environments

### Environment Configuration

| Environment    | Purpose                                | Typical Branch                                      | Owner                        | Auto-Deploy              |
|----------------|----------------------------------------|-----------------------------------------------------|------------------------------|--------------------------|
| **Sandbox**    | Personal development & experimentation | Any (e.g., `dev-mhodges-2203`, `feature/*`, `main`) | Individual (e.g., `mhodges`) | Yes                      |
| **Dev**        | Shared integration testing             | `develop` or `main`                                 | Team (`its-iam`)             | Yes                      |
| **Test**       | QA & staging                           | `release/*` or `test`                               | Team (`its-iam`)             | Yes (with approval)      |
| **Production** | Live system                            | `main` or `production`                              | Team (`its-iam`)             | Manual approval required |

### Branch Flexibility

**Sandbox Environments:**
- Can deploy from **any branch** - ideal for isolated feature development
- Change branch via CodePipeline configuration
- Example: Deploy `dev-mhodges-2203` to `mhodges-groupings-sandbox`

**Team Environments:**
- Follow GitFlow or trunk-based development patterns
- Branch protection and approval workflows enforced
- Standard naming: `its-iam-groupings-<env>-*`

### Changing Deployment Branch (Sandbox Example)

```bash
# Update sandbox pipeline to watch your feature branch
aws cloudformation update-stack \
  --stack-name mhodges-groupings-sandbox-pipeline \
  --use-previous-template \
  --parameters \
    ParameterKey=GitHubBranch,ParameterValue=dev-mhodges-2203 \
    ParameterKey=Owner,UsePreviousValue=true \
    ParameterKey=Project,UsePreviousValue=true \
    ParameterKey=Environment,UsePreviousValue=true \
  --capabilities CAPABILITY_NAMED_IAM

# Or in AWS Console:
# CodePipeline → mhodges-groupings-sandbox-pipeline → Edit → Source → Change Branch
```

**Result:** Your sandbox will now automatically deploy whenever you push to `dev-mhodges-2203`!

## Security Architecture

### Authentication & Authorization
- **User Auth:** JWT tokens
- **AWS IAM:** Role-based access control
- **Secrets:** Local (properties file) or AWS Secrets Manager (encrypted at rest)

### Secrets Management
- **Local Development:** 
  - Properties file: `~/.yourname-conf/uh-groupings-api-overrides.properties`
  - Converted to Docker .env via `docker/dev-overrides-properties.sh`
- **AWS Production:**
  - Stored in AWS Secrets Manager (AES-256 encryption)
  - Injected into containers via ECS Task Definition
  - Access controlled by IAM policies
- **See:** [docs/SECRETS.md](SECRETS.md) for complete guide

### Network Security
- **Encryption in Transit:** HTTPS (ALB to client)
- **Internal Communication:** HTTP over private networking
- **Security Groups:** Principle of least privilege

### Container Security
- **Non-root User:** Application runs as `appuser`
- **Image Scanning:** ECR scans on push
- **Minimal Image:** Multi-stage build (runtime only)

### Compliance
- **Logging:** All requests logged to CloudWatch
- **Audit:** CloudTrail for AWS API calls
- **Secrets Rotation:** Manual (recommend automation)

## Scalability & Resilience

### Horizontal Scaling
- **Auto Scaling:** CPU/Memory target tracking
- **Manual Scaling:** Adjust desired count
- **Min/Max:** 2-10 tasks (configurable)

### High Availability
- **Multi-AZ:** Tasks distributed across 2+ AZs
- **ALB:** Distributes traffic across healthy tasks
- **Rolling Updates:** Zero-downtime deployments

### Disaster Recovery
- **RTO:** < 15 minutes (manual recovery)
- **RPO:** ~5 minutes (last committed code)
- **Backup Strategy:**
  - ECR images retained (lifecycle policy)
  - Infrastructure as Code (CloudFormation)
  - Database backups (external Grouper system)

## Cost Optimization

### Current Costs (Estimated - Sandbox)
- **ECS Fargate:** ~$30-40/month
- **ALB:** ~$20/month
- **ECR:** ~$1-2/month
- **CloudWatch Logs:** ~$1-5/month
- **CodeBuild:** ~$0.005/minute (only during builds)
- **Total:** ~$50-70/month

### Cost Reduction Strategies
1. Use FARGATE_SPOT for non-production
2. Implement VPC endpoints (reduce data transfer)
3. Optimize log retention policies
4. Right-size task CPU/memory
5. Use Reserved Capacity for production

## Future Enhancements

### Short Term
- [ ] Add HTTPS/SSL certificate
- [ ] Implement blue/green deployments
- [ ] Add custom domain name
- [ ] Automated security scanning in pipeline
- [ ] Secrets rotation automation

### Medium Term
- [ ] Multi-region deployment
- [ ] CDN integration (CloudFront)
- [ ] Enhanced monitoring dashboards
- [ ] Automated performance testing
- [ ] Container vulnerability scanning

### Long Term
- [ ] Service mesh (AWS App Mesh)
- [ ] Serverless migration considerations
- [ ] Advanced autoscaling (predictive)
- [ ] Cost anomaly detection
- [ ] Chaos engineering implementation

---

**Document Version:** 1.0  
**Last Updated:** 2026-06-09  
**Authors:** UH ITS DevOps Team
