# Automated PR Review: GitHub Setup → Azure DevOps Server Port

Findings from analysing `/install-github-app`, the review workflows in this repo, and what an equivalent implementation on Azure DevOps **Server** (on-prem) requires.

- **Reference PR:** https://github.com/edgafner/GBrowser/pull/720
- **Sources inspected:** `~/.local/share/claude/versions/2.1.221` (CLI binary strings),
	`.github/workflows/claude-code-review.yml`, `.github/workflows/claude.yml`,
	`anthropics/claude-code-action` docs, Azure DevOps REST API 7.1 reference.

---

## 1. Executive summary

`/install-github-app` is a **setup wizard only**. It contains no review logic. It installs an identity, writes a secret, and drops two workflow files. All runtime behaviour lives
in
`anthropics/claude-code-action@v1`.

For an Azure DevOps Server port, that split is the whole story:

| Piece              | Portability                                                    |
|--------------------|----------------------------------------------------------------|
| The installer      | Trivial — replace with a doc page or one-off script            |
| The workflow files | Easy — direct YAML translation, one significant trigger caveat |
| **The action**     | **This is the work.** No equivalent exists; must be built      |

Realistic estimate for a v1 covering automatic-review-on-PR with both comment types:
**2–4 days**, with the `changeTrackingId` handling and the build-service permission setup as the two most likely time sinks.

---

## 2. What `/install-github-app` does

Four steps, all on the developer's machine:

1. **Preconditions** — git repo with a GitHub remote; caller must be a repo admin (required to install an App and write secrets).
2. **Installs the Claude GitHub App** on the repo/org via browser. This is the *identity* — it is what mints API tokens at runtime and makes comments appear as `claude[bot]` rather
   than as the installing user.
3. **Writes a repo secret** — `CLAUDE_CODE_OAUTH_TOKEN` (Claude subscription) or
	 `ANTHROPIC_API_KEY`.
4. **Writes two workflow files** and opens a PR containing them.

### The two workflows

| File                     | Trigger                                                                                                                       | Purpose                                                  |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| `claude.yml`             | `issue_comment`, `pull_request_review_comment`, `pull_request_review`, `issues` — each gated on the body containing `@claude` | **On-demand.** Mention the bot; it answers or implements |
| `claude-code-review.yml` | `pull_request` → `opened`, `synchronize`, `ready_for_review`, `reopened`                                                      | **Automatic.** Every PR gets reviewed                    |

> **Note on template drift:** the CLI's *current* default `claude-code-review.yml` no longer inlines
> a rubric. It delegates to a marketplace plugin:
> ```yaml
> plugin_marketplaces: 'https://github.com/anthropics/claude-code.git'
> plugins: 'code-review@claude-code-plugins'
> prompt: '/code-review:code-review ${{ github.repository }}/pull/${{ github.event.pull_request.number }}'
> ```
> This repo predates that and uses a hand-written rubric instead. Either approach is valid; the
> hand-written one is easier to port because the rubric is visible in the file.

---

## 3. Runtime architecture — what `claude-code-action` does

This is the component with no Azure DevOps equivalent. Its responsibilities:

1. **Context extraction** — parses the GitHub event payload; fetches PR metadata, diff, existing comments and (optionally) CI results via the GitHub API.
2. **Credential minting** — exchanges the GitHub App installation for a short-lived token.
3. **Headless Claude Code run** — executes in the runner with the repo checked out.
4. **Tool surface for posting** — the key part:
	- `gh` CLI via Bash → **PR-level** comments
	- MCP server `github_inline_comment`, tool `create_inline_comment` → **file/line-anchored**
		comments
	- `github_comment` → the sticky / progress-tracking comment
5. **Post-processing**
	- `use_sticky_comment` — edits one comment instead of stacking a new one per push
	- `track_progress` — posts a live checkbox comment updated as work proceeds
	- `classify_inline_comments` — buffers inline comments and screens them through Haiku before posting, to suppress test/probe noise

**Design takeaway for the port:** items 1, 3 and 4 are the mandatory core. Item 5 is polish, but
`use_sticky_comment`'s equivalent (idempotency) becomes *mandatory* on Azure DevOps because the build-validation trigger re-runs on every push and will otherwise duplicate every
thread.

---

## 4. How review instructions are configured

Three layers, all visible in `.github/workflows/claude-code-review.yml`.

### Layer 1 — the `prompt:` input (lines 40–124)

