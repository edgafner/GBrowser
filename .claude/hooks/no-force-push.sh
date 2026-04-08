#!/bin/bash
# Block force push commands
input=$(cat)
command=$(echo "$input" | jq -r '.tool_input.command // empty')
if echo "$command" | grep -qE 'git\s+push\s+.*(-f\b|--force\b|--force-with-lease\b)'; then
    echo "BLOCKED: Force push is NEVER allowed." >&2
    exit 2
fi
exit 0
