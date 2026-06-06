#!/usr/bin/env bash
set -euo pipefail

SOURCE_FILE="${1:-$HOME/.$(id -un)-conf/uh-groupings-api-overrides.properties}"
OUTPUT_FILE="${2:-docker/.env}"

if [[ ! -f "$SOURCE_FILE" ]]; then
  echo "ERROR: source properties file not found: $SOURCE_FILE" >&2
  exit 1
fi

mkdir -p "$(dirname "$OUTPUT_FILE")"

{
  echo "# Generated from: $SOURCE_FILE"
  echo "# Do not commit this file."
  echo

  while IFS= read -r line || [[ -n "$line" ]]; do
    # Trim leading/trailing whitespace.
    line="$(printf '%s' "$line" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"

    # Skip blank lines and comments.
    [[ -z "$line" ]] && continue
    [[ "$line" =~ ^# ]] && continue

    # Skip malformed lines.
    [[ "$line" != *"="* ]] && continue

    key="${line%%=*}"
    value="${line#*=}"

    # Trim whitespace around key only.
    key="$(printf '%s' "$key" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"

    # Convert Spring property name to environment variable name.
    env_key="$(printf '%s' "$key" \
      | tr '[:lower:]' '[:upper:]' \
      | sed 's/[^A-Z0-9]/_/g')"

    printf '%s=%s\n' "$env_key" "$value"
  done < "$SOURCE_FILE"
} > "$OUTPUT_FILE"

chmod 600 "$OUTPUT_FILE"

echo "Wrote $OUTPUT_FILE"