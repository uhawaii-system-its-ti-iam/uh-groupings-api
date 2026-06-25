# AWS Naming Conventions and Tagging Standards

## Overview

All AWS resources for the UH Groupings family of projects (Spring API, Angular UI, React UI) follow a single, length-aware naming convention so they can coexist in the same AWS account without collision and remain easy to attribute by owner, project, and environment.

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

| Component           | Purpose                                 | Examples                                                |
|---------------------|-----------------------------------------|---------------------------------------------------------|
| **AWS_OWNER**       | Person or team identifier               | `mhodges`, `its-iam`                                    |
| **AWS_PROJECT_ID**  | Short project identifier (≤13 chars)    | `groupings-api`, `groupings-aui`, `groupings-ui`       |
| **AWS_ENV**         | Deployment environment                  | `sandbx`, `dev`, `test`, `prod`                         |
| **resource-suffix** | Appended by CloudFormation per resource | `cluster`, `service`, `tg`, `alb`, `role-ecs-execution` |

### Why `AWS_PROJECT_ID` is short

AWS imposes a 32-character limit on Application Load Balancer and target group names. Keeping `AWS_PROJECT_ID` to ≤13 characters and `AWS_ENV` to ≤6 characters leaves enough room for owner and suffix:

```
mhodges - groupings-api - sandbx - alb
   7    +      13       +    6   +  3   = 32 chars  ✅ at the limit, fits
```

This is why the canonical sandbox environment uses **`sandbx`** rather than the more familiar `sandbox` — `sandbox` (7 chars) would push `mhodges-groupings-api-sandbox-alb` to 33 chars and AWS would reject the ALB. The other allowed environment values (`dev`, `test`, `prod`) are short enough not to hit the limit.

### Examples

#### Sandbox (PoC development)

```
mhodges-groupings-api-sandbx            (ECR repository)
mhodges-groupings-api-sandbx-cluster    (ECS cluster)
mhodges-groupings-api-sandbx-service    (ECS service)
mhodges-groupings-api-sandbx-tg         (target group)
mhodges-groupings-api-sandbx-alb        (load balancer)
mhodges-groupings-api-sandbx-role-ecs-execution
mhodges-groupings-api-sandbx-role-ecs-task
/ecs/mhodges-groupings-api-sandbx       (CloudWatch log group)
```

#### Test (team)

```
its-iam-groupings-api-test-cluster
its-iam-groupings-api-test-service
its-iam-groupings-api-test-tg
its-iam-groupings-api-test-alb
```

#### Production (team)

```
its-iam-groupings-api-prod-cluster
its-iam-groupings-api-prod-service
its-iam-groupings-api-prod-tg
its-iam-groupings-api-prod-alb
```

---

## CloudFormation Stack Names

`aws/setup.sh` creates three stacks per environment, named with `AWS_PROJECT_ID` and `AWS_ENV`:

```
groupings-api-ecr-sandbx
groupings-api-ecs-sandbx
groupings-api-pipeline-sandbx
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

Both `aws/cloudformation/ecr-repository.yml` and `aws/cloudformation/ecs-cluster.yml` accept three identifier parameters:

```yaml
Parameters:
  Owner:
    Type: String
    Description: Owner identifier (e.g., mhodges, its-iam)
    Default: mhodges
  Project:
    Type: String
    Description: Project identifier (≤13 chars; e.g., groupings-api, groupings-aui, groupings-ui)
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

| Resource                   | Final Name                                              |
|----------------------------|---------------------------------------------------------|
| ECR repository             | `${Owner}-${Project}-${Environment}`                    |
| ECS cluster                | `${Owner}-${Project}-${Environment}-cluster`            |
| ECS service                | `${Owner}-${Project}-${Environment}-service`            |
| ECS task definition family | `${Owner}-${Project}-${Environment}`                    |
| Container name             | `${Owner}-${Project}-${Environment}`                    |
| Target group               | `${Owner}-${Project}-${Environment}-tg`                 |
| Application Load Balancer  | `${Owner}-${Project}-${Environment}-alb`                |
| IAM execution role         | `${Owner}-${Project}-${Environment}-role-ecs-execution` |
| IAM task role              | `${Owner}-${Project}-${Environment}-role-ecs-task`      |
| CloudWatch log group       | `/ecs/${Owner}-${Project}-${Environment}`               |
| CloudFormation stacks      | `${Project}-{ecr\|ecs\|pipeline}-${Environment}`        |

---

## Validation Checklist

Before deploying:

- [ ] `AWS_PROJECT_ID` is ≤10 characters
- [ ] `AWS_OWNER` is set (defaults to `mhodges`)
- [ ] `AWS_ENV` is one of the allowed values
- [ ] No conflict with another developer's existing deployment in the same account

---

## Related Documentation

- [AWS_QUICKSTART.md](AWS_QUICKSTART.md) — provisioning workflow that uses these conventions
- [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md) — ongoing operations
- [SECRETS.md](SECRETS.md) — how secrets are stored separately from naming
- [aws/cloudformation/](../aws/cloudformation/) — the templates that consume the parameters
