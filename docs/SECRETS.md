# Secrets Management Guide

This guide explains how to manage secrets and configuration for the UH Groupings API in both local development and AWS production environments.

## Overview

The application uses **two approaches** for configuration and secrets:

| Environment           | Method                       | Storage Location     | Injection Method                                |
|-----------------------|------------------------------|----------------------|-------------------------------------------------|
| **Local Development** | Properties file + bind mount | `~/.yourname-conf/`  | Docker volume + `SPRING_CONFIG_IMPORT`          |
| **AWS Production**    | Secrets Manager + ECS env    | AWS Cloud            | ECS Task Definition (`secrets` + `environment`) |

---

## Local Development (Docker Desktop)

### Quick Start

1. **Create your properties file:**
   ```bash
   mkdir -p ~/.$(whoami)-conf
   nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   ```

2. **Add your configuration** (Spring Boot properties format):
   ```properties
   groupings.api.localhost.user=your_username
   groupings.api.test.admin_user=your_username
   grouperClient.webService.url=https://grouper-test.its.hawaii.edu/grouper-ws/servicesRest/
   grouperClient.webService.login=_groupings_api_2
   grouperClient.webService.password=redacted
   email.is.enabled=false
   email.send.recipient=your_email@hawaii.edu
   jwt.secret.key=your_local_jwt_secret_here
   properties.override.result=OVERRIDDEN
   ```

3. **Start the application:**
   ```bash
   docker compose up
   ```

### How It Works

```
┌──────────────────────────────────────────┐
│ ~/.yourname-conf/                        │
│   uh-groupings-api-overrides.properties  │
│ (Spring Boot properties)                 │
└───────────┬──────────────────────────────┘
            │
            │ docker compose up
            │ (bind mount, read-only)
            ▼
┌───────────────────────────────────────────────────┐
│ Docker Container                                  │
│ /app/config/uh-groupings-api-overrides.properties │
│ (loaded via SPRING_CONFIG_IMPORT)                 │
└───────────────────────────────────────────────────┘
```

### Required Properties for Local Development

Create `~/.$(whoami)-conf/uh-groupings-api-overrides.properties` with the following configuration:

```properties
# UH Usernames for testing
groupings.api.localhost.user=your_username
groupings.api.test.admin_user=your_username

# Grouper API client configuration
grouperClient.webService.url=https://grouper-test.its.hawaii.edu/grouper-ws/servicesRest/
grouperClient.webService.login=_groupings_api_2
grouperClient.webService.password=redacted

# Email configuration
email.is.enabled=false
email.send.recipient=your_email@hawaii.edu

# Secret keys (generate fresh for local development)
# Instructions: <https://uhawaii.atlassian.net/wiki/spaces/SITARd/pages/2040561680>
jwt.secret.key=your_local_jwt_secret_here

# Flag indicates a successful loading of the personal overrides file
properties.override.result=OVERRIDDEN
```

**Note on Secrets:** Only the following properties contain secrets and should be kept secure:
- `grouperClient.webService.password` — Grouper service account password (secret)
- `jwt.secret.key` — JWT signing key (secret)

All other properties are managed as non-secret settings (not stored in AWS Secrets Manager).

### Property Reference

For reference, here are the primary properties in the overrides file:

| Spring Property                     | Type        | Description                                       |
|-------------------------------------|-------------|---------------------------------------------------|
| `groupings.api.localhost.user`      | Setting     | Your UH username for local testing                |
| `groupings.api.test.admin_user`     | Setting     | Admin username for test operations                |
| `grouperClient.webService.url`      | Setting     | Grouper API endpoint URL                          |
| `grouperClient.webService.login`    | Setting     | Grouper service account username                  |
| `grouperClient.webService.password` | **Secret**  | Grouper web service password setting (sensitive)  |
| `email.is.enabled`                  | Setting     | Enable/disable email notifications                |
| `email.send.recipient`              | Setting     | Default email recipient for notifications         |
| `jwt.secret.key`                    | **Secret**  | JWT signing key for token generation (sensitive)  |
| `properties.override.result`        | Setting     | Flag indicating successful override file loading  |

**Secrets vs Settings:**
- **Secrets**: `grouperClient.webService.password`, `jwt.secret.key`
  - These contain sensitive credentials and should be treated as secrets
  - In AWS: stored in AWS Secrets Manager
  - In local dev: stored in your local properties file with restricted permissions
  
