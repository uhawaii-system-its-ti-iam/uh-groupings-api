# AWS Deployment Scope — API Project vs UI Project

> **Read this first when working anywhere under `aws/`.**
> This document is shared **verbatim** between the `uh-groupings-api` and
> `uh-groupings-ui` repositories. It defines *which project deploys what* so the
> two CloudFormation stacks compose into one running system. The visual source
> of record is [`docs/aws-architecture.mmd`](docs/aws-architecture.mmd);
> narrative architecture lives in [`docs/AWS_ARCHITECTURE.md`](docs/AWS_ARCHITECTURE.md).

## The one-sentence rule

**The API repo (`uh-groupings-api`) deploys everything the API application needs
to run as a *private* service; the UI repo (`uh-groupings-ui`) deploys the
public-facing UI and the edge that fronts it. Neither repo creates the VPC or
the data-center link.** The API is deployed **first** and must stand alone.

## Edge model: Cloudflare Tunnel (no public load balancer)

Public entry is via **Cloudflare** fronting a **Cloudflare Tunnel**
(`cloudflared`) that the UI task runs as an **outbound** connection. Cloudflare
terminates TLS at the edge and the tunnel carries traffic to the UI task, so
there is **no internet-facing ALB and no public HTTPS listener in AWS**. A
single-AZ test environment gains nothing from an ALB, so **neither repo
provisions one**. (This supersedes the earlier internet-facing-ALB design.)

## Ownership at a glance

