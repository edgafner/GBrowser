---
name: address-review
description: Use when told to check PR review, address feedback, resolve threads, or after creating a PR that received review comments. Also use when user says "check the review", "address issues", or "resolve threads". Checks BOTH file-level review threads AND PR-level comments (Copilot, Claude bot, reviewers).
---

# Address PR Review

Fetch all review threads on a PR, address actionable issues, resolve fixed/no-op threads, and push fixes.

## Usage

- `/address-review` — find PR from current branch automatically
- `/address-review 123` — address review on PR #123

## Step 1: Determine PR Number and Repo

If `$ARGUMENTS` is provided and is a number, use it as the PR number.

Otherwise, detect from current branch:

```bash
gh pr list --head "$(git branch --show-current)" --json number,url --jq '.[0]'
```

If no PR found, ask the user which PR to address.

Set variables for use in all subsequent steps:

```bash
PR_NUMBER=<detected or provided number>
OWNER=$(gh repo view --json owner --jq '.owner.login')
REPO=$(gh repo view --json name --jq '.name')
```

## Step 2: Fetch ALL Review Feedback

### Step 2a: Fetch Unresolved Review Threads (file-level comments)

```bash
gh api graphql -f query='
query($owner: String!, $repo: String!, $number: Int!) {
  repository(owner: $owner, name: $repo) {
    pullRequest(number: $number) {
      reviewThreads(first: 100) {
        nodes {
          id
          isResolved
          path
          line
          comments(first: 10) {
            nodes {
              body
              author { login }
              createdAt
            }
          }
        }
      }
    }
  }
}' -f owner="$OWNER" -f repo="$REPO" -F number=$PR_NUMBER
```

Filter to only unresolved threads. This is the **primary source** for file-level review comments.

### Step 2b: Fetch PR-level comments and review bodies

These are comments NOT attached to specific file lines — posted on the PR body itself.

**Issue comments** (bot summaries, reviewer questions):

```bash
gh api repos/{owner}/{repo}/issues/$PR_NUMBER/comments
```

**Review submissions** (reviewer summaries posted via GitHub's "Submit review" button):

```bash
gh api repos/{owner}/{repo}/pulls/$PR_NUMBER/reviews
```

> Note: `{owner}` and `{repo}` are auto-substituted by `gh` when inside a git repo.

Filter to comments/reviews that contain actionable feedback. Ignore:

- Bot status messages (CI reports with "all right", deployment notifications)
- Approval-only reviews with empty body
- "LGTM" or "looks good" comments

### Consolidating feedback

Combine findings from Steps 2a and 2b into a single triage list. Step 2a covers file-level threads; Step 2b covers PR-level comments and review bodies. There should be no
duplicates between them since they cover different types of feedback.

## Step 3: Classify Each Thread/Comment

For each unresolved thread or actionable PR-level comment, read the comment and the **current code** at the file path + line:

| Classification     | Action                                                      |
|--------------------|-------------------------------------------------------------|
| **Already fixed**  | Code already addresses the concern → resolve                |
| **Actionable**     | Valid issue, code needs change → fix it                     |
| **Praise / No-op** | Positive comment, no action needed → resolve                |
| **Question**       | Reviewer asked a question → reply with answer, then resolve |

## Step 4: Apply Fixes

If any threads are actionable:

1. Edit the code to address the issue
2. Build/test to verify (use the Quick Compilation Check table from root CLAUDE.md)

## Step 5: Commit & Push

If fixes were made:

1. Stage only the changed files (not `git add -A`)
2. Commit: `fix: address PR review feedback`
3. Push to the PR branch

## Step 6: Resolve Threads

For each addressed thread (fixed, already-fixed, praise, or answered):

```bash
gh api graphql -f query='
mutation($threadId: ID!) {
  resolveReviewThread(input: {threadId: $threadId}) {
    thread { id isResolved }
  }
}' -f threadId="THREAD_ID"
```

## Step 7: Report

```markdown
## Review Address Summary — PR #<number>

| Thread | File | Action | Status |
|--------|------|--------|--------|
| "concern about X" | path/file.kt:42 | Already fixed | Resolved |
| "missing null check" | path/other.kt:15 | Fixed | Resolved |
| "nice pattern!" | path/file.kt:30 | No-op (praise) | Resolved |

**Fixes pushed:** Yes/No
**Threads resolved:** X/Y
**Remaining unresolved:** Z (with reasons)
```

## Step 8: Check CI Status

After resolving threads and pushing fixes, check CI:

```bash
gh pr checks $PR_NUMBER
```

Report: passing/failing/pending. If any check failed, show `gh run view <run-id> --log-failed | tail -30`.

## Troubleshooting

- If GraphQL query fails: verify `$OWNER` and `$REPO` match the actual repo (`gh repo view`)
- If `gh pr checks` shows no checks: the CI may not have started yet — wait a moment and retry
- If `gh api` returns 403: rate limited — wait or check `gh auth status`
- If PR number is wrong: verify with `gh pr list --head "$(git branch --show-current)"`

## Rules

- **NEVER duplicate** comments on existing threads — check before commenting
- **NEVER re-report** issues in unchanged code
- **ALWAYS resolve** threads where the current code already addresses the concern
- **ALWAYS build/test** before pushing fixes
- Only create NEW commits for fixes (never amend previous commits)
- If unsure whether an issue is fixed, read the current code first
