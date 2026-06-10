# Secrets Management Guide

This guide explains how to manage secrets and configuration for the UH Groupings API in both local development and AWS production environments.

## Overview

The application uses **two different approaches** for secrets management:

| Environment           | Method                   | Storage Location    | Injection Method    |
|-----------------------|--------------------------|---------------------|---------------------|
| **Local Development** | Properties file + Script | `~/.yourname-conf/` | Docker .env file    |
| **AWS Production**    | AWS Secrets Manager      | AWS Cloud           | ECS Task Definition |

**Never commit secrets to Git!** Both approaches keep secrets out of version control.

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
   grouper.api.url=https://grouper-dev.example.com/grouper-ws
   grouper.username=your-dev-username
   grouper.password=your-dev-password
   jwt.secret.key=your-local-jwt-secret
   ```

3. **Generate Docker environment file:**
   ```bash
   ./docker/dev-overrides-properties.sh
   ```

4. **Start the application:**
   ```bash
   docker-compose up
   ```

### How It Works

```
┌──────────────────────────────────────────┐
│ ~/.yourname-conf/                        │
│   uh-groupings-api-overrides.properties  │
│ (Spring Boot properties)                 │
└───────────┬──────────────────────────────┘
            │
            │ ./docker/dev-overrides-properties.sh
            │ (converts to env vars)
            ▼
┌──────────────────────────────────────────┐
│ docker/.env                              │
│ (Docker environment variables)           │
└───────────┬──────────────────────────────┘
            │
            │ docker-compose up
            │ (loads env vars)
            ▼
┌──────────────────────────────────────────┐
│ Docker Container                         │
│ (environment variables available)        │
└──────────────────────────────────────────┘
```

### Required Properties for Local Development

Create `~/.$(whoami)-conf/uh-groupings-api-overrides.properties` with:

```properties
# ============================================================================
# Grouper Configuration
# ============================================================================
grouper.api.url=https://grouper-dev.example.com/grouper-ws/servicesRest/json/v2_5_000
grouper.username=your-dev-username
grouper.password=your-dev-password

# ============================================================================
# JWT Configuration
# ============================================================================
jwt.secret.key=your-local-jwt-secret-for-development-only-change-in-production
jwt.expiration.ms=86400000

# ============================================================================
# Email Configuration (if needed)
# ============================================================================
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=noreply@example.com
spring.mail.password=your-mail-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ============================================================================
# Vault Configuration (for local Vault instance)
# ============================================================================
spring.cloud.vault.enabled=false
spring.cloud.vault.uri=http://localhost:8200
spring.cloud.vault.token=your-dev-vault-token

# ============================================================================
# Logging
# ============================================================================
logging.level.root=INFO
logging.level.edu.hawaii.its=DEBUG

# ============================================================================
# Actuator
# ============================================================================
management.endpoints.web.exposure.include=health,info,metrics
```

### Property Name Conversion

The `dev-overrides-properties.sh` script automatically converts Spring property names to environment variable names:

| Spring Property              | Environment Variable         |
|------------------------------|------------------------------|
| `grouper.api.url`            | `GROUPER_API_URL`            |
| `grouper.username`           | `GROUPER_USERNAME`           |
| `grouper.password`           | `GROUPER_PASSWORD`           |
| `spring.datasource.url`      | `SPRING_DATASOURCE_URL`      |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` |
| `jwt.secret.key`             | `JWT_SECRET_KEY`             |

**Conversion Rules:**
- Lowercase → UPPERCASE
- Dots (`.`) → Underscores (`_`)
- Hyphens (`-`) → Underscores (`_`)

### Security for Local Development

```bash
# Set restrictive permissions on your properties file
chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties

# The script automatically sets docker/.env to 600
```

