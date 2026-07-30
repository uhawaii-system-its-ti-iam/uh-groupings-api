# Overview

The UH Groupings API project setup produces a complete, usable **private** API deployment. The UH Groupings UI project setup adds public access without changing API-owned subnets, route tables, or security groups.

The API provisions **only what the API application itself needs**: to run its container, and to reach Grouper WS. The UI project provisions everything else — including the AWS components that let the UI reach the API.

> **Authoritative sources:** [`AGENTS.md`](AGENTS.md) defines the ownership split; [`docs/aws-architecture.mmd`](docs/aws-architecture.mmd) is the visual record. This summary must stay consistent with both.

## API project (this project) owns

- ECS Fargate cluster, API service, and task definition (container port 8080)
- ECS Service Connect namespace (AWS Cloud Map HTTP namespace), exported for the UI
- `sg-api-backend` — the API task security group, created with **no ingress rule**
- `sg-vpce` and the VPC endpoints (ECR api/dkr, S3 gateway, Secrets Manager, CloudWatch Logs)
- One private subnet for the API task, created inside the pre-existing VPC (single-AZ by design)
- ECR repository, Secrets Manager entries, IAM task/execution roles, CloudWatch log group
- The CI/CD pipeline (CodePipeline + CodeBuild)
- CloudFormation exports forming a documented interface for the UI stack

**No load balancer.** The API has no ALB, no public endpoint, no hostname, and no certificate.

## UI project owns

- Cloudflare (DNS, TLS, WAF) and the Cloudflare Tunnel — the entire public edge
- All UI subnets, plus whatever outbound egress the tunnel requires
- `sg-ui-apps` — the UI task security group
- **The ingress rule that opens the API**: a standalone `AWS::EC2::SecurityGroupIngress`
  allowing `sg-ui-apps` → `sg-api-backend:8080`, referencing the API's exported SG id
- The UI ECS service, joined to the API-owned Service Connect namespace

The UI stack's only reach into API territory is *adding* that one ingress rule and *joining* the namespace. It must never mutate API-owned subnets, route tables, or security groups.

## Neither project owns

The VPC itself and the data-center connectivity to Grouper WS are provided by the VPC/infrastructure team and are only referenced, never created. The Grouper connectivity **mechanism** is still pending confirmation — see [`AGENTS.md`](AGENTS.md) → "Grouper WS connectivity".

# AWS Infrastructure

This directory contains the AWS-specific artifacts used to provision, deploy, and operate the **UH Groupings API** on AWS.

Be advised: this iteration of the project's scripts are macOS (and maybe Linux) compatible.

The project intentionally separates:

- **Application code** (Java/Spring Boot)
- **Deployment configuration** (CodeBuild)
- **Infrastructure as Code** (CloudFormation)
- **Operational scripts** (AWS setup and administration)

Detailed setup, deployment, architecture, and operational guidance are maintained in the project's documentation. This README serves only as an overview of the directory contents.

---

# Directory Structure

```
aws/
├── README.md
├── .env                        # Deployment configuration (non-secret)
├── AGENTS.md                   # AWS deployment scope and ownership guide
├── setup.sh                    # Automated AWS infrastructure provisioning
├── auth.sh                     # SSO profile bootstrap + sign-in dispatcher
├── lib-auth.sh                 # Shared SSO auth helpers (sourced by scripts)
├── check-vpc.sh                # Validates the VPC in .env meets requirements
├── github-connect.sh           # Creates/locates a GitHub CodeConnections connection
├── buildspec.yml               # AWS CodeBuild specification
├── task-definition.json        # ECS task definition reference (not deployed)
├── cloudformation/
│   ├── vpc.yml                 # One private subnet + VPC endpoints in an existing VPC
│   ├── ecr-repository.yml      # Amazon ECR repository
│   ├── ecs-service.yml         # ECS Fargate service, Service Connect, sg-api-backend
│   └── codepipeline.yml        # CI/CD pipeline
└── docs/
    ├── AWS_ARCHITECTURE.md     # AWS architecture and resource relationships
    ├── AWS_DEPLOYMENT.md       # Day-to-day deployment, rollback, scaling
    ├── AWS_NAMING_CONVENTIONS.md # Resource naming and tagging standards
    ├── AWS_QUICKSTART.md       # Initial AWS infrastructure provisioning
    ├── SECRETS.md              # Secrets management (local + AWS)
    └── aws-architecture.mmd    # Architecture diagram source (Mermaid)
```

---

# CloudFormation Organization

The CloudFormation templates are organized by infrastructure layer rather than deployment order.

| Template               | Purpose                                                                                                                                                                                                                    |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **vpc.yml**            | **One** private-posture subnet (single AZ) inside a pre-existing VPC, plus `sg-vpce` and the VPC endpoints (ECR api/dkr, S3 gateway, Secrets Manager, CloudWatch Logs). Exports the subnet id. No public subnets, no IGW, no NAT. |
| **ecr-repository.yml** | Container image repository.                                                                                                                                                                                                |
| **ecs-service.yml**    | Application runtime: ECS Fargate cluster, Service Connect namespace, API service + task definition, `sg-api-backend` (no ingress), IAM roles, CloudWatch log group. **No load balancer.** Exports the cross-stack interface. |
| **codepipeline.yml**   | Continuous integration and deployment infrastructure.                                                                                                                                                                      |

Keeping these layers separate minimizes stack coupling and allows networking, compute, and CI/CD resources to evolve independently.

---

# Common Make Targets

All AWS operations are performed through the project's Makefile, which runs the AWS CLI directly on your host. The AWS CLI v2 must be installed (macOS: `brew install awscli`). Docker is needed only by `aws-setup`, which builds and pushes the image.

