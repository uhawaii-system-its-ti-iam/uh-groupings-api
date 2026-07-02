#!/usr/bin/env bash
# aws/check-vpc.sh - Validate that the VPC referenced in aws/.env meets
# all requirements for this project.
#
# Checks:
#   1. VPC exists in the configured AWS_REGION.
#   2. An Internet Gateway is attached to the VPC.
#   3. The main route table has a 0.0.0.0/0 route to the IGW.
#   4. The two subnet CIDRs (SUBNET_A_CIDR/SUBNET_B_CIDR from aws/.env) fall
#      within the VPC's CIDR block and don't overlap existing subnets.
#   5. The region has at least 2 Availability Zones.

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

# Subnet CIDRs come from aws/.env (kept in sync with the vpc.yml parameter
# defaults) — not hardcoded here.
SUBNET_A_CIDR="${SUBNET_A_CIDR:-}"
SUBNET_B_CIDR="${SUBNET_B_CIDR:-}"
if [[ -z "${SUBNET_A_CIDR}" || -z "${SUBNET_B_CIDR}" ]]; then
    printf 'Error: SUBNET_A_CIDR and SUBNET_B_CIDR must be set in aws/.env\n' >&2
    printf '  (they must match the SubnetACidr/SubnetBCidr defaults in aws/cloudformation/vpc.yml)\n' >&2
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

# 2. Internet Gateway attached
IGW_ID="$(aws ec2 describe-internet-gateways \
  --filters "Name=attachment.vpc-id,Values=${VPC_ID}" \
  --query 'InternetGateways[0].InternetGatewayId' \
  --output text \
  --region "${AWS_REGION}")"

if [[ -z "${IGW_ID}" || "${IGW_ID}" == "None" ]]; then
    fail "No Internet Gateway attached to ${VPC_ID}"
else
    pass "Internet Gateway attached (${IGW_ID})"
fi

# 3. Main route table has 0.0.0.0/0 → IGW
MAIN_RT="$(aws ec2 describe-route-tables \
  --filters "Name=vpc-id,Values=${VPC_ID}" "Name=association.main,Values=true" \
  --query 'RouteTables[0].RouteTableId' \
  --output text \
  --region "${AWS_REGION}")"

if [[ -z "${MAIN_RT}" || "${MAIN_RT}" == "None" ]]; then
    fail "No main route table found for ${VPC_ID}"
else
    IGW_ROUTE="$(aws ec2 describe-route-tables \
      --route-table-ids "${MAIN_RT}" \
      --query "RouteTables[0].Routes[?DestinationCidrBlock=='0.0.0.0/0'].GatewayId" \
      --output text \
      --region "${AWS_REGION}")"

    if [[ "${IGW_ROUTE}" == igw-* ]]; then
        pass "Main route table (${MAIN_RT}) routes 0.0.0.0/0 → ${IGW_ROUTE}"
    elif [[ -z "${IGW_ROUTE}" || "${IGW_ROUTE}" == "None" ]]; then
        fail "Main route table (${MAIN_RT}) has no 0.0.0.0/0 route"
    else
        fail "Main route table (${MAIN_RT}) routes 0.0.0.0/0 → ${IGW_ROUTE} (not an IGW)"
    fi
fi

# 4. Subnet CIDRs (from aws/.env) are usable: inside the VPC's CIDR block(s)
#    and not overlapping any existing subnet. This is the exact condition that,
#    if violated, makes the vpc stack roll back during `make aws-setup`.
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
  CAND_A="${SUBNET_A_CIDR}" CAND_B="${SUBNET_B_CIDR}" python3 - <<'PY'
import os, ipaddress

vpc = [ipaddress.ip_network(c) for c in os.environ.get("VPC_CIDRS", "").split()]
existing = [ipaddress.ip_network(c) for c in os.environ.get("EXISTING_CIDRS", "").split()]

for name in ("A", "B"):
    raw = os.environ["CAND_" + name]
    try:
        net = ipaddress.ip_network(raw)
    except ValueError as e:
        print(f"FAIL|{name} ({raw}): invalid CIDR ({e})")
        continue
    if not any(net.subnet_of(v) for v in vpc):
        print(f"FAIL|{name} ({raw}): not within VPC CIDR [{', '.join(map(str, vpc))}]")
        continue
    clash = [str(s) for s in existing if net.overlaps(s)]
    if clash:
        print(f"FAIL|{name} ({raw}): overlaps existing subnet(s) {', '.join(clash)}")
        continue
    print(f"OK|{name} ({raw}): within VPC and unused")
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

if [[ -n "${EXISTING_CIDRS}" ]]; then
    log "  (existing subnets in VPC: $(printf '%s' "${EXISTING_CIDRS}" | tr '\n' ' '))"
fi

# 5. Region has ≥2 AZs
AZ_COUNT="$(aws ec2 describe-availability-zones \
  --filters "Name=region-name,Values=${AWS_REGION}" "Name=state,Values=available" \
  --query 'length(AvailabilityZones)' \
  --output text \
  --region "${AWS_REGION}")"

if [[ "${AZ_COUNT}" -ge 2 ]]; then
    pass "Region ${AWS_REGION} has ${AZ_COUNT} available AZs (≥2 required)"
else
    fail "Region ${AWS_REGION} has only ${AZ_COUNT} available AZ(s) — need at least 2"
fi

# Summary
log ""
if [[ "${FAILURES}" -eq 0 ]]; then
    log "Result: ALL CHECKS PASSED — VPC is ready for deployment."
else
    log "Result: ${FAILURES} check(s) FAILED — resolve before running make aws-setup."
fi
log ""
exit "${FAILURES}"
