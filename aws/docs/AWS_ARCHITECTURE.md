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
2. **The API reaches Grouper over the public internet.** Grouper sits behind an F5 with a public IP, so a NAT Gateway with a fixed Elastic IP provides egress and the UH firewall allow-lists that address. There is no VPN, Transit Gateway, or Direct Connect. The task itself keeps `AssignPublicIp DISABLED` and has no inbound path.

> **Visual source of record:** [`aws-architecture.mmd`](aws-architecture.mmd). **Authoritative ownership rules:** [`../AGENTS.md`](../AGENTS.md). If this document and either of those disagree, they win.

## Ownership Boundaries

| Layer | Owner | Notes |
|---|---|---|
| VPC | VPC/infrastructure team | Referenced via `VPC_ID`; never created by either repo |
| Data-center link to Grouper | VPC/infrastructure team | Mechanism **pending confirmation** |
| Internet edge (DNS, TLS, WAF, tunnel) | **Groupings UI** | Cloudflare — external to AWS |
| API private subnet + route table | **Groupings API** | Holds the task ENI; `0.0.0.0/0` to the NAT |
| Public subnet + NAT Gateway + EIP | **Groupings API** | NAT is the subnet's only occupant; the EIP is allow-listed |
| Internet Gateway | VPC/infrastructure team | Pre-existing; referenced, never created |
| ECS cluster, Service Connect namespace | **Groupings API** | Namespace exported for the UI to join |
| API service, task definition | **Groupings API** | Container port 8080 |
| `sg-api-backend` | **Groupings API** | Created with **no ingress** |
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
  │               ├──▶ S3 gateway endpoint (ECR image layers, free)     │
  │               └──▶ NAT Gateway + EIP ──▶ IGW ──▶ Grouper WS :443    │
  │                    (AWS APIs also egress via the NAT)               │
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

CodeBuild runs in an AWS-managed VPC, not in this project's subnets, so it needs no networking from this stack.

**Stage 3 — Deploy (ECS).** The ECS deploy action consumes `imagedefinitions.json` and performs a rolling update: `MaximumPercent` 200, `MinimumHealthyPercent` 100. Because the service has no load balancer, there is **no target-group health gate** on deployment — ECS replaces tasks based on task state alone.

Blue/green via CodeDeploy is **not available**: it requires a load balancer with two target groups. There is no appspec in the repo for that reason.

### 3. Container Registry (Amazon ECR)

- **Repository:** `${Owner}-${Project}-${Environment}` (e.g., `mhodges-groupings-api-sandbx`)
- **Image scanning:** on push
- **Encryption:** AES-256
- **Lifecycle:** retain recent images, expire untagged after 7 days

Image pulls reach the ECR API through the NAT Gateway, while the image layers themselves come via the free S3 gateway endpoint (ECR stores layers in S3), so the bulk of the bytes avoid NAT data-processing charges.

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

**Why there is no container health check.** The Spring Actuator health endpoint (`/uhgroupingsapi/actuator/health`) depends on reaching Grouper WS. Until the UH firewall allow-lists the NAT Gateway's Elastic IP, that call fails, so enabling a health check would make ECS kill and restart the task in a loop. Re-enable it once Grouper calls succeed.

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
- **Delivery:** the `awslogs` driver, egressing through the NAT Gateway
- **Metrics:** ECS service CPU/memory (Container Insights) and CodeBuild success/failure

There are **no ALB metrics** (request count, latency, 5xx) because there is no ALB. Request-level observability, if needed, must come from the application or from Service Connect's Envoy metrics and access logs.

Recommended alarms: CPU >80%, memory >80%, ECS task failures, CodePipeline failures.

### 8. Networking

`aws/cloudformation/vpc.yml` creates, inside the pre-existing VPC:

- **A private subnet** (`PRIVATE_SUBNET_CIDR`, a `/28`) holding the API task ENI, `MapPublicIpOnLaunch: false`, with its own route table sending `0.0.0.0/0` to the NAT Gateway.
- **A public subnet** (`PUBLIC_SUBNET_CIDR`, a `/28`) whose **only** occupant is the NAT Gateway. No application workload belongs here. It has no route table of its own — it inherits the VPC's main route table, whose `0.0.0.0/0 → IGW` route is what gives the NAT its path out. That keeps this stack from creating or owning an Internet Gateway, which belongs to the VPC team.
- **A NAT Gateway with an Elastic IP.** The EIP is the fixed source address the UH Palo Alto firewall allow-lists.
- **The S3 gateway endpoint** (free), attached to the **private** route table.

Both subnets are in the region's first AZ.

**Why the S3 gateway endpoint stays.** ECR image layers are stored in S3, and gateway endpoints intercept S3-bound traffic via the route table. Keeping it means image pulls — the bulk of the bytes — bypass the NAT Gateway's per-GB data processing charge entirely. It costs nothing.

**Why the four interface endpoints were removed.** `ecr.api`, `ecr.dkr`, `secretsmanager`, and `logs` (and `sg-vpce`) previously existed so a task with *no* internet route could reach AWS services. Once a NAT Gateway became mandatory for Grouper, they were a redundant second path costing ~$29/month. Those calls now egress through the NAT; the traffic is small (auth tokens, two secret fetches per task start, log shipping). The tradeoff accepted: no endpoint policies as a defense-in-depth control, and a NAT failure now breaks image pulls and secret resolution rather than just Grouper. Re-add them if a future branch removes internet egress.

**Now required:** an Internet Gateway with a `0.0.0.0/0` route on the VPC's main route table. `make aws-check-vpc` **fails** without it, because the NAT Gateway would be provisioned but unable to reach anything.

**Still not created:** any load balancer, any public endpoint, any inbound path.

