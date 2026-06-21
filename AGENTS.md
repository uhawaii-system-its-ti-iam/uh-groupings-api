# AGENTS.md — UH Groupings API

<!-- TOC -->
* [AGENTS.md — UH Groupings API](#agentsmd--uh-groupings-api)
  * [What This Project Does](#what-this-project-does)
  * [Architecture Overview](#architecture-overview)
  * [Critical Patterns](#critical-patterns)
    * [Command/Wrapper Pattern (wrapper/)](#commandwrapper-pattern-wrapper)
    * [UH Identifier Handling](#uh-identifier-handling)
    * [Grouping Path Structure](#grouping-path-structure)
    * [Swappable GrouperService via Profile](#swappable-grouperservice-via-profile)
    * [Authentication](#authentication)
    * [Async Operations](#async-operations)
  * [Package Guide](#package-guide)
  * [Build & Test Commands](#build--test-commands)
  * [Spring Profiles](#spring-profiles)
  * [Test Naming Convention](#test-naming-convention)
  * [Key Config Properties (`application.properties`)](#key-config-properties-applicationproperties)
  * [Secrets Strategy](#secrets-strategy)
    * [Application secrets (used by the running API)](#application-secrets-used-by-the-running-api)
    * [AWS account credentials (used by developers who deploy)](#aws-account-credentials-used-by-developers-who-deploy)
  * [Adding a New Grouper Operation](#adding-a-new-grouper-operation)
<!-- TOC -->

## What This Project Does
A Spring Boot (Java 21) REST API that serves as middleware between the UH Groupings UI and the Internet2 **Grouper** enterprise access management system. It manages UH group memberships (basis, include, exclude, owners sub-groups) and exposes them through a JWT-secured REST API at `/api/groupings/v2.1`.

## Architecture Overview

```
UI → GroupingsRestControllerv2_1
        └──→ Service Layer 
                └──→ GrouperService (interface)
                        └──→ GrouperApiService    (registered by `GrouperPropertyConfigurer` when `grouping.api.server.type=GROUPER`)
                                   ↓
                             ExecutorService (retry logic)
                                   ↓
                             GrouperCommand subclasses (wrapper/)
                                   ↓
                             Grouper WS Client (GrouperClient library)
```

**Two-layer response model:**
- `src/.../wrapper/` — thin wrappers around raw Grouper `Ws*` beans (e.g., `HasMembersResults`, `SubjectsResults`)
- `src/.../groupings/` — API-facing DTOs returned to the UI (e.g., `GroupingAddResult`, `MembershipResults`)

## Critical Patterns

### Command/Wrapper Pattern (wrapper/)
All Grouper calls use a Command pattern. `GrouperCommand<T>` is the abstract base; `ExecutorService.execute()` runs the command with up to 2 retries (1 s delay).

```java
// GrouperApiService delegates to ExecutorService:
exec.execute(new HasMembersCommand()
    .assignGroupPath(groupPath)
    .addUhIdentifier(uhIdentifier));
```

Never call Grouper WS beans directly — always go through a `*Command` class in `wrapper/`.

### UH Identifier Handling
A UH identifier is either an 8-digit numeric UUID (`^\\d{8}$`) or a string username. `GrouperCommand.isUhUuid()` determines which Grouper lookup field to populate. This distinction must be preserved everywhere.

### Grouping Path Structure
Every grouping path has four sub-groups: `{path}:basis`, `{path}:include`, `{path}:exclude`, `{path}:owners`. `GroupPathService` provides helpers (`getIncludeGroup()`, `getExcludeGroup()`, etc.). Path validation regex: `[\\w-:.]+` (max 255 chars).

### Swappable GrouperService via Profile
`GrouperPropertyConfigurer` registers the `grouperService` bean as `GrouperApiService` when `grouping.api.server.type=GROUPER` (default). Spring profiles such as `localhost`, `localTest`, `integrationTest`, and `dockerhost` only change environment/configuration details; there is no in-repo OOTB service implementation.

### Authentication
Stateless JWT. `JwtAuthenticationFilter` populates `SecurityContextHolder` before every request. Only `/v3/api-docs/**`, `/swagger-ui/**`, and `/api/groupings/v2.1/announcements/**` are public. Use `SecurityContextRoleService` (not a Grouper call) to check the current user's admin/owner role within service methods.

### Async Operations
`UpdateMemberService` uses `@Async` for add/remove operations. `AsyncJobsManager` tracks in-flight jobs. The controller returns a job ID immediately; callers poll for completion.

## Package Guide
| Package          | Purpose                                                                                                                                                                                          |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `configuration/` | Spring config, `SpringBootWebApplication` entry point, `SecurityConfig`, `GrouperPropertyConfigurer`                                                                                             |
| `controller/`    | `GroupingsRestControllerv2_1` (main API), `EmailRestController`, `ErrorControllerAdvice`                                                                                                         |
| `service/`       | Business logic facades/services (`GroupPathService`, `MemberService`, `UpdateMemberService`, `GroupingAttributeService`, `AnnouncementsService`, etc.) plus `GrouperService`/`GrouperApiService` |
| `wrapper/`       | Grouper WS command builders and result wrappers                                                                                                                                                  |
| `groupings/`     | API response DTOs                                                                                                                                                                                |
| `type/`          | Domain enums/types (`OptType`, `GroupType`, `PrivilegeType`, `SortBy`)                                                                                                                           |
| `util/`          | Shared helpers (`Strings`, `Dates`, `JsonUtil`, `PropertyLocator`, `OnlyUniqueItems`)                                                                                                            |
| `filter/`        | `JwtAuthenticationFilter`                                                                                                                                                                        |
| `exception/`     | Custom exceptions (`AccessDeniedException`, `GroupPathNotFoundException`, etc.)                                                                                                                  |

## Build & Test Commands
```bash
# Run the app (requires overrides properties file — see below)
./mvnw clean spring-boot:run

# Unit tests only (no Grouper network access required)
./mvnw clean test

# Run a single test class or method
./mvnw clean test -Dtest=GroupPathServiceTest
./mvnw clean test -Dtest=GroupPathServiceTest#isGroupingPath

# Integration tests (require live Grouper credentials)
./mvnw clean test -Dtest='Test*'

# Build WAR
./mvnw clean package

# Docker (recommended for local full-stack dev)
# ensure ~/.$(whoami)-conf/uh-groupings-api-overrides.properties exists (docker-compose mounts it read-only)
docker-compose up --build
```

**Default server port is `8081`** (not 8080). Health: `http://localhost:8081/uhgroupingsapi/actuator/health`. Swagger: `http://localhost:8081/uhgroupingsapi/swagger-ui.html`.

## Spring Profiles
| Profile           | Use case                                                                                                                                  |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `localhost`       | Local dev; imports `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`                                                              |
| `dockerhost`      | Docker-based local runtime; imports `/overrides/uh-groupings-api-overrides.properties` and enables Vault-based Grouper password overrides |
| `localTest`       | Unit tests with mocked/no Grouper (`GrouperApiServiceTest`)                                                                               |
| `integrationTest` | Integration tests hitting live Grouper (`TestGrouperApiService`, `TestGroupingAssignmentService`)                                         |
| `prod`            | Production (AWS ECS)                                                                                                                      |

## Test Naming Convention
- `*Test.java` (e.g., `GrouperApiServiceTest`) — unit/local tests, `@ActiveProfiles("localTest")`
- `Test*.java` (e.g., `TestGrouperApiService`) — integration tests, `@ActiveProfiles("integrationTest")`, require live Grouper

## Key Config Properties (`application.properties`)
```
grouping.api.server.type         # Selects the GrouperService bean; GROUPER is the default
groupings.api.grouping_admins    # Grouper path for admin group
groupings.api.basis / :include / :exclude / :owners   # Sub-group suffixes
groupings.api.validation.*       # Path/identifier regex + length limits
grouper.api.sync.destinations.location   # Sync destinations stem
```
Secrets (Grouper URL/credentials, JWT key) are **never committed**; injected via overrides file locally or AWS Secrets Manager in production.

## Secrets Strategy

The project deals with two distinct categories of credentials, stored differently:

### Application secrets (used by the running API)
The API itself needs Grouper service-account credentials, a JWT signing key, and a database password.

- **Local development (Docker Desktop):** values live in a developer-owned overrides file at `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`. `docker-compose` bind-mounts this file read-only and Spring imports it via `SPRING_CONFIG_IMPORT`. The file is never committed.
- **AWS deployment:** values live in **AWS Secrets Manager** under `groupings/api/*`. `aws/setup.sh` creates them once at provisioning time. ECS tasks read them via the task definition's `secrets[]` array (decrypted at container start, never in plaintext task config).

The Spring property names are identical in both environments; only the source mechanism differs.

### AWS account credentials (used by developers who deploy)
Only developers who run `make aws-setup` or other AWS Make targets need IAM access keys to authenticate **to** AWS. These are **never** stored on disk in plaintext, and they have nothing to do with application secrets — they only authorize a developer's CLI to call AWS APIs. The project uses [`aws-vault`](https://github.com/99designs/aws-vault):

- `make aws-vault-setup` (one time per developer) prompts for an Access Key ID and Secret Access Key, stores them in the macOS Keychain under a profile named `uh-groupings`.
- Each AWS Make target is wrapped: `aws-vault exec uh-groupings -- make aws-setup`. aws-vault releases credentials only as ephemeral environment variables for the duration of one command.
- `docker-compose.aws.yml` does not bind-mount `~/.aws`. The AWS CLI inside the container reads `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_SESSION_TOKEN` from the environment that aws-vault injected.

aws-vault does **not** hold `grouperClient.webService.password`, `jwt.secret.key`, or any other application-runtime value. Those go in AWS Secrets Manager (above) and are read by the ECS task at startup, not by aws-vault on a developer's laptop.

See [`docs/SECRETS.md`](docs/SECRETS.md) for the full reference, including the list of Spring properties classified as secrets vs settings.

## Adding a New Grouper Operation
1. Create a `*Command` class in `wrapper/` extending `GrouperCommand<T>` (builder pattern, fluent API).
2. Create matching `*Results` wrapper in `wrapper/`.
3. Add a method to `GrouperService` interface.
4. Implement in `GrouperApiService` using `exec.execute(new YourCommand()...)`.
5. Keep any profile-specific test fixtures and configuration aligned with `localTest`, `integrationTest`, and `dockerhost` behavior.
6. Expose through the appropriate `*Service` in `service/` and optionally a controller endpoint.

