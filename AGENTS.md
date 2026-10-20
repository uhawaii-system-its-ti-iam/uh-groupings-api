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
    * [Bulk Request Batching](#bulk-request-batching)
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

A lookup can also name the subject source to search (`subjectLookup(uhIdentifier, sourceId)`). Without one, Grouper
searches every subject source for each identifier, which takes about five times longer (15 ms vs 3 ms per identifier
on grouper-test). Bulk validation (`SubjectService.validateUhIdentifiers`) first looks identifiers up in the source of UH
people (`groupings.api.grouper.person-source-id`, `UH core LDAP`), then looks up the ones not found there in every
source, and returns each valid identifier's source (`UhIdentifierValidationResult.getSubjectSourceIds()`). The adds and
removes of `UpdateMemberService.moveGroupMembers` then look each member up in that source only.

### Async Operations
`UpdateMemberService` uses `@Async` for long-running operations. `AsyncJobsManager` tracks in-flight jobs.

**Flow:**
1. Controller calls async method (e.g., `addIncludeMembersAsync()`)
2. Method returns `CompletableFuture<T>` immediately
3. Controller returns job ID (202 ACCEPTED)
4. Caller polls `/api/groupings/v2.1/jobs/{jobId}` for results
5. `AsyncJobResult` contains status and result. If the job failed, polling rethrows the job's real exception, so it maps to its own status (e.g. 503 when Grouper is unavailable, 403 when access is denied) rather than a generic 500
6. A finished job stays readable for `groupings.api.async.finished-job-retention-millis` (10 min) after it finishes, and every poll in that time gets the same answer (the same result, or the same exception and status), so a poll sent again after its answer was lost still gets the outcome. Then `AsyncJobsManager.evictFinishedJobs` (scheduled every `job-eviction-interval-millis`, 1 min) forgets it, polled or not, and polling returns `NOT_FOUND`. A job in progress is never evicted

An async method does its work in its own body, on the async executor's thread (`AsyncConfig`), and returns
`CompletableFuture.completedFuture(result)`. Don't hand the work to `CompletableFuture.supplyAsync()` without an
executor: that runs it on the JVM's shared common pool, where a long import can hold up other jobs. An import holds
its thread for the whole import, so `AsyncConfig` starts all its threads (`groupings.api.async.threads`, 10) before it
queues a job. Once they are all busy and the queue (`groupings.api.async.queue-capacity`, 100) is full, a new job is
rejected with `TaskRejectedException`, so its request fails (500), instead of running on the request's thread.

**Progress:** `addIncludeMembersAsync` and `addExcludeMembersAsync` report their progress in an `AsyncJobProgress` that
the controller creates and puts with the job (`AsyncJobsManager.putJob(job, progress)`). While the job is
`IN_PROGRESS`, polling returns it as `progress`: the phase (`VALIDATING`, `REMOVING` or `ADDING`) and how many of the
phase's identifiers Grouper has answered (`done` of `total`), counted per finished batch. `VALIDATING` counts every
lookup (see UH Identifier Handling): identifiers looked up again (in every source after the person source, or one at
a time when a bulk answer can't be matched to them) are added to `total` before they are sent, so `done` never
exceeds `total`.

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

### Bulk Request Batching
`GrouperApiService.getSubjects(List)`, `addMembers` and `removeMembers` send a long list through `BatchExecutor`, as
Grouper requests of at most a batch size of identifiers each, and merge the batch results (`SubjectsResults.merge`,
`AddMembersResults.merge`, `RemoveMembersResults.merge`), so callers get the same result as from one request.

| Request | Batch size | Batches at once |
|---|---|---|
| Lookup (`getSubjects`) | `lookup-batch-size` (1000) | up to `max-concurrent-requests` (4) |
| Remove (`removeMembers`) | `update-batch-size` (250) | up to `max-concurrent-requests` (4) |
| Add (`addMembers`) | `update-batch-size` (250) | 1 |

(Properties under `groupings.api.grouper.`.) Measured on grouper-test (Grouper WS 2.2.2): a lookup takes about 3 ms per
identifier with its subject source named (see UH Identifier Handling); an add or remove about 115 ms per member;
re-adding a listed member about 13 ms.

- Why batches: Grouper answers only after processing every subject in a request, so one request for a large import
  outlasts the Grouper client's socket timeout (`grouperClient.webService.httpSocketTimeoutMillis`, 480 s).
- The concurrent batches of all lookups and removals share one pool of `max-concurrent-requests` threads, which caps
  the bulk requests one API instance sends at once. 4 lookups of 1,000 take about as long as one.
- Adds are sent one batch at a time: Grouper fails most of the members of concurrent adds to the same group with
  `EXCEPTION` (it conflicts updating the group's `last_membership_change`). For the same reason, two adds of more than
  one batch to the same group in one API instance wait for each other. Concurrent removes don't conflict.
- A batch is sent again, after each of `retry-delays-millis` (5 s, 15 s), when it failed without an answer from
  Grouper (an I/O error, or a response that isn't Grouper's) or when Grouper reports only `EXCEPTION` for its failed
  members. Any other failure would fail again and is thrown at once. `ExecutorService`'s own retry isn't used, since it
  treats a lookup batch in which nothing resolves as a failure.
- Batches are not one transaction: once a batch fails, no more are started and the failure is thrown, but the batches
  that succeeded stay applied. Re-adding a listed member (`SUCCESS_ALREADY_EXISTED`) or removing an unlisted one
  (`SUCCESS_WASNT_IMMEDIATE`) succeeds, so a failed batch can be sent again and the whole operation repeated.
- Each lookup batch in every source collapses its own unresolved identifiers into one `SUBJECT_NOT_FOUND` entry, so a
  merged lookup can hold several. `SubjectService` matches results to identifiers by uid and UH number, never by
  position or count.
- A new `GrouperApiService` operation that takes a list as long as an import should also go through `BatchExecutor`
  (`lookup`, `add` or `remove`), with a `merge` method on its results wrapper.
- Moving a list longer than one update batch into include or exclude (`UpdateMemberService.moveGroupMembers`) first
  fetches the opposite list's member ids (one request, without subject details) and removes only the identifiers
  listed there, instead of sending every identifier for a removal that does nothing. Its `removeResults` then lists
  only those.

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
| `BatchExecutor`              | Bulk lookups, adds and removes in batches, with concurrency and resends |
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
       → GroupingGroupsMembers (one page of the members of each group; paginationComplete once every group's
         page is empty. Its allMembers only accounts for the members on the same page, so the UI loads all the
         pages, 4 at a time, and works out the grouping's members and where each is listed from the whole lists)
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
       → GroupingMoveMembersResult (add to include; invalidUhIdentifiers lists the identifiers not added because
         they are malformed or unknown to Grouper)

PUT    /api/groupings/v2.1/groupings/{path}/include-members/async
       Body: ["uid1", "uid2"]
       → Integer (job ID, 202 ACCEPTED; the job's result is the GroupingMoveMembersResult above. The UI sends a
         large file import here unvalidated, shows the job's progress while it polls (see Async Operations), and
         reports invalidUhIdentifiers as not found)

PUT    /api/groupings/v2.1/groupings/{path}/exclude-members
       Body: ["uid1", "uid2"]
       → GroupingMoveMembersResult (add to exclude; invalidUhIdentifiers as for include-members)

PUT    /api/groupings/v2.1/groupings/{path}/exclude-members/async
       Body: ["uid1", "uid2"]
       → Integer (job ID, 202 ACCEPTED; the job's result is the GroupingMoveMembersResult above, and it reports
         its progress like include-members/async)

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
       → AsyncJobResult (job status and result; a failed job returns the failure's own error status. While a job
         that reports its progress is IN_PROGRESS, progress is {phase, done, total}; otherwise it is null. A finished
         job gives the same answer on every poll for 10 minutes after it finishes, then NOT_FOUND)
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
groupings.api.grouper.lookup-batch-size        # Max UH identifiers per bulk Grouper lookup request (default 1000)
groupings.api.grouper.update-batch-size        # Max UH identifiers per bulk add/remove request (default 250)
groupings.api.grouper.max-concurrent-requests  # Bulk lookup/remove requests one API instance sends at once (default 4)
groupings.api.grouper.retry-delays-millis      # Waits before each resend of a failed bulk request (default 5000,15000)
groupings.api.grouper.person-source-id         # Subject source of UH people, searched first (default UH core LDAP)
groupings.api.async.threads                    # Threads the @Async jobs run on (default 10)
groupings.api.async.queue-capacity             # @Async jobs that wait for a thread before new ones are rejected (default 100)
groupings.api.async.finished-job-retention-millis # How long a finished job stays readable after it finishes (default 600000)
groupings.api.async.job-eviction-interval-millis  # How often finished jobs past their retention are evicted (default 60000)
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
