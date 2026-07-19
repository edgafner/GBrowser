#!/bin/bash
# Block git commits on the main branch.
# Self-guards on the command from stdin: the settings.json `if` filter is not
# applied by all Claude Code versions, so this can run for every Bash call.
input=$(cat)
cmd=$(echo "$input" | jq -r '.tool_input.command // empty')
echo "$cmd" | grep -qE '\bgit\b[^|;&]*\bcommit\b' || exit 0
current_branch=$(git branch --show-current 2>/dev/null)
if [ "$current_branch" = "main" ] || [ "$current_branch" = "master" ]; then
    cat >&2 << 'BLOCK'
BLOCKED: You are on the main branch. Direct commits to main are not allowed.
To fix: git checkout -b feature/<description>
BLOCK
    exit 2
fi
exit 0
