#!/usr/bin/env bash
# aws/check-vpc.sh - Validate that the VPC referenced in aws/.env meets
# all requirements for this project.
#
# Checks (hard requirements — a failure blocks `make aws-setup`):
#   1. VPC exists in the configured AWS_REGION.
#   2. The VPC has a main route table.
#   3. That main route table routes 0.0.0.0/0 to an Internet Gateway. REQUIRED:
#      the public subnet created by vpc.yml inherits the main route table, and
#      that IGW route is what gives the NAT Gateway its path to the internet.
#      Without it the NAT is useless and the API cannot reach Grouper WS.
#   4. Both subnet CIDRs (PRIVATE_SUBNET_CIDR / PUBLIC_SUBNET_CIDR from
#      aws/.env) fall within the VPC's CIDR block, do not overlap each other,
#      and do not overlap an existing subnet.
#
# The deployment is SINGLE-AZ (both subnets in the region's first AZ, one task),
# so there is no multi-AZ requirement to validate.
#
# Informational only (reported, never fails the run):
#   - Availability Zone count and which AZ the subnets will land in.
#   - Whether a NAT Gateway already exists in the VPC.
#
# Note on Grouper connectivity: the API reaches Grouper WS over the PUBLIC
# INTERNET via the NAT Gateway (Grouper is behind an F5 with a public IP). There
# is no VPN, Transit Gateway, or Direct Connect to check for. What this script
# cannot verify is whether the UH Palo Alto firewall allow-lists the NAT
# Gateway's Elastic IP — that is a manual request to the network team.

# Note: intentionally NOT using `set -e`. Each check manages its own
# pass/fail and the script reports a summary + exit code at the end; letting
# a single failed `aws` call abort the whole run would skip that reporting.
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

log()   { printf '%s\n' "$1"; }
pass()  { printf '  ✓ %s\n' "$1"; }
fail()  { printf '  ✗ %s\n' "$1"; FAILURES=$((FAILURES + 1)); }
warn()  { printf '  ⚠ %s\n' "$1"; }
# Informational: reported for visibility, never affects the exit code.
info()  { printf '  · %s\n' "$1"; }

FAILURES=0

# Load .env
if [[ ! -f "${ENV_FILE}" ]]; then
    printf 'Error: %s not found\n' "${ENV_FILE}" >&2
    exit 1
fi
set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

AWS_REGION="${AWS_REGION:-us-west-2}"
VPC_ID="${VPC_ID:-}"

# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib-auth.sh"

if [[ -z "${VPC_ID}" ]]; then
    printf 'Error: VPC_ID is not set in aws/.env\n' >&2
    exit 1
fi

# The subnet CIDRs come from aws/.env (kept in sync with the vpc.yml parameter
# defaults) — not hardcoded here.
PRIVATE_SUBNET_CIDR="${PRIVATE_SUBNET_CIDR:-}"
PUBLIC_SUBNET_CIDR="${PUBLIC_SUBNET_CIDR:-}"
if [[ -z "${PRIVATE_SUBNET_CIDR}" || -z "${PUBLIC_SUBNET_CIDR}" ]]; then
    printf 'Error: PRIVATE_SUBNET_CIDR and PUBLIC_SUBNET_CIDR must be set in aws/.env\n' >&2
    printf '  (they must match the PrivateSubnetCidr/PublicSubnetCidr defaults in\n' >&2
    printf '   aws/cloudformation/vpc.yml)\n' >&2
    exit 1
fi

# Ensure a usable AWS session (bootstraps the profile and/or opens a browser
# to sign in as needed). Exports AWS_PROFILE for the checks below.
ensure_aws_session || exit 1

log ""
log "Checking VPC: ${VPC_ID} in ${AWS_REGION}"
log "─────────────────────────────────────────────────"

# 1. VPC exists
if ! VPC_INFO="$(aws ec2 describe-vpcs \
  --vpc-ids "${VPC_ID}" \
  --query 'Vpcs[0].{State:State,CidrBlock:CidrBlock}' \
  --output json \
  --region "${AWS_REGION}" 2>&1)"; then
    fail "VPC ${VPC_ID} not found in ${AWS_REGION}"
    log ""
    log "  AWS error: ${VPC_INFO}"
    log ""
    log "Result: FAILED"
    exit 1
