# AWS Infrastructure

This directory contains the AWS-specific artifacts used to provision, deploy, and operate the **UH Groupings API** on AWS.

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
├── .aws-state/                 # Cached AWS CLI SSO state (gitignored)
├── setup.sh                    # Automated AWS infrastructure provisioning
├── setup-sso.sh                # IAM Identity Center (SSO) setup
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

| Template               | Purpose                                                                                                                                 |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **vpc.yml**            | Public subnets (2 AZs) provisioned inside a pre-existing VPC; exports their IDs for `ecs-service.yml` and companion project stacks to consume. The VPC itself is assumed to exist. |
| **ecr-repository.yml** | Container image repository.                                                                                                             |
| **ecs-service.yml**    | Application runtime including ECS Fargate, Application Load Balancer, security groups, IAM roles, CloudWatch logs, and task definition. |
| **codepipeline.yml**   | Continuous integration and deployment infrastructure.                                                                                   |

Keeping these layers separate minimizes stack coupling and allows networking, compute, and CI/CD resources to evolve independently.

---

# Common Make Targets

All AWS operations are performed through the project's Makefile, which executes the AWS CLI inside a Docker container.

| Command                   | Purpose                             |
|---------------------------|-------------------------------------|
| `make aws-sso-setup`      | Configure IAM Identity Center (SSO) |
| `make aws-sso-login`      | Refresh an expired SSO session      |
| `make aws-setup`          | Provision AWS infrastructure        |
| `make aws-teardown`       | Remove deployed infrastructure      |
| `make aws-stack-events`   | View CloudFormation failures        |
| `make aws-service-events` | View ECS service events             |
| `make aws-task-status`    | View recent ECS task status         |
| `make aws-logs`           | Tail application CloudWatch logs    |

Before running AWS commands, ensure the desired AWS profile has been exported:

```bash
export AWS_PROFILE=uh-groupings
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