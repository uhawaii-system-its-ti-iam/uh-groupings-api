# AWS Deployment Scope — API Project vs UI Project

> **Read this first when working anywhere under `aws/`.**
> This document is shared **verbatim** between the `uh-groupings-api` and
> `uh-groupings-ui` repositories. It defines *which project deploys what* so the
> two CloudFormation stacks compose into one running system. The visual source
> of record is [`docs/aws-architecture.mmd`](docs/aws-architecture.mmd);
> narrative architecture lives in [`docs/AWS_ARCHITECTURE.md`](docs/AWS_ARCHITECTURE.md).

## The one-sentence rule

**The API repo (`uh-groupings-api`) deploys everything the API application needs
to run as a *private* service; the UI repo (`uh-groupings-ui`) deploys the
public-facing UI and the edge that fronts it. Neither repo creates the VPC or
the data-center link.** The API is deployed **first** and must stand alone.

## Edge model: Cloudflare Tunnel (no public load balancer)

Public entry is via **Cloudflare** fronting a **Cloudflare Tunnel**
(`cloudflared`) that the UI task runs as an **outbound** connection. Cloudflare
terminates TLS at the edge and the tunnel carries traffic to the UI task, so
there is **no internet-facing ALB and no public HTTPS listener in AWS**. A
single-AZ test environment gains nothing from an ALB, so **neither repo
provisions one**. (This supersedes the earlier internet-facing-ALB design.)

## Ownership at a glance

