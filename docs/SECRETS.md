# Secrets Overview

This document is the high-level map of where secrets live for the UH Groupings Spring API. It explains the model, classifies which configuration values are secrets, and points at the docs that hold the operational specifics.

If you want to do something concrete (provision AWS, rotate a key, set up local dev), follow the cross-references near the bottom.

<!-- TOC -->
* [Secrets Overview](#secrets-overview)
  * [Two Categories of Secrets](#two-categories-of-secrets)
  * [Application Secrets](#application-secrets)
    * [Which Spring properties are secrets?](#which-spring-properties-are-secrets)
    * [Storage by environment](#storage-by-environment)
    * [Local development setup](#local-development-setup)
    * [AWS deployment](#aws-deployment)
    * [JWT key ownership](#jwt-key-ownership)
  * [Secrets Manager Integration](#secrets-manager-integration)
    * [ECS task definition wiring](#ecs-task-definition-wiring)
    * [IAM permissions](#iam-permissions)
    * [Manual operations](#manual-operations)
    * [Auditing](#auditing)
    * [Cost](#cost)
    * [Automated rotation (advanced)](#automated-rotation-advanced)
  * [AWS Account Credentials (developer-side)](#aws-account-credentials-developer-side)
  * [Property Reference](#property-reference)
  * [Comparison: Local vs AWS](#comparison-local-vs-aws)
  * [Cross-References](#cross-references)
  * [External Resources](#external-resources)
<!-- TOC -->

---

## Two Categories of Secrets

The project handles two distinct categories of credentials. They are stored, accessed, and rotated by different mechanisms.

| Category                    | What it is                                                                                         | Who needs it                                                    | Where it lives                                                                                       |
|-----------------------------|----------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| **Application secrets**     | Values the running API needs at startup: a Grouper service-account password and a JWT signing key. | The API itself, every time it starts.                           | **Local dev:** the developer's overrides properties file. **AWS:** AWS Secrets Manager.              |
| **AWS account credentials** | An IAM access key + secret that lets a *developer* call AWS APIs.                                  | Only developers who run `make aws-setup`, `make aws-logs`, etc. | The developer's operating-system keychain via [`aws-vault`](https://github.com/99designs/aws-vault). |

These categories serve different purposes:

- Application secrets are *consumed by the deployed app*.
- AWS account credentials are used to *deploy* the app.

They never mix. aws-vault does not hold application secrets; AWS Secrets Manager does not hold IAM access keys.

---

## Application Secrets

### Which Spring properties are secrets?

Only two:

| Spring property                     | Why it's a secret                                                                                          |
|-------------------------------------|------------------------------------------------------------------------------------------------------------|
| `grouperClient.webService.password` | Grants the API write access to the Grouper service account.                                                |
| `jwt.secret.key`                    | Signs JWTs that authenticate every API request. Sharing or losing this key invalidates the trust boundary. |

Everything else (`grouperClient.webService.url`, `grouperClient.webService.login`, email flags, validation regexes, etc.) is non-secret configuration. It can sit in the developer's overrides file locally and in the ECS task definition `environment[]` array on AWS.

### Storage by environment

| Environment           | What stores the secrets                                                              | How they reach the running API                                                                                                  | What happens if a secret is missing                                                                                                                                            |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Local development** | `~/.$(whoami)-conf/uh-groupings-api-overrides.properties` (the developer edits it)   | Bind-mounted into the Docker container (read-only) and imported via `SPRING_CONFIG_IMPORT` at startup.                          | `jwt.secret.key` missing → Spring fails to start with `Could not resolve placeholder 'jwt.secret.key'`. Grouper password missing → the app boots, but every Grouper call fails authentication. |
| **AWS deployment**    | AWS Secrets Manager: `groupings/api/grouper-password` and `groupings/api/jwt-secret` | Injected into the ECS task as environment variables via the task definition's `secrets[]` array (decrypted at container start). | ECS cannot resolve the referenced secret → the task fails to start; CloudWatch records a `ResourceInitializationError`.                                                        |

The Spring property names are identical in both environments; only the source mechanism differs.

`aws/.env` is **not** a secrets store. It only carries non-sensitive deployment parameters for `setup.sh`. Application secrets reach AWS through `aws/setup.sh`: the Grouper password is read from the developer's overrides file and the JWT key is generated locally by `openssl`, then both are written to Secrets Manager.

#### Provisioning vs runtime

It's easy to confuse "where the secrets live" with "how they get there". Two different steps:

| Step             | Local development                                                                                              | AWS deployment                                                                                                                                                                                                                                                                  |
|------------------|----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Provisioning** | Developer hand-edits the overrides file once. There is no project script that prompts for or generates values. | `make aws-setup` runs `aws/setup.sh`, which **reads `grouperClient.webService.password` from the developer's overrides file** and **generates a fresh `jwt.secret.key` via `openssl rand -base64 32`**, then writes both to AWS Secrets Manager. The script never prompts.       |
| **Runtime**      | Spring imports the overrides file at boot via `spring.config.import`.                                          | ECS resolves each `secrets[]` entry against Secrets Manager and exposes the value as an environment variable inside the container.                                                                                                                                              |

The two AWS secrets have different origins:

- `groupings/api/grouper-password` mirrors the developer's overrides file. The Grouper service-account password is owned by an upstream identity provider, so the overrides file is its canonical source. Re-running setup re-writes it.
- `groupings/api/jwt-secret` is generated by the API's setup script. The companion Angular and React UI projects do not generate their own — they read this same Secrets Manager entry. Re-running `make aws-setup` preserves the existing JWT secret on purpose; rotating it without redeploying every UI consumer would break cross-service token validation.

If `grouperClient.webService.password` is missing or blank in the overrides file, `aws/setup.sh` exits with an error before any AWS API call.

### Local development setup

The overrides file is the **single source** of application secrets when running locally — there is no fallback, no auto-generation, no environment-variable lookup. Spring imports the file at startup; the API uses whatever is in it.

At minimum the file must contain:

| Property                            | Notes                                                                                                                                                                                                  |
|-------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `jwt.secret.key`                    | JWT signing key. **Required at startup.** `JwtService` reads it via `@Value("${jwt.secret.key}")` with no default, so omitting it stops Spring from starting. Generate one with `openssl rand -base64 32`. |
| `grouperClient.webService.password` | Grouper service-account password. Declared with an empty default, so the app will boot without it, but every Grouper call will fail authentication. Treat it as required.                              |

In practice the same file also carries non-secret settings the developer needs to override locally (Grouper URL, username, email flags, etc.). The full template — secrets and settings together — is in [DEV_QUICKSTART.md → Create Configuration File](DEV_QUICKSTART.md#2-create-configuration-file).

To set up:

1. Create the file:
   ```bash
   mkdir -p ~/.$(whoami)-conf
   nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   ```
2. Paste the template from DEV_QUICKSTART.md and fill in real values. The file is never committed.
3. Start the app:
   ```bash
   docker-compose up
   ```

`docker-compose.yml` bind-mounts the file read-only at `/app/config/uh-groupings-api-overrides.properties`. If the source path doesn't exist, the mount fails and the container won't start — that's the first symptom of a missing overrides file.

### AWS deployment

`aws/setup.sh` (invoked via `aws-vault exec uh-groupings -- make aws-setup`) creates the two AWS Secrets Manager entries from two different sources:

- `groupings/api/grouper-password` — read from `grouperClient.webService.password` in the developer's overrides file. The overrides file is bind-mounted read-only into the AWS CLI container at `/overrides/uh-groupings-api-overrides.properties` (see `aws/docker-compose.aws.yml`). If the file is missing or the property is blank, `setup.sh` exits before making any AWS API call.
- `groupings/api/jwt-secret` — generated by the script via `openssl rand -base64 32`. The API project owns this value; the companion Angular and React UI projects reference the same Secrets Manager entry from their own task definitions. Re-running `make aws-setup` preserves an existing JWT secret to avoid invalidating active UI sessions; rotation goes through the explicit CLI command in [Rotate the JWT key](#rotate-the-jwt-key).

The deployed ECS task definition references both secrets via `secrets[]`. Non-secret values come from the task definition's `environment[]` array.

For the technical specifics — task definition wiring, IAM permissions, manual CLI commands for inspection or rotation — see **[Secrets Manager Integration](#secrets-manager-integration)** below.

### JWT key ownership

The API project owns `jwt.secret.key`. `aws/setup.sh` generates it once (the first time setup runs against an environment) via `openssl rand -base64 32` and stores it as `groupings/api/jwt-secret`. Companion projects (Angular UI, React UI) do **not** generate their own key — their task definitions reference the same Secrets Manager entry, so all three services in a given AWS environment share an identical signing key.

Implications:

- **Single source of truth on AWS:** the API's setup script is the only thing that writes `groupings/api/jwt-secret`. The UI projects only read it. Re-running `make aws-setup` preserves the existing value to avoid silently invalidating UI tokens.
- **Shared read access:** the UI projects' task execution roles must be granted `secretsmanager:GetSecretValue` on `groupings/api/jwt-secret`.
- **Coordinated rotation:** rotating the key is a deliberate, multi-service operation. Use the manual CLI command in [Rotate the JWT key](#rotate-the-jwt-key) and redeploy every consumer at the same time. Rotating in only one place breaks token validation across the boundary.
- **No duplication:** never create a second JWT secret for the UI; a divergent key would cause every cross-service token check to fail.
- **Local vs AWS are independent:** the local overrides file's `jwt.secret.key` is whatever the developer chose for local dev. The AWS value is generated by `setup.sh` and lives only in AWS. The script does not copy one to the other.

---

## Secrets Manager Integration

This section is the technical reference for how the two AWS Secrets Manager entries (`groupings/api/grouper-password` and `groupings/api/jwt-secret`) are wired into the deployed ECS task. For the conceptual overview — what's a secret, where it lives, who provisions it — see [Application Secrets](#application-secrets) above.

### ECS task definition wiring

`aws/task-definition.json` (and the equivalent CloudFormation in `aws/cloudformation/ecs-cluster.yml`) splits values into two arrays — `secrets[]` for sensitive values pulled from Secrets Manager, `environment[]` for everything else:

```json
{
  "containerDefinitions": [
    {
      "name": "uh-groupings-api",
      "secrets": [
        {
          "name": "GROUPERCLIENT_WEBSERVICE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/grouper-password"
        },
        {
          "name": "JWT_SECRET_KEY",
          "valueFrom": "arn:aws:secretsmanager:us-west-2:123456789012:secret:groupings/api/jwt-secret"
        }
      ],
      "environment": [
        { "name": "GROUPERCLIENT_WEBSERVICE_URL",   "value": "https://grouper-prod.its.hawaii.edu/grouper-ws/servicesRest/" },
        { "name": "GROUPERCLIENT_WEBSERVICE_LOGIN", "value": "_groupings_api_2" },
        { "name": "GROUPINGS_API_LOCALHOST_USER",   "value": "service_account_user" },
        { "name": "GROUPINGS_API_TEST_ADMIN_USER",  "value": "service_account_user" },
        { "name": "EMAIL_IS_ENABLED",               "value": "false" },
        { "name": "EMAIL_SEND_RECIPIENT",           "value": "groupings-alerts@hawaii.edu" },
        { "name": "PROPERTIES_OVERRIDE_RESULT",     "value": "OVERRIDDEN" }
      ]
    }
  ]
}
```

How each array behaves at runtime:

- **`secrets[]`** — ECS calls Secrets Manager at container startup, decrypts each value, and exposes it as the named environment variable inside the container. The plaintext never appears in the task definition, the ECS console, or CloudWatch logs.
- **`environment[]`** — values are baked into the task definition as plain text. They appear in the ECS console and may show up in logs if the application echoes them. Use this only for non-sensitive settings.

The Spring application binds environment variables to property names automatically — `GROUPERCLIENT_WEBSERVICE_PASSWORD` becomes `grouperClient.webService.password`, etc.

### IAM permissions

The ECS task **execution** role needs read access to the two secrets so ECS can fetch them at container start. The CloudFormation in `aws/cloudformation/ecs-cluster.yml` creates a role named `${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-role-ecs-execution` (e.g., `mhodges-groupings-api-sandbx-role-ecs-execution`) with the following inline policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [ "secretsmanager:GetSecretValue" ],
      "Resource": [
        "arn:aws:secretsmanager:us-west-2:*:secret:groupings/api/*"
      ]
    }
  ]
}
```

The wildcard `groupings/api/*` covers both current secrets and any future ones added under the same prefix without requiring an IAM policy update.

### Manual operations

These commands are for ad-hoc work (rotation, inspection). All run inside the AWS CLI Docker container with `aws-vault` providing credentials.

#### Update a secret

```bash
aws-vault exec uh-groupings -- aws secretsmanager update-secret \
  --secret-id groupings/api/grouper-password \
  --secret-string "NEW_GROUPER_PASSWORD" \
  --region us-west-2

# Force ECS to restart tasks with the new value
source aws/.env
CLUSTER="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-cluster"
SERVICE="${AWS_OWNER}-${AWS_PROJECT_ID}-${AWS_ENV}-service"
aws-vault exec uh-groupings -- aws ecs update-service \
  --cluster "${CLUSTER}" --service "${SERVICE}" --force-new-deployment
```

#### Rotate the JWT key

The JWT key is shared between the API and any UI consumer. Rotation requires redeploying every consumer to pick up the new value at the same time; otherwise tokens issued by one and validated by the other will fail.

```bash
aws-vault exec uh-groupings -- aws secretsmanager update-secret \
  --secret-id groupings/api/jwt-secret \
  --secret-string "$(openssl rand -base64 32)" \
  --region us-west-2
```

After updating, redeploy the API and every UI service.

#### Inspect a secret value (carefully — prints in plaintext)

```bash
aws-vault exec uh-groupings -- aws secretsmanager get-secret-value \
  --secret-id groupings/api/grouper-password \
  --query SecretString \
  --output text \
  --region us-west-2
```

#### List the project's secrets

```bash
aws-vault exec uh-groupings -- aws secretsmanager list-secrets \
  --filters Key=name,Values=groupings/ \
  --query 'SecretList[*].[Name,CreatedDate]' \
  --output table \
  --region us-west-2
```

### Auditing

CloudTrail records every Secrets Manager API call. To view recent `GetSecretValue` events:

```bash
aws-vault exec uh-groupings -- aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=EventName,AttributeValue=GetSecretValue \
  --max-results 10 \
  --region us-west-2
```

### Cost

AWS Secrets Manager pricing (as of writing): $0.40 per secret per month + $0.05 per 10,000 API calls. With two secrets, the project pays roughly $0.80/month for secret storage. ECS retrieves each secret once at task start, so retrieval costs are negligible.

### Automated rotation (advanced)

AWS Secrets Manager supports Lambda-driven automatic rotation. This project does not currently use it because:

- The Grouper password is owned by an upstream team's identity provider, not by AWS, so rotation must be coordinated externally.
- The JWT key is shared across the API and UI services; rotation requires a coordinated multi-service redeploy.

If automated rotation becomes appropriate later, the entry point is `aws secretsmanager rotate-secret --rotation-lambda-arn ... --rotation-rules AutomaticallyAfterDays=30`. See AWS's [rotating secrets](https://docs.aws.amazon.com/secretsmanager/latest/userguide/rotating-secrets.html) documentation.

---

## AWS Account Credentials (developer-side)

A developer who runs any `make aws-*` target must authenticate to AWS. The project handles this via aws-vault, which stores IAM access keys in the operating system's keychain rather than in `~/.aws/credentials` or environment files.

One-time bootstrap:

```bash
make aws-vault-setup
```

That target installs aws-vault if necessary (Homebrew on macOS) and stores your IAM Access Key ID and Secret Access Key under a profile named `uh-groupings`. See the [aws/README.md](../aws/README.md) for the script's behavior and alternatives.

Every subsequent AWS command is wrapped:

```bash
aws-vault exec uh-groupings -- make aws-setup
```

aws-vault releases the credentials only as ephemeral environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`) for the duration of one command, then they vanish. Nothing on disk in plaintext, nothing in shell history, nothing inherited by future shells.

aws-vault holds **no application secrets**. The CLI inside the AWS-cli Docker container reads the three `AWS_*` variables from its environment to authenticate; it does not read any `~/.aws` files (the bind mount was removed).

---

## Property Reference

| Spring property                     | Type       | Local storage  | AWS storage                                        |
|-------------------------------------|------------|----------------|----------------------------------------------------|
| `grouperClient.webService.password` | **Secret** | overrides file | Secrets Manager (`groupings/api/grouper-password`) |
| `jwt.secret.key`                    | **Secret** | overrides file | Secrets Manager (`groupings/api/jwt-secret`)       |
| `grouperClient.webService.url`      | Setting    | overrides file | ECS task definition `environment[]`                |
| `grouperClient.webService.login`    | Setting    | overrides file | ECS task definition `environment[]`                |
| `groupings.api.localhost.user`      | Setting    | overrides file | ECS task definition `environment[]`                |
| `groupings.api.test.admin_user`     | Setting    | overrides file | ECS task definition `environment[]`                |
| `email.is.enabled`                  | Setting    | overrides file | ECS task definition `environment[]`                |
| `email.send.recipient`              | Setting    | overrides file | ECS task definition `environment[]`                |
| `properties.override.result`        | Setting    | overrides file | ECS task definition `environment[]`                |

**Key principle:** only sensitive credentials belong in Secrets Manager. Configuration values flow through plain environment variables.

---

## Comparison: Local vs AWS

| Aspect             | Local development                                   | AWS deployment                                                                                                    |
|--------------------|-----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| Secret storage     | `~/.$(whoami)-conf/...` properties file             | AWS Secrets Manager                                                                                               |
| Setting storage    | Same properties file                                | ECS task definition `environment[]`                                                                               |
| Format             | Spring properties                                   | Environment variables (Spring binds them automatically)                                                           |
| Encryption at rest | Filesystem permissions (`chmod 600`); no encryption | AES-256 (Secrets Manager)                                                                                         |
| Access control     | File permissions                                    | IAM policies on the ECS task execution role                                                                       |
| Rotation           | Edit the file, restart the container                | `make aws-setup` re-runs the create-or-update for each secret, or use `aws secretsmanager update-secret` directly |
| Auditing           | None                                                | CloudTrail logs every `GetSecretValue` call                                                                       |
| Use case           | Development and testing                             | Sandbox / dev / test / prod                                                                                       |

---

## Cross-References

For specifics, follow the doc that owns each topic:

| Topic                                                                        | Where to look                                                                          |
|------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| Initial AWS infrastructure provisioning                                      | [AWS_QUICKSTART.md](AWS_QUICKSTART.md)                                                 |
| ECS task definition wiring, IAM permissions, manual CLI commands for secrets | [Secrets Manager Integration](#secrets-manager-integration) (this doc)                |
| Ongoing AWS operations (deploys, rollback, scaling)                          | [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md)                                                 |
| aws-vault details (install, profiles, alternatives)                          | [aws/README.md](../aws/README.md)                                                      |
| Local Docker development                                                     | [DEV_QUICKSTART.md](DEV_QUICKSTART.md) and [DEV_README.md](DEV_README.md)              |
| Resource naming (why `AWS_PROJECT_ID=groupings-api`)                               | [AWS_NAMING_CONVENTIONS.md](AWS_NAMING_CONVENTIONS.md)                                 |
| Architecture overview                                                        | [ARCHITECTURE.md](ARCHITECTURE.md)                                                     |
| Project conventions for engineers and agents                                 | [AGENTS.md](../AGENTS.md)                                                              |

---

## External Resources

- [AWS Secrets Manager documentation](https://docs.aws.amazon.com/secretsmanager/)
- [`aws-vault` project](https://github.com/99designs/aws-vault)
- [Spring Boot externalized configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