The entire rubric, inline in YAML. This repo's covers Functionality / Security & Testing / Documentation, then Code Quality / Architecture / Testing / Dependencies, then a required
output format.

The two lines that govern **comment placement**:

```yaml
Use inline comments for code-specific issues via `mcp__github_inline_comment__create_inline_comment`.
Use `gh pr comment` for general feedback and summary.
```

That routing is *just prose*. There is no structured config for it — the model is told which tool to reach for. This is important for the port: the same routing decision has to be
made somewhere, and prose is a legitimate way to make it.

### Layer 2 — `CLAUDE.md`

Read from the checked-out repo at runtime; referenced explicitly at line 67. Project-specific rules live here rather than in the workflow, so they stay in sync with the code.

> ⚠️ This is why a factual error in `CLAUDE.md` is not self-contained — it becomes an assumption in
> every subsequent automated review. See finding 2 on PR #720.

### Layer 3 — `claude_args:` (lines 126–129)

Model, turn budget, tool allowlist. This repo scales cost by PR size:

```yaml
--model      ${{ changed_files <= 30 && 'claude-sonnet-4-6' || 'claude-haiku-4-5-20251001' }}
--max-turns  ${{ changed_files <= 10 && '30' || (changed_files <= 30 && '40' || '60') }}
--allowedTools Edit,Read,Write
```

`--append-system-prompt` is also available here for guidance that should not be visible in the review prompt itself.

### Supporting mechanics

| Mechanism                                           | Location    | Purpose                                                                                           |
|-----------------------------------------------------|-------------|---------------------------------------------------------------------------------------------------|
| Tracking marker `<!-- claude-review-{pr}-{run} -->` | line 124    | Lets Claude find its own prior review and do incremental follow-ups (lines 77–82)                 |
| Job-level `if:`                                     | lines 10–15 | Skips Dependabot, drafts, first-time contributors — cost control **and** prompt-injection control |
| `use_sticky_comment: true`                          | line 36     | One comment, edited in place                                                                      |
| `track_progress: true`                              | line 37     | Live checkbox progress comment                                                                    |

---

## 5. Azure DevOps Server port

### 5.1 Concept mapping

| GitHub                             | Azure DevOps Server                                      | Difficulty    |
|------------------------------------|----------------------------------------------------------|---------------|
| GitHub App identity                | PAT, or `$(System.AccessToken)` (build service identity) | Easy          |
| `on: pull_request`                 | **Branch Policy → Build Validation**                     | ⚠️ See 5.2    |
| `@claude` in a comment             | Service Hook → incoming-webhook pipeline resource        | Hard — defer  |
| `anthropics/claude-code-action@v1` | **Build it yourself**                                    | The real work |
| `gh pr comment`                    | `POST .../threads`, no `threadContext`                   | Easy          |
| `create_inline_comment`            | `POST .../threads`, **with** `threadContext`             | Easy          |
| Repo secret                        | Variable group / Key Vault-linked secret                 | Easy          |
| `use_sticky_comment`               | `GET .../threads` + marker match + reply                 | Must build    |

### 5.2 The trigger gotcha (most common failure)

**`pr:` triggers in YAML do not work for Azure Repos.** They are supported for GitHub and Bitbucket Cloud only. For Azure Repos you configure:

> Repo Settings → Branches → *target branch* → Branch Policies → **Build Validation** → add
> pipeline → Trigger: **Automatic**

This queues the pipeline on PR creation and on every push to the source branch.

A `pr:` block in the YAML will be silently ignored. Budget time for this — it is the single most common thing teams get wrong.

### 5.3 The comment API — both types, one endpoint

```
POST {collectionUri}{project}/_apis/git/repositories/{repoId}/pullRequests/{prId}/threads?api-version=7.1
```

**PR-level** — omit `threadContext` entirely:

```json
{
	"comments": [
		{
			"parentCommentId": 0,
			"content": "Overall this looks good…",
			"commentType": 1
		}
	],
	"status": 1
}
```

**File / line-anchored** — add `threadContext`:

```json
{
	"comments": [
		{
			"parentCommentId": 0,
			"content": "Null check missing here.",
			"commentType": 1
		}
	],
	"status": 1,
	"threadContext": {
		"filePath": "/src/main/kotlin/com/github/gbrowser/Foo.kt",
		"rightFileStart": {
			"line": 42,
			"offset": 1
		},
		"rightFileEnd": {
			"line": 42,
			"offset": 20
		}
	},
	"pullRequestThreadContext": {
		"changeTrackingId": 1,
		"iterationContext": {
			"firstComparingIteration": 1,
			"secondComparingIteration": 2
		}
	}
}
```