| Layer | VPC team (referenced, never created by either repo) | API repo (`uh-groupings-api`) | UI repo (`uh-groupings-ui`) |
|---|---|---|---|
| VPC | VPC (`VPC_ID` in `aws/.env`) | — | — |
| Internet edge | — | — | Cloudflare (DNS/TLS/WAF/DDoS) + Cloudflare Tunnel (`cloudflared`) |
| Data-center link | Connectivity to on-prem Grouper WS — *mechanism pending infra confirmation* (see below) | — | — |
| Subnets | — | **One** private subnet for the **API** task + the VPC endpoints (`SUBNET_CIDR` in `.env`) | **All** UI subnets, including outbound egress for the tunnel |
| Load balancing | — | **None** | **None** (Cloudflare Tunnel) |
| Compute | — | ECS cluster, Service Connect namespace, **API** service + task def | **UI** service + task def (joins the API's namespace) + `cloudflared` |
| Security groups | — | `sg-api-backend` (API tasks), `sg-vpce` (endpoints) | `sg-ui-apps` **and** the ingress rule that lets `sg-ui-apps` reach `sg-api-backend:8080` |
| Supporting | — | ECR repo, Secrets Manager secrets, IAM roles, CloudWatch logs, CI/CD pipeline | UI-side supporting resources + Cloudflare config (external to AWS) |

**The API repo creates no UI-facing AWS elements** — no shared or public
subnets, no ALB, no UI security groups. The only cross-project coupling is
(a) the interface the API **exports** and (b) the UI **adding** one ingress rule
and **joining** the API's Service Connect namespace.

## What the API repo's CloudFormation creates (the API app's full runtime)

Mapped to the templates in `aws/cloudformation/`:

- **`vpc.yml`** — Inside the pre-existing VPC: **one** subnet (CIDR from
  `SUBNET_CIDR` in `.env`) that is **private by posture**
  (`MapPublicIpOnLaunch: false`), plus the VPC endpoints that let the task reach
  AWS services with **no public IP and no NAT**: `ecr.api`, `ecr.dkr`,
  `secretsmanager`, `logs` (interface) and `s3` (gateway, on the main route
  table), fronted by the `sg-vpce` endpoint security group. Exports the subnet
  id. There are **no public subnets and no ALB**.
  - **SINGLE SUBNET / SINGLE AZ is deliberate.** The API runs one task
    (`DesiredCount: 1`), so a second subnet would sit empty and misrepresent the
    deployment as multi-AZ. Do not add one speculatively. Enabling real multi-AZ
    is a coordinated change: add a second subnet resource in a different AZ, add
    the `SubnetId` output to a comma-joined list, widen `sg-vpce` ingress, decide
    whether the interface endpoints need an ENI in both subnets, and raise
    `DesiredCount`.
- **`ecr-repository.yml`** — the container image registry.
- **`ecs-service.yml`** — ECS Fargate cluster, the Service Connect namespace
  (exported), the **API** service + task definition (container port **8080**,
  `AssignPublicIp: DISABLED`, no load balancer), the `sg-api-backend` task
  security group (**no ingress** — the UI adds it later), IAM task/execution
  roles, and the CloudWatch log group.
  - **UI→API traffic** arrives via **ECS Service Connect** (HTTP/TLS 8080);
    access is controlled purely by **`sg-api-backend`**, which will admit 8080
    **only from the UI's `sg-ui-apps`**.
  - The container **health check is intentionally disabled** until the Grouper
    path is verified end-to-end (see below), so the task is not restart-looped.
- **`codepipeline.yml`** — CodePipeline + CodeBuild that build and deploy the
  API image.

Secrets (`groupings/api/grouper-password`, `groupings/api/jwt-secret`) are
created by `setup.sh` and injected into the task via `secrets[]`.

## What the UI repo adds

- **Cloudflare + Cloudflare Tunnel**: the UI task runs `cloudflared` as an
  outbound tunnel; Cloudflare handles DNS/TLS/WAF at the edge. No public ALB.
- **All UI subnets** plus whatever **outbound egress** the tunnel needs (the API
  repo provides none of this).
- **`sg-ui-apps`** and a standalone `AWS::EC2::SecurityGroupIngress` that adds
  `sg-ui-apps` → `sg-api-backend:8080` (referencing the API's exported SG id).
- The **UI ECS service**, joined to the **API-owned** Service Connect namespace
  so it can reach the API by service name.

## Cross-stack contract (why API-first works)

- The API stack **exports** a stable interface for the UI to consume
  (CloudFormation exports, named `<stack-name>-<key>`):

  | Export key | Stack | What the UI does with it |
  |---|---|---|
  | `ApiTaskSecurityGroupId` | ecs | Target of the `sg-ui-apps` → 8080 ingress rule it adds |
  | `ServiceConnectNamespaceArn` | ecs | Namespace its own service joins |
  | `ServiceConnectDnsName` | ecs | Name it calls the API by (`groupings-api:8080`) |
  | `ClusterName` / `ServiceName` | ecs | Reference / operational lookups |
  | `SubnetId` | vpc | Reference (the UI provisions its own subnets) |
  | `EndpointSecurityGroupId` | vpc | Troubleshooting visibility only |
- The UI stack **only imports and adds**. It must **never mutate API-owned
  subnets, route tables, or security groups** — its sole reach into API
  territory is *adding* one ingress rule to `sg-api-backend` and *joining* the
  namespace.
- Because the UI is the API's only network client (server-to-server; the browser
  talks only to Cloudflare/UI), the SG source restriction is genuinely
  enforceable and replaces the need for an API-side load balancer.

## Grouper WS connectivity (pending infrastructure-team confirmation)

Grouper WS in the UH data center is a **live, required dependency** of the API
(no longer deferred). The API task reaches it over **HTTPS** (currently
`128.171.94.186:443`). **How VPC traffic reaches the data center is not yet
confirmed**, so the diagram marks this path as *pending infra*. Open questions
for the infrastructure team:

1. **Mechanism** — VGW + IPsec site-to-site VPN, Transit Gateway, or Direct
   Connect? Does the link already exist, or must it be provisioned?
2. **Route + ownership** — which destination CIDR(s) route to the gateway (e.g.
   `128.171.0.0/16`), and who adds the route to which route table?
3. **Endpoint** — stable IP `128.171.94.186:443`, or a DNS name / VIP? Any
   failover target?
4. **DNS** — will the Grouper hostname resolve from inside the VPC, or must we
   connect by IP?
5. **On-prem firewall** — which source addresses must be allow-listed (our
   subnet CIDR `172.18.10.16/28`, or a translated address)?
6. **Client auth** — client certificate / mTLS or IP allow-listing required in
   addition to the service-account credentials? Any CA trust bundle to ship?
7. **HA / SLA** — is the link redundant across AZs; expected latency/bandwidth?

Until these are answered, the data-center path stays marked "pending infra" and
the container health check (`/uhgroupingsapi/actuator/health`) remains disabled.

## Target traffic path

```
Browser → Cloudflare (TLS/WAF) → Cloudflare Tunnel → UI task
        → Service Connect (HTTP/TLS 8080) → API task
        → AWS services via VPC endpoints
        → on-prem Grouper WS via the data-center link (mechanism pending infra)
```

## Verification scope: the API is not exercised until the UI is deployed

**Decision:** the deployed API will **not** be functionally exercised before the
companion UI stack exists. The first end-to-end request is a request through the
UI. Do not add tooling, permissions, or infrastructure whose only purpose is to
poke the API earlier.

Concretely, this means:

- **`aws ecs execute-command` is deliberately NOT enabled.** `ecs-service.yml`
  does not set `EnableExecuteCommand: true`, and the ECS **task** role
  intentionally carries no `ssmmessages:*` permissions. Enabling it would widen
  the task role to obtain a shell we have decided we do not need. Do not add
  either one, and do not document `execute-command` as a verification step.
- **There is nothing to `curl`.** No ALB, no public endpoint, and no inbound
  client until the UI adds its ingress rule. The API is unreachable by design,
  not by omission.
- **No container health check.** Independently justified (the health endpoint
  depends on Grouper, whose connectivity mechanism is pending), and consistent
  with this decision: nothing probes the app before the UI arrives.

### What "verified" means before the UI exists

Provisioning is confirmed at the infrastructure level only:

1. The CloudFormation stacks reach `CREATE_COMPLETE` / `UPDATE_COMPLETE`.
2. The ECS service reports `runningCount` matching `desiredCount` — proving the
   image pulled through the ECR endpoints and the two secrets resolved through
   the Secrets Manager endpoint. This is the real signal that the no-NAT
   networking works.
3. CloudWatch Logs show `Started SpringBootWebApplication`.
4. The cross-stack exports the UI needs are present (see the contract table
   above).

Items 1, 2, and 4 are all reported by:

```bash
make aws-status
```

Use it rather than assembling the equivalent `describe-stacks` /
`describe-services` calls by hand. Item 3 is `make aws-logs`.

Grouper-related errors in the logs at this stage are **expected** and are not a
provisioning failure — see "Grouper WS connectivity" above.

Anything beyond this list waits for the UI. When the UI is deployed, the first
functional test is a request through the UI that traverses Service Connect to
the API; a `403`/timeout at that point points at the UI's ingress rule or
namespace join, not at the API stack.

## Deferred / explicitly out of current scope

- **Multi-environment (test → prod promotion)** — a single **test** tier for now
  (`APP_TIER=test`).
- **Multi-AZ high availability** — the **test** environment runs **one subnet in
  one AZ** with **`DesiredCount: 1`**. Multi-AZ is a prod concern for both repos
  and requires adding a subnet, not just raising the task count (see `vpc.yml`
  above).
- **More CIDR space** — revisit for prod; today's single `/28` (11 usable IPs
  after AWS reserves 5) holds the task ENI plus four endpoint ENIs, which is
  adequate for one task but leaves little headroom.

