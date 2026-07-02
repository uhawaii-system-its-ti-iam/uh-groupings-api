# Architecture Documentation - UH Groupings API

<!-- TOC -->
* [Architecture Documentation - UH Groupings API](#architecture-documentation---uh-groupings-api)
  * [System Overview](#system-overview)
  * [High-Level Architecture](#high-level-architecture)
  * [Component Details](#component-details)
    * [1. Source Control](#1-source-control)
      * [Changing Deployment Branch](#changing-deployment-branch)
    * [2. CI/CD Pipeline (AWS CodePipeline)](#2-cicd-pipeline-aws-codepipeline)
      * [Stage 1: Source](#stage-1-source)
      * [Stage 2: Build (AWS CodeBuild)](#stage-2-build-aws-codebuild)
      * [Stage 3: Deploy (ECS)](#stage-3-deploy-ecs)
    * [3. Container Registry (Amazon ECR)](#3-container-registry-amazon-ecr)
    * [4. Compute (Amazon ECS Fargate)](#4-compute-amazon-ecs-fargate)
      * [Cluster Configuration](#cluster-configuration)
      * [Service Configuration](#service-configuration)
      * [Task IAM Roles](#task-iam-roles)
    * [5. Load Balancing (Application Load Balancer)](#5-load-balancing-application-load-balancer)
    * [6. Secrets Management (AWS Secrets Manager)](#6-secrets-management-aws-secrets-manager)
    * [7. Monitoring & Logging](#7-monitoring--logging)
      * [CloudWatch Logs](#cloudwatch-logs)
      * [CloudWatch Metrics](#cloudwatch-metrics)
      * [Alarms (Recommended)](#alarms-recommended)
    * [8. Networking](#8-networking)
      * [VPC Configuration](#vpc-configuration)
      * [Network Flow](#network-flow)
  * [Data Flow](#data-flow)
    * [Request Flow](#request-flow)
    * [Deployment Flow](#deployment-flow)
  * [Technology Stack](#technology-stack)
    * [Application Layer](#application-layer)
    * [Infrastructure Layer](#infrastructure-layer)
    * [DevOps Tools](#devops-tools)
  * [Environments](#environments)
    * [Environment Configuration](#environment-configuration)
    * [Branch Flexibility](#branch-flexibility)
    * [Changing Deployment Branch](#changing-deployment-branch-1)
  * [Security Architecture](#security-architecture)
    * [Authentication & Authorization](#authentication--authorization)
    * [Secrets Management](#secrets-management)
    * [Network Security](#network-security)
    * [Container Security](#container-security)
    * [Compliance](#compliance)
  * [Scalability & Resilience](#scalability--resilience)
    * [Horizontal Scaling](#horizontal-scaling)
    * [High Availability](#high-availability)
    * [Disaster Recovery](#disaster-recovery)
  * [Cost Optimization](#cost-optimization)
    * [Current Costs (Estimated - Sandbox)](#current-costs-estimated---sandbox)
    * [Cost Reduction Strategies](#cost-reduction-strategies)
  * [Future Enhancements](#future-enhancements)
    * [Short Term](#short-term)
    * [Medium Term](#medium-term)
    * [Long Term](#long-term)
<!-- TOC -->

## System Overview

The UH Groupings API is a Spring Boot application deployed on AWS using a modern, cloud-native architecture with full CI/CD automation.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                           GitHub                              │
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
- **Service:** GitHub (github.com)
- **Repository:** uhawaii-system-its-ti-iam/uh-groupings-api
- **Default Branch:** `main`
- **Branching Strategy:**
  - **Sandbox / Pilot:** `main` (configurable per pilot — see below)
  - **Dev (Shared):** `develop` or `main`
  - **Test/Staging:** `release/*` or `test`
  - **Production:** `main` or `production`
- **Webhook:** Triggers CodePipeline on push/merge to configured branch
- **Branch Flexibility:** Sandbox pipelines can watch any branch for isolated feature development; the canonical configuration uses `main`.

#### Changing Deployment Branch

Sandbox environments can deploy from any branch:

```bash
# Update pipeline to watch a different branch (canonical: main)
aws cloudformation update-stack \
  --stack-name groupings-api-pipeline-sandbx \
  --use-previous-template \
  --parameters \
    ParameterKey=GitHubBranch,ParameterValue=main \
    ParameterKey=Owner,UsePreviousValue=true \
  --capabilities CAPABILITY_NAMED_IAM

# Or via AWS Console:
# CodePipeline → Your Pipeline → Edit → Source Stage → Change Branch
```

For pilot/feature work, this same command can be used to point the pipeline at a feature branch.

### 2. CI/CD Pipeline (AWS CodePipeline)

#### Stage 1: Source
- **Provider:** GitHub (via AWS CodeConnections)
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

Only **two** values are stored in AWS Secrets Manager — the truly sensitive runtime credentials the API needs at startup:

- `groupings/api/grouper-password` — Grouper service account password (`grouperClient.webService.password`)
- `groupings/api/jwt-secret` — JWT signing key (`jwt.secret.key`), generated at provisioning by `aws/setup.sh`

Non-secret values that the deployed API still needs (`grouperClient.webService.url`, `grouperClient.webService.login`, email flags, etc.) live in the ECS task definition `environment[]` array — not in Secrets Manager.

**Provisioning:** `aws/setup.sh` (invoked via `make aws-setup` with `AWS_PROFILE=uh-groupings` exported) creates both secrets idempotently — re-running updates rather than duplicates them.

**Access at runtime:** The ECS task execution role injects the two secrets as environment variables (`GROUPERCLIENT_WEBSERVICE_PASSWORD`, `JWT_SECRET_KEY`) when the container starts. The values never appear in plaintext task config or logs.

**JWT key ownership:** The API project owns this key. Future UI projects consume the same `groupings/api/jwt-secret` rather than generating their own. See [SECRETS.md](SECRETS.md#jwt-secret-ownership-api-generates-ui-consumes).

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
- **VPC:** Assumed to already exist and to provide public internet egress (main route table routes `0.0.0.0/0` to an Internet Gateway). Its ID is supplied via `VPC_ID` in `aws/.env`.
- **Subnets:** Two public subnets in different AZs, created by `vpc.yml` inside the existing VPC. Their IDs are exported and consumed by `ecs-service.yml` (the 2-AZ minimum is an AWS ALB requirement).
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
1. User → HTTP → ALB    (HTTPS:443 listener planned; current is HTTP:80 only)
2. ALB → Health Check → ECS Task:8080/actuator/health
3. ALB → Route Request → ECS Task:8080/api/v2.1/*
4. ECS Task → Authenticate (JWT) → Process Request
5. ECS Task → Query Grouper API (external)
6. ECS Task → Response → ALB → User
```

### Deployment Flow
```
1. Developer → Git Push → GitHub (any branch for sandbox)
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

**Note:** The canonical configuration deploys from `main`. Pilot or sandbox pipelines may temporarily watch a feature branch via the pipeline's `GitHubBranch` parameter; team environments (dev/test/prod) watch standard branches (`develop`, `test`, `main`).

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
- **Version Control:** Git (GitHub)
- **CI/CD:** AWS CodePipeline + CodeBuild
- **IaC:** AWS CloudFormation
- **Monitoring:** CloudWatch

## Environments

### Environment Configuration

| Environment    | Purpose                                | Typical Branch                                             | Owner                         | Auto-Deploy              |
|----------------|----------------------------------------|------------------------------------------------------------|-------------------------------|--------------------------|
| **Sandbox**    | Personal development & experimentation | `main` (configurable per pilot — feature branches allowed) | Individual (e.g., `mhodges`)  | Yes                      |
| **Dev**        | Shared integration testing             | `develop` or `main`                                        | Team (`its-iam`)              | Yes                      |
| **Test**       | QA & staging                           | `release/*` or `test`                                      | Team (`its-iam`)              | Yes (with approval)      |
| **Production** | Live system                            | `main` or `production`                                     | Team (`its-iam`)              | Manual approval required |

### Branch Flexibility

**Sandbox Environments:**
- Default to `main` for the canonical configuration
- May temporarily watch a feature branch for isolated pilot work
- Change branch via CodePipeline configuration

**Team Environments:**
- Follow GitFlow or trunk-based development patterns
- Branch protection and approval workflows enforced
- Standard naming: `its-iam-groupings-<env>-*`

### Changing Deployment Branch

```bash
# Point the sandbox pipeline at a feature branch (e.g., for a pilot)
aws cloudformation update-stack \
  --stack-name groupings-api-pipeline-sandbx \
  --use-previous-template \
  --parameters \
    ParameterKey=GitHubBranch,ParameterValue=feature/your-branch \
    ParameterKey=Owner,UsePreviousValue=true \
    ParameterKey=Project,UsePreviousValue=true \
    ParameterKey=Environment,UsePreviousValue=true \
  --capabilities CAPABILITY_NAMED_IAM

# Or in AWS Console:
# CodePipeline → groupings-api-pipeline-sandbx → Edit → Source → Change Branch
```

When the pilot work is complete, switch the pipeline back to `main`.

## Security Architecture

### Authentication & Authorization
- **User Auth:** JWT tokens
- **AWS IAM:** Role-based access control
- **Secrets:** Local (properties file) or AWS Secrets Manager (encrypted at rest)

### Secrets Management

The project handles two distinct categories of secrets, stored differently:

**Application secrets** (Grouper password, JWT key) — read by the running API at startup:
- **Local development:** `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`, bind-mounted read-only into the Docker container and loaded via `SPRING_CONFIG_IMPORT`. The file is never committed.
- **AWS deployment:** AWS Secrets Manager (`groupings/api/*`), encrypted at rest (AES-256), injected into ECS tasks via the task definition's `secrets[]` array.

**AWS account credentials** (temporary SSO session tokens) — used only by developers running `make aws-setup` and other AWS Make targets:
- Issued by IAM Identity Center (SSO) and cached by the AWS CLI in the developer's `~/.aws/` SSO token cache. Resolved via `AWS_PROFILE`. Sessions are short-lived (typically 1–12 h); any `make aws-*` target re-authenticates automatically, or refresh with `make aws-sso-login`.
- Bootstrapped automatically on the first `make aws-*` command (writes the profile from `aws/.env` and opens a browser to sign in); requires the AWS CLI v2 installed on the host.
- Holds **no application secrets**. The CLI reads credentials from the cached SSO token in `~/.aws/`.

**See:** [docs/SECRETS.md](SECRETS.md) for the complete model, including IAM permissions and rotation guidance.

### Network Security
- **Encryption in Transit:** ALB currently exposes HTTP:80; HTTPS:443 with ACM certificate is planned (see "Future Enhancements")
- **Internal Communication:** HTTP between ALB and ECS tasks (private VPC)
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