**Security groups:**

| Security group | Ingress | Source | Owner |
|---|---|---|---|
| `sg-api-backend` | none at creation; later 8080 | UI stack adds `sg-ui-apps` | API creates it; UI adds the rule |

Egress on `sg-api-backend` is the default allow-all so the task can reach the NAT Gateway, and through it Grouper WS and the AWS service endpoints. A NAT Gateway has no security group of its own — it is outbound-only by construction.

**Single AZ, deliberately.** One task, so a second AZ would add cost and imply HA that does not exist. Enabling real multi-AZ means adding a second private subnet in another AZ, making `PrivateSubnetId` a comma-joined list, adding a NAT Gateway per AZ (or accepting cross-AZ NAT traffic), and raising `DesiredCount` — a coordinated change, not a knob. Raising `DesiredCount` alone buys nothing.

## Grouper WS Connectivity

Grouper WS is a **live, required dependency**, reached over the **public internet**. Grouper sits behind an F5 with a public IP, so there is **no VPN, no Transit Gateway, and no Direct Connect** in this design.

```
API task (private subnet, no public IP)
  → private route table 0.0.0.0/0
  → NAT Gateway (public subnet, Elastic IP)
  → Internet Gateway
  → HTTPS 443 → grouper-test.its.hawaii.edu (F5 public IP)
```

Access control is a **firewall allow-list keyed on source IP**. Two distinct sources need entries:

| Source the firewall sees | Covers | Status |
|---|---|---|
| The NAT Gateway's **Elastic IP** | The deployed sandbox task | **Must be requested** per environment |
| Campus network / UH VPN egress ranges | Developers running the API locally | Already in place |

### The Elastic IP must be stable

`NAT_EIP_ALLOCATION_ID` in `aws/.env` makes the EIP pre-existing, so `make aws-teardown` leaves it alone. Left blank, CloudFormation owns the EIP, teardown **releases** it, and the next `make aws-setup` gets a different address — silently invalidating the firewall rule. Because teardown/re-setup is the normal loop on this branch, recording that allocation id is part of first-time setup. `setup.sh` prints the address and the allocation id, and says so explicitly.

### Local development bypasses AWS entirely

A developer running Swagger against `docker-compose` on `localhost:8081` calls Grouper **directly from their laptop**:

```
Developer browser → local container → HTTPS 443 → Grouper WS
```

The VPC, the NAT Gateway, and the deployed task are all outside that path. The NAT's Elastic IP is irrelevant to it, and **no AWS configuration is required for local Swagger to work**. This is why the API needs no public hostname and no certificate: developers use local Swagger, and the deployed API's only client is the UI over Service Connect.

### Why the container health check is disabled

`/uhgroupingsapi/actuator/health` depends on reaching Grouper. Until the firewall allow-list for the NAT EIP is in place, that call fails, and an enabled health check would make ECS restart-loop the task. Re-enable it once Grouper calls succeed.

## Data Flow

### Request flow

```
1. Browser → HTTPS 443 → Cloudflare (TLS terminates at the edge)
2. Cloudflare → Cloudflare Tunnel (outbound from the UI task) → UI task
3. UI task → Service Connect (HTTP 8080) → API task
      admitted only because sg-ui-apps is in sg-api-backend's ingress
4. API task → validate JWT → process request
5. API task → NAT Gateway → IGW → Grouper WS (HTTPS 443, from the NAT EIP)
6. API task → response → UI task → Cloudflare → browser
```

The browser never contacts the API. Because the UI **server** is the API's network client, the security-group source restriction is genuinely enforceable — this is what makes an API-side load balancer unnecessary.

### Deployment flow

```
1. Developer → git push → GitHub
2. GitHub → CodeConnections webhook → CodePipeline
3. CodePipeline → CodeBuild: Maven build → Docker build → ECR push
4. CodePipeline → ECS deploy action (imagedefinitions.json)
5. ECS pulls the new image (ECR API via NAT, layers via the S3 gateway endpoint)
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
| NAT Gateway — $0.045/hr | $33 |
| Public IPv4 address (the NAT's EIP) — $0.005/hr | $4 |
| NAT data processing — $0.045/GB | pennies at sandbox volume |
| S3 gateway endpoint | free |
| ECR + CloudWatch Logs | $2–7 |
| CodeBuild | ~$0.005/min, only while building |
| **Total** | **~$55–65** |

Three notes on the shape of this bill:

- **The NAT Gateway is the largest line item and is not optional.** It is the only way to give the task a *stable* source IP for the UH firewall allow-list. Putting the task in a public subnet with its own public IP would be cheaper, but a Fargate task's public IP changes on every restart, which breaks the allow-list continuously.
- **Dropping the four interface endpoints saved ~$29/month.** They existed to serve a task with no internet route; once the NAT became mandatory they were a redundant second path.
- **Keeping the S3 gateway endpoint keeps data-processing charges near zero.** ECR image layers live in S3, so the bulk of the bytes bypass the NAT. Without it, every image pull would incur $0.045/GB.

Scaling the service to zero saves the Fargate cost but **not** the NAT Gateway or EIP charges, which are hourly regardless:

```bash
source aws/.env
aws ecs update-service \
  --cluster "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster" \
  --service "${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service" \
  --desired-count 0
```

To stop the NAT charges you must tear down the vpc stack — which is fine on this branch, provided `NAT_EIP_ALLOCATION_ID` is recorded so the address survives.

## Future Enhancements

**Blocking / near term**
- [ ] Get the NAT Gateway's Elastic IP allow-listed on the UH Palo Alto firewall
- [ ] Record `NAT_EIP_ALLOCATION_ID` in `aws/.env` after the first deploy
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
