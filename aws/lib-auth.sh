#!/usr/bin/env bash
# aws/lib-auth.sh - shared IAM Identity Center (SSO) authentication helpers.
#
# Sourced by the project's AWS scripts (setup.sh, check-vpc.sh) and the
# auth.sh dispatcher used by the `make aws-sso-*` targets. The sourcing script
# must already have loaded aws/.env into the environment (so SSO_START_URL,
# AWS_ACCOUNT_ID, SSO_ROLE_NAME, and AWS_REGION are available).
#
# The public entry point is `ensure_aws_session`, which guarantees a usable
# session on the developer's host:
#   1. writes the SSO profile to ~/.aws/config if it isn't there yet
#      (bootstrapping from the aws/.env values), then
#   2. runs `aws sso login` (opens a browser) if there is no valid session,
#      then verifies the result.
# It exports AWS_PROFILE so the caller's subsequent `aws` commands use it.
#
# No `set -e` here: callers manage their own error handling, and each function
# returns a non-zero status the caller can act on.

# The profile every aws call should use: an already-exported AWS_PROFILE wins,
# then AWS_SSO_PROFILE, else the project default.
aws_profile_name() {
    printf '%s' "${AWS_PROFILE:-${AWS_SSO_PROFILE:-uh-groupings}}"
}

_auth_config_file() {
    printf '%s' "${AWS_CONFIG_FILE:-${HOME}/.aws/config}"
}

aws_profile_exists() {
    aws configure list-profiles 2>/dev/null | grep -qx "$(aws_profile_name)"
}

aws_session_valid() {
    aws sts get-caller-identity --region "${AWS_REGION:-}" >/dev/null 2>&1
}

# Write the [sso-session] and [profile] blocks to ~/.aws/config from aws/.env.
write_sso_profile() {
    local profile config_file
    profile="$(aws_profile_name)"
    config_file="$(_auth_config_file)"

    local missing=()
    [[ -z "${SSO_START_URL:-}" ]]  && missing+=("SSO_START_URL")
    [[ -z "${AWS_ACCOUNT_ID:-}" ]] && missing+=("AWS_ACCOUNT_ID")
    [[ -z "${SSO_ROLE_NAME:-}" ]]  && missing+=("SSO_ROLE_NAME")
    [[ -z "${AWS_REGION:-}" ]]     && missing+=("AWS_REGION")
    if [[ ${#missing[@]} -gt 0 ]]; then
        printf 'Error: cannot configure SSO profile "%s"; these are missing from aws/.env:\n' "${profile}" >&2
        printf '  %s\n' "${missing[@]}" >&2
        return 1
    fi

    mkdir -p "$(dirname "${config_file}")"
    if ! touch "${config_file}" 2>/dev/null; then
        printf 'Error: cannot write to %s (check permissions).\n' "${config_file}" >&2
        return 1
    fi

    printf 'Configuring SSO profile "%s" in %s...\n' "${profile}" "${config_file}"
    {
        printf '\n[sso-session %s]\n'           "${profile}"
        printf 'sso_start_url           = %s\n' "${SSO_START_URL}"
        printf 'sso_region              = %s\n' "${AWS_REGION}"
        printf 'sso_registration_scopes = sso:account:access\n'

        printf '\n[profile %s]\n'   "${profile}"
        printf 'sso_session    = %s\n' "${profile}"
        printf 'sso_account_id = %s\n' "${AWS_ACCOUNT_ID}"
        printf 'sso_role_name  = %s\n' "${SSO_ROLE_NAME}"
        printf 'region         = %s\n' "${AWS_REGION}"
    } >> "${config_file}"
    chmod 600 "${config_file}"
    printf '✓ Wrote profile "%s"\n' "${profile}"
}

# Guarantee a usable AWS session, bootstrapping the profile and logging in as
# needed. Pass "force" to always re-run `aws sso login` even if the current
# session is still valid. Exports AWS_PROFILE.
ensure_aws_session() {
    local force="${1:-}"
    local profile
    profile="$(aws_profile_name)"
    export AWS_PROFILE="${profile}"

    if [[ "${force}" != "force" ]] && aws_session_valid; then
        return 0
    fi

    if ! aws_profile_exists; then
        write_sso_profile || return 1
    fi

    printf 'Signing in to AWS (profile "%s"). A browser window will open...\n' "${profile}"
    if ! aws sso login --profile "${profile}"; then
        printf 'Error: SSO login failed for profile "%s".\n' "${profile}" >&2
        return 1
    fi

    if ! aws_session_valid; then
        printf 'Error: still unable to authenticate after login.\n' >&2
        return 1
    fi
    printf '✓ Signed in as profile "%s".\n' "${profile}"
}
