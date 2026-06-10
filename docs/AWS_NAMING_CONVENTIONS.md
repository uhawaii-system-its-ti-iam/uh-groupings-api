# AWS Naming Conventions and Tagging Standards

## Overview

All AWS resources for the UH Groupings API follow standardized naming and tagging conventions to ensure consistency, searchability, and proper cost allocation.

---

## Naming Convention

### Standard Format

```
<Owner>-<Project>-<Environment>-<Resource>
```

### Components

| Component       | Description               | Examples                         |
|-----------------|---------------------------|----------------------------------|
| **Owner**       | Person or team identifier | `mhodges`, `its-iam`, `jsmith`   |
| **Project**     | Project name              | `groupings`                      |
| **Environment** | Deployment environment    | `sandbox`, `dev`, `test`, `prod` |
| **Resource**    | AWS resource type/purpose | `api`, `pipeline`, `vpc`, `ecr`  |

### Examples

#### Sandbox Environment (Personal Development)
```
mhodges-groupings-sandbox-api
mhodges-groupings-sandbox-ecr
mhodges-groupings-sandbox-pipeline
mhodges-groupings-sandbox-vpc
mhodges-groupings-sandbox-cluster
```

#### Test Environment (Team/Shared)
```
its-iam-groupings-test-api
its-iam-groupings-test-ecr
its-iam-groupings-test-pipeline
its-iam-groupings-test-vpc
its-iam-groupings-test-cluster
```

#### Production Environment
```
its-iam-groupings-prod-api
its-iam-groupings-prod-ecr
its-iam-groupings-prod-pipeline
its-iam-groupings-prod-vpc
its-iam-groupings-prod-cluster
```

---

## Tagging Standards

### Required Tags

All resources **MUST** include these tags:

| Tag Key         | Description            | Allowed Values                                                               | Example              |
|-----------------|------------------------|------------------------------------------------------------------------------|----------------------|
| **Owner**       | Resource owner         | Any valid username/team                                                      | `mhodges`, `its-iam` |
| **Project**     | Project identifier     | `groupings`                                                                  | `groupings`          |
| **Environment** | Deployment environment | `sandbox`, `dev`, `test`, `prod`, `future-dev`, `future-test`, `future-prod` | `sandbox`            |
| **Component**   | Component/service type | `api`, `ui`, `db`, `pipeline`, etc.                                          | `api`                |
| **ManagedBy**   | Management method      | `CloudFormation`, `Terraform`, `Manual`                                      | `CloudFormation`     |

### Tag Policy Definition

```json
{
  "tags": {
    "Owner": {},
    "Project": {},
    "Environment": {
      "tag_value": {
        "@@assign": [
          "sandbox",
          "dev",
          "test",
          "prod",
          "future-dev",
          "future-test",
          "future-prod"
        ]
      }
    },
    "Component": {},
    "ManagedBy": {}
  }
}
```

### Optional Tags

| Tag Key        | Description           | Example                                      |
|----------------|-----------------------|----------------------------------------------|
| **CostCenter** | Billing allocation    | `ITS-IAM-001`                                |
| **Contact**    | Primary contact email | `groupings-dev@hawaii.edu`                   |
| **Repository** | Source code location  | `uhawaii-system-its-ti-iam/uh-groupings-api` |
| **Version**    | Application version   | `1.0.0`                                      |

---

## Environment Definitions

### Sandbox
- **Purpose:** Individual developer experimentation
- **Owner:** Individual (e.g., `mhodges`, `jsmith`)
- **Lifecycle:** Short-lived, can be deleted anytime
- **Example:** `mhodges-groupings-sandbox-api`

### Dev
- **Purpose:** Shared development environment
- **Owner:** Team (e.g., `its-iam`)
- **Lifecycle:** Persistent, regularly updated
- **Example:** `its-iam-groupings-dev-api`

### Test
- **Purpose:** QA and integration testing
- **Owner:** Team (e.g., `its-iam`)
- **Lifecycle:** Persistent, stable releases
- **Example:** `its-iam-groupings-test-api`