- **Settings**: All other properties
  - These are configuration values that control application behavior
  - Can be committed to repository (if not sensitive)
  - In AWS: typically passed as environment variables or task definition overrides
  - In local dev: stored in the properties file alongside secrets

### Security for Local Development

```bash
# Set restrictive permissions on your properties file
chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

**Best Practices:**
- ✅ Use separate credentials for local development
- ✅ Never use production credentials locally
- ✅ Keep properties file in your home directory
- ✅ Set file permissions to 600 (owner read/write only)
- ❌ Never commit generated `.env` files to Git (optional converter workflow)
- ❌ Never share your properties file

### Troubleshooting Local Development

**Problem:** Script can't find properties file
```bash
ERROR: source properties file not found
```

**Solution:**
```bash
# Check if file exists
ls -la ~/.$(whoami)-conf/uh-groupings-api-overrides.properties

# Create if missing
mkdir -p ~/.$(whoami)-conf
touch ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

**Problem:** Docker Compose can't mount overrides file
```bash
Error response from daemon: invalid mount config for type "bind"
```

**Solution:**
```bash
# Ensure file exists and has correct permissions
mkdir -p ~/.$(whoami)-conf
touch ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

**More help:** See [docker/README.md](../docker/README.md)

---

## AWS Production (ECS + Secrets Manager)

### Overview

In AWS container deployments, only sensitive values are stored in **AWS Secrets Manager**. Non-sensitive settings are configured in the ECS task definition `environment` array.

This split provides:

- ✅ Encryption at rest and in transit
- ✅ Fine-grained access control (IAM)
- ✅ Audit logging (CloudTrail)
- ✅ Secret rotation capabilities
- ✅ No secrets in code or environment files

### Architecture

```
┌────────────────────────────┐      ┌────────────────────────────┐
│  AWS Secrets Manager       │      │ ECS Task Definition        │
│  (2 sensitive properties)  │      │ environment[] settings     │
└──────────┬─────────────────┘      └──────────┬─────────────────┘
           │                                    │
           │ IAM Role grants access             │ Task definition values
           │                                    │
           └──────────────┬─────────────────────┘
                          ▼
               ┌────────────────────────────┐
               │  ECS Task (Container)      │
               │  (all values available as  │
               │   environment variables)   │
               └────────────────────────────┘
```

### Required Secrets

Only **two properties** are managed as secrets in AWS Secrets Manager:

| Secret Name                      | Spring Property                      | Description                          |
|----------------------------------|--------------------------------------|--------------------------------------|
| `groupings/api/grouper-password` | `grouperClient.webService.password`  | Grouper service account username     |
| `groupings/api/jwt-secret`       | `jwt.secret.key`                     | JWT signing key for token generation |

All other configuration properties are passed directly as environment variables in the ECS task definition and do **not** use AWS Secrets Manager.

### Required Settings (ECS Task Definition)

Configure these non-secret properties in the ECS task definition `environment` array:

| Spring Property                  | ECS Environment Variable         | Example Value                                                  |
|----------------------------------|----------------------------------|----------------------------------------------------------------|
| `groupings.api.localhost.user`   | `GROUPINGS_API_LOCALHOST_USER`   | `your_username`                                                |
| `groupings.api.test.admin_user`  | `GROUPINGS_API_TEST_ADMIN_USER`  | `your_username`                                                |
| `grouperClient.webService.url`   | `GROUPERCLIENT_WEBSERVICE_URL`   | `https://grouper-prod.its.hawaii.edu/grouper-ws/servicesRest/` |
| `grouperClient.webService.login` | `GROUPERCLIENT_WEBSERVICE_LOGIN` | `configured-value`                                             |
| `email.is.enabled`               | `EMAIL_IS_ENABLED`               | `false`                                                        |
| `email.send.recipient`           | `EMAIL_SEND_RECIPIENT`           | `groupings-alerts@hawaii.edu`                                  |
| `properties.override.result`     | `PROPERTIES_OVERRIDE_RESULT`     | `OVERRIDDEN`                                                   |

These settings are defined in your task definition JSON (for example, `aws/task-definition.json`) under `containerDefinitions[].environment` and then applied when you deploy/register the task definition in ECS.

### Creating Secrets in AWS

#### Method 1: Using AWS CLI (Recommended)