| Layer | VPC team (referenced, never created by either repo) | API repo (`uh-groupings-api`) | UI repo (`uh-groupings-ui`) |
|---|---|---|---|
| VPC | VPC (`VPC_ID` in `aws/.env`) | — | — |
| Internet edge | — | — | Cloudflare (DNS/TLS/WAF/DDoS) + Cloudflare Tunnel (`cloudflared`) |
| Data-center link | Connectivity to on-prem Grouper WS — *mechanism pending infra confirmation* (see below) | — | — |
| Subnets | — | Private subnets for the **API** tasks + VPC endpoints (existing `/28` CIDRs in `.env`) | **All** UI subnets, including outbound egress for the tunnel |
| Load balancing | — | **None** | **None** (Cloudflare Tunnel) |
| Compute | — | ECS cluster, Service Connect namespace, **API** service + task def | **UI** service + task def (joins the API's namespace) + `cloudflared` |
| Security groups | — | `sg-api-backend` (API tasks), `sg-vpce` (endpoints) | `sg-ui-apps` **and** the ingress rule that lets `sg-ui-apps` reach `sg-api-backend:8080` |
| Supporting | — | ECR repo, Secrets Manager secrets, IAM roles, CloudWatch logs, CI/CD pipeline | UI-side supporting resources + Cloudflare config (external to AWS) |

**The API repo creates no UI-facing AWS elements** — no shared or public
subnets, no ALB, no UI security groups. The only cross-project coupling is
(a) the interface the API **exports** and (b) the UI **adding** one ingress rule
and **joining** the API's Service Connect namespace.

## What the API repo's CloudFormation creates (the API app's full runtime)

Mapped to the templates in `aws/cloudformation/`:

- **`vpc.yml`** — Inside the pre-existing VPC: two subnets across 2 AZs (CIDRs
  from `SUBNET_A_CIDR`/`SUBNET_B_CIDR` in `.env`) that are **private by posture**
  (`MapPublicIpOnLaunch: false`), plus the VPC endpoints that let tasks reach AWS
  services with **no public IP and no NAT**: `ecr.api`, `ecr.dkr`,
  `secretsmanager`, `logs` (interface) and `s3` (gateway, on the main route
  table), fronted by the `sg-vpce` endpoint security group. Exports subnet IDs.
  There are **no public subnets and no ALB**. The 2-AZ subnets exist for future
  HA; the **test runtime is single-AZ** (`DesiredCount: 1`, one task).
- **`ecr-repository.yml`** — the container image registry.
- **`ecs-service.yml`** — ECS Fargate cluster, the Service Connect namespace
  (exported), the **API** service + task definition (container port **8080**,
  `AssignPublicIp: DISABLED`, no load balancer), the `sg-api-backend` task
  security group (**no ingress** — the UI adds it later), IAM task/execution
  roles, and the CloudWatch log group.
  - **UI→API traffic** arrives via **ECS Service Connect** (HTTP/TLS 8080);
    access is controlled purely by **`sg-api-backend`**, which will admit 8080
    **only from the UI's `sg-ui-apps`**.
  - The container **health check is intentionally disabled** until the Grouper
    path is verified end-to-end (see below), so the task is not restart-looped.
- **`codepipeline.yml`** — CodePipeline + CodeBuild that build and deploy the
  API image.

Secrets (`groupings/api/grouper-password`, `groupings/api/jwt-secret`) are
created by `setup.sh` and injected into the task via `secrets[]`.

## What the UI repo adds

- **Cloudflare + Cloudflare Tunnel**: the UI task runs `cloudflared` as an
  outbound tunnel; Cloudflare handles DNS/TLS/WAF at the edge. No public ALB.
- **All UI subnets** plus whatever **outbound egress** the tunnel needs (the API
  repo provides none of this).
- **`sg-ui-apps`** and a standalone `AWS::EC2::SecurityGroupIngress` that adds
  `sg-ui-apps` → `sg-api-backend:8080` (referencing the API's exported SG id).
- The **UI ECS service**, joined to the **API-owned** Service Connect namespace
  so it can reach the API by service name.

## Cross-stack contract (why API-first works)

- The API stack **exports** a stable interface for the UI to consume:
  `sg-api-backend` id, Service Connect namespace ARN, Service Connect DNS name,
  subnet IDs, cluster name (CloudFormation exports).
- The UI stack **only imports and adds**. It must **never mutate API-owned
  subnets, route tables, or security groups** — its sole reach into API
  territory is *adding* one ingress rule to `sg-api-backend` and *joining* the
  namespace.
- Because the UI is the API's only network client (server-to-server; the browser
  talks only to Cloudflare/UI), the SG source restriction is genuinely
  enforceable and replaces the need for an API-side load balancer.

## Grouper WS connectivity (pending infrastructure-team confirmation)

Grouper WS in the UH data center is a **live, required dependency** of the API
(no longer deferred). The API task reaches it over **HTTPS** (currently
`128.171.94.186:443`). **How VPC traffic reaches the data center is not yet
confirmed**, so the diagram marks this path as *pending infra*. Open questions
for the infrastructure team:

1. **Mechanism** — VGW + IPsec site-to-site VPN, Transit Gateway, or Direct
   Connect? Does the link already exist, or must it be provisioned?
2. **Route + ownership** — which destination CIDR(s) route to the gateway (e.g.
   `128.171.0.0/16`), and who adds the route to which route table?
3. **Endpoint** — stable IP `128.171.94.186:443`, or a DNS name / VIP? Any
   failover target?
4. **DNS** — will the Grouper hostname resolve from inside the VPC, or must we
   connect by IP?
5. **On-prem firewall** — which source addresses must be allow-listed (our
   subnet CIDRs `172.18.10.16/28`, `172.18.10.32/28`, or a translated address)?
6. **Client auth** — client certificate / mTLS or IP allow-listing required in
   addition to the service-account credentials? Any CA trust bundle to ship?
7. **HA / SLA** — is the link redundant across AZs; expected latency/bandwidth?

Until these are answered, the data-center path stays marked "pending infra" and
the container health check (`/uhgroupingsapi/actuator/health`) remains disabled.

## Target traffic path

```
Browser → Cloudflare (TLS/WAF) → Cloudflare Tunnel → UI task
        → Service Connect (HTTP/TLS 8080) → API task
        → AWS services via VPC endpoints
        → on-prem Grouper WS via the data-center link (mechanism pending infra)
```

Until the UI exists, the API has no inbound client; test/verify it in-VPC (e.g.
`aws ecs execute-command`).

## Deferred / explicitly out of current scope

- **Multi-environment (test → prod promotion)** — a single **test** tier for now
  (`APP_TIER=test`).
- **Multi-AZ high availability** — the **test** environment runs **single-AZ**
  with **`DesiredCount: 1`**. Multi-AZ (2+ AZ, higher `DesiredCount`) is a prod
  concern for both repos.
- **True private task subnets / more CIDR** — revisit for prod; today's two
  `/28`s are reused as-is.

*(On-prem Grouper connectivity is no longer in this list — it is a live
dependency; only the connectivity **mechanism** is pending, as above.)*

## Template status

The API templates now match this target: `ecs-service.yml` provisions **no ALB**
(Service Connect + `sg-api-backend` only) and exports the cross-stack interface;
`vpc.yml` creates only private subnets + endpoints. `aws/README.md`'s top
"API project owns / UI project owns" section predates this decision and still
mentions an *internal API ALB*, a *NAT*, and the *IGW as UI-owned* — all
superseded here. **This file is authoritative.**
