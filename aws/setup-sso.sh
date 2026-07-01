#!/usr/bin/env bash
# aws/setup-sso.sh - configure an IAM Identity Center (SSO) profile.
#
# This script is designed to run INSIDE the AWS CLI Docker container (the
# `aws-cli` service in aws/docker-compose.aws.yml). It is invoked by the
# `make aws-sso-setup` target, which mounts aws/.aws-state into the
# container at /root/.aws so the profile config and the cached SSO access
# token persist across container invocations.
#
# All inputs are read from aws/.env (SSO_START_URL, SSO_REGION,
# AWS_ACCOUNT_ID, SSO_ROLE_NAME, AWS_REGION). The script is fully
# non-interactive except for the browser-based device-code login.
#
# Result: no AWS CLI installation is needed on the host. The host only
# needs Docker, Make, and a web browser to complete the device-code login.
#
# Idempotent: re-running does not duplicate the profile and will re-trigger
# `aws sso login` only if the cached token has expired.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
PROFILE_NAME="${AWS_SSO_PROFILE:-uh-groupings}"
AWS_CONFIG_FILE="${AWS_CONFIG_FILE:-/root/.aws/config}"

log()   { printf '%s\n' "$1"; }
error() { printf 'Error: %s\n' "$1" >&2; }

load_env_file() {
    if [[ ! -f "${ENV_FILE}" ]]; then
        error "Configuration file not found: ${ENV_FILE}"
        exit 1
    fi
    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a
}

validate_sso_config() {
    local missing=()
    [[ -z "${SSO_START_URL:-}" ]]   && missing+=("SSO_START_URL")
    [[ -z "${AWS_ACCOUNT_ID:-}" ]]  && missing+=("AWS_ACCOUNT_ID")
    [[ -z "${SSO_ROLE_NAME:-}" ]]   && missing+=("SSO_ROLE_NAME")
    [[ -z "${AWS_REGION:-}" ]]      && missing+=("AWS_REGION")

    if [[ ${#missing[@]} -gt 0 ]]; then
        error "The following required values are missing from aws/.env:"
        for v in "${missing[@]}"; do
            error "  ${v}"
        done
        exit 1
    fi
}

# Defensive check; under normal usage the make target mounts /root/.aws.
ensure_writable_aws_dir() {
    mkdir -p "$(dirname "${AWS_CONFIG_FILE}")"
    if ! touch "${AWS_CONFIG_FILE}" 2>/dev/null; then
        error "Cannot write to ${AWS_CONFIG_FILE}."
        error "This script expects to run inside the aws-cli container, where"
        error "the host directory aws/.aws-state is mounted at /root/.aws."
        error "Run 'make aws-sso-setup' from the repository root instead of"
        error "invoking this script directly."
        exit 1
    fi
}

profile_exists() {
    aws configure list-profiles 2>/dev/null | grep -qx "${PROFILE_NAME}"
}

write_profile() {
    log ""
    log "Writing SSO profile '${PROFILE_NAME}' to ${AWS_CONFIG_FILE}..."
    log "  SSO start URL:  ${SSO_START_URL}"
    log "  SSO region:     ${AWS_REGION}"
    log "  Account ID:     ${AWS_ACCOUNT_ID}"
    log "  Role name:      ${SSO_ROLE_NAME}"
    log "  Default region: ${AWS_REGION}"

    {
        printf '\n[sso-session %s]\n'           "${PROFILE_NAME}"
        printf 'sso_start_url           = %s\n' "${SSO_START_URL}"
        printf 'sso_region              = %s\n' "${AWS_REGION}"
        printf 'sso_registration_scopes = sso:account:access\n'

        printf '\n[profile %s]\n'   "${PROFILE_NAME}"
        printf 'sso_session    = %s\n' "${PROFILE_NAME}"
        printf 'sso_account_id = %s\n' "${AWS_ACCOUNT_ID}"
        printf 'sso_role_name  = %s\n' "${SSO_ROLE_NAME}"
        printf 'region         = %s\n' "${AWS_REGION}"
    } >> "${AWS_CONFIG_FILE}"

    chmod 600 "${AWS_CONFIG_FILE}"
    log "✓ Wrote profile '${PROFILE_NAME}' to ${AWS_CONFIG_FILE}"
    log "  (persisted on host at aws/.aws-state/config)"
}

ensure_profile_exists() {
    if profile_exists; then
        log "✓ AWS profile '${PROFILE_NAME}' already exists in ${AWS_CONFIG_FILE}"
        return
    fi
    write_profile
}

trigger_login() {
    log ""
    log "Starting SSO login. The container has no browser, so the AWS CLI will"
    log "print a verification URL and code; open the URL in your host browser"
    log "and enter the code to complete the login."
    log ""
    aws sso login --profile "${PROFILE_NAME}"
    log ""
    log "✓ SSO session established (cached at aws/.aws-state/sso/cache/)"
}

print_summary() {
    log ""
    log "IAM Identity Center is ready. Run any AWS-related Make target without a wrapper:"
    log ""
    log "  AWS_PROFILE=${PROFILE_NAME} make aws-setup"
    log ""
    log "Or set AWS_PROFILE once in your shell:"
    log "  export AWS_PROFILE=${PROFILE_NAME}"
    log "  make aws-setup"
    log ""
    log "When the SSO session expires (~1-12 h, set by your org), refresh with:"
    log "  make aws-sso-login"
}

load_env_file
validate_sso_config
ensure_writable_aws_dir
ensure_profile_exists
trigger_login
print_summary