### Prod
- **Purpose:** Production workloads
- **Owner:** Team (e.g., `its-iam`)
- **Lifecycle:** Persistent, change-controlled
- **Example:** `its-iam-groupings-prod-api`

### Future Environments
- **future-dev**, **future-test**, **future-prod**
- Reserved for planned infrastructure upgrades or migrations

---

## CloudFormation Parameters

### Standard Parameter Set

Every CloudFormation template should include:

```yaml
Parameters:
  Owner:
    Type: String
    Description: Owner identifier (e.g., mhodges, its-iam)
    Default: mhodges
  
  Project:
    Type: String
    Description: Project name
    Default: groupings
  
  Environment:
    Type: String
    Default: sandbox
    AllowedValues:
      - sandbox
      - dev
      - test
      - prod
      - future-dev
      - future-test
      - future-prod
    Description: Environment name
  
  Component:
    Type: String
    Description: Component identifier (api, ui, db, etc.)
    Default: api
```

### Resource Naming in Templates

```yaml
Resources:
  MyResource:
    Type: AWS::...
    Properties:
      # Use Sub function to build names
      Name: !Sub '${Owner}-${Project}-${Environment}-${Component}'
      
      # Apply standard tags
      Tags:
        - Key: Name
          Value: !Sub '${Owner}-${Project}-${Environment}-${Component}'
        - Key: Owner
          Value: !Ref Owner
        - Key: Project
          Value: !Ref Project
        - Key: Environment
          Value: !Ref Environment
        - Key: Component
          Value: !Ref Component
        - Key: ManagedBy
          Value: CloudFormation
```

---

## CLI Usage Examples

### Creating Resources with Naming Convention

```bash
# Set your variables
export OWNER="mhodges"
export PROJECT="groupings"
export ENVIRONMENT="sandbox"
export COMPONENT="api"

# Deploy ECR repository
aws cloudformation create-stack \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecr \
  --template-body file://aws/cloudformation/ecr-repository.yml \
  --parameters \
    ParameterKey=Owner,ParameterValue=${OWNER} \
    ParameterKey=Project,ParameterValue=${PROJECT} \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=Component,ParameterValue=${COMPONENT}

# Deploy ECS cluster
aws cloudformation create-stack \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-ecs \
  --template-body file://aws/cloudformation/ecs-cluster.yml \
  --parameters \
    ParameterKey=Owner,ParameterValue=${OWNER} \
    ParameterKey=Project,ParameterValue=${PROJECT} \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=Component,ParameterValue=${COMPONENT} \
    ParameterKey=VpcId,ParameterValue=vpc-xxxxx \
    ParameterKey=SubnetIds,ParameterValue="subnet-xxxxx,subnet-yyyyy" \
  --capabilities CAPABILITY_NAMED_IAM

# Deploy pipeline
aws cloudformation create-stack \
  --stack-name ${OWNER}-${PROJECT}-${ENVIRONMENT}-pipeline \
  --template-body file://aws/cloudformation/codepipeline.yml \
  --parameters \
    ParameterKey=Owner,ParameterValue=${OWNER} \
    ParameterKey=Project,ParameterValue=${PROJECT} \
    ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
    ParameterKey=Component,ParameterValue=${COMPONENT} \
  --capabilities CAPABILITY_NAMED_IAM
```

### Querying Resources by Tags

```bash
# Find all sandbox resources for a user
aws resourcegroupstaggingapi get-resources \
  --tag-filters \
    Key=Owner,Values=mhodges \
    Key=Environment,Values=sandbox \
    Key=Project,Values=groupings

# Find all production groupings resources
aws resourcegroupstaggingapi get-resources \
  --tag-filters \
    Key=Project,Values=groupings \
    Key=Environment,Values=prod

# Find all resources for cost allocation
aws resourcegroupstaggingapi get-resources \
  --tag-filters \
    Key=Project,Values=groupings \
  --resource-type-filters \
    ecs:cluster \
    ecs:service \
    elasticloadbalancing:loadbalancer
```

