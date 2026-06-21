#!/usr/bin/env bash
# aws/setup-vault.sh - install AWS vault and profile
# - AWS Vault will be installed locally if not found.
# - This script is idempotent.

set -euo pipefail

#
# Variables
#

PROFILE_NAME="${AWS_VAULT_PROFILE:-uh-groupings}"

#
# Functions
#

log() {
    printf '%s\n' "$1"
}

error() {
    printf 'Error: %s\n' "$1" >&2
}

ensure_aws_vault_installed() {
    if command -v aws-vault >/dev/null 2>&1; then
        log "✓ aws-vault is installed ($(aws-vault --version 2>&1 | head -1))"
        return
    fi

    log "aws-vault is not installed."

    case "$(uname -s)" in
        Darwin)
            if ! command -v brew >/dev/null 2>&1; then
                error "Homebrew is required to install aws-vault on macOS."
                error "Install Homebrew first: https://brew.sh/"
                exit 1
            fi
            log "Installing aws-vault via Homebrew..."
            brew install --cask aws-vault
            log "✓ aws-vault installed"
            ;;
        Linux)
            error "Automatic install on Linux is not supported."
            error "See https://github.com/99designs/aws-vault#installing for instructions."
            exit 1
            ;;
        *)
            error "Unsupported OS: $(uname -s)"
            error "See https://github.com/99designs/aws-vault#installing for instructions."
            exit 1
            ;;
    esac
}

profile_has_credentials() {
    # `aws-vault list --credentials` prints profile names that have stored
    # long-term credentials. Fall back to scanning `aws-vault list` output if
    # the flag is not supported by the installed version.
    if aws-vault list --credentials >/dev/null 2>&1; then
        aws-vault list --credentials 2>/dev/null | grep -qx "${PROFILE_NAME}"
        return
    fi

    aws-vault list 2>/dev/null \
        | awk -v p="${PROFILE_NAME}" 'NR>2 && $1==p && $2==p {found=1} END {exit !found}'
}

ensure_profile_exists() {
    if profile_has_credentials; then
        log "✓ aws-vault profile '${PROFILE_NAME}' already has stored credentials"
        return
    fi

    log "Profile '${PROFILE_NAME}' is not yet configured."
    log "You will now be prompted for an AWS Access Key ID and Secret Access Key."
    log "These values are stored in your operating system keychain, never on disk."
    aws-vault add "${PROFILE_NAME}"
    log "✓ Profile '${PROFILE_NAME}' configured"
}

print_summary() {
    log ""
    log "aws-vault is ready. Run any AWS-related Make target with:"
    log "  aws-vault exec ${PROFILE_NAME} -- make aws-setup"
    log ""
    log "Tip: alias the wrapper to save typing:"
    log "  alias avx='aws-vault exec ${PROFILE_NAME} --'"
    log "Then:"
    log "  avx make aws-setup"
}

#
# Main
#

ensure_aws_vault_installed
ensure_profile_exists
print_summary
