#!/usr/bin/env bash
# aws/github-connect.sh - Create or locate a GitHub CodeConnections connection.
#
# Workflow:
#   1. Look for an existing AVAILABLE GitHub connection in this account/region.
#      - If exactly one found: print its ARN and suggest updating aws/.env.
#      - If multiple found: list all and prompt the user to choose.
#   2. If no AVAILABLE connection exists:
#      - Check for PENDING ones and offer to reuse them (browser already opened).
#      - Otherwise create a new connection (returns PENDING immediately).
#   3. Open the IAM Identity Center start URL (from aws/.env) and then the AWS
#      Console Connections page so the user can complete the OAuth handshake.
#   4. Poll until the selected connection becomes AVAILABLE (up to ~5 min).
#   5. Print the final ARN with an exact sed command to update aws/.env.
#
# Usage:
#   bash aws/github-connect.sh            (called by `make aws-github-connect`)
#   bash aws/github-connect.sh --create-only
#
#   --create-only
#     Create/reuse a connection and print its ARN, but do not open browser tabs
#     or poll for AVAILABLE. Useful when OAuth must be completed manually later.
#
# Prerequisites:
#   - aws/.env loaded (done by this script)
#   - AWS CLI v2 installed
#   - Valid AWS session (ensured via lib-auth.sh / ensure_aws_session)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

# ---------------------------------------------------------------------------
# Load environment
# ---------------------------------------------------------------------------

if [[ ! -f "${ENV_FILE}" ]]; then
    printf 'Error: %s not found\n' "${ENV_FILE}" >&2
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib-auth.sh"

AWS_REGION="${AWS_REGION:-us-west-2}"
AWS_OWNER="${AWS_OWNER:-}"
AWS_PROJECT_ID="${AWS_PROJECT_ID:-groupings-api}"
SSO_START_URL="${SSO_START_URL:-}"
CREATE_ONLY="0"

if [[ "${1:-}" == "--create-only" ]]; then
    CREATE_ONLY="1"
fi

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

log()   { printf '%s\n' "$1"; }
warn()  { printf '⚠ %s\n' "$1"; }
ok()    { printf '✓ %s\n' "$1"; }
err()   { printf 'Error: %s\n' "$1" >&2; }
blank() { printf '\n'; }

# Open a URL in the default browser (macOS + Linux).
open_browser() {
    local url="$1"
    if command -v open >/dev/null 2>&1; then
        open "${url}"
    elif command -v xdg-open >/dev/null 2>&1; then
        xdg-open "${url}" 2>/dev/null &
    else
        log "  → Open this URL manually: ${url}"
    fi
}

# List GitHub connections: outputs tab-separated Name<TAB>ARN<TAB>Status lines.
list_github_connections() {
    aws codeconnections list-connections \
        --provider-type GitHub \
        --query 'Connections[*].[ConnectionName,ConnectionArn,ConnectionStatus]' \
        --output text \
        --region "${AWS_REGION}" 2>/dev/null || true
}

# Filtered helpers — print only ARN column for lines matching a status.
connections_with_status() {
    local status="$1"
    list_github_connections | awk -v s="${status}" '$3 == s { print $2 }'
}

names_with_status() {
    local status="$1"
    list_github_connections | awk -v s="${status}" '$3 == s { print $1 "\t" $2 }'
}

# Poll a connection ARN until AVAILABLE or timeout.
wait_for_available() {
    local arn="$1"
    local max_seconds=300
    local elapsed=0
    local interval=10

    log "Polling for connection to become AVAILABLE (up to ${max_seconds}s)..."
    while [[ ${elapsed} -lt ${max_seconds} ]]; do
        local status
        status="$(aws codeconnections get-connection \
            --connection-arn "${arn}" \
            --query 'Connection.ConnectionStatus' \
            --output text \
            --region "${AWS_REGION}" 2>/dev/null || echo "UNKNOWN")"

        if [[ "${status}" == "AVAILABLE" ]]; then
            return 0
        fi

        printf '  %3ds — status: %s\r' "${elapsed}" "${status}"
        sleep "${interval}"
        elapsed=$((elapsed + interval))
    done

    blank
    return 1
}

