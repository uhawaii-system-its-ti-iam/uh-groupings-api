# AGENTS.md — UH Groupings API

<!-- TOC -->
* [AGENTS.md — UH Groupings API](#agentsmd--uh-groupings-api)
  * [What This Project Does](#what-this-project-does)
  * [Architecture Overview](#architecture-overview)
  * [Core Domain Model](#core-domain-model)
    * [Grouping Structure](#grouping-structure)
    * [Membership Logic](#membership-logic)
    * [Ownership Model](#ownership-model)
  * [Authorization & Authentication](#authorization--authentication)
    * [JWT-Based Authorization](#jwt-based-authorization)
    * [Grouper-Based Authorization](#grouper-based-authorization)
    * [Public vs Protected Endpoints](#public-vs-protected-endpoints)
  * [Critical Patterns](#critical-patterns)
    * [Command/Wrapper Pattern](#commandwrapper-pattern)
    * [UH Identifier Handling](#uh-identifier-handling)
    * [Async Operations](#async-operations)
    * [Retry & Failure Handling](#retry--failure-handling)
    * [Validation & Sanitization](#validation--sanitization)
    * [Timestamp Updates](#timestamp-updates)
  * [Service Layer Architecture](#service-layer-architecture)
    * [Service Responsibilities](#service-responsibilities)
    * [Service Interactions](#service-interactions)
  * [API Endpoints](#api-endpoints)
    * [Admin Management](#admin-management)
    * [Grouping Membership](#grouping-membership)
    * [Owner Management](#owner-management)
    * [Opt-In/Out Operations](#opt-inout-operations)
    * [Privilege & Attribute Management](#privilege--attribute-management)
    * [Grouping Membership Operations](#grouping-membership-operations)
    * [Queries & Counts](#queries--counts)
    * [Async Job Management](#async-job-management)
  * [Type System & Enums](#type-system--enums)
  * [Error Handling](#error-handling)
  * [Request/Response Models](#requestresponse-models)
  * [Package Guide](#package-guide)
  * [Configuration Properties](#configuration-properties)
  * [Build & Test Commands](#build--test-commands)
  * [Spring Profiles](#spring-profiles)
  * [Test Naming Convention](#test-naming-convention)
  * [Adding a New Grouper Operation](#adding-a-new-grouper-operation)
<!-- TOC -->

## What This Project Does
A Spring Boot (Java 21) REST API that serves as middleware between the UH Groupings UI and the Internet2 **Grouper** enterprise access management system. It manages UH group memberships (basis, include, exclude, owners subgroups) and exposes them through a JWT-secured REST API at `/api/groupings/v2.1`.

## Architecture Overview

```
UI → GroupingsRestControllerv2_1
        └──→ Service Layer (GroupingAssignmentService, UpdateMemberService, etc.)
                └──→ GrouperService (interface)
                        └──→ GrouperApiService (registered by `GrouperPropertyConfigurer`)
                                   ↓
                             ExecutorService (retry logic, up to 2 retries)
                                   ↓
                             GrouperCommand subclasses (wrapper/)
                                   ↓
                             Grouper WS Client (GrouperClient library)
```

**Two-layer response model:**
- `src/.../wrapper/` — thin wrappers around raw Grouper `Ws*` beans (e.g., `HasMembersResults`, `SubjectsResults`)
- `src/.../groupings/` — API-facing DTOs returned to the UI (e.g., `GroupingAddResult`, `MembershipResults`)

## Core Domain Model

### Grouping Structure
Every grouping is identified by a path (e.g., `hawaii:its:groupings:test-grouping`) and has four subgroups:
- `{path}:basis` — source of truth (e.g., all faculty from LDAP)
- `{path}:include` — additional members to add
- `{path}:exclude` — members to remove
- `{path}:owners` — users/groups who can manage this grouping

Path validation: `[\\w-:.]+` regex, max 255 characters.

### Membership Logic
**Effective membership = (basis + include) - exclude**

- A user is a member if they are in basis OR include, AND not in exclude
- Opt-in moves a user from exclude to include
- Opt-out moves a user from include to exclude
- Basis members cannot be removed unless they are also in exclude

### Ownership Model
Owners manage a grouping and can add/remove members, change attributes, etc.

**Direct owners:** Individual users in the `:owners` group.

**Indirect owners (owner-groupings):** Groups used as owners. Members of these groups inherit owner privileges.

**Duplicate owners:** Users who are both direct owners AND members of owner-groupings. Tracked separately to prevent accidental removal.

**Owner limit:** Configurable maximum to prevent excessive ownership.

## Authorization & Authentication

### JWT-Based Authorization
Stateless JWT tokens populate `SecurityContextHolder` before every request. Tokens contain roles: `ROLE_ADMIN`, `ROLE_OWNER`.

**Use JWT checks for general authorization:**
- `memberService.isCurrentUserAdmin()` — checks JWT for ROLE_ADMIN
- `memberService.isCurrentUserOwner()` — checks JWT for ROLE_OWNER

These are fast, local checks used in service methods for authorization decisions.

### Grouper-Based Authorization
Queries Grouper to determine roles. Used during login to populate JWT tokens.

**Use Grouper checks for role population endpoints:**
- `memberService.isAdmin(uhIdentifier)` — queries Grouper admin group
- `memberService.isOwner(uhIdentifier)` — queries Grouper owners group
- `memberService.isOwner(groupingPath, uhIdentifier)` — checks if user owns specific grouping

### Public vs Protected Endpoints
**Public endpoints (no JWT required):**
- `/v3/api-docs/**` — OpenAPI documentation
- `/swagger-ui/**` — Swagger UI
- `/api/groupings/v2.1/announcements/**` — system announcements

**Protected endpoints (JWT required):**
- All other `/api/groupings/v2.1/**` endpoints

## Critical Patterns

### Command/Wrapper Pattern
All Grouper calls use a Command pattern. `GrouperCommand<T>` is the abstract base; `ExecutorService.execute()` runs the command with retry logic.

```
// GrouperApiService delegates to ExecutorService:
exec.execute(new HasMembersCommand()
    .assignGroupPath(groupPath)
    .addUhIdentifier(uhIdentifier)
    .setRetry(true));
```

Never call Grouper WS beans directly — always go through a `*Command` class in `wrapper/`.

**Command classes:** `HasMembersCommand`, `AddMembersCommand`, `RemoveMembersCommand`, `FindGroupsCommand`, `SubjectsCommand`, `GetMembersCommand`, `GroupAttributeCommand`, `AssignAttributesCommand`, `AssignGrouperPrivilegesCommand`, etc.

### UH Identifier Handling
A UH identifier is either an 8-digit numeric UUID (`^\\d{8}$`) or a string username. `GrouperCommand.isUhUuid()` determines which Grouper lookup field to populate.

```
protected WsSubjectLookup subjectLookup(String uhIdentifier) {
    WsSubjectLookup lookup = new WsSubjectLookup();
    if (isUhUuid(uhIdentifier)) {
        lookup.setSubjectId(uhIdentifier);  // 8-digit UUID
    } else {
        lookup.setSubjectIdentifier(uhIdentifier);  // username
    }
    return lookup;
}
```

This distinction must be preserved everywhere identifiers are used.

### Async Operations
`UpdateMemberService` uses `@Async` for long-running operations. `AsyncJobsManager` tracks in-flight jobs.

**Flow:**
1. Controller calls async method (e.g., `addIncludeMembersAsync()`)
2. Method returns `CompletableFuture<T>` immediately
3. Controller returns job ID (202 ACCEPTED)
4. Caller polls `/api/groupings/v2.1/jobs/{jobId}` for results
5. `AsyncJobResult` contains status, result, or error

**Async methods:**
- `addIncludeMembersAsync()`
- `addExcludeMembersAsync()`
- `resetIncludeGroupAsync()`
- `resetExcludeGroupAsync()`
- `memberAttributeResultsAsync()`

### Retry & Failure Handling
`ExecutorService.execute()` retries failed commands up to 2 times with exponential backoff.

```
for (int i = 0; i <= MAX_RETRIES; i++) {  // 0, 1, 2
    try {
        result = command.execute();
        if (result.getResultCode().startsWith("SUCCESS")) {
            return result;
        }
    } catch (Exception e) {
        ex = e;
    }
    if (!retry || i == MAX_RETRIES) break;
    delay(i);  // 1s * i
}
throw new GrouperException(...);
```

**Retry conditions:**
- Only if `command.setRetry(true)` was called
- Delay: 1 second × attempt number (1s, 2s)
- Max 2 retries (3 total attempts)
- Throws `GrouperException` if all retries fail

### Validation & Sanitization
Input validation happens at multiple layers:

**Path validation:**
- Regex: `[\\w-:.]+`
- Max length: 255 characters
- `GroupPathService.checkPath()` validates before operations

**UH identifier validation:**
- 8-digit UUID or username
- `SubjectService.getValidUhUuid()` resolves and validates
- Throws `UhIdentifierNotFoundException` if not found

**Description validation:**
- Regex pattern (configurable)
- Max length (configurable, default 255)
- HTML sanitization using OWASP sanitizer
- `GroupingAttributeService.validateAndSanitizeDescription()`

### Timestamp Updates
After successful Grouper operations, `UpdateTimestampService.update()` updates the `:lastModified` attribute on affected groups.

```
if (grouperService instanceof GrouperApiService) {
    timestampService.update(result);
}
```

Only called in production (not in test profiles). Tracks when groupings were last modified.

## Service Layer Architecture

### Service Responsibilities

| Service                      | Responsibility                                                         |
|------------------------------|:-----------------------------------------------------------------------|
| `GroupingAssignmentService`  | Grouping paths, admins, opt-in/out logic, owner queries                |
| `GroupingAttributeService`   | Grouping attributes (opt-in, opt-out, sync destinations, descriptions) |
| `GroupingOwnerService`       | Owner operations, member queries, pagination                           |
| `UpdateMemberService`        | Add/remove members, manage owners, async operations                    |
| `MemberService`              | Membership checks (basis, include, exclude, owners)                    |
| `MembershipService`          | User's memberships and groupings                                       |
| `SubjectService`             | UH identifier validation and resolution                                |
| `GroupPathService`           | Path validation and construction                                       |
| `MemberAttributeService`     | Member attributes and owned groupings                                  |
| `UpdateTimestampService`     | Last-modified timestamp updates                                        |
| `GrouperService` (interface) | Abstraction for Grouper operations                                     |
| `GrouperApiService`          | Real Grouper integration via GrouperClient                             |
| `ExecutorService`            | Command execution with retry logic                                     |
| `AsyncJobsManager`           | Async job tracking and result retrieval                                |
| `AnnouncementsService`       | System announcements                                                   |
| `EmailService`               | Error notification emails                                              |

### Service Interactions
```
Controller
  ├─→ GroupingAssignmentService (queries, admin ops)
  ├─→ GroupingAttributeService (attributes, descriptions)
  ├─→ GroupingOwnerService (owner queries, pagination)
  ├─→ UpdateMemberService (add/remove, async)
  ├─→ MembershipService (user memberships)
  └─→ AsyncJobsManager (job tracking)
        ↓
  Service Layer (all services)
        ↓
  GrouperService (interface)
        ↓
  GrouperApiService
        ├─→ ExecutorService (retry logic)
        │     ↓
        │   GrouperCommand subclasses
        │     ↓
        │   GrouperClient (WS calls)
        │
        └─→ UpdateTimestampService (timestamp updates)
```

## API Endpoints

### Admin Management
```
GET    /api/groupings/v2.1/groupings/admins
       → GroupingGroupMembers (list of all admins)

POST   /api/groupings/v2.1/admins/{uhIdentifier}
       → GroupingAddResult (add admin)

DELETE /api/groupings/v2.1/admins/{uhIdentifier}
       → GroupingRemoveResult (remove admin)

DELETE /api/groupings/v2.1/admins/{paths}/{uhIdentifier}
       → GroupingRemoveResults (remove from multiple groups)
```

### Grouping Membership
```
GET    /api/groupings/v2.1/groupings/{groupingPath}
       ?pageNumber=0&pageSize=50&sortBy=NAME&isAscending=true&searchString=optional
       → GroupingGroupMembers (paginated members)

POST   /api/groupings/v2.1/groupings/{groupingPath}/where-listed
       Body: ["uid1", "uid2"]
       → GroupingMembers (where each user is listed)

POST   /api/groupings/v2.1/groupings/{groupingPath}/is-basis
       Body: ["uid1", "uid2"]
       → GroupingMembers (which users are in basis)

POST   /api/groupings/v2.1/groupings/{groupingPath}/include-members/in-list
       Body: ["uid1", "uid2"]
       → GroupingMembers (which users are in include)

POST   /api/groupings/v2.1/groupings/{groupingPath}/exclude-members/in-list
       Body: ["uid1", "uid2"]
       → GroupingMembers (which users are in exclude)
```

### Owner Management
```
GET    /api/groupings/v2.1/grouping/{path}/owners
       → GroupingOwnerMembers (immediate owners + owner-groupings)

GET    /api/groupings/v2.1/groupings/{path}/owners/count
       → Integer (all owners: direct + indirect)

GET    /api/groupings/v2.1/members/{path}/owners/count
       → Integer (direct owners only)

GET    /api/groupings/v2.1/groupings/{path}/owners/duplicates
       → Map<String, OwnerResult> (users with multiple ownership sources)

PUT    /api/groupings/v2.1/groupings/{path}/owners/{uhIdentifier}
       → GroupingAddResults (add owner)

PUT    /api/groupings/v2.1/groupings/{path}/owners/owner-groupings/{ownerGroupings}
       → GroupingAddResults (add owner-grouping)

DELETE /api/groupings/v2.1/groupings/{path}/owners/{uhIdentifier}
       → GroupingRemoveResults (remove owner)

DELETE /api/groupings/v2.1/groupings/{path}/owners/owner-groupings/{ownerGroupings}
       → GroupingRemoveResults (remove owner-grouping)

POST   /api/groupings/v2.1/groupings/group
       Body: groupPaths, pageNumber, pageSize, sortBy, isAscending
       → GroupingGroupsMembers (paginated owned groupings)
```

### Opt-In/Out Operations
```
PUT    /api/groupings/v2.1/groupings/{path}/include-members/{uhIdentifier}/self
       → GroupingMoveMemberResult (opt-in: move to include)

PUT    /api/groupings/v2.1/groupings/{path}/exclude-members/{uhIdentifier}/self
       → GroupingMoveMemberResult (opt-out: move to exclude)

GET    /api/groupings/v2.1/groupings/members/{uhIdentifier}/opt-in-groups
       → GroupingPaths (groupings user can opt into)
```

### Privilege & Attribute Management
```
GET    /api/groupings/v2.1/grouping/{path:[\\w-:.]+}/is-valid
       → Boolean (is valid grouping path)

GET    /api/groupings/v2.1/groupings/{path}/opt-attributes
       → GroupingOptAttributes (opt-in/out status)

GET    /api/groupings/v2.1/groupings/{path}/groupings-sync-destinations
       → GroupingSyncDestinations (available sync destinations)

GET    /api/groupings/v2.1/groupings/{path}/description
       → GroupingDescription (grouping description)

PUT    /api/groupings/v2.1/groupings/{path}/description
       Body: description string
       → GroupingUpdateDescriptionResult

PUT    /api/groupings/v2.1/groupings/{path}/sync-destination/{id}/{status}
       → GroupingUpdateSyncDestResult (enable/disable sync destination)

PUT    /api/groupings/v2.1/groupings/{path}/opt-attribute/{id}/{status}
       → GroupingUpdateOptAttributeResult (enable/disable opt-in/out)
```

### Grouping Membership Operations
```
PUT    /api/groupings/v2.1/groupings/{path}/include-members
       Body: ["uid1", "uid2"]
       → GroupingMoveMembersResult (add to include)

PUT    /api/groupings/v2.1/groupings/{path}/include-members/async
       Body: ["uid1", "uid2"]
       → Integer (job ID, 202 ACCEPTED)

PUT    /api/groupings/v2.1/groupings/{path}/exclude-members
       Body: ["uid1", "uid2"]
       → GroupingMoveMembersResult (add to exclude)

PUT    /api/groupings/v2.1/groupings/{path}/exclude-members/async
       Body: ["uid1", "uid2"]
       → Integer (job ID, 202 ACCEPTED)

DELETE /api/groupings/v2.1/groupings/{path}/include-members
       Body: ["uid1", "uid2"]
       → GroupingRemoveResults (remove from include)

DELETE /api/groupings/v2.1/groupings/{path}/exclude-members
       Body: ["uid1", "uid2"]
       → GroupingRemoveResults (remove from exclude)

DELETE /api/groupings/v2.1/groupings/{path}/include
       → GroupingReplaceGroupMembersResult (clear all include members)

DELETE /api/groupings/v2.1/groupings/{path}/include/async
       → Integer (job ID, 202 ACCEPTED)

DELETE /api/groupings/v2.1/groupings/{path}/exclude
       → GroupingReplaceGroupMembersResult (clear all exclude members)

DELETE /api/groupings/v2.1/groupings/{path}/exclude/async
       → Integer (job ID, 202 ACCEPTED)
```

### Queries & Counts
```
GET    /api/groupings/v2.1/groupings
       → GroupingPaths (all groupings, admin only)

GET    /api/groupings/v2.1/members/memberships
       → MembershipResults (current user's memberships)

GET    /api/groupings/v2.1/members/{uhIdentifier}/groupings
       → ManageSubjectResults (all groupings for user)

GET    /api/groupings/v2.1/owners/groupings
       → GroupingPaths (current user's owned groupings)

GET    /api/groupings/v2.1/owners/groupings/count
       → Integer (number of groupings owned)

GET    /api/groupings/v2.1/members/memberships/count
       → Integer (number of memberships)

GET    /api/groupings/v2.1/groupings/{path}/count
       → Integer (number of members in grouping)

GET    /api/groupings/v2.1/members/{uhIdentifier}/is-admin
       → Boolean (is user admin)

GET    /api/groupings/v2.1/members/{uhIdentifier}/is-owner
       → Boolean (is user any owner)

GET    /api/groupings/v2.1/members/{path}/{uhIdentifier}/is-owner
       → Boolean (is user owner of specific grouping)

POST   /api/groupings/v2.1/members
       Body: ["uid1", "uid2"]
       → MemberAttributeResults (member attributes; if any identifier is malformed or unknown to Grouper,
         only the `invalid` list is returned, in submitted order, instead of failing the request)

POST   /api/groupings/v2.1/members/async
       Body: ["uid1", "uid2"]
       → Integer (job ID, 202 ACCEPTED)
```

### Async Job Management
```
GET    /api/groupings/v2.1/jobs/{jobId}
       → AsyncJobResult (job status, result, or error)
```

## Type System & Enums

**GroupType** — subgroup suffixes:
- `BASIS` = `:basis`
- `INCLUDE` = `:include`
- `EXCLUDE` = `:exclude`
- `OWNERS` = `:owners`

**OptType** — opt-in/out types:
- `IN` — opt-in privilege
- `OUT` — opt-out privilege

**PrivilegeType** — privilege assignment types:
- `IN` — opt-in privilege
- `OUT` — opt-out privilege

**SortBy** — member list sorting:
- `NAME` — sort by name
- `UUID` — sort by UUID
- `UID` — sort by username

**InclusionType** — membership types:
- `DIRECT` — direct member
- `INDIRECT` — indirect member (via group)

**Feedback** — user feedback model (for future use)

**Announcement** — system announcements

## Error Handling

All exceptions are caught by `ErrorControllerAdvice` and mapped to HTTP responses with `ApiError` objects.

| Exception                       | HTTP Status               | Message                                   |
|---------------------------------|---------------------------|-------------------------------------------|
| `AccessDeniedException`         | 403 FORBIDDEN             | Access Denied Exception                   |
| `InvalidGroupPathException`     | 400 BAD_REQUEST           | Invalid Group Path Exception              |
| `InvalidUhIdentifierException`  | 400 BAD_REQUEST           | Invalid Uh Member Exception               |
| `GroupPathNotFoundException`    | 404 NOT_FOUND             | Group Path found failed                   |
| `UhIdentifierNotFoundException` | 404 NOT_FOUND             | UH Member found failed                    |
| `OwnerLimitExceededException`   | 409 CONFLICT              | Owner Limit Exceeded Exception            |
| `DirectOwnerRemovedException`   | 422 UNPROCESSABLE_ENTITY  | Direct Owner Removed Exception            |
| `GrouperException`              | 503 SERVICE_UNAVAILABLE   | Groupings data is temporarily unavailable |
| `InvalidDescriptionException`   | 400 BAD_REQUEST           | Description validation failed             |
| `IllegalArgumentException`      | 404 NOT_FOUND             | Illegal Argument Exception                |
| `UnsupportedOperationException` | 501 NOT_IMPLEMENTED       | Unsupported Operation Exception           |
| Generic `Exception`             | 500 INTERNAL_SERVER_ERROR | Runtime Exception                         |

**Error response format:**
```json
{
  "status": 403,
  "message": "Access Denied Exception",
  "resultCode": "FAILURE",
  "path": "/api/groupings/v2.1/groupings/test/include-members",
  "stackTrace": "..."
}
```

## Request/Response Models

**Response DTOs** (in `groupings/` package):
- `GroupingAddResult`, `GroupingAddResults` — add operation results
- `GroupingRemoveResult`, `GroupingRemoveResults` — remove operation results
- `GroupingMoveMemberResult`, `GroupingMoveMembersResult` — move operation results
- `GroupingMembers`, `GroupingGroupMembers` — member lists
- `GroupingOwnerMembers` — owner lists with limit tracking
- `GroupingPaths` — list of grouping paths
- `GroupingGroupsMembers` — paginated groupings
- `MembershipResults` — user's memberships
- `ManageSubjectResults` — subject management data
- `GroupingOptAttributes` — opt-in/out status
- `GroupingSyncDestinations` — available sync destinations
- `GroupingDescription` — grouping description
- `GroupingUpdateDescriptionResult` — description update result
- `GroupingUpdateOptAttributeResult` — opt attribute update result
- `GroupingUpdateSyncDestResult` — sync destination update result
- `MemberAttributeResults` — member attributes
- `OwnerResult` — owner details with ownership sources

**Request models:**
- `OptRequest` — opt-in/out request (builder pattern)
- List of UH identifiers (JSON array)

## Package Guide
| Package          | Purpose                                                          |
|------------------|------------------------------------------------------------------|
| `configuration/` | Spring config, entry point, security, property configuration     |
| `controller/`    | REST endpoints, request handling, response formatting            |
| `service/`       | Business logic, Grouper integration, member/grouping operations  |
| `wrapper/`       | Grouper WS command builders and result wrappers                  |
| `groupings/`     | API response DTOs                                                |
| `type/`          | Domain enums and types (GroupType, OptType, etc.)                |
| `util/`          | Shared helpers (Strings, Dates, JsonUtil, PropertyLocator, etc.) |
| `filter/`        | JWT authentication filter                                        |
| `exception/`     | Custom exceptions with specific HTTP mappings                    |

## Configuration Properties

**Grouping paths:**
```
groupings.api.grouping_admins          # Admin group path
groupings.api.grouping_owners          # Owners group path
groupings.api.stale_subject_id         # Stale subject marker
```

**Limits:**
```
groupings.max.owner.limit              # Max owners per grouping (default 50)
```

**Grouper operations:**
```
groupings.api.assign_type_group        # Grouper assign type for groups
groupings.api.operation_assign_attribute # Grouper operation: assign
groupings.api.operation_remove_attribute # Grouper operation: remove
groupings.api.every_entity             # Grouper entity identifier
```

**Validation:**
```
groupings.api.validation.description.maxlength  # Max description length
groupings.api.validation.description.regex      # Description regex pattern
```

**API:**
```
grouping.api.server.type               # GROUPER (default)
app.groupings.controller.uuid          # Controller UUID (required)
```

**Grouper connection:**
```
grouper.api.url                        # Grouper WS URL
grouper.api.username                   # Service account username
grouper.api.password                   # Service account password (from Vault/Secrets Manager)
```

**JWT:**
```
jwt.signing.key                        # JWT signing key (from Vault/Secrets Manager)
```

## Build & Test Commands
```bash
# Run the app (requires overrides properties file)
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
```

**Local dev server port is `8081`** (base `application.properties`). 
- Health: `http://localhost:8081/uhgroupingsapi/actuator/health`
- Swagger: `http://localhost:8081/uhgroupingsapi/swagger-ui.html`

## Spring Profiles
| Profile           | Use case                                                                               |
|-------------------|----------------------------------------------------------------------------------------|
| `localhost`       | Local dev; imports `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`           |
| `localTest`       | Unit tests with mocked/no Grouper (`GrouperApiServiceTest`)                            |
| `integrationTest` | Integration tests hitting live Grouper (`TestGrouperApiService`)                       |
| `dockerhost`      | Docker-based local runtime; imports `/overrides/uh-groupings-api-overrides.properties` |
| `prod`            | Production deployment                                                                  |

## Test Naming Convention
- `*Test.java` (e.g., `GrouperApiServiceTest`) — unit/local tests, `@ActiveProfiles("localTest")`
- `Test*.java` (e.g., `TestGrouperApiService`) — integration tests, `@ActiveProfiles("integrationTest")`, require live Grouper

## Adding a New Grouper Operation
1. Create a `*Command` class in `wrapper/` extending `GrouperCommand<T>` (builder pattern, fluent API).
2. Create matching `*Results` wrapper in `wrapper/` implementing `Results` interface.
3. Add a method to `GrouperService` interface.
4. Implement in `GrouperApiService` using `exec.execute(new YourCommand()...)`.
5. Create a service method in appropriate service class (e.g., `GroupingAssignmentService`).
6. Add controller endpoint in `GroupingsRestControllerv2_1`.
7. Write unit tests in `*Test.java` and integration tests in `Test*.java`.
8. Update the AGENTS.md file with new endpoint documentation.