*(On-prem Grouper connectivity is no longer in this list — it is a live
dependency; only the connectivity **mechanism** is pending, as above.)*

## Template status

The API templates match this target: `ecs-service.yml` provisions **no ALB**
(Service Connect + `sg-api-backend` only) and exports the cross-stack interface;
`vpc.yml` creates only private-posture subnets + VPC endpoints. `aws/.env`
retains `API_HOSTNAME`, `API_CERTIFICATE_ARN`, and `API_HOSTED_ZONE_ID` as
blank/deprecated keys that `setup.sh` no longer reads, and `check-vpc.sh` treats
Internet Gateway presence as informational rather than required.

There is deliberately **no `appspec.yml`**: CodeDeploy blue/green for ECS
requires a load balancer with two target groups, so it cannot apply to this
architecture. Do not add one back without first adding a load balancer.

`aws/README.md` and `aws/docs/` have been brought in line with this document.
**If any of them disagree with this file, this file wins.**

## Rules for `docs/aws-architecture.mmd`

The Mermaid file is the **visual source of record** for the topology described
above. When the ownership model, templates, or traffic path change, update the
diagram in the same commit. These rules exist because Mermaid fails in quiet
ways — bad syntax renders a blank box, and long labels silently overflow their
node.

### Verify every edit by rendering

Never commit a change you have not rendered. A diagram that parses is not the
same as a diagram that is readable.

```bash
npx -y -p @mermaid-js/mermaid-cli mmdc \
  -i aws/docs/aws-architecture.mmd -o /tmp/arch-check.png -w 1800
```

Open the PNG and confirm: no clipped text, no label spilling past its node
border, no overlapping edges obscuring a label. Delete the temp file when done.
The `.mmd` file is what gets committed — do **not** commit generated SVG/PNG
output.

### Markup constraints

- **Line breaks are `<br/>`, never `\n`.** The literal `\n` escape is deprecated
  in current Mermaid and renders inconsistently across versions (sometimes as a
  literal backslash-n). `<br/>` is stable.
- **Quote every label** that contains spaces, punctuation, or `<br/>`:
  `NODE["First line<br/>second line"]`.
- **Stick to basic shapes** — `["rect"]` for AWS/infrastructure resources,
  `(["stadium"])` for actors and systems outside our control (browser, on-prem
  Grouper). Avoid exotic shapes; they add no information and vary by renderer.