fi
VPC_CIDR="$(printf '%s' "${VPC_INFO}" | python3 -c "import sys,json; print(json.load(sys.stdin)['CidrBlock'])")"
pass "VPC exists (CIDR: ${VPC_CIDR})"

# 2. Main route table must exist — the public subnet inherits it.
MAIN_RT="$(aws ec2 describe-route-tables \
  --filters "Name=vpc-id,Values=${VPC_ID}" "Name=association.main,Values=true" \
  --query 'RouteTables[0].RouteTableId' \
  --output text \
  --region "${AWS_REGION}")"

if [[ -z "${MAIN_RT}" || "${MAIN_RT}" == "None" ]]; then
    fail "No main route table found for ${VPC_ID}"
else
    pass "Main route table found (${MAIN_RT})"
fi

# 3. Internet Gateway + default route — HARD REQUIREMENT.
#    The public subnet created by vpc.yml inherits the main route table. That
#    table's 0.0.0.0/0 → IGW route is what gives the NAT Gateway its path out.
#    Without it, the NAT Gateway is provisioned but useless and the API cannot
#    reach Grouper WS.
IGW_ID="$(aws ec2 describe-internet-gateways \
  --filters "Name=attachment.vpc-id,Values=${VPC_ID}" \
  --query 'InternetGateways[0].InternetGatewayId' \
  --output text \
  --region "${AWS_REGION}")"

if [[ -z "${IGW_ID}" || "${IGW_ID}" == "None" ]]; then
    fail "No Internet Gateway attached to ${VPC_ID} — required for NAT Gateway egress"
else
    pass "Internet Gateway attached (${IGW_ID})"
fi

if [[ -n "${MAIN_RT}" && "${MAIN_RT}" != "None" ]]; then
    DEFAULT_ROUTE="$(aws ec2 describe-route-tables \
      --route-table-ids "${MAIN_RT}" \
      --query "RouteTables[0].Routes[?DestinationCidrBlock=='0.0.0.0/0'].GatewayId" \
      --output text \
      --region "${AWS_REGION}")"

    if [[ "${DEFAULT_ROUTE}" == igw-* ]]; then
        pass "Main route table routes 0.0.0.0/0 → ${DEFAULT_ROUTE} (NAT egress path)"
    elif [[ -z "${DEFAULT_ROUTE}" || "${DEFAULT_ROUTE}" == "None" ]]; then
        fail "Main route table (${MAIN_RT}) has no 0.0.0.0/0 route — NAT Gateway would have no path out"
    else
        fail "Main route table (${MAIN_RT}) routes 0.0.0.0/0 → ${DEFAULT_ROUTE} (not an IGW)"
    fi
fi

# 3b. Existing NAT Gateway — INFORMATIONAL. Reported so a leftover NAT from a
#     prior run (which still bills ~$32/month) does not go unnoticed.
EXISTING_NAT="$(aws ec2 describe-nat-gateways \
  --filter "Name=vpc-id,Values=${VPC_ID}" "Name=state,Values=available,pending" \
  --query 'NatGateways[].NatGatewayId' \
  --output text \
  --region "${AWS_REGION}" 2>/dev/null)"

if [[ -n "${EXISTING_NAT}" && "${EXISTING_NAT}" != "None" ]]; then
    info "NAT Gateway(s) already present in this VPC: ${EXISTING_NAT}"
else
    info "No NAT Gateway yet — 'make aws-setup' creates one"
fi

# 3c. The UH firewall allow-list cannot be verified from here. Remind, don't fail.
info "Reminder: the NAT Gateway's Elastic IP must be allow-listed on the UH"
info "  Palo Alto firewall before Grouper WS calls will succeed"

# 4. The subnet CIDR (from aws/.env) is usable: inside the VPC's CIDR block(s)
#    and not overlapping any existing subnet. This is the exact condition that,
#    if violated, makes the vpc stack roll back during `make aws-setup`.
#
#    Note: if the vpc stack is already deployed, its own subnet WILL appear in
#    the existing-subnet list and register as an overlap. That is expected on a
#    re-check and does not indicate a problem — CloudFormation recognizes the
#    subnet as already managed.
VPC_CIDRS="$(aws ec2 describe-vpcs \
  --vpc-ids "${VPC_ID}" \
  --query 'Vpcs[0].CidrBlockAssociationSet[].CidrBlock' \
  --output text \
  --region "${AWS_REGION}")"

