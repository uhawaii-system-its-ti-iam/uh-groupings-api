# AWS Naming Conventions and Tagging Standards

## Overview

All AWS resources for the UH Groupings family of projects (Spring API, Angular UI, React UI) follow a single, length-aware naming convention. This allows them to coexist in the same AWS account without collision and remain easy to attribute by owner, project, and environment.

<!-- TOC -->
* [AWS Naming Conventions and Tagging Standards](#aws-naming-conventions-and-tagging-standards)
  * [Overview](#overview)
  * [Naming Convention](#naming-convention)
    * [Standard format](#standard-format)
    * [Why `AWS_PROJECT_ID` is short](#why-aws_project_id-is-short)
    * [Examples](#examples)
      * [Sandbox (PoC development)](#sandbox-poc-development)
      * [Test (team)](#test-team)
      * [Production (team)](#production-team)
  * [CloudFormation Stack Names](#cloudformation-stack-names)
  * [Tagging Standards](#tagging-standards)
    * [Optional tags](#optional-tags)
  * [Environment Values](#environment-values)
  * [CloudFormation Parameters](#cloudformation-parameters)
  * [Driving the Convention from `aws/.env`](#driving-the-convention-from-awsenv)
  * [Resource Naming by Type](#resource-naming-by-type)
  * [Validation Checklist](#validation-checklist)
  * [Related Documentation](#related-documentation)
<!-- TOC -->

---

## Naming Convention

### Standard format

```
<AWS_OWNER>-<AWS_PROJECT_ID>-<AWS_ENV>-<resource-suffix>
```

The three identifier components are read from `aws/.env` and passed to the CloudFormation templates as the `Owner`, `Project`, and `Environment` parameters.

| Component           | Purpose                                 | Examples                                                 |
|---------------------|-----------------------------------------|----------------------------------------------------------|
| **AWS_OWNER**       | Person or team identifier               | `mhodges`, `its-iam`                                     |
| **AWS_PROJECT_ID**  | Short project identifier (≤13 chars)    | `groupings-api`, `groupings-aui`, `groupings-ui`         |
| **AWS_ENV**         | Deployment environment                  | `sandbx`, `dev`, `test`, `prod`                          |
| **resource-suffix** | Appended by CloudFormation per resource | `cluster`, `service`, `tg`, `alb`, `role-ecs-execution`  |

### Why `AWS_PROJECT_ID` is short

The binding limit today is the **64-character IAM role name**, which the longest generated name approaches:

```
mhodges - groupings-api - sandbx - role-ecs-execution
   7    +      13       +    6   +        18          + 3 hyphens = 47 chars  ✅
```

Historically the constraint was tighter: AWS caps Application Load Balancer and target group names at **32 characters**, and the ≤13-character `AWS_PROJECT_ID` / ≤6-character `AWS_ENV` convention was chosen to fit `mhodges-groupings-api-sandbx-alb` exactly at 32. That is also why the sandbox environment is spelled **`sandbx`** rather than `sandbox` — the extra character would have pushed the ALB name to 33 and AWS would have rejected it.

**The API no longer creates a load balancer or target group** (it is a private service reached over ECS Service Connect), so the 32-character limit no longer applies here. The convention is retained anyway for two reasons: the companion UI projects *do* create load balancers and share this naming scheme, and keeping the identifiers short leaves headroom if a length-constrained resource is introduced later. Do not lengthen `AWS_PROJECT_ID` on the assumption that the limit is gone.

### Examples

#### Sandbox (PoC development)

```
mhodges-groupings-api-sandbx                     (ECR repository)
mhodges-groupings-api-sandbx                     (Service Connect namespace)
mhodges-groupings-api-sandbx-cluster             (ECS cluster)
mhodges-groupings-api-sandbx-service             (ECS service)
mhodges-groupings-api-sandbx-sg-api-backend      (API task security group)
mhodges-groupings-api-sandbx-subnet-private      (API task subnet)
mhodges-groupings-api-sandbx-subnet-public-nat   (NAT Gateway subnet)
mhodges-groupings-api-sandbx-rtb-private         (private route table)
mhodges-groupings-api-sandbx-nat                 (NAT Gateway)
mhodges-groupings-api-sandbx-eip-nat             (NAT Elastic IP)
mhodges-groupings-api-sandbx-role-ecs-execution
mhodges-groupings-api-sandbx-role-ecs-task
/ecs/mhodges-groupings-api-sandbx                (CloudWatch log group)
```

There is no `-alb` or `-tg` name: the API creates no load balancer or target group.

#### Test (team)

```
its-iam-groupings-api-test-cluster
its-iam-groupings-api-test-service
its-iam-groupings-api-test-sg-api-backend
```

#### Production (team)

```
its-iam-groupings-api-prod-cluster
its-iam-groupings-api-prod-service
its-iam-groupings-api-prod-sg-api-backend
```

---

## CloudFormation Stack Names

`aws/setup.sh` creates three stacks per environment, named with `AWS_PROJECT_ID` and `AWS_ENV`; the pipeline stack is deployed separately (see [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md)):

```
groupings-api-vpc-sandbx        (subnet + VPC endpoints)
groupings-api-ecr-sandbx
groupings-api-ecs-sandbx
groupings-api-pipeline-sandbx   (deployed separately, not by setup.sh)
```

Stack names always lead with `AWS_PROJECT_ID` so each project's stacks are listable as a group via `aws cloudformation list-stacks --stack-status-filter CREATE_COMPLETE | grep '^groupings-api-'`.

---

## Tagging Standards

Every resource created by the project's CloudFormation templates carries this tag set:

| Tag           | Source                                        | Example                                  |
|---------------|-----------------------------------------------|------------------------------------------|
| `Name`        | `${Owner}-${Project}-${Environment}-{suffix}` | `mhodges-groupings-api-sandbx-cluster`   |
| `Owner`       | `Owner` parameter                             | `mhodges`                                |
| `Project`     | `Project` parameter                           | `groupings-api`                          |
| `Environment` | `Environment` parameter                       | `sandbx`                                 |
| `ManagedBy`   | Literal string                                | `CloudFormation`                         |

### Optional tags

Add these manually when relevant:

| Tag          | Purpose               | Example                                      |
|--------------|-----------------------|----------------------------------------------|
| `CostCenter` | Billing allocation    | `ITS-IAM-001`                                |
| `Contact`    | Primary contact email | `groupings-dev@hawaii.edu`                   |
| `Repository` | Source code location  | `uhawaii-system-its-ti-iam/uh-groupings-api` |
| `Version`    | Application version   | `1.0.0`                                      |

---

## Environment Values

The `Environment` CloudFormation parameter accepts:

```
sandbx  dev  test  prod 
```

| Environment | Purpose                              | Owner                        | Lifecycle                     |
|-------------|--------------------------------------|------------------------------|-------------------------------|
| `sandbx`    | Individual developer experimentation | Individual (e.g., `mhodges`) | Short-lived                   |
| `dev`       | Shared development environment       | Team (e.g., `its-iam`)       | Persistent                    |
| `test`      | QA and integration testing           | Team                         | Persistent                    |
| `prod`      | Production workloads                 | Team                         | Persistent, change-controlled |
---

## CloudFormation Parameters

All four templates in `aws/cloudformation/` (`vpc.yml`, `ecr-repository.yml`, `ecs-service.yml`, `codepipeline.yml`) accept the same three identifier parameters:

```yaml
Parameters:
  Owner:
    Type: String
    Description: Owner identifier (e.g., mhodges, its-iam)
    Default: mhodges
  Project:
    Type: String
    Description: Project identifier (≤13 chars; must match AWS_PROJECT_ID in aws/.env)
    Default: groupings-api
  Environment:
    Type: String
    Default: sandbx
    AllowedValues:
      - sandbx
      - dev
      - test
      - prod
```

Templates compose resource names via `!Sub`:

```yaml
ClusterName: !Sub '${Owner}-${Project}-${Environment}-cluster'
```

There is no `Component` parameter. Earlier versions of these templates used a separate `Component` value (e.g., `api`), but it has been folded into `AWS_PROJECT_ID` itself (`groupings-api` already says "API," `groupings-aui` already says "Angular UI"). Adding a separate component layer produced redundant names like `mhodges-groupings-api-sandbx-api` and was removed.

---

## Driving the Convention from `aws/.env`

`aws/setup.sh` reads:

```bash
AWS_OWNER=mhodges
AWS_PROJECT_ID=groupings-api
AWS_ENV=sandbx
```

…and passes them to CloudFormation as `Owner`, `Project`, `Environment`. The companion UI projects use the same `setup.sh` pattern with their own `AWS_PROJECT_ID` values.

To deploy under a different owner or to a different environment, edit `aws/.env` and re-run `make aws-setup`. There are no script flags or environment-variable overrides — the `.env` file is the single source of truth.

---

## Resource Naming by Type

| Resource                    | Final Name                                              |
|-----------------------------|---------------------------------------------------------|
| ECR repository              | `${Owner}-${Project}-${Environment}`                    |
| Service Connect namespace   | `${Owner}-${Project}-${Environment}`                    |
| ECS cluster                 | `${Owner}-${Project}-${Environment}-cluster`            |
| ECS service                 | `${Owner}-${Project}-${Environment}-service`            |
| ECS task definition family  | `${Owner}-${Project}-${Environment}`                    |
| Container name              | `${Owner}-${Project}-${Environment}`                    |
| API task security group     | `${Owner}-${Project}-${Environment}-sg-api-backend`     |

| Private subnet (API task)   | `${Owner}-${Project}-${Environment}-subnet-private`     |
| Public subnet (NAT only)    | `${Owner}-${Project}-${Environment}-subnet-public-nat`  |
| Private route table         | `${Owner}-${Project}-${Environment}-rtb-private`        |
| NAT Gateway                 | `${Owner}-${Project}-${Environment}-nat`                |
| NAT Elastic IP              | `${Owner}-${Project}-${Environment}-eip-nat`            |
| IAM execution role          | `${Owner}-${Project}-${Environment}-role-ecs-execution` |
| IAM task role               | `${Owner}-${Project}-${Environment}-role-ecs-task`      |
| CloudWatch log group        | `/ecs/${Owner}-${Project}-${Environment}`               |
| S3 artifact bucket          | `${Owner}-${Project}-${Environment}-s3-artifacts`       |
| CloudFormation stacks       | `${Project}-{vpc\|ecr\|ecs\|pipeline}-${Environment}`   |

Not created by this project: Application Load Balancer, target group, NAT gateway, Internet Gateway.

---

## Validation Checklist

Before deploying:

- [ ] `AWS_PROJECT_ID` is ≤13 characters and matches the `Project` parameter defaults in the CloudFormation templates
- [ ] `AWS_OWNER` is set (defaults to `mhodges`)
- [ ] `AWS_ENV` is one of the allowed values
- [ ] No conflict with another developer's existing deployment in the same account

---

## Related Documentation

- [AWS_QUICKSTART.md](AWS_QUICKSTART.md) — provisioning workflow that uses these conventions
- [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md) — ongoing operations
- [SECRETS.md](SECRETS.md) — how secrets are stored separately from naming
- [aws/cloudformation/](../cloudformation/) — the templates that consume the parameters
