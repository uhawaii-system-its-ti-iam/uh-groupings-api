# Certificate Management Strategy

## Purpose

This document defines the recommended TLS certificate strategy for the UH Groupings deployments, covering:

- the **API**
- the future **React UI**
- the future **Spring UI**
- the API's outbound HTTPS connection to the **Grouper Web Server** in the UH data center
- **automatic rotation** of certificates

It assumes the project continues to obtain publicly trusted certificates from **InCommon**.

---

## Executive Summary

Recommended strategy:

1. **Use stable DNS names** for every public service endpoint.
2. **Terminate TLS at AWS-managed front doors**:
   - ALB for the API
   - ALB or CloudFront/ALB for the UIs, depending on their final deployment model
3. **Use InCommon-issued certificates for every public hostname**.
4. **Automate issuance/renewal/import** of those InCommon certificates into ACM.
5. **Redirect all HTTP to HTTPS** and treat plaintext HTTP as a temporary sandbox-only fallback.
6. **Keep API → Grouper on HTTPS:443**, validating the Grouper server certificate through the JVM trust store.
7. **Do not use self-signed certificates** outside local development.
8. **Do not rely on manual renewal/import** as the long-term operating model.
9. **Drive the hostname and certificate from one explicit tier value** — ideally an `APP_TIER` variable in `aws/.env` — that also selects the Spring profile, so a non-prod tier always serves a `test` hostname/cert and prod never does. See [Environment, Spring Profile, and Certificate Coordination](#environment-spring-profile-and-certificate-coordination).

The key architectural point is that this project should not invent a separate certificate mechanism for each component. The API and both UIs should use the same operational pattern:

- stable hostname
- InCommon certificate
- TLS terminated at AWS edge/load balancer
- automated renewal before expiry
- controlled deployment of the replacement cert

---

## Scope and Trust Boundaries

### 1. Public ingress certificates

These are certificates presented to external clients connecting to AWS-hosted services.

Planned consumers:

- **Spring UI** users connecting to the Spring UI endpoint
- **React UI** users connecting to the React UI endpoint
- **UI server(s)** connecting to the API endpoint

Recommended hostnames (examples only):

- `groupings-api.<uh-domain>`
- `groupings-ui.<uh-domain>`
- `groupings-spring-ui.<uh-domain>`

The exact hostnames should be decided once the production DNS plan is finalized, but the strategy requires that they be **stable, named FQDNs**, not raw ALB hostnames.

### 2. Service-to-service TLS inside the application boundary

The companion UI deployments connect to the API over HTTPS.

Recommended controls for **UI → API**:

- TLS using a publicly trusted InCommon certificate on the API ALB
- ALB ingress restricted to the UI deployment(s) where feasible
- JWT authentication as already designed
- CORS pinned to the expected UI origin(s)

### 3. Outbound TLS from API to Grouper

The API connects to Grouper Web Services on **HTTPS:443** over the UH networking path/VPN.

Recommended controls for **API → Grouper**:

- Keep TLS end-to-end to the Grouper endpoint
- Validate the Grouper server certificate normally
- Trust the appropriate CA chain in the JVM trust store
- Treat the Grouper server certificate as **externally owned** by the Grouper/data-center team

This project should manage **trust and monitoring** for the Grouper certificate, but should not assume it owns issuance for that endpoint.

---

## Recommended Architecture

### A. API ingress

### Recommended pattern

- Internet-facing **ALB** presents the API certificate
- Listener **443/HTTPS** forwards to the target group
- Listener **80/HTTP** redirects to 443
- ECS tasks continue to serve HTTP on port 8080 inside the VPC unless a separate requirement appears for task-level TLS

### Why this is the right default

- AWS ALB is the operational TLS boundary already implied by the architecture
- certificate replacement is simpler at the ALB than inside the Java container
- the app remains unchanged and does not need to manage keystores, private keys, or Spring SSL config for inbound traffic
- automatic rotation can be centralized per environment/account

---

### B. Spring UI ingress

### Recommended pattern

Use the same pattern as the API:

- ALB terminates HTTPS with an InCommon cert
- HTTP redirects to HTTPS
- app/container can remain plain HTTP behind the ALB unless a specific internal-TLS requirement is introduced

This gives the Spring UI and API the same certificate lifecycle and automation model.

---

### C. React UI ingress

The right ingress depends on how the React UI is ultimately hosted.

### If the React UI is hosted behind ALB

Use the same pattern as the API and Spring UI.

### If the React UI is hosted via CloudFront

Recommended pattern:

- CloudFront presents the UI certificate
- the certificate still comes from InCommon, but must be imported into **ACM in `us-east-1`** for CloudFront use
- the UI origin can be S3, ALB, or another service depending on the final design

### Important note

The React UI's browser-facing certificate is separate from the API's server certificate. Both can come from InCommon, but they are attached to different AWS edge resources and may live in different ACM regions.

---

### D. API to Grouper Web Server

### Recommended pattern

- keep the API configured to talk to Grouper on `https://...:443`
- do not terminate or downgrade TLS in transit
- trust Grouper's certificate chain in the JVM
- do not use `curl -k`, trust-all managers, or disabled certificate validation in production

### Trust-store recommendation

Use one of these two models:

1. **Preferred:** Grouper presents a certificate chain already trusted by the standard JVM trust store.
2. **Fallback:** package a dedicated trust store for the API runtime containing the required InCommon/campus CA chain.

If the fallback is needed, the trust store should be:

- built in CI or prepared by automation
- delivered as deployment configuration/secrets, not committed with private material
- versioned operationally so trust updates can be rolled out before certificate cutover

### What this project owns vs. does not own

This project owns:

- making sure the API trusts the Grouper certificate chain
- monitoring Grouper certificate expiry and trust compatibility
- redeploying the API if trust configuration must change

This project does **not** own:

- the Grouper server certificate private key
- the Grouper issuance process itself

---

## Why stable DNS names matter

Automatic certificate rotation is much easier and safer when every service has a stable hostname.

### Required future state

Before production TLS is finalized, establish stable hostnames for:

- API
- Spring UI
- React UI

### Why raw ALB DNS names are not enough

- ALB-generated hostnames are infrastructure identifiers, not service identities
- InCommon certificates should be issued for service-owned DNS names
- a service hostname can survive ALB replacement; an ALB hostname cannot
- DNS-based validation and cutover workflows are much cleaner with stable names

---

## Environment, Spring Profile, and Certificate Coordination

This is the core of the "test vs prod" coordination problem: the **hostname/certificate deployed to the ALB must always match the application configuration tier the container runs**. A `test` URL must never be served by a container running the `aws-prod` profile, and vice versa.

The repo already has a single source of truth for the tier. The certificate and hostname must be driven by that **same** source — not chosen independently.

### The existing single source of truth

One input decides the tier today:

```
aws/.env: AWS_ENV
   │
   └─▶ CloudFormation parameter  Environment            (aws/cloudformation/ecs-service.yml)
          │
          └─▶ Condition  IsProd = (Environment == prod)
                 │
                 └─▶ SPRING_PROFILES_ACTIVE = IsProd ? aws-prod : aws-test
                        │
                        └─▶ application-aws-prod.properties  OR  application-aws-test.properties
                               (Grouper URL, app.environment=test|prod, email on/off, …)
```

So `AWS_ENV` → `IsProd` already selects the Spring profile **and** the properties file. The recommendation is simple: **extend that same `IsProd` decision to also select the hostname and certificate.** Nothing new decides the tier; the cert just rides the existing switch.

### The required extension

```
                 IsProd = (Environment == prod)
                    │
      ┌─────────────┼───────────────────────────────┐
      │             │                                │
      ▼             ▼                                ▼
SPRING_PROFILES   Public hostname                ACM certificate
_ACTIVE           (must contain "test"           (must match the
(aws-test/        for every non-prod tier)       selected hostname)
 aws-prod)
```

Because all three branches read the **same** `IsProd`, they cannot drift apart.

### Non-prod hostname rule ("test" in the URL)

Adopt a naming convention where **every non-prod public hostname contains `test`**, and prod does not:

| Tier (`AWS_ENV`) | `IsProd` | Spring profile | Properties file | Example API hostname |
|---|---|---|---|---|
| `sandbx` | false | `aws-test` | `application-aws-test.properties` | `groupings-api-test.its.hawaii.edu` |
| `test` (future) | false | `aws-test` | `application-aws-test.properties` | `groupings-api-test.its.hawaii.edu` |
| `prod` | true | `aws-prod` | `application-aws-prod.properties` | `groupings-api.its.hawaii.edu` |

Notes:
- Every non-prod `AWS_ENV` maps to the **same** `aws-test` profile *and* the same `test` hostname/cert. That is intentional and consistent with how the profile mapping already collapses all non-prod environments onto `aws-test`.
- This matches the existing `app.environment=test` / `app.environment=prod` values in the two properties files, so the in-app environment label, the Grouper backend (`grouper-test` vs `grouper`), and the public hostname all agree.

### How to wire the certificate selection

Three viable designs. All keep a single tier decision; they differ in **where that decision is expressed** and **how explicit it is**.

#### Option A — select in CloudFormation with the existing `IsProd`

Pass both tiers' values into `ecs-service.yml` and let the **same** `IsProd` condition that already picks the Spring profile also pick the hostname and certificate:

```yaml
Parameters:
  TestCertificateArn:   { Type: String, Default: '' }
  ProdCertificateArn:   { Type: String, Default: '' }
  TestHostname:         { Type: String, Default: '' }   # e.g. groupings-api-test.its.hawaii.edu
  ProdHostname:         { Type: String, Default: '' }   # e.g. groupings-api.its.hawaii.edu

# Reuse the existing IsProd condition:
#   HTTPS listener cert:  !If [IsProd, ProdCertificateArn, TestCertificateArn]
#   DNS record / output:  !If [IsProd, ProdHostname, TestHostname]
```

Strongest structural guarantee: the profile and the cert are chosen by one condition in one template, so they are incapable of diverging. But the tier is still *inferred* from the environment **name** (`Environment == prod`), which is implicit.

#### Option B — resolve upstream in `setup.sh`, pass a single value

Keep the single `CertificateArn` parameter already in the template and compute the tier in `setup.sh` from `AWS_ENV`, passing the resolved hostname + cert:

```bash
# aws/setup.sh (sketch)
if [[ "${AWS_ENV}" == "prod" ]]; then
  CERT_ARN="${PROD_CERT_ARN}"; HOSTNAME="${PROD_HOSTNAME}"
else
  CERT_ARN="${TEST_CERT_ARN}"; HOSTNAME="${TEST_HOSTNAME}"
fi
```

Simpler template, but the tier logic now lives in two places (`IsProd` in CFN and the `if` in bash). If you choose Option B, keep both keyed to the literal `prod` check so they stay in lockstep.

#### Option C (recommended) — an explicit tier value in `aws/.env` as the single source of truth

Instead of *inferring* the tier from the environment name, **name it explicitly** with one new variable in `aws/.env`, and make every downstream artifact read that one value.

Today the tier is implicit: `IsProd` is derived from `AWS_ENV == prod`, and "everything not prod is test" is a rule buried in the template. Option C makes the tier a first-class, explicit input — which is exactly what "specify a value in `.env` as the single source of truth" asks for. It also cleanly **separates two concerns that are currently conflated**:

- `AWS_ENV` → **resource identity** (stack/resource naming, per-owner isolation: `sandbx`, `dev`, …)
- `APP_TIER` → **configuration tier** (Spring profile + hostname + certificate: `test` | `prod`)

`aws/.env`:

```dotenv
# Single source of truth for the deployment tier.
# Allowed values: test | prod
# Drives the Spring profile (aws-<tier>), the public hostname, and the ACM cert.
# Decoupled from AWS_ENV, which only names/identifies the resources.
APP_TIER=test
```

`aws/setup.sh` passes it straight through as a stack parameter:

```bash
# aws/setup.sh (sketch)
aws cloudformation deploy \
  ... \
  --parameter-overrides "Tier=${APP_TIER}" "CertificateArn=${CERT_ARN}" "Hostname=${HOSTNAME}" ...
```

`ecs-service.yml` reads that one value for **everything**:

```yaml
Parameters:
  Tier:
    Type: String
    Default: test
    AllowedValues: [test, prod]      # rejects typos at deploy time

Conditions:
  IsProd: !Equals [!Ref Tier, prod]  # tier now comes from the explicit value

# Spring profile maps 1:1 to the tier — no inference:
#   SPRING_PROFILES_ACTIVE: !Sub 'aws-${Tier}'     → aws-test | aws-prod
# Cert + hostname selected by the same value (Option A style, or passed in via setup.sh).
```

Why this is the best fit here:

- **One explicit knob.** `APP_TIER` is *the* source of truth. The Spring profile (`aws-${Tier}`), the hostname, and the cert all read it. Nothing is inferred from a name.
- **`AWS_ENV` stops doing double duty.** A shared sandbox (`AWS_ENV=sandbx`) can run `APP_TIER=test` — giving `aws-test` config and a `groupings-api-test…` URL — while resources stay named `…-sandbx-…`. That is precisely the "test in the URL for non-prod" outcome you want, without overloading the environment name.
- **`SPRING_PROFILES_ACTIVE` becomes `aws-${Tier}`** — a literal 1:1 mapping, removing the `!If` inference entirely for the profile.
- **`AllowedValues: [test, prod]`** turns a typo into an immediate CloudFormation validation error instead of a silently mis-tiered deploy.

Cost of Option C: one additional `.env` variable. Because `AWS_ENV` and `APP_TIER` are now distinct, add a small consistency guard so they can't contradict each other in dangerous ways (see the guardrail below) — e.g., `APP_TIER=prod` should only be used with the production environment.

> **Status: implemented.** This project now ships Option C:
> - `aws/.env` defines `APP_TIER` (`test`|`prod`) and `API_HOSTNAME` (blank until DNS/cert exist).
> - `aws/cloudformation/ecs-service.yml` has a `Tier` parameter (`AllowedValues: [test, prod]`) and a `Hostname` parameter; `SPRING_PROFILES_ACTIVE` is `!Sub 'aws-${Tier}'` (the `IsProd` inference was removed); the `LoadBalancerUrl` output uses the hostname when set and `https` when a certificate is present.
> - `aws/setup.sh` validates the tier before any AWS call (`APP_TIER` must be `test`/`prod`; `APP_TIER=prod` only when `AWS_ENV=prod`; non-prod `API_HOSTNAME` must contain `test`, prod must not) and passes `Tier`/`Hostname` to the ECS stack.
>
> Defaults are backward-compatible: with `APP_TIER=test` and an empty `API_HOSTNAME`/`CertificateArn`, the sandbox still runs the `aws-test` profile over HTTP on the raw ALB DNS name. A production deploy must set `APP_TIER=prod` (with `AWS_ENV=prod`).

### Guardrail: fail the deploy on a tier/hostname mismatch

Whichever option you choose, enforce the invariants automatically so a mistake can't ship. `setup.sh` is the natural place (it already validates inputs before any AWS call):

```bash
# aws/setup.sh (sketch) — TIER is AWS_ENV-derived (A/B) or APP_TIER (C)
TIER="${APP_TIER:-$([[ "${AWS_ENV}" == "prod" ]] && echo prod || echo test)}"

# 1) Hostname must match the tier
if [[ "${TIER}" == "prod" ]]; then
  [[ "${HOSTNAME}" == *test* ]] && { error "prod hostname must not contain 'test': ${HOSTNAME}"; exit 1; }
else
  [[ "${HOSTNAME}" != *test* ]] && { error "non-prod hostname must contain 'test': ${HOSTNAME}"; exit 1; }
fi

# 2) (Option C) Guard against a prod tier in a non-prod environment
if [[ "${TIER}" == "prod" && "${AWS_ENV}" != "prod" ]]; then
  error "APP_TIER=prod is only allowed when AWS_ENV=prod (got AWS_ENV=${AWS_ENV})"; exit 1
fi
```

CloudFormation `Rules` (plus `AllowedValues` on `Tier`) can enforce the same invariants template-side if you prefer a stack-level guard.

### Keep DNS and CORS on the same switch

Two more things must track the selected hostname so the whole tier is coherent:

- **DNS record:** the Route53 (or external) record that points the selected hostname at the ALB should be produced from the **same** tier decision (`APP_TIER` / `IsProd`) — not maintained by hand.
- **CORS / UI origin:** the API's allowed UI origin(s) should resolve to the tier-matching UI hostname (also a `test` host in non-prod). Driving CORS from the same tier value prevents a prod API from trusting a test UI origin or vice versa.

### One-line principle

**One tier value is the single source of truth — ideally an explicit `APP_TIER` in `aws/.env` — and the Spring profile, the properties file, the public hostname, the certificate, the DNS record, and the CORS origin all derive from it; never set independently.**

---

## Certificate Source Strategy: InCommon

The project requirement is to obtain certificates from **InCommon**.

### Recommended interpretation

Use **InCommon as the certificate authority of record** for public service certificates, but automate the operational workflow as much as possible.

### Best target state

Use an **InCommon-supported automated issuance flow** (for example, ACME if available in the current InCommon/Sectigo service model and approved by UH policy) with DNS validation against the authoritative DNS zone.

That gives the project the trust requirements of InCommon **and** the operational benefits of automation.

### If InCommon automation is not immediately available

The next-best pattern is still:

- request/renew from InCommon
- import into ACM
- automate ALB/CloudFront updates

But treat that as an interim state only. Manual certificate renewal/import does not meet the long-term operational goal.

---

## Automatic Rotation Strategy

### Recommended automation components

For public ingress certificates, build one shared automation workflow per AWS account/region combination:

- **EventBridge schedule** (daily or weekly)
- **Lambda or CodeBuild job** to run renewal/import logic
- **Route53 DNS update permissions** if DNS validation is automated
- **ACM import/update permissions**
- **ELB/CloudFront update permissions**
- **SNS / email / Slack notification** for success/failure/expiring-soon alerts

### Rotation window

Recommended:

- begin renewal **30 days before expiry**
- alert at **30 / 14 / 7 / 3 / 1 days** if automation has not completed

### Rotation workflow for ALB-backed services

1. Discover certificates by tag or service metadata.
2. Check expiry.
3. Request/renew a replacement certificate from InCommon.
4. Import the new certificate into ACM.
5. Attach/update the certificate on the ALB HTTPS listener.
6. Validate listener health and service health.
7. Keep the old certificate long enough for rollback confidence.
8. Remove the old certificate after verification.
9. Emit audit/notification events.

### Rotation workflow for CloudFront-backed services

Same overall pattern, except:

- the certificate must exist in **ACM `us-east-1`**
- the distribution must be updated to reference the new certificate
- allow for CloudFront propagation time

---

## Private key handling

### Requirements

- private keys must never be committed to Git
- private keys must not be left in developer-owned working directories as the standard operating model
- private keys should be generated/handled in ephemeral automation contexts where practical

### Recommended handling for imported ACM certs

- generate or receive key/cert material inside the automation job
- import immediately into ACM
- destroy local plaintext artifacts after import
- restrict who can request/export/source the cert material
- tag imported ACM certs with service/environment ownership

If the InCommon process requires human involvement for key/cert retrieval initially, document it as an exception and move toward fully automated retrieval.

---

## Inventory and ownership model

Use a single inventory table for operational ownership. Non-prod hostnames contain `test` (e.g., `groupings-api-test.<uh-domain>`); the prod hostnames below omit it.

| Certificate use | Example hostname | AWS attachment point | Source | Rotation owner | Notes |
|---|---|---|---|---|---|
| API public ingress | `groupings-api.<uh-domain>` | API ALB (regional ACM) | InCommon | App/platform team | UI servers call this endpoint |
| Spring UI public ingress | `groupings-spring-ui.<uh-domain>` | Spring UI ALB (regional ACM) | InCommon | App/platform team | Browser-facing |
| React UI public ingress | `groupings-ui.<uh-domain>` | ALB or CloudFront (`us-east-1` ACM if CloudFront) | InCommon | App/platform team | Browser-facing |
| Grouper server certificate | Grouper WS FQDN | Grouper-managed endpoint | Grouper/data-center team | Grouper/data-center team | This project manages trust/monitoring, not issuance |

---

## Recommended security posture by connection

| Connection | TLS recommendation | Auth recommendation | Notes |
|---|---|---|---|
| Browser → Spring UI | HTTPS with InCommon cert | CAS/session as designed by UI | Browser-facing public endpoint |
| Browser → React UI | HTTPS with InCommon cert | UI-specific session/token model | Browser-facing public endpoint |
| Spring UI server → API | HTTPS with InCommon cert | JWT + SG/IP restriction | Server-to-server |
| React UI server → API | HTTPS with InCommon cert | JWT + SG/IP restriction | Server-to-server |
| API → Grouper WS | HTTPS with validated server cert | Grouper service account credentials | Over VPN/data-center path |

### mTLS?

Not the recommended first step.

For this project, the better near-term control set is:

- TLS server authentication with InCommon certs
- security-group restriction between AWS services
- JWT authentication/authorization at the API layer

mTLS can be revisited later if a policy requirement emerges, but it adds client-certificate issuance, trust-store distribution, and rotation complexity across multiple services.

---

## What not to do

Avoid these patterns except for local development experiments:

- **self-signed certificates** for shared sandbox, test, or production ingress
- **manual ALB cert swaps** as the steady-state operating model
- **committing private keys or PKCS#12/JKS files** to the repo
- **disabling certificate validation** for API → Grouper
- **pinning to leaf certificates** where routine renewal would cause avoidable breakage
- **using raw ALB DNS names as the long-term public endpoint identity**

---

## Recommended phased rollout

### Phase 1 — decide names and trust boundaries

- assign stable DNS names for API, Spring UI, and React UI
- adopt the non-prod naming rule: every non-prod hostname contains `test`, prod does not
- confirm which services are browser-facing vs server-to-server only
- confirm whether React UI uses ALB or CloudFront
- confirm what certificate chain Grouper presents today

### Phase 2 — make ALB/edge TLS ready everywhere

- ensure API/UI templates can accept certificate ARNs
- enforce HTTP → HTTPS redirect where certificates exist
- standardize tagging for certificate discovery and rotation

### Phase 3 — automate InCommon renewal/import

- implement scheduled renewal automation
- implement ACM import and listener/distribution update steps
- add notifications and failure alerts

### Phase 4 — production hardening

- restrict API ALB ingress to the UI deployment(s)
- validate end-to-end behavior across all UIs and the API
- test certificate rotation in sandbox before production
- document runbooks for renewal failures and emergency replacement

---

## Immediate recommendation for this project

Given the current state of the repo and the future direction:

1. **Keep the API template capable of running with or without a certificate** while certificate provisioning is unresolved.
2. **Standardize now on certificate-ARN-driven ALB configuration** so future TLS enablement is a configuration change, not a template redesign.
3. **Plan on InCommon-issued certificates imported into ACM**, with automation added around renewal/import.
4. **Use stable DNS names before treating TLS as production-ready**.
5. **Make one explicit tier value the single source of truth** — an `APP_TIER` (`test`|`prod`) in `aws/.env` that selects the Spring profile, hostname, and certificate together — and enforce the `test`-in-non-prod-hostname rule with a `setup.sh` guardrail.
6. **Treat API → Grouper certificate trust as a separate concern from ALB/UI certificates**.
7. **Build one rotation pipeline shared across API and UI services**, rather than a one-off process per application.

That approach gives the project:

- public trust via InCommon
- compatibility with AWS ALB/CloudFront
- automatic rotation
- a single strategy for API + UI services
- a separate, correctly scoped trust model for Grouper

---

## Related docs

- [ARCHITECTURE.md](ARCHITECTURE.md)
- [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md)
- [AWS_QUICKSTART.md](AWS_QUICKSTART.md)
- [SECRETS.md](SECRETS.md)
- [AGENTS.md](../AGENTS.md)