EXISTING_CIDRS="$(aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=${VPC_ID}" \
  --query 'Subnets[].CidrBlock' \
  --output text \
  --region "${AWS_REGION}")"

CIDR_REPORT="$(VPC_CIDRS="${VPC_CIDRS}" EXISTING_CIDRS="${EXISTING_CIDRS}" \
  CAND_PRIVATE="${PRIVATE_SUBNET_CIDR}" CAND_PUBLIC="${PUBLIC_SUBNET_CIDR}" python3 - <<'PY'
import os, ipaddress

vpc = [ipaddress.ip_network(c) for c in os.environ.get("VPC_CIDRS", "").split()]
existing = [ipaddress.ip_network(c) for c in os.environ.get("EXISTING_CIDRS", "").split()]

parsed = {}
for label, key in (("private", "CAND_PRIVATE"), ("public (NAT)", "CAND_PUBLIC")):
    raw = os.environ[key]
    try:
        net = ipaddress.ip_network(raw)
    except ValueError as e:
        print(f"FAIL|{label} ({raw}): invalid CIDR ({e})")
        continue
    parsed[label] = net
    if not any(net.subnet_of(v) for v in vpc):
        print(f"FAIL|{label} ({raw}): not within VPC CIDR [{', '.join(map(str, vpc))}]")
        continue
    clash = [str(s) for s in existing if net.overlaps(s)]
    if clash:
        print(f"FAIL|{label} ({raw}): overlaps existing subnet(s) {', '.join(clash)}")
        continue
    print(f"OK|{label} ({raw}): within VPC and unused")

# The two new subnets must not collide with each other.
if len(parsed) == 2:
    a, b = parsed["private"], parsed["public (NAT)"]
    if a.overlaps(b):
        print(f"FAIL|private ({a}) overlaps public ({b}) — they must be distinct")
    else:
        print(f"OK|private and public CIDRs do not overlap each other")
PY
)"

while IFS= read -r line; do
    [[ -z "${line}" ]] && continue
    case "${line}" in
        OK\|*)   pass "Subnet CIDR ${line#OK|}" ;;
        FAIL\|*) fail "Subnet CIDR ${line#FAIL|}" ;;
        *)       warn "CIDR check: ${line}" ;;
    esac
done <<< "${CIDR_REPORT}"

# If the vpc stack already exists, an "overlaps" failure above is just this
# project's own subnet. Say so, so the output isn't misread.
if aws cloudformation describe-stacks \
     --stack-name "${AWS_PROJECT_ID:-groupings-api}-vpc-${AWS_ENV:-sandbx}" \
     --region "${AWS_REGION}" >/dev/null 2>&1; then
    info "The vpc stack already exists — a CIDR 'overlaps' failure above is likely its own subnets"
fi

if [[ -n "${EXISTING_CIDRS}" ]]; then
    log "  (existing subnets in VPC: $(printf '%s' "${EXISTING_CIDRS}" | tr '\n' ' '))"
fi

# 5. Availability Zones — INFORMATIONAL. The deployment is single-AZ (one
#    subnet, one task), so there is no minimum to enforce. Reported only so the
#    AZ the subnet will land in is visible, and so the headroom for a future
#    multi-AZ change is known.
AZ_COUNT="$(aws ec2 describe-availability-zones \
  --filters "Name=region-name,Values=${AWS_REGION}" "Name=state,Values=available" \
  --query 'length(AvailabilityZones)' \
  --output text \
  --region "${AWS_REGION}")"

FIRST_AZ="$(aws ec2 describe-availability-zones \
  --filters "Name=region-name,Values=${AWS_REGION}" "Name=state,Values=available" \
  --query 'AvailabilityZones[0].ZoneName' \
  --output text \
  --region "${AWS_REGION}")"

if [[ "${AZ_COUNT}" -ge 1 ]]; then
    pass "Region ${AWS_REGION} has ${AZ_COUNT} available AZ(s); subnet will be created in ${FIRST_AZ}"
    info "Deployment is single-AZ by design — a second subnet would sit empty"
else
    fail "Region ${AWS_REGION} reports no available AZs"
fi

# Summary
log ""
log "  (· lines are informational and do not affect the result)"
log ""
if [[ "${FAILURES}" -eq 0 ]]; then
    log "Result: ALL CHECKS PASSED — VPC is ready for deployment."
else
    log "Result: ${FAILURES} check(s) FAILED — resolve before running make aws-setup."
fi
log ""
exit "${FAILURES}"