# Print the final "paste this ARN" instructions.
print_arn_instructions() {
    local arn="$1"
    blank
    log "─────────────────────────────────────────────────────────────────"
    ok "Connection AVAILABLE"
    blank
    log "  ARN: ${arn}"
    blank
    log "Paste this into aws/.env:"
    blank
    log "  GITHUB_CONNECTION_ARN=${arn}"
    blank
    log "Or run this to update it automatically:"
    blank
    log "  sed -i '' 's|^GITHUB_CONNECTION_ARN=.*|GITHUB_CONNECTION_ARN=${arn}|' \\"
    log "    \"${SCRIPT_DIR}/.env\""
    log "─────────────────────────────────────────────────────────────────"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

log "─────────────────────────────────────────────────────────────────"
log "  GitHub CodeConnections Setup"
log "  Region: ${AWS_REGION}  |  Account: ${AWS_ACCOUNT_ID:-<from session>}"
log "─────────────────────────────────────────────────────────────────"
blank

# Ensure we have a valid AWS session.
ensure_aws_session || { err "AWS authentication failed."; exit 1; }

# ── Step 1: Check for existing AVAILABLE connections ──────────────────────

available_arns=()
while IFS= read -r line; do
    [[ -n "${line}" ]] && available_arns+=("${line}")
done < <(connections_with_status "AVAILABLE")

if [[ ${#available_arns[@]} -eq 1 ]]; then
    ok "Found an existing AVAILABLE GitHub connection."
    print_arn_instructions "${available_arns[0]}"
    exit 0
fi

if [[ ${#available_arns[@]} -gt 1 ]]; then
    ok "Found ${#available_arns[@]} AVAILABLE GitHub connections:"
    blank
    avail_names=()
    avail_index_arns=()
    i=1
    while IFS=$'\t' read -r name arn; do
        log "  [${i}] ${name}"
        log "      ${arn}"
        avail_names+=("${name}")
        avail_index_arns+=("${arn}")
        i=$((i + 1))
    done < <(names_with_status "AVAILABLE")
    blank
    printf 'Enter number to use (or press Enter to create a new one): '
    read -r choice
    if [[ -n "${choice}" ]] && [[ "${choice}" -ge 1 ]] && [[ "${choice}" -le "${#avail_index_arns[@]}" ]] 2>/dev/null; then
        print_arn_instructions "${avail_index_arns[$((choice - 1))]}"
        exit 0
    fi
fi

# ── Step 2: Check for PENDING connections ─────────────────────────────────

pending_arns=()
while IFS= read -r line; do
    [[ -n "${line}" ]] && pending_arns+=("${line}")
done < <(connections_with_status "PENDING")

selected_arn=""

if [[ ${#pending_arns[@]} -gt 0 ]]; then
    warn "Found ${#pending_arns[@]} PENDING GitHub connection(s) — OAuth not yet completed:"
    blank
    pending_index_arns=()
    i=1
    while IFS=$'\t' read -r name arn; do
        log "  [${i}] ${name}"
        log "      ${arn}"
        pending_index_arns+=("${arn}")
        i=$((i + 1))
    done < <(names_with_status "PENDING")
    blank
    printf 'Use a pending connection? Enter number (or press Enter to create a new one): '
    read -r choice
    if [[ -n "${choice}" ]] && [[ "${choice}" -ge 1 ]] && [[ "${choice}" -le "${#pending_index_arns[@]}" ]] 2>/dev/null; then
        selected_arn="${pending_index_arns[$((choice - 1))]}"
    fi
fi

# ── Step 3: Create a new connection if needed ─────────────────────────────

if [[ -z "${selected_arn}" ]]; then
    connection_name="${AWS_OWNER:+${AWS_OWNER}-}${AWS_PROJECT_ID}-github"
    log "Creating new GitHub connection: ${connection_name} ..."

    selected_arn="$(aws codeconnections create-connection \
        --provider-type GitHub \
        --connection-name "${connection_name}" \
        --query 'ConnectionArn' \
        --output text \
        --region "${AWS_REGION}")"

    ok "Connection created (PENDING): ${selected_arn}"
fi

if [[ "${CREATE_ONLY}" == "1" ]]; then
    blank
    warn "Create-only mode: skipping browser and polling steps."
    log "  Complete OAuth later in AWS Console, then re-run without --create-only"
    log "  to wait for AVAILABLE."
    blank
    log "Current connection ARN:"
    log "  ${selected_arn}"
    exit 0
fi

# ── Step 4: Open SSO start URL + AWS Console so the user can complete OAuth ─

console_url="https://${AWS_REGION}.console.aws.amazon.com/codesuite/settings/connections?region=${AWS_REGION}"
blank
if [[ -n "${SSO_START_URL}" ]]; then
    log "Opening IAM Identity Center start URL..."
    log "  ${SSO_START_URL}"
    open_browser "${SSO_START_URL}"
    blank
fi

log "Opening AWS Console to complete the GitHub OAuth handshake..."
log "  ${console_url}"
blank
warn "In the browser:"
log "  0. Sign in at the IAM Identity Center page first (use your company-issued AWS account)."
log "  1. Find the connection listed above (status: Pending)."
log "  2. Click 'Update pending connection'."
log "  3. Authorize the AWS Connector for GitHub OAuth app."
log "  4. Return here — this script will detect when it becomes AVAILABLE."
blank

open_browser "${console_url}"

# ── Step 5: Poll until AVAILABLE ──────────────────────────────────────────

if wait_for_available "${selected_arn}"; then
    blank
    print_arn_instructions "${selected_arn}"
else
    blank
    warn "Timed out waiting for connection to become AVAILABLE."
    log "  The connection ARN is: ${selected_arn}"
    log "  Complete the OAuth flow in the browser and then run:"
    log "    make aws-github-connect"
    exit 1
fi

