# AWS Architecture — UH Groupings API

<!-- TOC -->
* [AWS Architecture — UH Groupings API](#aws-architecture--uh-groupings-api)
  * [System Overview](#system-overview)
  * [Ownership Boundaries](#ownership-boundaries)
  * [High-Level Architecture](#high-level-architecture)
  * [Component Details](#component-details)
    * [1. Source Control](#1-source-control)
    * [2. CI/CD Pipeline (AWS CodePipeline)](#2-cicd-pipeline-aws-codepipeline)
    * [3. Container Registry (Amazon ECR)](#3-container-registry-amazon-ecr)
    * [4. Compute (Amazon ECS on AWS Fargate)](#4-compute-amazon-ecs-on-aws-fargate)
    * [5. Service-to-Service Connectivity (ECS Service Connect)](#5-service-to-service-connectivity-ecs-service-connect)
    * [6. Secrets Management (AWS Secrets Manager)](#6-secrets-management-aws-secrets-manager)
    * [7. Monitoring & Logging](#7-monitoring--logging)
    * [8. Networking](#8-networking)
  * [Grouper WS Connectivity (Pending Infrastructure Confirmation)](#grouper-ws-connectivity-pending-infrastructure-confirmation)
  * [Data Flow](#data-flow)
  * [Technology Stack](#technology-stack)
  * [Environments and Tiers](#environments-and-tiers)
  * [Security Architecture](#security-architecture)
  * [Scalability & Resilience](#scalability--resilience)
  * [Cost](#cost)
  * [Future Enhancements](#future-enhancements)
<!-- TOC -->

## System Overview

The UH Groupings API is a Spring Boot application deployed on **Amazon ECS / AWS Fargate** as a **private service**. It has no load balancer, no public endpoint, no hostname, and no TLS certificate of its own. Its only network client is the companion UH Groupings UI, which reaches it over **ECS Service Connect** inside the VPC.

Two facts drive nearly every decision below:

1. **The API is private.** Public entry, DNS, and TLS belong to the UI deployment (Cloudflare + Cloudflare Tunnel). There is no internet-facing AWS resource in this project.
2. **The API needs no internet egress.** AWS service access is via VPC endpoints (AWS PrivateLink); Grouper access is via the UH data-center link. There is no Internet Gateway dependency and no NAT gateway.

> **Visual source of record:** [`aws-architecture.mmd`](aws-architecture.mmd). **Authoritative ownership rules:** [`../AGENTS.md`](../AGENTS.md). If this document and either of those disagree, they win.

## Ownership Boundaries

| Layer | Owner | Notes |
|---|---|---|
| VPC | VPC/infrastructure team | Referenced via `VPC_ID`; never created by either repo |
| Data-center link to Grouper | VPC/infrastructure team | Mechanism **pending confirmation** |
| Internet edge (DNS, TLS, WAF, tunnel) | **Groupings UI** | Cloudflare — external to AWS |
| API subnet, VPC endpoints | **Groupings API** | One subnet, private posture, single AZ |
| ECS cluster, Service Connect namespace | **Groupings API** | Namespace exported for the UI to join |
| API service, task definition | **Groupings API** | Container port 8080 |
| `sg-api-backend`, `sg-vpce` | **Groupings API** | `sg-api-backend` created with **no ingress** |
| Ingress rule `sg-ui-apps` → `sg-api-backend:8080` | **Groupings UI** | The UI opens the door; the API only provides it |
| ECR, Secrets Manager, IAM, CloudWatch, CI/CD | **Groupings API** | |
| UI subnets, tunnel egress, `sg-ui-apps` | **Groupings UI** | |

The API repo creates **no UI-facing AWS elements** — no public subnets, no load balancer, no UI security groups.

## High-Level Architecture

```
                          GitHub (source)
                                │ push to configured branch
                                ▼
                        AWS CodePipeline
                    Source → Build → Deploy
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
             AWS CodeBuild            Amazon ECS (Fargate)
          Maven + Docker build         rolling update
                    │
                    ▼
              Amazon ECR
          (container images)


  Browser ──HTTPS 443──▶ Cloudflare (TLS/WAF) ──tunnel──▶ UI task
                                                             │
  ┌──────────────── Provided VPC (VPC team) ─────────────────┼──────────┐
  │                                                          │          │
  │   UI repo:  UI task (cloudflared) ──Service Connect 8080─┤          │
  │                                                          ▼          │
  │   API repo: API task :8080  (sg-api-backend, no public IP)          │
  │               ├──▶ VPC endpoints (ECR, S3, Secrets Mgr, CW Logs)    │
  │               └──▶ data-center path ──▶ on-prem Grouper WS :443     │
  │                    (mechanism PENDING INFRA)                        │
  └─────────────────────────────────────────────────────────────────────┘
```

No Application Load Balancer appears in this picture, in either project. A single-AZ test environment gains nothing from one, and the UI's Cloudflare Tunnel is an **outbound** connection that needs no inbound listener.

## Component Details

### 1. Source Control
- **Service:** GitHub
- **Repository:** `uhawaii-system-its-ti-iam/uh-groupings-api`
- **Trigger:** CodePipeline watches the branch set by `GITHUB_BRANCH` in `aws/.env`
- **Changing branches:** update `GITHUB_BRANCH` and redeploy the pipeline stack (see [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md))

### 2. CI/CD Pipeline (AWS CodePipeline)

**Stage 1 — Source.** GitHub via AWS CodeConnections; emits a source ZIP artifact. The OAuth handshake is manual by design.

**Stage 2 — Build (AWS CodeBuild).** Image `aws/codebuild/standard:7.0`, privileged mode for Docker. Runs the Maven build (compile + unit tests), builds the image, pushes to ECR, and emits `imagedefinitions.json`. Maven dependencies are cached in S3. **The buildspec path is `aws/buildspec.yml`**, not the repo root.

Environment variables: `AWS_ACCOUNT_ID`, `AWS_DEFAULT_REGION`, `IMAGE_REPO_NAME`, `IMAGE_TAG`.

CodeBuild runs in an AWS-managed VPC, not in this project's subnets, so it needs no VPC endpoints.

**Stage 3 — Deploy (ECS).** The ECS deploy action consumes `imagedefinitions.json` and performs a rolling update: `MaximumPercent` 200, `MinimumHealthyPercent` 100. Because the service has no load balancer, there is **no target-group health gate** on deployment — ECS replaces tasks based on task state alone.

Blue/green via CodeDeploy is **not available**: it requires a load balancer with two target groups. There is no appspec in the repo for that reason.

### 3. Container Registry (Amazon ECR)

- **Repository:** `${Owner}-${Project}-${Environment}` (e.g., `mhodges-groupings-api-sandbx`)
- **Image scanning:** on push
- **Encryption:** AES-256
- **Lifecycle:** retain recent images, expire untagged after 7 days

Image pulls reach ECR through the `ecr.api` and `ecr.dkr` interface endpoints plus the S3 gateway endpoint — never over the internet.

### 4. Compute (Amazon ECS on AWS Fargate)

**Cluster:** `${Owner}-${Project}-${Environment}-cluster`, capacity providers FARGATE and FARGATE_SPOT (default strategy pins FARGATE), Container Insights enabled, with the Service Connect namespace set as the cluster default.

**Service and task:**

| Setting | Value |
|---|---|
| Launch type | Fargate |
| Desired count | 1 (`ECS_TASK_COUNT`) — one task, one subnet, one AZ |
| CPU / memory | 512 (0.5 vCPU) / 1024 MB |
| Container port | 8080, named `api-8080` for Service Connect |
| `AssignPublicIp` | `DISABLED` |
| Load balancer | **none** |
| Container health check | **intentionally omitted** (see below) |

**Why there is no container health check.** The Spring Actuator health endpoint (`/uhgroupingsapi/actuator/health`) depends on reaching Grouper WS. Grouper is a live, required dependency, but the data-center connectivity mechanism is unconfirmed, so the endpoint cannot succeed yet. Enabling a health check now would make ECS kill and restart the task in a loop. Re-add it once the Grouper path is verified end to end.

**IAM roles.** The **execution** role pulls images, reads the two secrets, and writes logs. The **task** role carries application runtime permissions.

### 5. Service-to-Service Connectivity (ECS Service Connect)

The API publishes itself into an **AWS Cloud Map HTTP namespace** created by this project and exported for the UI stack. The UI service joins the same namespace and reaches the API by name.

| Item | Value |
|---|---|
| Namespace | `${Owner}-${Project}-${Environment}` (`AWS::ServiceDiscovery::HttpNamespace`) |
| Port name | `api-8080` |
| Discovery name | `groupings-api` |
| Client alias | `groupings-api:8080` |

Service Connect injects an Envoy sidecar that is supplied and managed by the Fargate platform, so it requires no additional VPC endpoint and no customer-side image pull.

**Access control is purely by security group.** `sg-api-backend` is created with **no ingress rule at all**. The UI stack later adds a standalone `AWS::EC2::SecurityGroupIngress` permitting 8080 from `sg-ui-apps` only. Until then the API has no inbound client at all, and by project decision it is **not** functionally exercised before the UI is deployed — `aws ecs execute-command` is deliberately left disabled rather than widening the task role to obtain a shell. See [`../AGENTS.md`](../AGENTS.md) → "Verification scope".

### 6. Secrets Management (AWS Secrets Manager)

Exactly **two** secrets, both created idempotently by `aws/setup.sh`:

- `groupings/api/grouper-password` → injected as `GROUPERCLIENT_WEBSERVICE_PASSWORD`
- `groupings/api/jwt-secret` → injected as `JWT_SECRET_KEY`

Non-secret runtime values (Grouper URL and login, email flags) live in the task definition `environment[]` array, or come from the `aws-test` / `aws-prod` Spring profile baked into the image.

The task execution role resolves both at container start; plaintext never appears in task config or logs. Secrets Manager is reached through its interface VPC endpoint.

**JWT key ownership:** the API project owns this key. Companion UI projects read the same entry rather than generating their own. See [SECRETS.md](SECRETS.md#jwt-key-ownership).

### 7. Monitoring & Logging

- **Log group:** `/ecs/${Owner}-${Project}-${Environment}`, 30-day retention, stream prefix `ecs`
- **Delivery:** the `awslogs` driver via the CloudWatch Logs interface endpoint
- **Metrics:** ECS service CPU/memory (Container Insights) and CodeBuild success/failure

There are **no ALB metrics** (request count, latency, 5xx) because there is no ALB. Request-level observability, if needed, must come from the application or from Service Connect's Envoy metrics and access logs.

Recommended alarms: CPU >80%, memory >80%, ECS task failures, CodePipeline failures.

### 8. Networking

`aws/cloudformation/vpc.yml` creates, inside the pre-existing VPC:

- **One subnet** (`SUBNET_CIDR`, a `/28`) in the region's first AZ, private by posture (`MapPublicIpOnLaunch: false`).
- **`sg-vpce`** and the VPC endpoints below.

**Single subnet, single AZ, deliberately.** The API runs one task, so a second subnet would sit empty while implying the deployment is multi-AZ. It is not. Enabling real multi-AZ means adding a subnet in another AZ, changing the `SubnetId` output to a comma-joined list, widening `sg-vpce` ingress, deciding whether the interface endpoints need an ENI in both subnets, and raising `DesiredCount` — a coordinated change, not a knob.

A `/28` yields 11 usable addresses after AWS reserves 5. Today that holds one task ENI plus four interface-endpoint ENIs, so it fits with room to spare but not much headroom — worth revisiting before prod.

| Endpoint | Type | Purpose |
|---|---|---|
| `ecr.api` | Interface | ECR API calls (auth token, metadata) |
| `ecr.dkr` | Interface | Docker registry / image layers |
| `s3` | **Gateway** | Image layer storage; attaches to the main route table |
| `secretsmanager` | Interface | `secrets[]` injection at task start |
| `logs` | Interface | `awslogs` log delivery |

All four interface endpoints live in the same subnet as the task, so every endpoint call stays within one AZ — no cross-AZ data transfer charges, and no dependency on private DNS resolving to a remote ENI. The S3 gateway endpoint is a route-table entry and is free. Collapsing to one subnet removed the cross-AZ endpoint traffic the earlier two-subnet layout could incur.

**Not required, and not created:** Internet Gateway, NAT gateway, public subnets, `0.0.0.0/0` route, load balancer. `make aws-check-vpc` reports IGW presence as informational only — its absence does not block setup.

**Security groups:**

| Security group | Ingress | Source | Owner |
|---|---|---|---|
| `sg-api-backend` | none at creation; later 8080 | UI stack adds `sg-ui-apps` | API creates it; UI adds the rule |
| `sg-vpce` | 443 | The subnet CIDR | API |

Egress on `sg-api-backend` is the default allow-all so the task can reach the VPC endpoints and, once wired, on-prem Grouper.

## Grouper WS Connectivity (Pending Infrastructure Confirmation)

Grouper WS in the UH data center is a **live, required dependency** — not deferred. The API reaches it over HTTPS (currently `128.171.94.186:443`). **How VPC traffic reaches the data center is not yet confirmed**, so this path is marked pending in the diagram and the container health check stays disabled.

Open questions for the infrastructure team:

1. **Mechanism** — Site-to-Site VPN (VGW), Transit Gateway, or Direct Connect? Does the link exist already?
2. **Route and ownership** — which destination CIDR(s) route to the gateway (e.g. `128.171.0.0/16`), and who adds the route to which route table?
3. **Endpoint** — stable IP, or a DNS name / VIP? Any failover target?
4. **DNS** — will the Grouper hostname resolve from inside the VPC, or must we connect by IP?
5. **On-prem firewall** — which source addresses must be allow-listed (the subnet CIDR `172.18.10.16/28`, or a translated address)?
6. **Client auth** — client certificate / mTLS or IP allow-listing in addition to the service-account credentials? Any CA bundle to ship?
7. **HA / SLA** — is the link redundant across AZs; expected latency and bandwidth?

## Data Flow

### Request flow

```
1. Browser → HTTPS 443 → Cloudflare (TLS terminates at the edge)
2. Cloudflare → Cloudflare Tunnel (outbound from the UI task) → UI task
3. UI task → Service Connect (HTTP 8080) → API task
      admitted only because sg-ui-apps is in sg-api-backend's ingress
4. API task → validate JWT → process request
5. API task → Grouper WS over the data-center path  [PENDING INFRA]
6. API task → response → UI task → Cloudflare → browser
```

The browser never contacts the API. Because the UI **server** is the API's network client, the security-group source restriction is genuinely enforceable — this is what makes an API-side load balancer unnecessary.

### Deployment flow

```
1. Developer → git push → GitHub
2. GitHub → CodeConnections webhook → CodePipeline
3. CodePipeline → CodeBuild: Maven build → Docker build → ECR push
4. CodePipeline → ECS deploy action (imagedefinitions.json)
5. ECS pulls the new image through the ECR VPC endpoints
6. ECS starts the replacement task (rolling update)
7. ECS drains the old task
```

## Technology Stack

**Application:** Java 21, Spring Boot, WAR packaged and run standalone, Spring Web MVC / Security / Actuator, Grouper Client, JWT, Spring Cloud Vault.

**Infrastructure:** Docker, ECS Fargate, Maven, Eclipse Temurin 21 JRE base image, CloudFormation.

**DevOps:** Git/GitHub, CodePipeline + CodeBuild, CloudFormation, CloudWatch.

## Environments and Tiers

The project separates two independent axes, and conflating them is the mistake this design exists to prevent:

- **`AWS_ENV`** — *resource identity*. Names and tags resources (`sandbx`, `dev`, `test`, `prod`). Also selects the stack names.
- **`APP_TIER`** — *configuration tier*. `test` or `prod`. Selects the Spring profile `aws-${APP_TIER}`, which selects the Grouper backend (grouper-test vs grouper), the `app.environment` label, and the email flags.

`setup.sh` enforces that `APP_TIER=prod` is only permitted when `AWS_ENV=prod`.

| `AWS_ENV` | `APP_TIER` | Spring profile | Grouper backend | Purpose |
|---|---|---|---|---|
| `sandbx` | `test` | `aws-test` | grouper-test | Shared ITS sandbox (today) |
| `test` | `test` | `aws-test` | grouper-test | Future team test tier |
| `prod` | `prod` | `aws-prod` | grouper (prod) | Production (not yet deployed) |

`APP_TIER` no longer selects a hostname or certificate — the API has no public endpoint.

## Security Architecture

**Authentication and authorization.** Stateless JWT on every non-public endpoint. User authentication happens at the UI against campus CAS; the API trusts the JWT presented on each REST call. AWS access uses IAM roles.

**Network posture.** The strongest control here is that the API is simply **not reachable from the internet**: no public IP, no load balancer, no public subnet. Reachability is limited to holders of `sg-ui-apps` on port 8080. The network scoping and JWT are complementary — the security group limits *who can connect*, JWT limits *who is authorized* once connected.

**Transport.** Browser→Cloudflare and Cloudflare→tunnel are TLS. UI→API is HTTP over Service Connect inside the VPC (Service Connect can be configured for TLS if a requirement emerges). API→Grouper is HTTPS 443 with normal certificate validation — never disable validation.

**Secrets.** Two Secrets Manager entries, AES-256 at rest, injected at container start via `secrets[]`. Locally, a developer-owned overrides file that is never committed. Developer AWS access uses short-lived IAM Identity Center (SSO) session tokens, which hold no application secrets. See [SECRETS.md](SECRETS.md).

**Container.** Runs as non-root `appuser`; multi-stage build ships runtime only; ECR scans on push.

**Compliance.** Container logs to CloudWatch; CloudTrail records AWS API calls including every `GetSecretValue`. Secret rotation is manual and deliberate (the JWT key is shared with UI consumers).

## Scalability & Resilience

**Current test posture is deliberately minimal:** one subnet, one AZ, `DesiredCount: 1`, no autoscaling. Availability is not a test-tier goal, and the network reflects that honestly rather than pre-staging capacity that isn't used.

**For production, both repos would need:** a second subnet in another AZ (see [Networking](#8-networking)), `DesiredCount >= 2` spread across those AZs, CPU/memory target-tracking autoscaling, and a decision on whether Service Connect alone suffices or an internal load balancer is warranted. Note that raising `DesiredCount` alone does **not** produce multi-AZ — every task would land in the single existing subnet.

**Recovery.** Infrastructure is reproducible from CloudFormation; images are retained in ECR under a lifecycle policy; application data lives in the external Grouper system, so this service holds no durable state of its own.

## Cost

Rough monthly estimate for the sandbox (test tier, single task):

| Resource | Approx. monthly |
|---|---|
| ECS Fargate — 1 task, 0.5 vCPU / 1 GB | $15–20 |
| Interface VPC endpoints — 4 × 1 ENI | $30 |
| ECR + CloudWatch Logs | $2–7 |
| CodeBuild | ~$0.005/min, only while building |
| **Total** | **~$50–60** |

Two notes on the shape of this bill. Removing the ALB saved roughly $20/month, but the four interface endpoints cost about $30/month — cheaper than a NAT gateway (~$32 plus data processing) while keeping traffic off the internet, and they are what makes the no-NAT posture possible. Endpoints are billed per AZ per endpoint, which is why they are provisioned in one subnet rather than two.

To reduce sandbox cost, scale the service to zero when idle:

```bash
source aws/.env
aws ecs update-service \
  --cluster "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster" \
  --service "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service" \
  --desired-count 0
```

Note that scaling to zero does not stop endpoint charges — those are hourly per ENI regardless of task count.

## Future Enhancements

**Blocking / near term**
- [ ] Confirm and provision the Grouper data-center path (the open questions above)
- [ ] Re-enable the container health check once Grouper is reachable
- [ ] Deploy the companion UI stack and verify the Service Connect + security-group path end to end

**Medium term**
- [ ] Multi-AZ for prod — add a second subnet in `vpc.yml`, then raise `DesiredCount`
- [ ] CloudWatch alarms and dashboards; route to SNS
- [ ] Automated security scanning in the pipeline
- [ ] Manual approval stage for the production pipeline

**Longer term / open questions**
- [ ] Whether Service Connect TLS is required for UI→API traffic
- [ ] Whether prod warrants an internal load balancer (and therefore CodeDeploy blue/green)
- [ ] Secret rotation automation, coordinated across API and UI consumers
- [ ] Dedicated private subnets with more address space for prod