#### Field details that will bite you

- `filePath` — repo-root-relative, **with a leading slash**.
- `line` starts at **1**; `offset` starts at **0**.
- `rightFile*` = new/head side. `leftFile*` = base side — use for comments on deleted lines.
- `status`: `1` = active (open thread, can block policy), `4` = closed. Use `4` for informational notes so they do not gate the PR.
- **`changeTrackingId`** is what makes a comment follow the code across subsequent pushes. Without it the comment posts but does not track. Obtain it from the PR *iteration
  changes* endpoint. **This is the fiddliest part of the port.**
- Required scopes: `vso.code_write` + `vso.threads_full`.

### 5.4 Implementation checklist

1. **Pipeline YAML** + Build Validation policy on the target branch (see 5.2).
2. **Agent setup** — Node, then `npm i -g @anthropic-ai/claude-code`. ⚠️ Self-hosted on-prem agents need **egress to `api.anthropic.com`**, or routing via Bedrock / Vertex /
   Foundry. **Verify this first** — it is the most likely hard blocker in a locked-down Server environment, and it invalidates the whole approach if unavailable.
3. **Diff extraction** — `git diff origin/$(System.PullRequest.TargetBranch)...HEAD`.
4. **Headless Claude run** — `claude -p "<rubric>" --output-format json`, with a schema forcing structured findings: `{ file, line, endLine, severity, body }[]` plus one summary
   string. **Do not let the model call the Azure DevOps REST API directly.** Have it emit JSON and post it yourself — far more reliable than wrapping the REST API in an MCP server,
   and it makes the posting layer independently testable.
5. **Poster script** — iterate findings → `threads` POST. Summary goes PR-level; each finding gets a `threadContext`.
6. **Idempotency** — `GET .../threads` first, match on an embedded marker (`<!-- claude-review -->`), then reply into the existing thread rather than duplicating. This is the
   `use_sticky_comment` equivalent and is **mandatory**, not optional, because build validation re-runs on every push.
7. **Permissions** — grant *Project Collection Build Service* the **"Contribute to pull requests"**
	 permission on the repo, and enable "Allow scripts to access the OAuth token" on the job. Without both, `$(System.AccessToken)` returns 403 on thread creation.

### 5.5 Pipeline variables you will need

| Variable                             | Use                                                                           |
|--------------------------------------|-------------------------------------------------------------------------------|
| `System.PullRequest.PullRequestId`   | Thread endpoint path                                                          |
| `System.PullRequest.TargetBranch`    | Diff base                                                                     |
| `System.PullRequest.SourceBranch`    | Diff head                                                                     |
| `Build.Repository.ID`                | Thread endpoint path                                                          |
| `System.TeamProject`                 | Thread endpoint path                                                          |
| `System.TeamFoundationCollectionUri` | On-prem collection root, e.g. `https://tfs.corp.local/tfs/DefaultCollection/` |
| `System.AccessToken`                 | Auth (requires the job setting above)                                         |

### 5.6 The `@claude` mention trigger — recommend deferring

The hardest piece to port. Azure DevOps has a **"Pull request commented on"** service hook, but it cannot queue a pipeline directly. The path is:

```
Service Hook (PR commented) → relay endpoint → incoming-webhook pipeline resource
                                               (resources: webhooks:, ADO Server 2019+)
```

That relay is an extra deployed component with its own availability and auth story.

**Recommendation: skip for v1.** The automatic-review-on-PR path delivers roughly 90% of the value for roughly 30% of the work. Revisit once the core review loop is proven.

---

## 6. Demonstration — PR #720

Created to show the two comment mechanisms side by side.

- **Change:** docs-only. `CLAUDE.md` documented Kotlin `2.3.20`; the actual compiler version in
	`gradle/libs.versions.toml:4` is `2.4.10`. The stale figure came from the *language API level*, which `build.gradle.kts:140` pins to `KotlinVersion.KOTLIN_2_3` — a separate knob.

- **PR-level comment** — summary, findings table, overall assessment. Equivalent to what
	`gh pr comment` produces, and to an Azure DevOps thread with **no** `threadContext`.