- **Avoid characters that break the parser.** Do not put unescaped `(`, `)`,
  `{`, `}`, `[`, `]`, `#`, `;`, or `"` inside an unquoted label. Prefer a
  hyphen over an em dash and over a `/` when separating clauses in subgraph
  titles — subgraph titles are the most fragile labels in the file.
- **Keep lines short.** Roughly 45 characters per `<br/>`-separated line. Longer
  lines force the node wider than its neighbors and cause the overflow this
  section is meant to prevent.
- **Cap a node at about 6 label lines.** If an element needs more, it is really
  two elements — split it.

### Content rules

Every element label carries **two trailing parenthetical lines, in this order**:
first the AWS component, then the owning project. Both are required; a node with
one and not the other is incomplete.

```
APITASK["API task, port 8080<br/>AssignPublicIp DISABLED<br/>(Amazon ECS on AWS Fargate)<br/>(Groupings API)"]
```

#### Line 1 of 2 — the AWS component

- Use the official service name (`Amazon ECS`, `AWS Fargate`, `AWS PrivateLink`,
  `Amazon EC2 security groups`, `AWS Secrets Manager`), not an abbreviation or
  an internal nickname.
- **Non-AWS elements say so explicitly** — `(no AWS service)`,
  `(external to AWS)`, `(on-prem - not AWS)`. Making the boundary explicit is
  the point; leaving it implied invites the reader to assume everything in the
  picture is ours.

#### Line 2 of 2 — the owning project

Exactly one of:

| Marker | Meaning |
|---|---|
| `(Groupings API)` | Created and managed by the `uh-groupings-api` stacks |
| `(Groupings UI)` | Created and managed by the `uh-groupings-ui` stacks |
| `(Neither project - VPC team)` | Pre-existing, referenced but never created by either repo |
| `(Neither project)` | Outside both repos entirely (end-user browser, on-prem Grouper) |

Assign the marker by the **project whose CloudFormation creates the resource**,
not the project that consumes it. This is the distinction the diagram exists to
communicate, so get it right:

- **The Groupings API project provisions only what the API application itself
  needs** — to run its container and to reach Grouper WS. That is: its private
  subnets, its VPC endpoints, the ECS cluster and Service Connect namespace, the
  API task definition and service, `sg-api-backend` and `sg-vpce`, and the
  supporting ECR / Secrets Manager / IAM / CloudWatch / CI-CD resources. Nothing
  else.
- **The Groupings UI project provisions everything else**, including the AWS
  components that let the UI reach the API. The API repo creates
  `sg-api-backend` with **no ingress rule at all**; the UI repo adds the
  `sg-ui-apps` → `sg-api-backend:8080` ingress rule and joins the API's Service
  Connect namespace. So the security group itself is `(Groupings API)` while the
  **ingress rule that opens it** is `(Groupings UI)` — they are separate nodes
  precisely because they have different owners. Cloudflare and the tunnel are
  `(Groupings UI)` for the same reason.
- **A resource the API repo merely exports is still `(Groupings API)`.** Export
  is not a transfer of ownership. Likewise, a resource the UI repo imports stays
  `(Groupings API)`.
- **Pure annotation boxes are exempt** from both parentheticals — the
  `OPEN QUESTIONS` note is commentary, not an element.

#### Remaining content rules

- **No orphan nodes.** Every declared node needs at least one edge. A node with
  no edges states that a resource exists but not how it relates to anything,
  which is the one thing a diagram is for.

- **Label every edge** with the relationship or protocol (`HTTPS 443`,
  `Service Connect 8080`, `runs in`, `guarded by`). An unlabeled arrow is an
  assertion the reader has to guess at.

- **Solid arrows for live paths, dotted (`-.->`) for pending or
  reference-only** relationships — the data-center link to Grouper and the UI's
  cross-stack references are dotted for exactly this reason.

### Structural rules

- **Subgraph boundaries mirror ownership**, matching the table in this document:
  the VPC subgraph is VPC-team-provided, with `APIREPO` and `UIREPO` nested
  inside it. Do not draw a resource inside the API subgraph that the API repo
  does not create.
- **Subgraph placement and the `(Groupings …)` marker must agree.** Every node in
  `APIREPO` is `(Groupings API)`; every node in `UIREPO` is `(Groupings UI)`. A
  mismatch means either the marker or the placement is wrong — resolve it against
  the ownership table above, not by guessing.
- **Keep the diagram consistent with the templates.** No ALB, no NAT, no public
  subnets — if `ecs-service.yml` does not provision it, it does not appear as an
  API-owned element.
- **Pending infrastructure stays visibly pending.** The data-center path keeps
  its `PENDING INFRA CONFIRMATION` label and its dotted edge until the
  infrastructure team confirms the mechanism.
