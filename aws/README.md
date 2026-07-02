# AWS Infrastructure

This directory contains the AWS-specific artifacts used to provision, deploy, and operate the **UH Groupings API** on AWS.

Be advised: this iteration of the project's scripts are macOS (and maybe Linux) compatible.

The project intentionally separates:

- **Application code** (Java/Spring Boot)
- **Deployment configuration** (CodeBuild, CodeDeploy)
- **Infrastructure as Code** (CloudFormation)
- **Operational scripts** (AWS setup and administration)

Detailed setup, deployment, architecture, and operational guidance are maintained in the project's documentation. This README serves only as an overview of the directory contents.

---

# Directory Structure

```
aws/
├── README.md
├── .env                        # Deployment configuration (non-secret)
├── setup.sh                    # Automated AWS infrastructure provisioning
├── auth.sh                     # SSO profile bootstrap + sign-in dispatcher
├── lib-auth.sh                 # Shared SSO auth helpers (sourced by scripts)
├── check-vpc.sh                # Validates the VPC in .env meets requirements
├── github-connect.sh           # Creates/locates a GitHub CodeConnections connection
├── buildspec.yml               # AWS CodeBuild specification
├── appspec.yml                 # AWS CodeDeploy specification
├── task-definition.json        # ECS task definition template
└── cloudformation/
    ├── vpc.yml                 # Public subnets created in an existing VPC
    ├── ecr-repository.yml      # Amazon ECR repository
    ├── ecs-service.yml         # ECS Fargate service and supporting resources
    └── codepipeline.yml        # CI/CD pipeline
```

---

# CloudFormation Organization

The CloudFormation templates are organized by infrastructure layer rather than deployment order.

| Template               | Purpose                                                                                                                                                                            |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **vpc.yml**            | Public subnets (2 AZs) provisioned inside a pre-existing VPC; exports their IDs for `ecs-service.yml` and companion project stacks to consume. The VPC itself is assumed to exist. |
| **ecr-repository.yml** | Container image repository.                                                                                                                                                        |
| **ecs-service.yml**    | Application runtime including ECS Fargate, Application Load Balancer, security groups, IAM roles, CloudWatch logs, and task definition.                                            |
| **codepipeline.yml**   | Continuous integration and deployment infrastructure.                                                                                                                              |

Keeping these layers separate minimizes stack coupling and allows networking, compute, and CI/CD resources to evolve independently.

---

# Common Make Targets

All AWS operations are performed through the project's Makefile, which runs the AWS CLI directly on your host. The AWS CLI v2 must be installed (macOS: `brew install awscli`).

| Command                   | Purpose                                                                                    |
|---------------------------|--------------------------------------------------------------------------------------------|
| `make aws-sso-setup`      | Configure SSO profile and sign in                                                          |
| `make aws-sso-login`      | Force a fresh SSO login (refresh)                                                          |
| `make aws-check-vpc`      | Validate the VPC meets requirements                                                        |
| `make aws-github-connect` | Create/locate GitHub connection + display ARN for `aws/.env` (OAuth approval still manual) |
| `make aws-setup`          | Provision AWS infrastructure                                                               |
| `make aws-teardown`       | Remove deployed infrastructure                                                             |
| `make aws-stack-events`   | View CloudFormation failures                                                               |
| `make aws-service-events` | View ECS service events                                                                    |
| `make aws-task-status`    | View recent ECS task status                                                                |
| `make aws-logs`           | Tail application CloudWatch logs                                                           |

Any `aws-*` target signs you in automatically (opening a browser) when there's no valid session, using the SSO values in `aws/.env`. The scripts default to the `uh-groupings` profile; to use a different one, export it:

```bash
export AWS_PROFILE=my-other-profile
```

---

# Configuration

The `aws/.env` file contains **deployment parameters only** such as:

- AWS Region
- Environment name
- Project identifier
- VPC ID (the VPC must already exist; subnets are created by `vpc.yml`)
- ECS task count

Application secrets are **not** stored in this file. Runtime secrets are managed through AWS Secrets Manager.

---

# Related Documentation

This README intentionally provides only a directory overview.

| Document                      | Purpose                                                  |
|-------------------------------|----------------------------------------------------------|
| `docs/AWS_QUICKSTART.md`      | Initial AWS infrastructure provisioning                  |
| `docs/AWS_DEPLOYMENT.md`      | Day-to-day deployment, rollback, scaling, and operations |
| `docs/ARCHITECTURE.md`        | AWS architecture and resource relationships              |
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