- **File-anchored comment 1** — `CLAUDE.md:100`, single line, includes a
	` ```suggestion ` block. Flags that hardcoding `2.4.10` recreates the drift the PR fixes.

- **File-anchored comment 2** — `CLAUDE.md:101–102`, **multi-line span**. Flags that the stated rationale for the API-level pin is unverified and likely conflates `languageVersion`
  (language features) with `apiVersion` (stdlib surface, which is what actually governs runtime compatibility).

Both file-anchored comments map to an Azure DevOps thread **with** `threadContext`; the multi-line one demonstrates `rightFileStart.line` ≠ `rightFileEnd.line`.

> Posted manually under the `Jonatha1983` account, not as `claude[bot]` — these are illustrative of
> placement mechanics, not output of the automated workflow.

### 6.1 ⚠️ Incidental finding: the live review workflow is currently broken

Opening PR #720 triggered the repo's real `claude-code-review.yml`
([run 30883081266](https://github.com/edgafner/GBrowser/actions/runs/30883081266)). It failed:

```
error: Claude Code returned an error result: Failed to authenticate.
API Error: 401 OAuth access token has expired. Re-authenticate to continue.
```

**The `CLAUDE_CODE_OAUTH_TOKEN` repo secret has expired.** This is not specific to this PR — automated review is non-functional across the whole repository until the secret is
refreshed:

```bash
claude setup-token          # generates a fresh token
gh secret set CLAUDE_CODE_OAUTH_TOKEN --repo edgafner/GBrowser
```

**Relevance to the Azure DevOps port:** OAuth tokens expire; long-lived API keys and Azure DevOps PATs do too (PATs cap at one year). Whatever credential the pipeline uses needs a
documented rotation owner and, ideally, an alert on 401 — otherwise reviews fail silently and the team simply stops receiving them without noticing. Using `$(System.AccessToken)`
sidesteps this entirely for the Azure DevOps side of the auth (it is minted per-run), but the *Anthropic* credential still needs rotation. Prefer a Key Vault-linked variable group
over a raw pipeline secret.

**Second item to verify once the token is fixed:** the run's resolved `ALLOWED_TOOLS` was

```
Glob,Grep,LS,Read,mcp__github_comment__update_claude_comment,Bash(git add:*),Bash(git commit:*),Bash(git-push.sh:*),Bash(git rm:*)
```

That list contains **neither** `mcp__github_inline_comment__create_inline_comment` **nor**
`Bash(gh pr comment:*)` — the two tools the prompt explicitly instructs Claude to use for file-anchored and PR-level comments respectively. The workflow's
`claude_args: --allowedTools Edit,Read,Write` may be overriding rather than extending the default set. Since the run died at authentication before reaching the posting stage, this
is unconfirmed — but it should be checked, because a rubric that names tools the model is not permitted to call will produce a review that silently posts nothing.

---

## 7. Open questions / risks

| # | Item                                                                   | Why it matters                                                                                                                                             |
|---|------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | **Outbound network access from on-prem agents to `api.anthropic.com`** | Hard blocker if unavailable. Determines whether you need Bedrock/Vertex/Foundry routing. **Verify before any other work.**                                 |
| 2 | Azure DevOps Server **version** in use                                 | Governs REST `api-version` (7.1 on Server 2022, 7.0 on Server 2020) and incoming-webhook support (2019+)                                                   |
| 3 | Build service account permissions                                      | "Contribute to pull requests" is not granted by default                                                                                                    |
| 4 | Cost controls                                                          | GitHub uses `if:` filters plus model tiering by PR size. Azure DevOps has no direct `author_association` equivalent — needs a different exclusion strategy |
| 5 | Prompt-injection exposure                                              | GitHub's template excludes first-time contributors. Decide the equivalent trust boundary for your Azure DevOps repos                                       |

---

## 8. References

- [claude-code-action setup](https://github.com/anthropics/claude-code-action/blob/main/docs/setup.md)
- [claude-code-action usage](https://github.com/anthropics/claude-code-action/blob/main/docs/usage.md)
- [Azure DevOps REST — PR Threads Create 7.1](https://learn.microsoft.com/en-us/rest/api/azure/devops/git/pull-request-threads/create?view=azure-devops-rest-7.1)
- [Azure Repos branch policies](https://learn.microsoft.com/en-us/azure/devops/repos/git/branch-policies?view=azure-devops)
- [`pr` trigger definition (notes the Azure Repos limitation)](https://learn.microsoft.com/en-us/azure/devops/pipelines/yaml-schema/pr?view=azure-pipelines)
- [`resources.webhooks.webhook` definition](https://learn.microsoft.com/en-us/azure/devops/pipelines/yaml-schema/resources-webhooks-webhook?view=azure-pipelines)