```bash
# Set your environment
export AWS_REGION="us-west-2"

# Create Grouper login secret
aws secretsmanager create-secret \
  --name groupings/api/grouper-login \
  --description "Grouper service account username" \
  --secret-string "your-grouper-service-account" \
  --region $AWS_REGION

# Generate and create JWT secret
JWT_SECRET=$(openssl rand -base64 32)
aws secretsmanager create-secret \
  --name groupings/api/jwt-secret \
  --description "JWT signing key for token generation" \
  --secret-string "$JWT_SECRET" \
  --region $AWS_REGION
```

**Note:** All other configuration properties (URLs, email settings, etc.) are passed as environment variables directly in the ECS task definition, not as AWS Secrets Manager secrets.

#### Method 2: Using AWS Console

1. Go to [AWS Secrets Manager Console](https://console.aws.amazon.com/secretsmanager/)
2. Click **"Store a new secret"**
3. Select **"Other type of secret"**
4. Choose **"Plaintext"** tab
5. Enter your secret value
6. Click **"Next"**
7. Enter secret name (e.g., `groupings/api/grouper-login`)
8. Add description
9. Click **"Next"** through remaining screens
10. Click **"Store"**

### Updating Secrets

```bash
# Update Grouper login secret
aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-login \
  --secret-string "NEW_GROUPER_LOGIN" \
  --region $AWS_REGION

# Update JWT secret
aws secretsmanager update-secret \
  --secret-id groupings/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)" \
  --region $AWS_REGION

# After updating secrets, force ECS to restart tasks with new values
aws ecs update-service \
  --cluster uh-groupings-production \
  --service uh-groupings-api-service \
  --force-new-deployment
```

To update non-secret configuration properties (URLs, email settings, etc.), update the ECS task definition's `environment` array and deploy the new task definition.

### Retrieving Secrets (for verification)

```bash
# Get a secret value (BE CAREFUL - displays secret!)
aws secretsmanager get-secret-value \
  --secret-id groupings/api/grouper-login \
  --query SecretString \
  --output text \
  --region $AWS_REGION

aws secretsmanager get-secret-value \
  --secret-id groupings/api/jwt-secret \
  --query SecretString \
  --output text \
  --region $AWS_REGION

# List all groupings secrets
aws secretsmanager list-secrets \
  --filters Key=name,Values=groupings/ \
  --query 'SecretList[*].[Name,Description]' \
  --output table \
  --region $AWS_REGION
```

### ECS Task Definition Integration

The task definition references secrets in AWS Secrets Manager and passes non-secret settings in `environment`. Example:

```json
{
  "containerDefinitions": [
    {
      "name": "uh-groupings-api",
      "secrets": [
        {
          "name": "GROUPERCLIENT_WEBSERVICE_LOGIN",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-login"
        },
        {
          "name": "JWT_SECRET_KEY",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/jwt-secret"
        }
      ],
      "environment": [
        {
          "name": "GROUPINGS_API_LOCALHOST_USER",
          "value": "your_username"
        },
        {
          "name": "GROUPINGS_API_TEST_ADMIN_USER",
          "value": "your_username"
        },
        {
          "name": "GROUPERCLIENT_WEBSERVICE_URL",
          "value": "https://grouper-prod.its.hawaii.edu/grouper-ws/servicesRest/"
        },
        {
          "name": "GROUPERCLIENT_WEBSERVICE_PASSWORD",
          "value": "configured-value"
        },
        {
          "name": "EMAIL_IS_ENABLED",
          "value": "false"
        },
        {
          "name": "EMAIL_SEND_RECIPIENT",
          "value": "groupings-alerts@hawaii.edu"
        },
        {
          "name": "PROPERTIES_OVERRIDE_RESULT",
          "value": "OVERRIDDEN"
        }
      ]
    }
  ]
}
```

**How it works:**

- **`secrets` array:** References sensitive values from AWS Secrets Manager
  - Injected as environment variables at container startup
  - Automatically decrypted by ECS
  - Not visible in task definition or logs
  
- **`environment` array:** Non-sensitive configuration properties
  - Passed directly as environment variables
  - Plain text (not encrypted)
  - Visible in ECS console and task definition

### IAM Permissions

The ECS Task Execution Role needs permission to read secrets:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:us-west-2:*:secret:groupings/api/*"
      ]
    }
  ]
}
```

This is automatically configured by the CloudFormation template `aws/cloudformation/ecs-cluster.yml`.

### Secret Rotation

#### Manual Rotation

```bash
# Update the Grouper login secret
aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-login \
  --secret-string "NEW_GROUPER_LOGIN"

# Update the JWT secret
aws secretsmanager update-secret \
  --secret-id groupings/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)"

# Force ECS to restart tasks with new secret
aws ecs update-service \
  --cluster uh-groupings-production \
  --service uh-groupings-api-service \
  --force-new-deployment
```

#### Automated Rotation (Advanced)

AWS Secrets Manager supports automatic rotation with Lambda functions:

```bash
# Enable automatic rotation (requires Lambda function)
aws secretsmanager rotate-secret \
  --secret-id groupings/api/jwt-secret \
  --rotation-lambda-arn arn:aws:lambda:us-west-2:123456789012:function:SecretsManagerRotation \
  --rotation-rules AutomaticallyAfterDays=30
```

**Note:** Automatic rotation requires:
- Lambda function to perform rotation
- Coordination with the secret backend (e.g., database)
- See [AWS documentation](https://docs.aws.amazon.com/secretsmanager/latest/userguide/rotating-secrets.html)

### Monitoring and Auditing

#### CloudWatch Logs

Secrets Manager logs access attempts to CloudWatch:

```bash
# View secret access logs
aws logs tail /aws/secretsmanager/groupings --follow
```

#### CloudTrail

All Secrets Manager API calls are logged to CloudTrail:

```bash
# View CloudTrail events for Secrets Manager
aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=EventName,AttributeValue=GetSecretValue \
  --max-results 10 \
  --region $AWS_REGION
```

#### Cost Monitoring

```bash
# Secrets Manager pricing:
# - $0.40 per secret per month
# - $0.05 per 10,000 API calls

# Example cost for 2 secrets: ~$0.80/month

# List all secrets (for cost tracking)
aws secretsmanager list-secrets \
  --query 'SecretList[*].[Name,CreatedDate]' \
  --output table
```

---

## Secrets vs. Configuration Settings

This project distinguishes between **secrets** (sensitive credentials) and **settings** (configuration values):

| Category     | Properties                                                                                                                                                                                                  | Local Storage                                 | AWS Storage                             | Visibility                                     |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|-----------------------------------------|------------------------------------------------|
| **Secrets**  | `grouperClient.webService.password`, `jwt.secret.key`                                                                                                                                                       | Encrypted properties file (~/.yourname-conf/) | AWS Secrets Manager                     | Not logged, not visible in console             |
| **Settings** | `groupings.api.localhost.user`, `groupings.api.test.admin_user`, `grouperClient.webService.url`, `grouperClient.webService.login`, `email.is.enabled`, `email.send.recipient`, `properties.override.result` | Properties file                               | ECS task definition `environment` array | Visible in task definition, may appear in logs |

**Key Principle:** Only truly sensitive credentials (passwords, tokens, keys) belong in Secrets Manager. Configuration values should be passed as environment variables.

---

## Comparison: Local vs AWS

| Aspect               | Local Development            | AWS Production                          |
|----------------------|------------------------------|-----------------------------------------|
| **Secrets Storage**  | `~/.yourname-conf/` file     | AWS Secrets Manager                     |
| **Settings Storage** | Properties file with secrets | ECS task definition `environment` array |
| **Format**           | Spring properties            | Environment variables                   |
| **Encryption**       | None (local machine)         | AES-256 (at rest), TLS (in transit)     |
| **Access Control**   | File permissions             | IAM policies                            |
| **Rotation**         | Manual edit                  | Manual or automated                     |
| **Auditing**         | None                         | CloudTrail logs                         |
| **Use Case**         | Development/testing          | Staging/production                      |

---

## Migration Path

### From Local to AWS

When you're ready to deploy to AWS:

1. **Review your local properties file:**
   ```bash
   cat ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   ```

2. **Identify which properties are secrets:**
   - Secrets: `grouperClient.webService.password`, `jwt.secret.key`
   - Settings: all others

3. **Create AWS Secrets for sensitive properties:**
   ```bash
   # Only create secrets for sensitive credentials
   aws secretsmanager create-secret \
     --name groupings/api/grouper-login \
     --secret-string "your-grouper-service-account"
   
   aws secretsmanager create-secret \
     --name groupings/api/jwt-secret \
     --secret-string "$(openssl rand -base64 32)"
   ```

4. **Update ECS task definition** with configuration settings in the `environment` array:
   - `grouperClient.webService.url`
   - `grouperClient.webService.login`
   - `groupings.api.localhost.user`
   - `groupings.api.test.admin_user`
   - `email.is.enabled`
   - `email.send.recipient`
   - `properties.override.result`

5. **Deploy to ECS:**
   ```bash
   aws ecs update-service --force-new-deployment ...
   ```

6. **Verify secrets and settings are loading:**
   ```bash
   aws logs tail /ecs/uh-groupings-api --follow
   ```

### Environment-Specific Secrets

Use different secret names for each environment:

| Environment  | Secret Naming Pattern                    |
|--------------|------------------------------------------|
| Sandbox      | `groupings/sandbox/api/grouper-login`    |
| Test         | `groupings/test/api/grouper-login`       |
| Production   | `groupings/production/api/grouper-login` |

**Example:**
```bash
# Sandbox
aws secretsmanager create-secret \
  --name groupings/sandbox/api/grouper-login \
  --secret-string "sandbox-grouper-account"

# Production
aws secretsmanager create-secret \
  --name groupings/production/api/grouper-login \
  --secret-string "production-grouper-account"
```

Update task definitions accordingly for each environment.

---

## Troubleshooting

### Common Issues

#### Secret Not Found in ECS

**Error in logs:**
```
Error: Unable to fetch secret from AWS Secrets Manager
```

**Check:**
```bash
# 1. Verify secret exists
aws secretsmanager describe-secret \
  --secret-id groupings/api/grouper-login

# 2. Verify task execution role has permission
aws iam get-role-policy \
  --role-name uh-groupings-ecs-execution-sandbox \
  --policy-name SecretsManagerAccess

# 3. Check task definition references correct ARN
aws ecs describe-task-definition \
  --task-definition uh-groupings-api \
  --query 'taskDefinition.containerDefinitions[0].secrets'
```

#### Access Denied Error

**Error:**
```
AccessDeniedException: User is not authorized to perform: secretsmanager:GetSecretValue
```

**Solution:**
```bash
# Add permission to task execution role
aws iam put-role-policy \
  --role-name uh-groupings-ecs-execution-sandbox \
  --policy-name SecretsManagerAccess \
  --policy-document file://secrets-policy.json
```

#### Wrong Environment Variable Name

**Problem:** Secret or setting is present in ECS but Spring Boot cannot bind it

**Solution:** Verify environment variable naming matches your Spring Boot configuration:
- `grouperClient.webService.url` -> `GROUPERCLIENT_WEBSERVICE_URL`
- `groupings.api.test.admin_user` -> `GROUPINGS_API_TEST_ADMIN_USER`
- `jwt.secret.key` -> `JWT_SECRET_KEY`

### Verification Commands

```bash
# List all secrets
aws secretsmanager list-secrets --region us-west-2

# Get a secret value (BE CAREFUL - displays secret!)
aws secretsmanager get-secret-value \
  --secret-id groupings/api/grouper-login

# Check IAM permissions
aws iam simulate-principal-policy \
  --policy-source-arn arn:aws:iam::123456789012:role/uh-groupings-ecs-execution \
  --action-names secretsmanager:GetSecretValue \
  --resource-arns arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-login

# View CloudTrail logs for secret access
aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=EventName,AttributeValue=GetSecretValue
```

---

## Quick Start Workflow

```bash
# 1. Create properties file
mkdir -p ~/.$(whoami)-conf
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
# Add your configuration (see docker/README.md for format)

# 2. Start application (mounts overrides file read-only)
docker compose up
```

### AWS Production Workflow

```bash
# 1. Create only the two secrets
aws secretsmanager create-secret \
  --name groupings/api/grouper-login \
  --secret-string "your-grouper-service-account"

aws secretsmanager create-secret \
  --name groupings/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)"

# 2. Put non-secret settings in task definition environment[]
# 3. Deploy
aws ecs update-service --force-new-deployment ...

# 4. Verify
aws logs tail /ecs/uh-groupings-api --follow
```

---

## Related Documentation

- **[docker/README.md](../docker/README.md)** - Local development setup
- **[AWS_SETUP.md](./AWS_SETUP.md)** - AWS infrastructure setup
- **[AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md)** - Deployment procedures
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - System architecture

## External Resources

- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Docker Environment Variables](https://docs.docker.com/compose/environment-variables/)

---
