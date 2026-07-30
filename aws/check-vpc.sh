#!/usr/bin/env bash
# aws/check-vpc.sh - Validate that the VPC referenced in aws/.env meets
# all requirements for this project.
#
# Checks (hard requirements — a failure blocks `make aws-setup`):
#   1. VPC exists in the configured AWS_REGION.
#   2. The subnet CIDR (SUBNET_CIDR from aws/.env) falls within the VPC's CIDR
#      block and doesn't overlap an existing subnet.
#   3. The VPC has a main route table (the S3 gateway endpoint attaches to it).
#
# The deployment is SINGLE-SUBNET / SINGLE-AZ, so there is no multi-AZ
# requirement to validate.
#
# Informational only (reported, never fails the run):
#   - Internet Gateway attachment and a 0.0.0.0/0 default route.
#     The API deployment creates NO load balancer and needs NO internet egress:
#     tasks run with AssignPublicIp DISABLED and reach AWS services through the
#     VPC endpoints created by aws/cloudformation/vpc.yml. An IGW is therefore
#     irrelevant to this project, and its absence must not block setup.
#   - A route toward the UH data center for Grouper WS. Required eventually, but
#     the mechanism is pending infra-team confirmation (see aws/AGENTS.md), so
#     there is nothing definitive to assert yet.

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

# The subnet CIDR comes from aws/.env (kept in sync with the vpc.yml parameter
# default) — not hardcoded here.
SUBNET_CIDR="${SUBNET_CIDR:-}"
if [[ -z "${SUBNET_CIDR}" ]]; then
    printf 'Error: SUBNET_CIDR must be set in aws/.env\n' >&2
    printf '  (it must match the SubnetCidr default in aws/cloudformation/vpc.yml)\n' >&2
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

# 2. Main route table must exist — the S3 gateway endpoint in vpc.yml attaches
#    to it, so this one IS a hard requirement.
MAIN_RT="$(aws ec2 describe-route-tables \
  --filters "Name=vpc-id,Values=${VPC_ID}" "Name=association.main,Values=true" \
  --query 'RouteTables[0].RouteTableId' \
  --output text \
  --region "${AWS_REGION}")"

if [[ -z "${MAIN_RT}" || "${MAIN_RT}" == "None" ]]; then
    fail "No main route table found for ${VPC_ID} (required by the S3 gateway endpoint)"
else
    pass "Main route table found (${MAIN_RT})"
    if [[ -n "${MAIN_ROUTE_TABLE_ID:-}" && "${MAIN_ROUTE_TABLE_ID}" != "${MAIN_RT}" ]]; then
        fail "MAIN_ROUTE_TABLE_ID in aws/.env is ${MAIN_ROUTE_TABLE_ID}, but the VPC's main route table is ${MAIN_RT}"
    elif [[ -n "${MAIN_ROUTE_TABLE_ID:-}" ]]; then
        pass "MAIN_ROUTE_TABLE_ID in aws/.env matches the VPC's main route table"
    fi
fi

# 3. Internet Gateway / default route — INFORMATIONAL ONLY.
#    The API has no load balancer and no internet egress requirement; AWS access
#    is via VPC endpoints. Reported so the VPC's posture is visible, but never a
#    failure. See the header comment.
IGW_ID="$(aws ec2 describe-internet-gateways \
  --filters "Name=attachment.vpc-id,Values=${VPC_ID}" \
  --query 'InternetGateways[0].InternetGatewayId' \
  --output text \
  --region "${AWS_REGION}")"

if [[ -z "${IGW_ID}" || "${IGW_ID}" == "None" ]]; then
    info "No Internet Gateway attached — fine, the API needs none (no ALB, no NAT)"
else
    info "Internet Gateway attached (${IGW_ID}) — not used by the API deployment"
fi

if [[ -n "${MAIN_RT}" && "${MAIN_RT}" != "None" ]]; then
    DEFAULT_ROUTE="$(aws ec2 describe-route-tables \
      --route-table-ids "${MAIN_RT}" \
      --query "RouteTables[0].Routes[?DestinationCidrBlock=='0.0.0.0/0'].GatewayId" \
      --output text \
      --region "${AWS_REGION}")"

    if [[ -z "${DEFAULT_ROUTE}" || "${DEFAULT_ROUTE}" == "None" ]]; then
        info "Main route table has no 0.0.0.0/0 route — fine, none is required"
    else
        info "Main route table routes 0.0.0.0/0 → ${DEFAULT_ROUTE} (unused by API tasks)"
    fi
fi

# 3b. Data-center path for Grouper WS — INFORMATIONAL. The mechanism is pending
#     infra confirmation, so only report what is present today.
if [[ -n "${MAIN_RT}" && "${MAIN_RT}" != "None" ]]; then
    DC_ROUTES="$(aws ec2 describe-route-tables \
      --route-table-ids "${MAIN_RT}" \
      --query "RouteTables[0].Routes[?GatewayId!=null && starts_with(GatewayId, 'vgw-')].DestinationCidrBlock" \
      --output text \
      --region "${AWS_REGION}" 2>/dev/null)"
    TGW_ROUTES="$(aws ec2 describe-route-tables \
      --route-table-ids "${MAIN_RT}" \
      --query "RouteTables[0].Routes[?TransitGatewayId!=null].DestinationCidrBlock" \
      --output text \
      --region "${AWS_REGION}" 2>/dev/null)"

    if [[ -n "${DC_ROUTES}" && "${DC_ROUTES}" != "None" ]]; then
        info "Virtual private gateway route(s) present: ${DC_ROUTES}"
    elif [[ -n "${TGW_ROUTES}" && "${TGW_ROUTES}" != "None" ]]; then
        info "Transit gateway route(s) present: ${TGW_ROUTES}"
    else
        info "No VGW/TGW route found — Grouper WS will be unreachable until the"
        info "  data-center path is provisioned (mechanism pending infra team)"
    fi
fi

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
  CANDIDATE="${SUBNET_CIDR}" python3 - <<'PY'
import os, ipaddress

vpc = [ipaddress.ip_network(c) for c in os.environ.get("VPC_CIDRS", "").split()]
existing = [ipaddress.ip_network(c) for c in os.environ.get("EXISTING_CIDRS", "").split()]

raw = os.environ["CANDIDATE"]
try:
    net = ipaddress.ip_network(raw)
except ValueError as e:
    print(f"FAIL|({raw}): invalid CIDR ({e})")
else:
    if not any(net.subnet_of(v) for v in vpc):
        print(f"FAIL|({raw}): not within VPC CIDR [{', '.join(map(str, vpc))}]")
    else:
        clash = [str(s) for s in existing if net.overlaps(s)]
        if clash:
            print(f"FAIL|({raw}): overlaps existing subnet(s) {', '.join(clash)}")
        else:
            print(f"OK|({raw}): within VPC and unused")
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
    info "The vpc stack already exists — a CIDR 'overlaps' failure above is likely its own subnet"
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
