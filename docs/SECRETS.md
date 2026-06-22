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
- `groupings/api/jwt-secret` — generated by the script via `openssl rand -base64 32`. The API project owns this value; the companion Angular and React UI projects reference the same Secrets Manager entry from their own task definitions. Re-running `make aws-setup` preserves an existing JWT secret to avoid invalidating active UI sessions; rotation goes through the explicit CLI command in [AWS_SETUP.md → Rotate the JWT key](AWS_SETUP.md#rotate-the-jwt-key).

The deployed ECS task definition references both secrets via `secrets[]`. Non-secret values come from the task definition's `environment[]` array.

For the technical specifics — task definition wiring, IAM permissions, manual CLI commands for inspection or rotation — see **[AWS_SETUP.md → Secrets Manager Integration](AWS_SETUP.md#secrets-manager-integration)**.

### JWT key ownership

The API project owns `jwt.secret.key`. `aws/setup.sh` generates it once (the first time setup runs against an environment) via `openssl rand -base64 32` and stores it as `groupings/api/jwt-secret`. Companion projects (Angular UI, React UI) do **not** generate their own key — their task definitions reference the same Secrets Manager entry, so all three services in a given AWS environment share an identical signing key.

Implications:

- **Single source of truth on AWS:** the API's setup script is the only thing that writes `groupings/api/jwt-secret`. The UI projects only read it. Re-running `make aws-setup` preserves the existing value to avoid silently invalidating UI tokens.
- **Shared read access:** the UI projects' task execution roles must be granted `secretsmanager:GetSecretValue` on `groupings/api/jwt-secret`.
- **Coordinated rotation:** rotating the key is a deliberate, multi-service operation. Use the manual CLI command in [AWS_SETUP.md → Rotate the JWT key](AWS_SETUP.md#rotate-the-jwt-key) and redeploy every consumer at the same time. Rotating in only one place breaks token validation across the boundary.
- **No duplication:** never create a second JWT secret for the UI; a divergent key would cause every cross-service token check to fail.
- **Local vs AWS are independent:** the local overrides file's `jwt.secret.key` is whatever the developer chose for local dev. The AWS value is generated by `setup.sh` and lives only in AWS. The script does not copy one to the other.

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
