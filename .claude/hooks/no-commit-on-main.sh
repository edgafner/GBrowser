#!/bin/bash
# Block git commits on the main branch
current_branch=$(git branch --show-current 2>/dev/null)
if [ "$current_branch" = "main" ] || [ "$current_branch" = "master" ]; then
    cat >&2 << 'BLOCK'
BLOCKED: You are on the main branch. Direct commits to main are not allowed.
To fix: git checkout -b feature/<description>
BLOCK
    exit 2
fi
exit 0
