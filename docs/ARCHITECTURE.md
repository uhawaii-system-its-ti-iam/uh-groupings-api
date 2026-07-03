# Architecture Documentation - UH Groupings API

<!-- TOC -->
* [Architecture Documentation - UH Groupings API](#architecture-documentation---uh-groupings-api)
  * [System Overview](#system-overview)
  * [High-Level Architecture](#high-level-architecture)
  * [Component Details](#component-details)
    * [1. Source Control](#1-source-control)
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
      * [Sandbox Architecture (Current)](#sandbox-architecture-current)
      * [Production Architecture (Target)](#production-architecture-target)
      * [Security Groups](#security-groups)
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
  * [Security Architecture](#security-architecture)
    * [Authentication & Authorization](#authentication--authorization)
    * [Secrets Management](#secrets-management)
    * [Network Security](#network-security)
    * [Access Restriction: HTTPS from the UI Deployment Only (Planned)](#access-restriction-https-from-the-ui-deployment-only-planned)
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
│                           GitHub                                │
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
- **Webhook:** Triggers CodePipeline on push/merge to the branch configured in `aws/.env` (`GITHUB_BRANCH`).
- **Branch Flexibility:** To change the deployed branch, update `GITHUB_BRANCH` in `aws/.env` and redeploy the pipeline stack (see [docs/AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md)).

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
- **Health Check:** ALB monitors `/uhgroupingsapi/actuator/health` on port 8080
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
  - Health Check: `/uhgroupingsapi/actuator/health`
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

#### Sandbox Architecture (Current)

The sandbox deploys all components into two shared subnets. Tasks have no public IP;
VPC endpoints provide access to AWS services, and a VPN route (once configured)
provides the path back to the on-prem Grouper server.

```
                              Internet
                                 │
                          Internet Gateway
                                 │
  ┌──────────────────── VPC: 172.18.0.128/25 ─────────────────────┐
  │                                                               │
  │   Application Load Balancer (internet-facing, spans both AZs) │
  │        ┌──────────────────────┴──────────────────────┐        │
  │  ┌─────┴─────────────────────┐  ┌────────────────────┴─────┐  │
  │  │ Subnet A (AZ-1)           │  │ Subnet B (AZ-2)          │  │
  │  │ 172.18.0.176/28           │  │ 172.18.0.192/28          │  │
  │  │                           │  │                          │  │
  │  │  • ALB ENI                │  │  • ALB ENI               │  │
  │  │  • Fargate task :8080     │  │  (task may run here      │  │
  │  │    (ECS_TASK_COUNT=1;     │  │   instead — ECS picks    │  │
  │  │     no public IP)         │  │   the subnet)            │  │
  │  │  • Interface endpoints:   │  │                          │  │
  │  │    ecr.api, ecr.dkr,      │  │                          │  │
  │  │    secretsmanager, logs   │  │                          │  │
  │  │    (Subnet A only)        │  │                          │  │
  │  └───────────────────────────┘  └──────────────────────────┘  │
  │                                                               │
  │   S3 Gateway endpoint → attached to main route table          │
  │                                                               │
  │   Main Route Table:                                           │
  │     172.18.0.128/25  → local                                  │
  │     0.0.0.0/0        → Internet Gateway                       │
  │     128.171.0.0/16   → Virtual Private Gateway  *             │
  └───────────────────────────────┼───────────────────────────────┘
                                  │
                    Virtual Private Gateway  *
                                  │
                          IPsec VPN Tunnel  *
                                  │
                       UH Firewall / Router
                                  │
                      Grouper Web Services
                       128.171.94.186:443

  * The VPN already exists in the account; the 128.171.0.0/16 route is not
    yet added to the route table — required for Grouper connectivity.
```

**Key characteristics:**
- ALB and Fargate tasks share the same two subnets. The internet-facing ALB places an ENI in each AZ (both AZs required); the single task lands in whichever subnet ECS selects.
- Fargate tasks have no public IP (`AssignPublicIp: DISABLED`); the subnets set `MapPublicIpOnLaunch: false`. The internet-facing ALB still gets public IPs on its own ENIs.
- Interface VPC endpoints (`ecr.api`, `ecr.dkr`, `secretsmanager`, `logs`) live in **Subnet A only** to reduce cost; private DNS lets a task in either subnet reach them cross-AZ. The S3 gateway endpoint attaches to the main route table (free).
- Security groups: ALB SG allows inbound 80/443 from internet; ECS SG allows inbound 8080 from the ALB SG only; endpoint SG allows 443 from the subnet CIDRs.
- The VPN tunnel already exists in the account, but the `128.171.0.0/16 → VGW` route is not yet added to the route table — required for Grouper access (see notes in `aws/.env`).
- Single task (`ECS_TASK_COUNT=1`) — no HA requirement for sandbox.

#### Production Architecture (Target)

Production separates public and private subnets, adds HTTPS termination,
restricts the ALB to UI-only traffic, and uses a properly sized VPC.

```
                                Internet
                                   │
                            Internet Gateway
                                   │
               ┌─────────────────────────────────────┐
               │   VPC (larger CIDR, e.g. /24)       │
               │                                     │
               │  ┌────────────────────────────────┐ │
               │  │      Public Subnets (2 AZs)    │ │
               │  │                                │ │
               │  │   ┌──────ALB (HTTPS:443)─────┐ │ │
               │  │   │  ACM cert, UI SG only    │ │ │
               │  │   └────────────┬─────────────┘ │ │
               │  └────────────────┼───────────────┘ │
               │                   │                 │
               │  ┌────────────────┼───────────────┐ │
               │  │     Private Subnets (2 AZs)    │ │
               │  │                │               │ │
               │  │  ┌─────────┐   │  ┌─────────┐  │ │
               │  │  │ Fargate │   │  │ Fargate │  │ │
               │  │  │ Task    │   │  │ Task    │  │ │
               │  │  └────┬────┘   │  └────┬────┘  │ │
               │  │       │        │       │       │ │
               │  └───────┼────────┼───────┼───────┘ │
               │          │                │         │
               │        VPC Endpoints (ECR, S3,      │
               │          Secrets Mgr, Logs)         │
               │                   │                 │
               │         Private Route Table         │
               │          128.171.0.0/16 → VGW       │
               │           (no IGW route)            │
               └───────────────────┼─────────────────┘
                                   │
                        Virtual Private Gateway
                                   │
                            IPsec VPN Tunnel
                                   │
                         UH Firewall / Router
                                   │
                         Grouper Web Services
                         128.171.94.186:443
```

**Key differences from sandbox:**
- **Public/private subnet separation:** ALB lives in public subnets (IGW route); Fargate tasks live in private subnets (no IGW route, VPN route only).
- **HTTPS-only:** ALB listener on 443 with an ACM certificate; HTTP:80 removed or redirected.
- **UI-only access:** ALB security group ingress restricted to the companion UI deployment's security group (`SourceSecurityGroupId` via cross-stack import) — not open to the internet.
- **Multi-AZ HA:** `ECS_TASK_COUNT >= 2`, tasks spread across AZs.
- **Larger VPC CIDR** to accommodate the additional subnets and future growth.
- **VPN route** on the private route table provides the only egress path for task → Grouper traffic (no internet path from tasks).
- **Auto Scaling:** CPU/memory target tracking for horizontal scaling.

#### Security Groups

| Security Group  | Inbound                                   | Source                                     | Notes                                |
|-----------------|-------------------------------------------|--------------------------------------------|--------------------------------------|
| ALB SG          | 80 + 443 (sandbox); 443 only (production) | `0.0.0.0/0` (sandbox) → UI SG (production) | Sandbox currently listens on HTTP:80 |
| ECS SG          | 8080                                      | ALB SG                                     | Same in both environments            |
| VPC Endpoint SG | 443                                       | Subnet CIDRs                               | Interface endpoints only             |

#### Network Flow
```
Sandbox:     Internet → ALB → ECS task (shared subnet) → VPN * → Grouper API
Production:  Internet → ALB (public subnets) → ECS task (private subnets) → VPN → Grouper API

  * sandbox VPN route pending
```

## Data Flow

### Request Flow
```
1. User → HTTP → ALB    (HTTPS:443 listener planned; current is HTTP:80 only)
2. ALB → Health Check → ECS Task:8080/uhgroupingsapi/actuator/health
3. ALB → Route Request → ECS Task:8080/uhgroupingsapi/api/groupings/v2.1/*
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

| Environment    | Purpose                                | Branch (from `GITHUB_BRANCH` in `aws/.env`) | Owner                         | Auto-Deploy              |
|----------------|----------------------------------------|---------------------------------------------|-------------------------------|--------------------------|
| **Sandbox**    | Shared team experimentation            | Feature branch or `main`                    | Individual contributor        | Yes                      |
| **Test**       | QA & staging                           | `test` or `main`                            | Team (`its-iam`)              | Yes (with approval)      |
| **Production** | Live system                            | `main`                                      | Team (`its-iam`)              | Manual approval required |

To change the deployed branch, update `GITHUB_BRANCH` in `aws/.env` and redeploy the pipeline stack (see [docs/AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md)). The pipeline watches whichever branch `.env` specifies.

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
- **Access restriction:** the deployed API is intended to accept HTTPS **only from the companion UI deployment**, not the public internet (see below)

### Access Restriction: HTTPS from the UI Deployment Only (Planned)

A companion project — the UH Groupings **UI** — is the sole intended client of this API.

**Confirmed topology (server-to-server).** The end user's browser interacts *only* with the UI project. The UI server, in turn, handles user SSO against the campus **CAS** server and makes **REST** calls to this API for backend transactions. The browser never calls this API directly. Because the UI *server* (not the user's browser) is the API's network client, a network-level source restriction is genuinely enforceable — this is the deciding fact that makes the layers below work.

The target production posture is that the API accepts **only HTTPS traffic originating from the UI deployment**, across three layers:

1. **Transport (HTTPS-only).** The ALB terminates TLS on a 443 listener backed by an ACM certificate. The plaintext HTTP:80 listener is removed or set to redirect to 443. TLS terminates at the ALB; ALB→task traffic remains HTTP inside the VPC.

2. **Network (source restriction).** The API's ALB security group ingress is narrowed from `0.0.0.0/0` to the UI deployment as the only source:
   - **Same VPC (recommended):** the UI stack exports its security group ID, and `ecs-service.yml` references it as `SourceSecurityGroupId` on the 443 ingress rule via `Fn::ImportValue` — instead of opening 443 to the internet. This is the same cross-stack export pattern `vpc.yml` already uses for subnet IDs.
   - **Cross-VPC:** restrict ingress to the UI's known egress/NAT IP range(s), and consider AWS WAF for finer-grained rules.

3. **Application (defense in depth).** JWT authentication remains required on every non-public endpoint, and CORS is pinned to the UI's origin. User authentication happens at the UI via CAS; the API trusts the JWT presented on each REST call. The network scoping and JWT are complementary: the security group limits *who can reach* the ALB; JWT limits *who is authorized* once they do.

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
