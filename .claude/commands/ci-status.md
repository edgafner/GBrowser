---
description: Check CI pipeline status for current branch
allowed-tools: Bash
---

Check CI pipeline status for the current branch:

1. Get current branch: `git branch --show-current`
2. Check if there's a PR: `gh pr list --head "$(git branch --show-current)" --json number,url --jq '.[0]'`
3. If PR exists: `gh pr checks <number>` to show check status
4. If no PR: `gh run list --branch "$(git branch --show-current)" --limit 3`
5. If any check failed, show the failing step logs: `gh run view <id> --log-failed | tail -50`
6. Summarize: passing/failing/pending counts