**Best Practices:**
- ✅ Use separate credentials for local development
- ✅ Never use production credentials locally
- ✅ Keep properties file in your home directory
- ✅ Set file permissions to 600 (owner read/write only)
- ❌ Never commit docker/.env to Git (it's in .gitignore)
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

**Problem:** Docker Compose can't find .env
```bash
ERROR: Couldn't find env file: docker/.env
```

**Solution:**
```bash
# Run the conversion script
./docker/dev-overrides-properties.sh

# Verify it was created
ls -la docker/.env
```

**More help:** See [docker/README.md](../docker/README.md)

---

## AWS Production (Secrets Manager)

### Overview

In AWS, secrets are stored in **AWS Secrets Manager** and automatically injected into ECS containers at runtime. This provides:

- ✅ Encryption at rest and in transit
- ✅ Fine-grained access control (IAM)
- ✅ Audit logging (CloudTrail)
- ✅ Secret rotation capabilities
- ✅ No secrets in code or environment files

### Architecture

```
┌────────────────────────────┐
│  AWS Secrets Manager       │
│  (Encrypted secrets)       │
└──────────┬─────────────────┘
           │
           │ IAM Role grants access
           │
┌──────────▼─────────────────┐
│  ECS Task Execution Role   │
│  (Retrieves secrets)       │
└──────────┬─────────────────┘
           │
           │ Injects as environment variables
           │
┌──────────▼─────────────────┐
│  ECS Task (Container)      │
│  (Secrets available as     │
│   environment variables)   │
└────────────────────────────┘
```

### Required Secrets

Create these secrets in AWS Secrets Manager:

| Secret Name                      | Description             | Example Value                           |
|----------------------------------|-------------------------|-----------------------------------------|
| `groupings/api/grouper-url`      | Grouper API endpoint    | `https://grouper.hawaii.edu/grouper-ws` |
| `groupings/api/grouper-username` | Grouper service account | `uh-groupings-service`                  |
| `groupings/api/grouper-password` | Grouper password        | `<strong-password>`                     |
| `groupings/api/jwt-secret`       | JWT signing key         | `<random-32-byte-base64>`               |
| `groupings/api/db-password`      | Database password       | `<strong-password>`                     |
| `groupings/api/db-url`           | Database connection URL | `jdbc:postgresql://...`                 |
| `groupings/api/mail-password`    | SMTP password           | `<mail-password>`                       |

### Creating Secrets in AWS

#### Method 1: Using AWS CLI (Recommended)

```bash
# Set your environment
export AWS_REGION="us-west-2"

# Create Grouper configuration secrets
aws secretsmanager create-secret \
  --name groupings/api/grouper-url \
  --description "Grouper API endpoint URL" \
  --secret-string "https://grouper.hawaii.edu/grouper-ws/servicesRest/json/v2_5_000" \
  --region $AWS_REGION

aws secretsmanager create-secret \
  --name groupings/api/grouper-username \
  --description "Grouper service account username" \
  --secret-string "uh-groupings-production" \
  --region $AWS_REGION

aws secretsmanager create-secret \
  --name groupings/api/grouper-password \
  --description "Grouper service account password" \
  --secret-string "YOUR_STRONG_PASSWORD_HERE" \
  --region $AWS_REGION

# Generate and create JWT secret
JWT_SECRET=$(openssl rand -base64 32)
aws secretsmanager create-secret \
  --name groupings/api/jwt-secret \
  --description "JWT signing key for token generation" \
  --secret-string "$JWT_SECRET" \
  --region $AWS_REGION

# Create database secrets
aws secretsmanager create-secret \
  --name groupings/api/db-url \
  --description "Database connection URL" \
  --secret-string "jdbc:postgresql://your-rds-endpoint:5432/groupings" \
  --region $AWS_REGION

aws secretsmanager create-secret \
  --name groupings/api/db-password \
  --description "Database password" \
  --secret-string "YOUR_DB_PASSWORD_HERE" \
  --region $AWS_REGION

# Create email configuration secret
aws secretsmanager create-secret \
  --name groupings/api/mail-password \
  --description "SMTP password for email notifications" \
  --secret-string "YOUR_MAIL_PASSWORD_HERE" \
  --region $AWS_REGION
```

#### Method 2: Using AWS Console

1. Go to [AWS Secrets Manager Console](https://console.aws.amazon.com/secretsmanager/)
2. Click **"Store a new secret"**
3. Select **"Other type of secret"**
4. Choose **"Plaintext"** tab
5. Enter your secret value
6. Click **"Next"**
7. Enter secret name (e.g., `groupings/api/grouper-password`)
8. Add description
9. Click **"Next"** through remaining screens
10. Click **"Store"**

### Updating Secrets

```bash
# Update an existing secret
aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-password \
  --secret-string "NEW_PASSWORD_HERE" \
  --region $AWS_REGION
```

### Retrieving Secrets (for verification)

```bash
# Get a secret value
aws secretsmanager get-secret-value \
  --secret-id groupings/api/grouper-url \
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

The secrets are referenced in `aws/task-definition.json`:

```json
{
  "containerDefinitions": [
    {
      "name": "uh-groupings-api",
      "secrets": [
        {
          "name": "GROUPER_API_URL",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-url"
        },
        {
          "name": "GROUPER_USERNAME",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-username"
        },
        {
          "name": "GROUPER_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-password"
        }
      ]
    }
  ]
}
```

**ECS automatically:**
1. Retrieves secrets from Secrets Manager
2. Injects them as environment variables
3. Makes them available to your application

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
# Update the secret with a new value
aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-password \
  --secret-string "NEW_PASSWORD_HERE"

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
  --secret-id groupings/api/db-password \
  --rotation-lambda-arn arn:aws:lambda:us-west-2:123456789012:function:SecretsManagerRotation \
  --rotation-rules AutomaticallyAfterDays=30
```

**Note:** Automatic rotation requires:
- Lambda function to perform rotation
- Coordination with the secret backend (e.g., database)
- See [AWS documentation](https://docs.aws.amazon.com/secretsmanager/latest/userguide/rotating-secrets.html)

### Security Best Practices

#### ✅ DO

- ✅ Use unique, strong passwords for each secret
- ✅ Generate JWT secrets with: `openssl rand -base64 32`
- ✅ Use different secrets for each environment (sandbox, test, production)
- ✅ Rotate secrets regularly (at least annually)
- ✅ Use least-privilege IAM policies
- ✅ Enable CloudTrail logging for audit
- ✅ Tag secrets with environment and application
- ✅ Use secret versioning for safe rotation
- ✅ Document secret rotation procedures

#### ❌ DON'T

- ❌ Never commit secrets to Git
- ❌ Never log secret values
- ❌ Never share secrets via email or Slack
- ❌ Never use the same secrets across environments
- ❌ Never use weak or default passwords
- ❌ Never bypass IAM access controls
- ❌ Never store secrets in EC2 user data or CloudFormation templates
- ❌ Never use production secrets in development

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

# Example cost for 7 secrets: ~$2.80/month

# List all secrets (for cost tracking)
aws secretsmanager list-secrets \
  --query 'SecretList[*].[Name,CreatedDate]' \
  --output table
```

---

## Comparison: Local vs AWS

| Aspect             | Local Development        | AWS Production                      |
|--------------------|--------------------------|-------------------------------------|
| **Storage**        | `~/.yourname-conf/` file | AWS Secrets Manager                 |
| **Format**         | Spring properties        | Key-value pairs                     |
| **Encryption**     | None (local machine)     | AES-256 (at rest), TLS (in transit) |
| **Access Control** | File permissions         | IAM policies                        |
| **Rotation**       | Manual edit              | Manual or automated                 |
| **Injection**      | Docker .env file         | ECS task definition                 |
| **Auditing**       | None                     | CloudTrail logs                     |
| **Cost**           | Free                     | ~$0.40/secret/month                 |
| **Use Case**       | Development/testing      | Staging/production                  |

---

## Migration Path

### From Local to AWS

When you're ready to deploy to AWS:

1. **Review your local properties:**
   ```bash
   cat ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   ```

2. **Create corresponding AWS secrets:**
   ```bash
   # For each property, create a secret
   # Example: grouper.api.url → groupings/api/grouper-url
   ```

3. **Update task definition** (already configured in `aws/task-definition.json`)

4. **Deploy to ECS:**
   ```bash
   aws ecs update-service --force-new-deployment ...
   ```

5. **Verify secrets are loading:**
   ```bash
   aws logs tail /ecs/uh-groupings-api --follow
   ```

### Environment-Specific Secrets

Use different secret names for each environment:

| Environment  | Secret Naming Pattern        |
|--------------|------------------------------|
| Sandbox      | `groupings/sandbox/api/*`    |
| Test         | `groupings/test/api/*`       |
| Production   | `groupings/production/api/*` |

**Example:**
```bash
# Sandbox
aws secretsmanager create-secret \
  --name groupings/sandbox/api/grouper-password \
  --secret-string "sandbox-password"

# Production
aws secretsmanager create-secret \
  --name groupings/production/api/grouper-password \
  --secret-string "strong-production-password"
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
  --secret-id groupings/api/grouper-password

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

**Problem:** Secret loads but application can't find it

**Solution:** Verify environment variable naming matches your Spring Boot configuration:
- Task definition: `GROUPER_API_URL`
- Spring Boot expects: `GROUPER_API_URL` or `grouper.api.url`

### Verification Commands

```bash
# List all secrets
aws secretsmanager list-secrets --region us-west-2

# Get a secret value (BE CAREFUL - displays secret!)
aws secretsmanager get-secret-value \
  --secret-id groupings/api/grouper-url

# Check IAM permissions
aws iam simulate-principal-policy \
  --policy-source-arn arn:aws:iam::123456789012:role/uh-groupings-ecs-execution \
  --action-names secretsmanager:GetSecretValue \
  --resource-arns arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-password

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

# 2. Generate Docker .env file
./docker/dev-overrides-properties.sh

# 3. Start application
docker-compose up
```

### AWS Production Workflow

```bash
# 1. Create secrets
aws secretsmanager create-secret \
  --name groupings/api/grouper-password \
  --secret-string "YOUR_PASSWORD"

# 2. Update task definition (already configured)
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
