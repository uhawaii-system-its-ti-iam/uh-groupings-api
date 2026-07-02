#!/usr/bin/env bash
# aws/auth.sh - configure and/or refresh the IAM Identity Center (SSO) session.
#
# Thin dispatcher over aws/lib-auth.sh, used by the `make aws-sso-setup` and
# `make aws-sso-login` targets. Loads aws/.env, then ensures a usable session:
#   - no argument : bootstrap the profile if needed and log in only if there
#                   is no valid session (idempotent first-time setup).
#   - "force"     : always re-run `aws sso login` (proactive refresh).

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

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

ensure_aws_session "${1:-}" || exit 1