---

## Benefits

### Consistency
- All resources follow the same pattern
- Easy to identify resource ownership and purpose
- Reduces naming conflicts

### Searchability
- Quickly find resources by owner, project, or environment
- Tag-based filtering in AWS Console
- Programmatic resource discovery

### Cost Allocation
- Track costs by owner, project, or environment
- Cost Explorer filtering by tags
- Chargeback and showback reports

### Automation
- Scripted resource creation with consistent naming
- CloudFormation parameter validation
- Infrastructure as Code friendly

### Security & Compliance
- Clear ownership for security audits
- Environment isolation
- Access control by tags (IAM conditions)

---

## Common Resource Types

| Resource Type            | Component Suffix  | Example                                  |
|--------------------------|-------------------|------------------------------------------|
| **ECR Repository**       | `ecr`             | `mhodges-groupings-sandbox-ecr`          |
| **ECS Cluster**          | `cluster`         | `mhodges-groupings-sandbox-cluster`      |
| **ECS Service**          | `api`             | `mhodges-groupings-sandbox-api`          |
| **Load Balancer**        | `alb`             | `mhodges-groupings-sandbox-alb`          |
| **CodePipeline**         | `pipeline`        | `mhodges-groupings-sandbox-pipeline`     |
| **CodeBuild**            | `build`           | `mhodges-groupings-sandbox-build`        |
| **VPC**                  | `vpc`             | `mhodges-groupings-sandbox-vpc`          |
| **Security Group**       | `sg-*`            | `mhodges-groupings-sandbox-sg-ecs`       |
| **IAM Role**             | `role-*`          | `mhodges-groupings-sandbox-role-ecs`     |
| **S3 Bucket**            | `s3-*`            | `mhodges-groupings-sandbox-s3-artifacts` |
| **CloudWatch Log Group** | `logs`            | `/ecs/mhodges-groupings-sandbox-api`     |

---

## Validation Checklist

Before deploying resources, verify:

- [ ] Resource name follows `<Owner>-<Project>-<Environment>-<Resource>` format
- [ ] All required tags are present (Owner, Project, Environment, Component, ManagedBy)
- [ ] Environment value is from allowed list
- [ ] Owner matches your AWS account username or team identifier
- [ ] Component name accurately describes the resource
- [ ] CloudFormation stack name matches resource naming convention

---

## Examples by Environment

### Personal Sandbox (mhodges)
```bash
Stack Name:        mhodges-groupings-sandbox-ecr
ECR Repository:    mhodges-groupings-sandbox-api
ECS Cluster:       mhodges-groupings-sandbox-cluster
ECS Service:       mhodges-groupings-sandbox-api
ALB:               mhodges-groupings-sandbox-alb
Pipeline:          mhodges-groupings-sandbox-pipeline
```

### Team Test Environment (its-iam)
```bash
Stack Name:        its-iam-groupings-test-ecr
ECR Repository:    its-iam-groupings-test-api
ECS Cluster:       its-iam-groupings-test-cluster
ECS Service:       its-iam-groupings-test-api
ALB:               its-iam-groupings-test-alb
Pipeline:          its-iam-groupings-test-pipeline
```

### Production (its-iam)
```bash
Stack Name:        its-iam-groupings-prod-ecr
ECR Repository:    its-iam-groupings-prod-api
ECS Cluster:       its-iam-groupings-prod-cluster
ECS Service:       its-iam-groupings-prod-api
ALB:               its-iam-groupings-prod-alb
Pipeline:          its-iam-groupings-prod-pipeline
```

---

## Related Documentation

- [AWS_QUICKSTART.md](./AWS_QUICKSTART.md) - Uses these conventions
- [AWS_SETUP.md](./AWS_SETUP.md) - Detailed setup with naming
- [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md) - Deployment procedures
<!--suppress HtmlUnknownTarget -->
- [aws/deployment.json](../aws/deployment.json) - Configuration file

---