Every resource name used by these targets is derived in one place in the Makefile from `AWS_OWNER`, `AWS_PROJECT_ID`, and `AWS_ENV`, so the naming convention cannot drift between targets.

**Setup**

| Command                   | Purpose                                                                                    |
|---------------------------|--------------------------------------------------------------------------------------------|
| `make aws-sso-setup`      | Configure SSO profile and sign in                                                          |
| `make aws-sso-login`      | Force a fresh SSO login (refresh)                                                          |
| `make aws-list-vpcs`      | List VPCs in the configured account/region                                                 |
| `make aws-check-vpc`      | Validate the VPC meets requirements                                                        |
| `make aws-github-connect` | Create/locate GitHub connection + display ARN for `aws/.env` (OAuth approval still manual) |
| `make aws-setup`          | Provision the sandbox (idempotent — safe to re-run)                                        |
| `make aws-teardown`       | Delete the sandbox stacks in dependency order (secrets preserved)                          |

**Operations**

| Command                   | Purpose                                                                                    |
|---------------------------|--------------------------------------------------------------------------------------------|
| `make aws-status`         | Stack status, whether the API task is running, and the connection values the UI project needs |
| `make aws-redeploy`       | Force a new ECS deployment (picks up a re-pushed `:latest`)                                |
| `make aws-logs`           | Tail application CloudWatch logs                                                           |

**Troubleshooting**

| Command                   | Purpose                                                                                    |
|---------------------------|--------------------------------------------------------------------------------------------|
| `make aws-stack-events`   | Failed CloudFormation events across **all** sandbox stacks                                 |
| `make aws-service-events` | Recent ECS service events                                                                  |
| `make aws-task-status`    | Why the most recent ECS task stopped                                                       |

`make aws-status` is the primary verification command on this branch. Since the API has no public endpoint and is not functionally exercised until the UI is deployed, a healthy sandbox means three things, all of which it reports:

1. All stacks in a `*_COMPLETE` state.
2. One ECS task running — which proves the image pulled and both secrets resolved through the VPC endpoints.
3. The API's connection values present, so the UI project has something to point at.

That third item is the security group id, Service Connect namespace, and DNS name the API publishes for the UI to consume. They are CloudFormation outputs with `Export:` blocks, so the UI's own stack reads them with `Fn::ImportValue` rather than having them copied by hand. See [`AGENTS.md`](AGENTS.md) → "Cross-stack contract" for the full list and what each is for.

Any `aws-*` target signs you in automatically (opening a browser) when there's no valid session, using the SSO values in `aws/.env`. The scripts default to the `uh-groupings` profile; to use a different one, export it:

```bash
export AWS_PROFILE=my-other-profile
```

---

# Configuration

The `aws/.env` file contains **deployment parameters only** such as:

- AWS Region
- Environment name (`AWS_ENV`) — names and tags resources
- Deployment tier (`APP_TIER`) — selects the Spring profile `aws-test` / `aws-prod`
- Project identifier
- VPC ID (the VPC must already exist; the subnet is created by `vpc.yml`)
- Main route table ID (required by the S3 gateway endpoint)
- `SUBNET_CIDR` — the single subnet's CIDR
- ECS task count (1; the deployment is single-AZ)

`API_HOSTNAME`, `API_CERTIFICATE_ARN`, and `API_HOSTED_ZONE_ID` are retained blank and **deprecated** — the API has no public endpoint, so there is no hostname or certificate to configure. Public DNS and TLS belong to the UI deployment.

Application secrets are **not** stored in this file. Runtime secrets are managed through AWS Secrets Manager.

## Identifying individual work in the shared sandbox

Deployments target a **shared ITS sandbox account**, so `aws/.env` already carries the correct account/SSO values. To keep each developer's resources distinct, the project encodes an owner into every resource name and tag:

- `AWS_OWNER` → your short identifier (e.g., your username, as in the default `mhodges`)
- `AWS_PROJECT_ID` → `groupings-api`
- `AWS_ENV` → environment label (e.g., `sandbx`)

These combine into names like `mhodges-groupings-api-sandbx-cluster`, so your stacks, ECS resources, and connections are easy to tell apart from a teammate's. Set `AWS_OWNER` to your own value before running `make aws-setup`.

Confirm you're authenticated to the shared account before provisioning:

```bash
aws sts get-caller-identity --query Account --output text   # matches AWS_ACCOUNT_ID in aws/.env
```

See [AWS_NAMING_CONVENTIONS.md](docs/AWS_NAMING_CONVENTIONS.md) for how the values combine, and [`docs/AWS_QUICKSTART.md`](docs/AWS_QUICKSTART.md) for the full setup path.

---

# Related Documentation

This README intentionally provides only a directory overview.

| Document                      | Purpose                                                  |
|-------------------------------|----------------------------------------------------------|
| `docs/AWS_QUICKSTART.md`      | Initial AWS infrastructure provisioning                  |
| `docs/AWS_DEPLOYMENT.md`      | Day-to-day deployment, rollback, scaling, and operations |
| `docs/AWS_ARCHITECTURE.md`    | AWS architecture and resource relationships              |
| `docs/SECRETS.md`             | Secrets management and AWS Secrets Manager integration   |

---

# Design Principles

The AWS infrastructure follows several guiding principles:

- Infrastructure is managed through **CloudFormation**.
- Networking, compute, and CI/CD resources are maintained as separate CloudFormation templates.
- Infrastructure provisioning is automated through `setup.sh`.
- Deployment operations are performed through `make` targets.
- Secrets are stored in **AWS Secrets Manager** rather than source-controlled configuration.
- IAM Identity Center (SSO) provides developer authentication using temporary credentials.