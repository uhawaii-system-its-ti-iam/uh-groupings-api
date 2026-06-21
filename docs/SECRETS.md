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

| Environment           | Storage                                                                               | How it reaches the app                                                                                                          |
|-----------------------|---------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| **Local development** | `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`                             | Bind-mounted into the Docker container (read-only) and loaded via `SPRING_CONFIG_IMPORT`.                                       |
| **AWS deployment**    | AWS Secrets Manager: `groupings/api/grouper-password` and `groupings/api/jwt-secret`. | Injected into the ECS task as environment variables via the task definition's `secrets[]` array (decrypted at container start). |

The Spring property names are identical in both environments; only the source mechanism differs.

`aws/.env` is **not** a secrets store. It only carries non-sensitive deployment parameters for `setup.sh`. The setup script prompts for the actual secret values at runtime and writes them straight to Secrets Manager.

### Local development setup

1. Create the overrides file (note that the project provides a template):
   ```bash
   mkdir -p ~/.$(whoami)-conf
   nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   ```
2. Populate it. The file is never committed.
3. Start the app:
   ```bash
   docker-compose up
   ```

### AWS deployment

`aws/setup.sh` (invoked via `aws-vault exec uh-groupings -- make aws-setup`) creates exactly two AWS Secrets Manager entries:

- `groupings/api/grouper-password` — prompted at runtime
- `groupings/api/jwt-secret` — auto-generated via `openssl rand -base64 32`

The deployed ECS task definition references these via `secrets[]`. Non-secret values come from the task definition's `environment[]` array.

For the technical specifics — task definition wiring, IAM permissions, manual CLI commands for inspection or rotation — see **[AWS_SETUP.md → Secrets Manager Integration](AWS_SETUP.md#secrets-manager-integration)**.

### JWT key ownership

The API project owns `jwt.secret.key`. `aws/setup.sh` generates it once at provisioning time. Companion projects (Angular UI, React UI) do **not** generate their own key — they read the same `groupings/api/jwt-secret` so all three services share an identical signing key.

Implications:

- **Single source of truth:** only the API setup creates or rotates this key.
- **Shared read access:** the UI projects' task execution roles must be granted `secretsmanager:GetSecretValue` on `groupings/api/jwt-secret`.
- **Coordinated rotation:** rotating the key requires redeploying every consumer so they pick up the new value together. Rotating in only one place breaks token validation across the boundary.
- **No duplication:** never create a second JWT secret for the UI; a divergent key would cause every cross-service token check to fail.

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
| ECS task definition wiring, IAM permissions, manual CLI commands for secrets | [AWS_SETUP.md → Secrets Manager Integration](AWS_SETUP.md#secrets-manager-integration) |
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
