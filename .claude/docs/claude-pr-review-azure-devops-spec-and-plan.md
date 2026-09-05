# Claude automated PR review on Azure DevOps **Server** — Spec & Implementation Plan

**Status:** ready to implement · **Target:** Azure DevOps Server (on-premises) · **Scope:** replicate
`claude-code-review.yml` (automatic review on every PR) — PR-level *and* file/line-anchored comments.

> **Reading this in a fresh session?** Start at **§1.5 (what you are actually building)**, then §1
> (decision), §9 (questions you must answer before writing code), §7 (setup runbook), §6 (code).
> Everything here was verified on
> 2026-08-08 against shipped source, live CLI experiments, and Microsoft Learn. Claims are marked
> **[verified]**, **[documented]**, or **[unverified]** — treat the last as work to do, not fact.
>
> Companion document: `claude-pr-review-azure-devops-port.md` (the original findings).
> **That document contains three errors this one corrects — see §12.**

---

## 1. Decision summary

### The blocker

**The official Microsoft Azure DevOps MCP server cannot talk to Azure DevOps Server.** **[verified
in shipped source]** — `@azure-devops/mcp@2.9.0`, `dist/index.js:55`:

```js
const orgUrl = "https://dev.azure.com/" + orgName;
```

The organization URL is built by string concatenation against a hardcoded cloud host. The CLI accepts
an organization *name*, not a URL. There is no flag and no environment variable that overrides it.
`dist/org-tenants.js:28` similarly hardcodes `https://vssps.dev.azure.com/`. Microsoft's position is
explicit and current: on-prem is not supported and not planned; the requests have been closed.

PAT auth *does* exist (`--authentication pat`, reading a base64 `PERSONAL_ACCESS_TOKEN`) — but it
still authenticates against `dev.azure.com`. **So "ADO MCP + PAT" as originally scoped does not work
on Server.**

> **⚠️ This applies to the *official Microsoft* server only.** If your organisation already runs a
> third-party / in-house Azure DevOps MCP server that works against your Server instance, that
> constraint does not apply to you — see **§6.5**, which covers how to wire it in and, more
> importantly, **what it should and should not be responsible for**.

### The recommendation

**Have Claude emit structured JSON, and post it with deterministic code.** Whether that code talks
REST directly (§6.3) or delegates to your org's MCP server (§6.5) is a secondary decision.

**The distinction that actually matters — and it is not about which server you use:**

| | Invoked by | Deterministic? | Right job |
|---|---|---|---|
| **MCP tools** | the **model** | ❌ — the model decides whether, when, and how many times to call | **Reading** context: PR metadata, existing threads, linked work items |
| **A script** | your **pipeline** | ✅ | **Writing** comments: idempotency, fingerprints, `changeTrackingId`, sticky summary |

An MCP server cannot be called by your YAML — only by the model. So "post via MCP" necessarily means
*the model does the posting*, and you inherit: it may skip a finding, post twice on a retry, or
silently post nothing if the allowlist is wrong (§2.4b). Those are exactly the properties you cannot
afford when **build validation re-runs on every push**.

**So: use your org MCP for reads if it helps; keep the write path deterministic.** If org policy
mandates that *all* Azure DevOps access goes through the MCP server, §6.5 covers that variant and the
verification step it requires.

```
Branch Policy (Build Validation)
   └─> Azure Pipeline
         ├─ git diff origin/<target>...HEAD          → pr.diff
         ├─ claude -p --json-schema … < prompt.txt   → review.json   (model decides WHAT)
         └─ node post-review.mjs                     → PR threads    (script decides HOW to post)
```

Five reasons the deterministic write path beats a model-driven one:

1. **It works on-prem with no third-party code.** ~150 lines, zero npm dependencies (Node 18+ global
   `fetch`), so it survives an air-gapped or proxy-restricted agent.
2. **No known MCP implementation sets `changeTrackingId`** **[verified for the official server and
   the two community ones]**, so comments can render "This file no longer exists in the latest pull
   request changes". Worth checking against your org's server (§6.5 checklist item 4).
3. **Idempotency is mandatory here, not optional.** Build validation re-runs on *every push*. Sticky
   summary + per-finding fingerprints require deterministic control the model should not be improvising.
4. **The posting layer becomes independently testable** — `DRY_RUN=1` prints payloads with no server.
5. **Fewer moving parts in the demo.** MCP adds a subprocess, a startup race (`MCP_TIMEOUT`), and an
   allowlist that silently posts nothing if you get it wrong (see §2.4).

**Effort: 2–3 days**, dominated by the one-time Azure DevOps permission/policy setup, not the code.

> **Caveat, stated plainly:** none of this has been executed against a live Azure DevOps Server. The
> code is syntax-checked and dry-run-verified; the REST contract is from Microsoft's published schema.
> **§8 Phase 0 is a 30-minute spike that de-risks the entire plan — do it first.**

---

## 1.5 What you are actually building

### 1.5.1 Component inventory — GitHub vs Azure DevOps Server

`/install-github-app` is a **setup wizard**. It contains no review logic. It produces four things, and
the port is a question of what replaces each one.

| # | GitHub component | What it does | Azure DevOps Server equivalent | Who builds it | Where it lives |
|---|---|---|---|---|---|
| 1 | **Claude GitHub App** (installed on the repo) | The *identity*. Mints short-lived tokens; comments appear as **`claude[bot]`** | **⚠️ No equivalent exists.** Either the built-in *Project Collection Build Service* (`System.AccessToken`) or a **dedicated bot user + PAT** | You choose (§9 Q4) | Server-side config — **not a file** |
| 2 | **Repo secret** `CLAUDE_CODE_OAUTH_TOKEN` | The Anthropic credential | **Library variable group** with a locked variable | You, once | Server-side config — **not a file** |
| 3 | **Two workflow files** (`.github/workflows/*.yml`) | Trigger + orchestration + the review rubric | **Pipeline YAML** *plus* a **Branch Policy → Build Validation** | You | YAML in repo; **policy is server-side config** |
| 4 | **`anthropics/claude-code-action@v1`** | **The engine.** Extracts PR context, runs Claude headless, exposes the tools that post comments, handles stickiness/progress | **Nothing exists. This is the component you build.** Anthropic closed the ADO task request as *not planned* | **You** | See §1.5.3 |

**The one-line answer:** components 1–3 are configuration you can do in an afternoon. **Component 4 is
the project.** On GitHub it arrives as a versioned Marketplace action; on Azure DevOps Server you are
writing it.

### 1.5.2 Two things that surprise people coming from GitHub

**a) There is no "bot identity" to install.** Azure DevOps has no App concept for PR automation.
Comments will be authored by either the build service (name: *"Project Collection Build Service
(YourCollection)"*) or a user account you create for the purpose. If the review appearing under a
human-looking name matters for adoption, create a dedicated **"Claude Review"** bot user with a PAT —
that is the only way to control the displayed author.

**b) A meaningful part of the setup cannot live in any file.** On GitHub, everything except the app
install and the secret is in-repo. On Server, **three** pieces are click-ops and cannot be scripted
(`az repos policy build create` is not available on Server):

- Branch Policy → Build Validation
- "Contribute to pull requests" grant to the build service identity
- The variable group and its pipeline authorization

Budget for this in the runbook (§7) and expect to need a **Project Administrator**.

### 1.5.3 Where does the engine's logic live? — packaging options

This is the question the GitHub setup answers for you and Azure DevOps does not. Four viable shapes,
in increasing order of effort:

| Option | Shape | Consumer experience | Best for |
|---|---|---|---|
| **1. Files in the repo** | `.azuredevops/claude/` + pipeline YAML, committed alongside code | Copy 4 files into each repo | ✅ **POC / this spec's §6** |
| **2. Central template repo** | Scripts + a YAML template in a tooling repo; consumers `extends:` it | 6-line YAML per repo; update once, everywhere | ✅ **Rollout beyond 1–2 repos** |
| **3. Container image** | Prebuilt image with Node + pinned Claude Code + scripts; job runs `container:` | Same as 2, but no npm at run time | ✅ **Air-gapped agents / faster runs** |
| **4. Custom pipeline task (extension)** | TypeScript task packaged as `.vsix`, installed on the collection | `- task: ClaudeCodeReview@1` — feels exactly like the GitHub Action | Org-wide product, later |

**Recommended path: 1 → 2 + 3 → (4 only if you want the polish).**

Options 2 and 3 compose: the template repo holds the pipeline logic, the container holds the runtime.
That combination is the practical equivalent of `claude-code-action@v1` — versioned, centrally
maintained, no per-repo drift — at a fraction of the cost of building an extension.

> **Security reason to move off Option 1 early.** In an Azure Repos PR build, the pipeline YAML **and
> any scripts it runs are taken from the PR's source branch**. Under Option 1, a pull request can
> modify `post-review.mjs`, the rubric, or the pipeline itself — and that modified code then runs with
> `System.AccessToken` and your Anthropic key in the environment. On an internal Server with trusted
> contributors this is an accepted risk for a POC; **it is not acceptable for broad rollout.**
> Option 2 (with the template in a separate, permission-controlled repo) removes the scripts from the
> attacker's reach. Fully closing it also requires preventing the top-level YAML from being edited —
> via a **Required template check**, or by defining the pipeline's YAML in the tooling repo and
> checking out the reviewed repo as a resource. **[unverified on Server — confirm in Phase 3.]**

### 1.5.4 Concrete deliverable for the POC (Option 1)

Four files, ~250 lines total. Nothing else is built; no Docker, no extension, no service.

```
<your-repo>/
├── azure-pipelines-claude-review.yml     §6.4  ~120 lines  orchestration
└── .azuredevops/claude/
    ├── rubric.md                         §6.2  ~25 lines   what to review (the GitHub `prompt:` block)
    ├── review-schema.json                §6.1  ~45 lines   the model's output contract
    └── post-review.mjs                   §6.3  ~150 lines  the poster (replaces the action's tool layer)
```

Plus the three click-ops steps in §7. **That is the whole POC.**

Mapping back to the action so the correspondence is explicit:

| `claude-code-action` responsibility | Where it goes here |
|---|---|
| Extract PR context (metadata, diff) | `git diff` step in the pipeline YAML |
| Mint credentials | `System.AccessToken` (automatic) + variable group |
| Run Claude headless | `claude -p --json-schema` step |
| Tool surface for posting (`gh pr comment`, `create_inline_comment`) | **`post-review.mjs`** — one `POST /threads`, `threadContext` present or absent |
| `use_sticky_comment` | Marker + `PATCH` of the existing summary comment |
| `track_progress` | **Dropped.** Live progress comments are polish; not worth building for v1 |
| `classify_inline_comments` | **Dropped.** The schema's `severity` field covers the need |

---

## 1.6 Runtime trace — what actually happens when a PR opens

**[verified against `anthropics/claude-code-action` `action.yml` and `package.json`, 2026-08-08]**

### 1.6.1 On GitHub, step by step

You open a PR. Then:

| # | What happens | Where |
|---|---|---|
| 1 | GitHub matches `on: pull_request` → `opened`, then applies the job `if:` filters (dependabot, draft, first-time contributor) | GitHub service |
| 2 | GitHub provisions a **fresh ephemeral VM** (`ubuntu-latest`). **Not a container** — a whole throwaway machine | GitHub-hosted runner |
| 3 | `actions/checkout@v6` clones your repo at the PR head SHA | runner |
| 4 | GitHub clones the **action's own repo** into `$GITHUB_ACTION_PATH` | runner |
| 5 | The action is a **`using: "composite"`** action. First step: install **Bun 1.3.14** (~35 MB, downloaded from `oven-sh/setup-bun` releases) | runner |
| 6 | `bun install --production` — pulls the action's npm dependencies, **including `@anthropic-ai/claude-agent-sdk` (~4 MB)**. **This is the Claude engine** | runner ← npm |
| 7 | `bun run src/entrypoints/run.ts` — the action's TypeScript entrypoint runs **in-process** | runner |
| 8 | That code reads the event payload, calls the **GitHub API** (Octokit) for PR metadata / diff / existing comments, and mints a token from the App installation | runner → `api.github.com` |
| 9 | It starts **in-process MCP servers** (`github_inline_comment`, `github_comment`) that wrap the GitHub API, then invokes the Agent SDK | runner |
| 10 | The SDK streams the conversation to **`api.anthropic.com`**. The model calls the MCP tools; those tools POST comments back to GitHub | runner ↔ Anthropic, runner → GitHub |
| 11 | VM is destroyed | — |

### 1.6.2 Direct answers

**"Where does the Claude executable come from?"** It is **downloaded fresh on every single run**, as an
npm dependency (step 6). Nothing is pre-installed and nothing persists.

**"Is it on the agent?"** No — GitHub-hosted runners are ephemeral. Every run starts from a clean VM
image and reinstalls Bun + the SDK.

**"Is it running in Docker?"** **No.** This is a composite action, not a Docker action. It runs
directly on the runner VM. (Docker *containers* are only involved if *you* add a `container:` key.)

**"Does it use the API only for messages?"** Two distinct network paths, and it matters for the port:

| Traffic | Endpoint | Purpose |
|---|---|---|
| **Inference** | `api.anthropic.com` | The model conversation. Substitutable — **this project uses Amazon Bedrock, see §1.7** |
| **Platform ops** | `api.github.com` | Read PR metadata & diff, **post comments**. Not substitutable — this is the part you re-implement |
| Bootstrap | npm + `oven-sh` releases | Downloading Bun and the SDK each run |

> **Note — CLI vs SDK.** The action does **not** shell out to the `claude` CLI. It embeds
> **`@anthropic-ai/claude-agent-sdk`**, the same engine as a library. The CLI (`claude -p`) and the SDK
> are two front doors to the same thing. This spec uses the **CLI** because it is shell-friendly and
> needs no code to drive it. If you later build the pipeline-task extension (§1.5.3 Option 4), that
> extension would use the **SDK** — exactly as `claude-code-action` does.

### 1.6.3 How this maps to Azure DevOps — and the one thing that changes

The trace is nearly identical, with **one structural difference that works in your favour**:

> **GitHub-hosted runners are ephemeral. Your Azure DevOps Server agents are persistent.**

GitHub *has to* reinstall the engine every run because the machine is destroyed. **You do not.** That
turns a forced cost into a choice:

| Option | How | Cost per run | Works air-gapped? | Use when |
|---|---|---|---|---|
| **A. Install per run** | `npm install -g @anthropic-ai/claude-code@<pinned>` in the pipeline | ~20–40 s + npm egress **every run** | ✗ needs a registry | **POC** — this is what §6.4 does |
| **B. Pre-installed on the agent** | Install once when provisioning the agent VM; the pipeline just calls `claude` | **0 s** | ✓ (install once) | **Fixed self-hosted pool** — simplest rollout |
| **C. Container job** | Prebuilt image (Node 22 + pinned Claude Code + scripts); job uses `container:` | image pull, then cached | ✓ | Shared/heterogeneous pool; want reproducibility |

**Recommendation: A for the POC, then B or C in Phase 3.5.** Option B is the pragmatic win for a
dedicated pool — pre-baking the agent removes the npm dependency at run time entirely, which is often
the difference between "works" and "blocked" on a locked-down network. Option C if the pool is shared
or you want the runtime version-pinned in source control.

If you take **B**, delete the install step from §6.4 and add a version assertion instead, so a drifted
agent fails loudly rather than reviewing with an unexpected build:

```bash
# Fail fast if the agent image drifted. --json-schema is only reliable from 2.1.205.
REQUIRED=2.1.205
ACTUAL=`claude --version | grep -oE '[0-9]+\.[0-9]+\.[0-9]+'`
if [ "`printf '%s\n%s' "$REQUIRED" "$ACTUAL" | sort -V | head -1`" != "$REQUIRED" ]; then
  echo "##vso[task.logissue type=error]Claude Code $ACTUAL is older than required $REQUIRED"
  exit 1
fi
```

**Network egress the agent needs**, regardless of option:

| Destination | Required? | Notes |
|---|---|---|
| **`bedrock-runtime.<region>.amazonaws.com:443`** | **Mandatory** | **Phase 1 uses Bedrock (§1.7)** — `api.anthropic.com` is *not* used |
| `sts.<region>.amazonaws.com:443` | If assuming a role | Not needed with a Bedrock API key |
| Your ADO Server | Mandatory | Internal — put it in `NO_PROXY` so it bypasses the corporate proxy |
| `registry.npmjs.org` | **Only for Option A** | Options B and C remove this from the run-time path |

**And the component that has no substitute:** step 8–10's *platform ops* path. Inference is
swappable; **posting comments is the part you re-implement** — `post-review.mjs` (§6.3) is precisely
the Azure DevOps counterpart of the action's in-process `github_inline_comment` MCP server.

---

## 1.7 Model access — Amazon Bedrock (Phase 1 decision) 🔶

**Decided: Phase 1 routes inference through Amazon Bedrock, not `api.anthropic.com`.**

**[verified against the official Bedrock page, 2026-08-08]**. This is a good fit — it keeps model
traffic inside an account you already govern, and it answers §9 Q1 outright. But it introduces
**four Bedrock-specific failure modes**, two of which cost money silently. Read 1.7.2 before running
anything.

### 1.7.1 What changes

| | Direct Anthropic API | **Amazon Bedrock (this project)** |
|---|---|---|
| Credential | `ANTHROPIC_API_KEY` | AWS credentials (SDK chain) or `AWS_BEARER_TOKEN_BEDROCK` |
| Endpoint | `api.anthropic.com` | `bedrock-runtime.<region>.amazonaws.com` |
| Model IDs | `claude-sonnet-4-6` | `us.anthropic.claude-sonnet-4-6` — **inference-profile prefixed** |
| Enable step | none | **Submit the use case form once per AWS account** |
| Prompt caching | always on | **may be unavailable in some regions** — see 1.7.2b |
| Cost reporting | `total_cost_usd` populated | **[unverified] — do not gate CI on it** |
| WebSearch tool | available | **not available on Bedrock** (irrelevant here — we use `Read,Grep,Glob`) |

### 1.7.2 The four things that will bite you

**a) 💸 Not pinning the model bills you at the Opus rate.** From the docs, verbatim:

> *"Opus models have a higher per-token price than Sonnet models, so a deployment that doesn't pin a
> primary model is billed at the Opus rate once it updates to v2.1.207 or later."*

On Bedrock the built-in default primary model is **Opus 5**, and `sonnet`/`opus` are *aliases that do
not act as pins*. A PR reviewer running on every push at Opus rates is a budget incident.

> **✅ Mandatory: set `ANTHROPIC_MODEL` to a full Bedrock model ID.** Do not pass a bare alias to
> `--model`.

**b) 💸 Prompt caching may silently not work in your region.** §2.4e measured cold-start cache
creation dominating cost. Bedrock prompt caching is **not available in every region** — and the
failure is silent: *"If cache token counts stay at zero, check supported models, regions, and
limits."* Without caching, every run re-pays full input cost.

> **Check in Phase 0:** run one review and confirm `usage.cache_creation_input_tokens` /
> `cache_read_input_tokens` are non-zero. If they are zero, either switch region or accept the cost.

**c) 🔌 A TLS-inspecting corporate proxy will break streaming.** Highly relevant on-prem. Bedrock
streams in a binary format (`application/vnd.amazon.eventstream`); a gateway that re-emits it as
`text/event-stream` produces:

```
Bedrock streaming response has content-type ...
```

Fix the gateway to pass the body **and** `Content-Type` through unmodified. Only as a temporary
workaround, and only if the binary body is intact, set
`CLAUDE_CODE_DISABLE_BEDROCK_CONTENT_TYPE_GUARD=1`.

**d) 🔑 Model access is not on by default.** You must submit the **use case form** once per AWS
account (Bedrock console → Model catalog → select an Anthropic model). Access is granted immediately
on submission. With AWS Organizations, call `PutUseCaseForModelAccess` once from the management
account and it extends to child accounts.

### 1.7.3 Pipeline configuration

Replace the `ANTHROPIC_API_KEY` wiring in §6.4 with this.

```yaml
variables:
  - group: claude-review-secrets     # holds the AWS credential, locked
  - name: awsRegion
    value: us-east-1
  # Full Bedrock inference-profile IDs. NEVER a bare alias — see 1.7.2a.
  - name: reviewModel
    value: 'us.anthropic.claude-sonnet-4-6'
  - name: reviewModelLarge          # cheaper tier for big diffs
    value: 'us.anthropic.claude-haiku-4-5-20251001-v1:0'
```

```yaml
        env:
          CLAUDE_CODE_USE_BEDROCK: '1'
          AWS_REGION: $(awsRegion)
          # Simplest CI credential — one secret, no STS, no profile. See 1.7.4.
          AWS_BEARER_TOKEN_BEDROCK: $(AWS_BEARER_TOKEN_BEDROCK)
          # Pin every alias so nothing can silently resolve to Opus.
          ANTHROPIC_MODEL: $(reviewModel)
          ANTHROPIC_DEFAULT_SONNET_MODEL: $(reviewModel)
          ANTHROPIC_DEFAULT_HAIKU_MODEL: $(reviewModelLarge)
```

And in the `claude -p` invocation, pass the **full ID**:

```bash
claude -p --model "${ANTHROPIC_MODEL}" …
```

> **Drop `--max-budget-usd`** from §6.4 when running on Bedrock, or treat it as advisory:
> it depends on cost accounting that is **[unverified]** on Bedrock. Enforce spend with AWS Budgets
> and the `maxDiffBytes` guard instead.

Region-prefix reference — Claude Code derives the prefix from the resolved region:

| AWS region | Prefix | | AWS region | Prefix |
|---|---|---|---|---|
| `us-*` | `us.` | | `ap-*` | `apac.` |
| `eu-*` | `eu.` | | `us-gov-*` | `us-gov.` |
| all others | `global.` | | | |

`ANTHROPIC_BEDROCK_REGION_PREFIX` (v2.1.224+) overrides the preference; valid values `us`, `eu`,
`apac`, `jp`, `au`, `global`.

### 1.7.4 Getting AWS credentials into an Azure DevOps pipeline

Three options, easiest first:

| Option | How | Verdict |
|---|---|---|
| **1. Bedrock API key** | One locked variable → `AWS_BEARER_TOKEN_BEDROCK`. Bypasses the SDK credential chain entirely | ✅ **Recommended for the POC** — one secret, no STS round-trip, no profile files on the agent |
| 2. Static IAM access key | `AWS_ACCESS_KEY_ID` + `AWS_SECRET_ACCESS_KEY` as locked variables | Works; two long-lived secrets to rotate |
| 3. AWS Toolkit for Azure DevOps | Install the extension, create an AWS service connection, run via `AWSShellScript@1` | Best governance (supports assume-role); **[verify the extension installs on your Server]** |

> With option 1, also grant **`bedrock:GetInferenceProfile`**. The docs note bearer-token policies are
> typically narrower, and without it Claude Code silently pays an extra round-trip per new model.

Note the credential is **not** auto-mapped — like every Azure DevOps secret it must appear in the
step's `env:` block explicitly (§2.5).

### 1.7.5 IAM policy

Attach to the identity behind whichever credential option you chose:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowModelAndInferenceProfileAccess",
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream",
        "bedrock:ListInferenceProfiles",
        "bedrock:GetInferenceProfile"
      ],
      "Resource": [
        "arn:aws:bedrock:*:*:inference-profile/*",
        "arn:aws:bedrock:*:*:application-inference-profile/*",
        "arn:aws:bedrock:*:*:foundation-model/*"
      ]
    },
    {
      "Sid": "AllowMarketplaceSubscription",
      "Effect": "Allow",
      "Action": ["aws-marketplace:ViewSubscriptions", "aws-marketplace:Subscribe"],
      "Resource": "*",
      "Condition": { "StringEquals": { "aws:CalledViaLast": "bedrock.amazonaws.com" } }
    }
  ]
}
```

Tighten `Resource` to the specific inference-profile ARNs you pinned once it works. Anthropic's own
guidance: *"Create a dedicated AWS account for Claude Code to simplify cost tracking and access
control."*

### 1.7.6 Air-gapped / restricted networks

If the agents cannot reach AWS public endpoints, the answer is a **VPC interface endpoint
(PrivateLink)** for `bedrock-runtime`, reached over Direct Connect or VPN, with
`ANTHROPIC_BEDROCK_BASE_URL` pointed at it. **[unverified for your topology]** — treat as a Phase 0
question if egress is blocked.

Claude Code uses the Bedrock **Invoke API**, not the Converse API — relevant when writing firewall or
gateway rules.

---

## 2. Verified facts

Everything in this section was checked directly. Where the truth differs from what you might assume,
that is called out.

### 2.1 Trigger: `pr:` does not work on Azure Repos

**[verified — adversarially confirmed]** YAML `pr:` triggers are honoured for GitHub and Bitbucket
Cloud only. For Azure Repos Git they are **silently ignored** — not rejected, not warned about. PR
validation is implemented *exclusively* through **Branch Policy → Build validation**.

Consequences that shape the design:

| Behaviour | Detail |
|---|---|
| Queues on | PR creation, **and every push to the source branch** |
| Draft PRs | **Do not trigger.** Publishing a draft does. This is a free `ready_for_review` equivalent — no YAML filter needed or possible |
| `Build.Reason` | Exactly `PullRequest` |
| Checkout | Server-computed **merge commit, detached HEAD**. `Build.SourceBranch` = `refs/pull/<id>/merge` |
| Scripted setup | `az repos policy build create` is **not available on Server** — browser UI or REST API only |
| Rights needed | **Project Administrator** to configure the policy |
| `trigger:` | Keep it **`none`**. A conventional CI trigger on the source branch fires a *second, distinct* run with no `Build.Reason=PullRequest`, no `System.PullRequest.*` variables, and not the merge commit — it would double-fire and then fail the guard |
| Not documented | Behaviour on PR *reopen*, on *target-branch change*, and whether in-flight builds auto-cancel. **[unverified]** — do not design around auto-cancel |

### 2.2 `fetchDepth: 0` is non-negotiable

**[verified]** Since Sprint 209 (Sept 2022) shallow fetch is the default. With it, the agent fetches
**only the merge commit by SHA** — neither `HEAD^1` nor `origin/<target>` exists locally, so **no diff
is possible at all**. With `fetchDepth: 0` the agent fetches `+refs/heads/*:refs/remotes/origin/*`
and both parents become available.

The merge commit `M` has `M^1` = target tip and `M^2` = PR source head. Use
`git diff origin/<target>...HEAD` (matches the PR UI). **Never `git diff HEAD~1 HEAD`.**

### 2.3 REST api-version ceiling — the original doc was wrong

**[verified — the earlier doc's claim was adversarially REFUTED]**

| Azure DevOps Server release | Max REST `api-version` |
|---|---|
| Server 2019 | **5.0** |
| Server 2020 | **6.0** |
| Server 2022 (RTW) | **7.0** |
| Server 2022.1+ | **7.1** |
| Current unversioned "Azure DevOps Server" (25H2) | 7.2 (no published on-prem doc view) |

The earlier doc said "7.1 on Server 2022, 7.0 on Server 2020". **Both halves are wrong.** Sending too
high a version returns an explicit, useful error naming the true maximum:

> `The requested REST API version of X is out of range for this server. The latest REST API version this server supports is Y.`

**Use that as a probe in Phase 0** — it is faster than asking anyone which build is installed.

### 2.4 Claude Code CLI — five behaviours that change the design

All **[verified empirically]** on CLI v2.1.226 during this investigation.

**a) `--tools` is the security boundary. `--allowedTools` is not.**

| Flag | What it does | Test result |
|---|---|---|
| `--tools "Read"` | **Replaces** the built-in tool set | Model reported `BASH_UNAVAILABLE` — Bash did not exist |
| `--allowedTools "Read"` | Permission allowlist only — **grants, never restricts** | **Bash still ran** |

**Nuance worth being precise about:** the Bash call that "still ran" was `echo`, which belongs to a
built-in, non-configurable set of **read-only** commands (`ls`, `cat`, `echo`, `pwd`, `head`, `tail`,
`grep`, `find`, `wc`, `which`, `diff`, `stat`, `du`, `cd`, read-only `git`) that never require an
allow rule, in any permission mode. Write-capable, exec-capable and network commands **do** need an
explicit `Bash(...)` entry. So headless mode is not blanket-permissive — but the conclusion stands:
**`--allowedTools` cannot take a capability away, and `--tools` is what constrains a CI run.**

**And a denied call does not fail the run.** It is returned to the model as a tool result; the process
still exits **0** with `is_error: false` and `terminal_reason: "completed"`. The only signal is a
non-empty **`permission_denials`** array. **Any CI gate must check that array, never the exit code
alone** — otherwise a run that posted nothing reports success.

> This also retires an open question from the earlier doc: `--allowedTools Edit,Read,Write` in the
> GitHub workflow was never removing the inline-comment MCP tool. That failure was purely the
> expired token.

**b) But MCP tools *are* different — they always require explicit allowlisting.** **[verified by A/B
test]** Without an allow rule an MCP call is denied. And allow-rule globs must be anchored to a
literal server prefix: `mcp__ado__*` works; **`mcp__*` is silently skipped and auto-approves
nothing**. This is a silent-failure trap and a further argument against the MCP path.

**c) `--json-schema` is first-class and is the backbone of this design.** Emits a validated object in
a top-level `structured_output` key.

- Takes **inline JSON only** — `--json-schema schema.json` errors with
  `--json-schema is not valid JSON`. Use `` --json-schema "`cat schema.json`" ``.
- **Only reliable from v2.1.205.** Earlier versions *silently ignored* an invalid schema and returned
  unstructured text. **Pin the version.**

**d) `subtype` is not a reliable success signal.** A hard auth failure still returns
`"subtype":"success"` with `is_error:true`. **Gate on `is_error` / `terminal_reason`.**

**e) Cold-start cost is dominated by cache creation.** A trivial prompt measured **$1.95** because it
loaded CLAUDE.md, skills, plugins and ~200 MCP tool definitions. The same prompt with
`--setting-sources "" --strict-mcp-config --model sonnet` cost **~$0.04**. Both flags are in the
pipeline below — they are cost controls *and* security controls.

**f) `claude -p` reads the prompt from stdin.** This matters on Azure DevOps specifically: `$(cat f)`
inside a `script:` block collides with ADO's `$(var)` macro syntax, which expands **before bash runs**.
Feed the prompt via `< prompt.txt` and use **backticks** for any other substitution.

### 2.5 Auth and permissions

- **`System.AccessToken`** is always available in YAML. There is **no** "Allow scripts to access the
  OAuth token" checkbox in YAML — that is the Classic designer. It must be mapped explicitly:
  `env: ADO_TOKEN: $(System.AccessToken)`.
- The identity is **`Project Collection Build Service ({Collection})`** when job authorization scope
  is collection (default), or **`{Project} Build Service ({Collection})`** when scoped to project.
  The project-scoped identity **is only created after the pipeline has run once.**
- Required repo permission: **"Contribute to pull requests"** (`GitRepositories` /
  `PullRequestContribute`) plus **Read**. It does **not** grant push — the reviewer stays read-only
  on code, which is what you want.
- PAT alternative: HTTP Basic with an **empty username** — `Basic base64(":" + PAT)`. Scopes:
  `vso.code_write` + `vso.threads_full`.
- **On-prem gotcha [documented]:** enabling **IIS Basic Authentication** on the Azure DevOps Server
  web application **breaks PAT authentication entirely.**
- **Secret variables are not auto-mapped** into the environment. Non-secret predefined variables *are*
  (`System.PullRequest.PullRequestId` → `SYSTEM_PULLREQUEST_PULLREQUESTID`).
- **Scripts do not inherit the agent's proxy.** It is exposed only as `Agent.ProxyUrl` /
  `Agent.ProxyUsername` / `Agent.ProxyPassword` (secret) / `Agent.ProxyBypassList`. `HTTPS_PROXY` must
  be set explicitly, and the ADO Server host belongs in `NO_PROXY`.

### 2.6 The comment API — both types, one endpoint

```
POST {collectionUri}{project}/_apis/git/repositories/{repoId}/pullRequests/{prId}/threads?api-version=7.0
```

**Omit `threadContext`** → PR-level comment. **Include it** → file/line-anchored comment. That single
distinction is the whole mapping from `gh pr comment` / `create_inline_comment`.

- `filePath` — repo-root-relative, **leading slash**.
- `line` is **1-based**. `offset` is documented inconsistently by Microsoft (6.0/7.0 say "starts at
  1", 7.1 says "starts at 0"); **every real implementation uses `offset: 1`** for a whole-line
  comment. Use 1.
- `CommentType`: `Unknown=0, Text=1, CodeChange=2, System=3`.
- `CommentThreadStatus`: `Unknown=0, Active=1, Fixed=2, WontFix=3, Closed=4, ByDesign=5, Pending=6`.
  Both accept the integer *or* the camelCase string.
- **`changeTrackingId`** is **not** required for the POST to succeed, but without it the UI can show
  *"This file no longer exists in the latest pull request changes"*. A **wrong** value is worse than
  none — it anchors the comment to the wrong file. Source it from
  `GET .../pullRequests/{id}/iterations/{iterationId}/changes` → `changeEntries[].changeTrackingId`,
  matched on `changeEntries[].item.path`.
- Listing threads for idempotency has **no server-side filter** — you get system threads too, so
  dedupe client-side.

---

## 3. Options considered

| # | Option | On-prem? | Inline comments? | Verdict |
|---|---|---|---|---|
| **A** | **Structured JSON + REST poster script** | ✅ | ✅ full (multi-line, `changeTrackingId`) | ✅ **Recommended for the write path** |
| **H** | **Your org's existing non-official MCP server** | ✅ (in use today) | **run the §6.5.1 checklist** | ✅ **Recommended for reads**; viable for writes if it passes items 1–3 |
| B | Official `@azure-devops/mcp` | ❌ **hard blocker** | ✅ (but cloud-only) | ❌ Impossible |
| C | Vendored one-line patch of B | ⚠️ likely | ✅ | ⚠️ Fork maintenance; 3 more cloud endpoints remain |
| D | `Tiberriver256/mcp-server-azure-devops` | ✅ explicit | ⚠️ offset hardcoded 1, right side only, **no multi-line** | Fallback if MCP is mandated |
| E | `burcusipahioglu/azure-devops-mcp-onprem` | ✅ built for it | ✅ | ⚠️ 12★; 3 headless-hostile defaults (10 writes/min cap, always-ask directive, readonly switch) |
| F | Marketplace `listellm.claude-code-base-task@3` | ⚠️ structurally yes (configurable `collectionUri`) | ✅ with severity filtering | **Evaluate — could beat building** (§8 Phase 0) |
| G | `rios0rios0/code-guru` | ⚠️ unverified | ✅ inline + general, mature re-review | Best OSS reference to read |

**Do not use `lamees-hourani/ado-pr-reviewer`** — it claims on-prem support via Microsoft's MCP
server, and that claim is **false**.

**Anthropic ships nothing for Azure DevOps.** Issue #342 (ADO Pipeline Task) was **closed as not
planned**; #277 (ADO extension) has been open since 2025-07-15 with no response. The closest official
template is the **GitLab CI/CD** page — and it is maintained by GitLab, not Anthropic, and does *not*
demonstrate file-anchored comments. Azure DevOps Server has **no built-in AI PR review**; GitHub
Copilot code review for Azure Repos is Services-only and refuses self-hosted agent pools.

---

## 4. Target architecture

```
Branch Policy: Build Validation (Automatic, Optional, expiration Never)
        │
        ▼
 azure-pipelines-claude-review.yml       condition: Build.Reason == PullRequest && !IsFork
        │
        ├─ 1. checkout self (fetchDepth: 0)         ← merge commit, detached HEAD
        ├─ 2. install Node 22 + pinned Claude Code
        ├─ 3. git diff origin/<target>...HEAD       → pr.diff, pr.files
        ├─ 4. claude -p --json-schema … < prompt.txt → claude-out.json
        │        --tools "Read,Grep,Glob"            (model can only read; cannot post)
        │        jq .structured_output               → review.json
        └─ 5. node post-review.mjs                   → PR threads
                 ├─ GET  /threads              (dedupe by fingerprint)
                 ├─ GET  /iterations/{n}/changes (changeTrackingId)
                 ├─ POST /threads  × findings  (threadContext ⇒ file-anchored)
                 └─ POST or PATCH  summary     (no threadContext ⇒ PR-level, sticky)
```

**The separation is the point:** the model decides *what to say*; the script decides *how to post it*.
The model never holds a credential and never calls the Azure DevOps API.

---

## 5. Contracts

### 5.1 Marker conventions (idempotency)

| Marker | Where | Purpose |
|---|---|---|
| `<!-- claude-review:summary -->` | summary comment | Find and **PATCH in place** — the `use_sticky_comment` equivalent |
| `<!-- claude-review:fp:{path}:{fingerprint} -->` | each inline comment | Skip if an open thread already carries it |

`fingerprint` is a stable kebab-case slug for the *kind* of issue (`cancellation-swallowed`), emitted
by the model and constrained by the schema's `pattern`. It must **not** encode a line number — lines
shift on every push, and the whole point is to survive that.

### 5.2 Severity → thread status

| Severity | Thread status | Effect |
|---|---|---|
| `blocker` | `Active` (1) | Open thread; can gate the PR if the policy is Required |
| `major` / `minor` / `nit` | `Closed` (4) | Visible, informational, does not gate |

---

## 6. Reference implementation

Three files under `.azuredevops/claude/`, plus the pipeline. **All syntax-checked; the poster script
dry-runs correctly; the schema chain was verified end to end against a sample diff.**

### 6.1 `.azuredevops/claude/review-schema.json`

```json
{
  "type": "object",
  "additionalProperties": false,
  "required": ["summary", "assessment", "findings"],
  "properties": {
    "summary": {
      "type": "string",
      "description": "2-5 sentences: what the PR does and your overall assessment. Markdown."
    },
    "assessment": { "type": "string", "enum": ["approve", "comment", "request_changes"] },
    "findings": {
      "type": "array",
      "description": "One entry per file-anchored comment. Empty if nothing is worth flagging.",
      "items": {
        "type": "object",
        "additionalProperties": false,
        "required": ["file", "startLine", "endLine", "severity", "fingerprint", "body"],
        "properties": {
          "file": {
            "type": "string",
            "description": "Repo-root-relative path exactly as it appears in the diff, no leading slash."
          },
          "startLine": {
            "type": "integer", "minimum": 1,
            "description": "1-based line number in the HEAD (right/new) version of the file."
          },
          "endLine": {
            "type": "integer", "minimum": 1,
            "description": "Same as startLine for a single-line comment; greater for a span."
          },
          "severity": {
            "type": "string", "enum": ["blocker", "major", "minor", "nit"],
            "description": "Only 'blocker' leaves the thread Active and able to gate the PR."
          },
          "fingerprint": {
            "type": "string", "pattern": "^[a-z0-9]+(-[a-z0-9]+)*$",
            "description": "Stable kebab-case slug for the KIND of issue, e.g. 'cancellation-swallowed'. Must be identical across runs for the same underlying issue so re-runs do not duplicate comments."
          },
          "body": {
            "type": "string",
            "description": "Markdown explaining the issue and the fix. May contain a ```suggestion fenced block."
          }
        }
      }
    }
  }
}
```

### 6.2 `.azuredevops/claude/rubric.md`

Port of the GitHub `prompt:` block. **Note what is deliberately absent:** the two lines telling the
model which tool to post with. It no longer posts — it returns data.

````markdown
Review this pull request. Return your findings using the required JSON schema.

## What to look for

**Functionality** — bugs, logic errors, unhandled edge cases, performance, resource leaks.
**Security & testing** — vulnerabilities, input validation, missing test coverage.
**Architecture** — separation of concerns, impact on existing behaviour, alignment with the
patterns already used in this repository.
**Dependencies** — flag newly added dependencies, and any added to the build file that should
instead come from the platform.
**Documentation** — comments on non-obvious logic; conformance to CLAUDE.md.

Read CLAUDE.md in the repository root for project-specific rules and treat them as binding.

## Rules for findings

- One finding per distinct issue. Anchor it to the most specific line span you can.
- `file` must match a path in the "Changed files" list exactly.
- `startLine`/`endLine` are line numbers in the **new** version of the file.
- `fingerprint` must describe the KIND of issue, never the location. It has to stay identical if
  the same issue is re-reported after the author pushes more commits.
- Reserve `blocker` for things that must change before merge — it leaves an open thread that can
  gate the PR. Style preferences are `nit`.
- Prefer a ```suggestion block over prose when the fix is mechanical.
- Say nothing about formatting a linter would catch, and do not flag duplicate CHANGELOG entries.
- If the change is trivial (a version bump), return an empty `findings` array and a one-line summary.
- If you find nothing worth saying, return zero findings. Do not invent issues to seem useful.
````

### 6.3 `.azuredevops/claude/post-review.mjs`

Zero dependencies — Node 18+ global `fetch`. Nothing is installed on the agent.

```js
#!/usr/bin/env node
// Posts a Claude review to an Azure DevOps Server pull request.
//
// Required env: ADO_COLLECTION_URI, ADO_PROJECT, ADO_REPO_ID, ADO_PR_ID, ADO_TOKEN
// Optional env: ADO_API_VERSION (default 7.1), REVIEW_JSON (default review.json), DRY_RUN=1

const {
  ADO_COLLECTION_URI, ADO_PROJECT, ADO_REPO_ID, ADO_PR_ID, ADO_TOKEN,
  ADO_API_VERSION = '7.1', REVIEW_JSON = 'review.json', DRY_RUN = '',
} = process.env;

for (const [k, v] of Object.entries({ ADO_COLLECTION_URI, ADO_PROJECT, ADO_REPO_ID, ADO_PR_ID, ADO_TOKEN })) {
  if (!v) { console.error(`Missing required env var ${k}`); process.exit(2); }
}

const MARKER = '<!-- claude-review -->';
const SUMMARY_MARKER = '<!-- claude-review:summary -->';
const fp = (f) => `<!-- claude-review:fp:${f} -->`;

const STATUS = { Unknown: 0, Active: 1, Fixed: 2, WontFix: 3, Closed: 4, ByDesign: 5, Pending: 6 };
const COMMENT_TYPE_TEXT = 1;

const base = `${ADO_COLLECTION_URI.replace(/\/+$/, '')}/${encodeURIComponent(ADO_PROJECT)}` +
  `/_apis/git/repositories/${encodeURIComponent(ADO_REPO_ID)}/pullRequests/${ADO_PR_ID}`;

// PAT and System.AccessToken both authenticate as Basic with an empty username.
const authHeader = 'Basic ' + Buffer.from(`:${ADO_TOKEN}`).toString('base64');

async function api(path, { method = 'GET', body } = {}) {
  const url = `${base}${path}${path.includes('?') ? '&' : '?'}api-version=${ADO_API_VERSION}`;
  const res = await fetch(url, {
    method,
    headers: { Authorization: authHeader, 'Content-Type': 'application/json', Accept: 'application/json' },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  if (!res.ok) {
    // An HTML body here almost always means the collection URI is wrong or auth was
    // rejected and the server bounced us to a sign-in page.
    throw new Error(`${method} ${url} -> ${res.status} ${res.statusText}\n${text.slice(0, 800)}`);
  }
  return text ? JSON.parse(text) : null;
}

const severityBadge = (s) =>
  ({ blocker: '🔴 Blocker', major: '🟠 Major', minor: '🟡 Minor', nit: '⚪ Nit' }[s] ?? s);

// ADO expects a repo-root-relative path with a leading slash.
function normalizePath(p) {
  const clean = String(p).replace(/^\.\//, '');
  return clean.startsWith('/') ? clean : `/${clean}`;
}

function threadContextFor(f) {
  const ctx = { filePath: normalizePath(f.file) };
  if (Number.isInteger(f.startLine) && f.startLine >= 1) {
    // offset is 1-based. Supply both start and end.
    ctx.rightFileStart = { line: f.startLine, offset: 1 };
    const endLine = Number.isInteger(f.endLine) && f.endLine >= f.startLine ? f.endLine : f.startLine;
    ctx.rightFileEnd = { line: endLine, offset: 1 };
  }
  return ctx;
}

// changeTrackingId ties a comment to a file within a PR iteration. Not required for the
// POST to succeed, but without it the UI can render "This file no longer exists in the
// latest pull request changes". A WRONG value is worse than none — it anchors the comment
// to the wrong file — so we only set it on an exact path match.
async function loadChangeTracking() {
  try {
    const iterations = (await api('/iterations')).value ?? [];
    if (!iterations.length) return { iterationId: null, byPath: new Map() };
    const iterationId = Math.max(...iterations.map((i) => i.id));
    const { changeEntries = [] } = await api(`/iterations/${iterationId}/changes`);
    const byPath = new Map(changeEntries
      .filter((c) => c.item?.path && c.changeTrackingId != null)
      .map((c) => [c.item.path, c.changeTrackingId]));
    return { iterationId, byPath };
  } catch (e) {
    console.error(`Warning: could not load change tracking (${e.message.split('\n')[0]}). ` +
      `Comments will still post, but may show a stale-file warning in the UI.`);
    return { iterationId: null, byPath: new Map() };
  }
}

async function main() {
  const { readFile } = await import('node:fs/promises');
  const review = JSON.parse(await readFile(REVIEW_JSON, 'utf8'));
  const findings = Array.isArray(review.findings) ? review.findings : [];

  // ---- 1. Read existing threads so re-runs do not duplicate ----------------
  // DRY_RUN never touches the network, so the script is testable with no server.
  const existing = DRY_RUN === '1' ? [] : ((await api('/threads')).value ?? []);
  const liveComments = existing
    .filter((t) => t.status !== STATUS.Closed)
    .flatMap((t) => (t.comments ?? []).map((c) => ({ thread: t, content: c.content ?? '' })));

  const summaryThread = existing.find((t) =>
    (t.comments ?? []).some((c) => (c.content ?? '').includes(SUMMARY_MARKER)));

  const alreadyPosted = new Set(liveComments
    .map((c) => c.content.match(/<!-- claude-review:fp:([^\s>]+) -->/)?.[1])
    .filter(Boolean));

  // ---- 2. Inline, file-anchored threads -----------------------------------
  const { iterationId, byPath } = DRY_RUN === '1'
    ? { iterationId: null, byPath: new Map() }
    : await loadChangeTracking();

  let posted = 0, skipped = 0;
  for (const f of findings) {
    const path = normalizePath(f.file);
    const key = `${path}:${f.fingerprint}`;
    if (alreadyPosted.has(key)) { skipped++; continue; }

    const body = {
      comments: [{
        parentCommentId: 0,
        commentType: COMMENT_TYPE_TEXT,
        content: `${fp(key)}**${severityBadge(f.severity)}** — ${f.body}`,
      }],
      // Informational findings do not gate the PR; blockers stay Active.
      status: f.severity === 'blocker' ? STATUS.Active : STATUS.Closed,
      threadContext: threadContextFor(f),
    };

    const trackingId = byPath.get(path);
    if (trackingId != null && iterationId != null) {
      body.pullRequestThreadContext = {
        changeTrackingId: trackingId,
        iterationContext: { firstComparingIteration: 1, secondComparingIteration: iterationId },
      };
    }

    if (DRY_RUN === '1') console.log(JSON.stringify(body, null, 2));
    else await api('/threads', { method: 'POST', body });
    posted++;
  }

  // ---- 3. PR-level summary thread (sticky) --------------------------------
  const summaryBody =
    `${MARKER}${SUMMARY_MARKER}\n## Claude review\n\n${review.summary}\n\n` +
    (findings.length
      ? `| Severity | File | Line | Issue |\n|---|---|---|---|\n` +
        findings.map((f) =>
          `| ${severityBadge(f.severity)} | \`${f.file}\` | ${f.startLine} | ${f.body.split('\n')[0].slice(0, 120)} |`
        ).join('\n')
      : '_No issues found._') +
    `\n\n<sub>Build ${process.env.BUILD_BUILDNUMBER ?? 'local'} · ${posted} new comment(s), ${skipped} already open.</sub>`;

  if (DRY_RUN === '1') {
    console.log(summaryBody);
  } else if (summaryThread) {
    // Sticky: edit the existing summary in place rather than stacking a new thread.
    const first = summaryThread.comments[0];
    await api(`/threads/${summaryThread.id}/comments/${first.id}`,
      { method: 'PATCH', body: { content: summaryBody } });
  } else {
    await api('/threads', {
      method: 'POST',
      body: {
        comments: [{ parentCommentId: 0, commentType: COMMENT_TYPE_TEXT, content: summaryBody }],
        status: STATUS.Closed,
      },
    });
  }

  console.log(`Claude review posted: ${posted} new inline comment(s), ${skipped} skipped as duplicates.`);
}

main().catch((err) => { console.error(err.message); process.exit(1); });
```

### 6.4 `azure-pipelines-claude-review.yml`

```yaml
# TRIGGER: there is deliberately no `pr:` trigger. YAML `pr:` triggers are ignored for
# Azure Repos Git. This pipeline is invoked by a Branch Policy:
#   Repos > Branches > (…) on target branch > Branch policies > Build validation
#   Trigger: Automatic · Policy requirement: Optional · Expiration: Never
# Draft PRs do not trigger build validation, and publishing a draft does — that is the
# free equivalent of GitHub's `ready_for_review`.
#
# SHELL NOTE: inside `script:` bodies use backticks for command substitution, never
# $(...). Azure DevOps expands $(name) as its own macro BEFORE bash sees it.

trigger: none
pr: none          # explicit no-op for Azure Repos; kept so nobody "fixes" it later

pool:
  name: $(claudeReviewPool)          # self-hosted pool with egress to bedrock-runtime.<region>.amazonaws.com

variables:
  - group: claude-review-secrets     # AWS_BEARER_TOKEN_BEDROCK (locked). Authorize the pipeline
                                     # under Library > <group> > Pipeline permissions.
  - name: awsRegion
    value: us-east-1
  # FULL Bedrock inference-profile IDs. Never a bare alias — an unpinned model is
  # billed at the Opus rate on Bedrock. See §1.7.2a.
  - name: reviewModel
    value: 'us.anthropic.claude-sonnet-4-6'
  - name: reviewModelLarge
    value: 'us.anthropic.claude-haiku-4-5-20251001-v1:0'
  - name: maxDiffBytes
    value: '400000'                  # ~100k tokens; skip review above this
  - name: adoApiVersion
    value: '7.0'                     # 2022 -> 7.0 | 2022.1 -> 7.1 | 2020 -> 6.0 | 2019 -> 5.0
  - name: claudeVersion
    value: '2.1.226'                 # pin: --json-schema only reliable from 2.1.205

jobs:
  - job: review
    displayName: Claude PR review
    # Build-validation pipelines can also be queued manually; only review real PRs.
    # IsFork gate keeps the API key away from fork contributors.
    condition: >-
      and(eq(variables['Build.Reason'], 'PullRequest'),
          ne(variables['System.PullRequest.IsFork'], 'True'))
    timeoutInMinutes: 30

    steps:
      # fetchDepth: 0 is NON-NEGOTIABLE. Build validation checks out the server-computed
      # merge commit in detached HEAD. With shallow fetch (default since Sprint 209) only
      # that commit is fetched by SHA — neither HEAD^1 nor origin/<target> exists locally
      # and no diff is possible.
      - checkout: self
        fetchDepth: 0
        # persistCredentials is NOT needed: we call REST with System.AccessToken and
        # never push. Leaving it off keeps the token out of .git/config.

      - task: NodeTool@0
        displayName: Install Node 22
        inputs:
          versionSpec: '22.x'        # npm install of Claude Code requires Node 22+

      - script: |
          set -euo pipefail
          npm install -g "@anthropic-ai/claude-code@${CLAUDE_VERSION}"
          claude --version
        displayName: Install Claude Code
        env:
          CLAUDE_VERSION: $(claudeVersion)
          DISABLE_AUTOUPDATER: '1'   # keep CI runs reproducible
          HTTPS_PROXY: $(Agent.ProxyUrl)
          HTTP_PROXY: $(Agent.ProxyUrl)

      - script: |
          set -euo pipefail
          TARGET="${TARGET_BRANCH#refs/heads/}"
          git fetch --no-tags origin "+refs/heads/${TARGET}:refs/remotes/origin/${TARGET}"

          # Fail loudly and specifically if fetchDepth was misconfigured.
          if ! git rev-parse --verify -q "origin/${TARGET}" >/dev/null; then
            echo "##vso[task.logissue type=error]origin/${TARGET} missing — set fetchDepth: 0."
            exit 1
          fi

          # Three-dot diffs from the merge-base, matching what a reviewer sees in the PR UI.
          git diff --unified=3 "origin/${TARGET}...HEAD" > pr.diff
          git diff --name-only "origin/${TARGET}...HEAD" > pr.files

          SIZE=`wc -c < pr.diff`
          echo "Diff: ${SIZE} bytes across `wc -l < pr.files` files"
          echo "##vso[task.setvariable variable=diffBytes]${SIZE}"
        displayName: Compute PR diff
        env:
          TARGET_BRANCH: $(System.PullRequest.TargetBranch)

      - script: |
          set -euo pipefail
          if [ "${DIFF_BYTES}" -gt "${MAX_DIFF_BYTES}" ]; then
            echo "##vso[task.logissue type=warning]Diff too large (${DIFF_BYTES}B) — skipping."
            echo '{"summary":"Diff too large for automated review.","assessment":"comment","findings":[]}' > review.json
            exit 0
          fi

          # Build the prompt on disk and feed via stdin. `claude -p` reads stdin, which
          # keeps the diff off the command line (ARG_MAX) and away from macro expansion.
          {
            cat .azuredevops/claude/rubric.md
            printf '\n\n## Pull request\nPR ID: %s\nSource: %s\nTarget: %s\n' \
              "${PR_ID}" "${SOURCE_BRANCH}" "${TARGET_BRANCH}"
            printf '\n## Changed files\n'; cat pr.files
            printf '\n## Diff\n```diff\n'; cat pr.diff; printf '\n```\n'
          } > prompt.txt

          # --tools is the CAPABILITY boundary: Bash/Edit/Write do not exist for this run,
          #   so the model can only read the checkout.
          # --allowedTools is ONLY a permission allowlist and does NOT restrict anything
          #   (verified) — never use it as a security control.
          # --setting-sources "" stops the PR branch's own .claude/settings.json and hooks
          #   from executing. The PR branch is untrusted input. It also cuts cold-start
          #   cache cost dramatically (measured ~$1.95 -> ~$0.04 on a trivial prompt).
          claude -p \
            --output-format json \
            --json-schema "`cat .azuredevops/claude/review-schema.json`" \
            --model "${ANTHROPIC_MODEL}" \
            --max-turns 40 \
            --tools "Read,Grep,Glob" \
            --setting-sources "" \
            --strict-mcp-config \
            < prompt.txt > claude-out.json
          # NOTE: --max-budget-usd is omitted on Bedrock — its cost accounting is
          # unverified there. Spend is bounded by maxDiffBytes + AWS Budgets.

          # `subtype` is NOT a reliable success signal — an auth failure still reports
          # "success". Gate on is_error / terminal_reason.
          if [ "`jq -r '.is_error' claude-out.json`" != "false" ]; then
            echo "##vso[task.logissue type=error]Claude failed: `jq -r '.terminal_reason // \"unknown\"' claude-out.json`"
            jq -r '.result // .api_error_status' claude-out.json
            exit 1
          fi

          jq '.structured_output' claude-out.json > review.json
          echo "Findings: `jq '.findings | length' review.json`"

          # Bedrock prompt caching is region-dependent and fails SILENTLY (§1.7.2b).
          # Zero cache tokens means you are re-paying full input cost on every run.
          CACHED=`jq -r '(.usage.cache_creation_input_tokens // 0) + (.usage.cache_read_input_tokens // 0)' claude-out.json`
          if [ "$CACHED" = "0" ]; then
            echo "##vso[task.logissue type=warning]Bedrock prompt caching appears inactive in ${AWS_REGION} — cost will be materially higher."
          fi
        displayName: Run Claude review
        env:
          # --- Amazon Bedrock (§1.7) ---
          CLAUDE_CODE_USE_BEDROCK: '1'
          AWS_REGION: $(awsRegion)
          # Secret variables are NOT auto-mapped into the environment — map explicitly.
          AWS_BEARER_TOKEN_BEDROCK: $(AWS_BEARER_TOKEN_BEDROCK)
          # Pin every alias so nothing can silently resolve to Opus and bill at Opus rates.
          ANTHROPIC_MODEL: $(reviewModel)
          ANTHROPIC_DEFAULT_SONNET_MODEL: $(reviewModel)
          ANTHROPIC_DEFAULT_HAIKU_MODEL: $(reviewModelLarge)
          # Scripts do NOT inherit the agent's proxy config; it is only exposed as vars.
          HTTPS_PROXY: $(Agent.ProxyUrl)
          HTTP_PROXY: $(Agent.ProxyUrl)
          NO_PROXY: $(adoServerHost)
          NODE_EXTRA_CA_CERTS: $(corporateCaBundle)   # blank if not needed
          DIFF_BYTES: $(diffBytes)
          MAX_DIFF_BYTES: $(maxDiffBytes)
          PR_ID: $(System.PullRequest.PullRequestId)
          SOURCE_BRANCH: $(System.PullRequest.SourceBranch)
          TARGET_BRANCH: $(System.PullRequest.TargetBranch)

      - script: node .azuredevops/claude/post-review.mjs
        displayName: Post review to PR
        condition: succeeded()
        env:
          # On Server this resolves to your collection, e.g.
          # https://tfs.corp.local/tfs/DefaultCollection/ — never dev.azure.com.
          ADO_COLLECTION_URI: $(System.TeamFoundationCollectionUri)
          ADO_PROJECT: $(System.TeamProject)
          ADO_REPO_ID: $(Build.Repository.ID)
          ADO_PR_ID: $(System.PullRequest.PullRequestId)
          ADO_TOKEN: $(System.AccessToken)   # needs "Contribute to pull requests"
          ADO_API_VERSION: $(adoApiVersion)
          REVIEW_JSON: review.json
          NO_PROXY: $(adoServerHost)         # keep agent<->server traffic off the proxy
```

### 6.5 Variant B — using your organisation's existing Azure DevOps MCP server

Your org already runs a non-official ADO MCP server that works against Server. That is a real
advantage: it is already approved, already credentialed, and already understood by your team. This
section is how to use it well.

#### 6.5.1 First: enumerate what it can actually do

Do not assume. Different community/in-house servers differ sharply — of the two public on-prem-capable
ones, `Tiberriver256` hardcodes `offset` to 1, supports **only the right side of the diff**, and
**cannot do multi-line spans**; `burcusipahioglu` ships a 10-writes-per-minute rate limit and an
always-ask directive that will stall an unattended run.

```bash
# What is registered, and is it healthy?
claude mcp list
claude mcp get <server-name>

# Enumerate the tool names exactly as the allowlist will need them.
claude -p "List every tool available to you whose name begins with mcp__. Output one per line, names only, nothing else." \
  --mcp-config .azuredevops/claude/ado-mcp.json \
  --strict-mcp-config --setting-sources "" --output-format json | jq -r '.result'
```

Then check it against this table. **Items 1–3 are mandatory** for the write path; 4–6 determine how
much the script still has to do.

| # | Capability | Why it matters | If missing |
|---|---|---|---|
| 1 | Create a thread **with** file path + line | The file-anchored comment | Blocker — use Variant A |
| 2 | Create a thread **without** a file path | The PR-level summary | Blocker — use Variant A |
| 3 | **List** existing threads | Idempotency across re-runs | Blocker — build validation re-runs on every push |
| 4 | Sets `pullRequestThreadContext.changeTrackingId` | Prevents the "file no longer exists" warning | Acceptable — cosmetic |
| 5 | **Multi-line** spans (`endLine` ≠ `startLine`) | Nice for block-level findings | Degrade: collapse to a single line |
| 6 | **Update / PATCH** an existing comment | True sticky summary | Degrade: reply into the thread instead |

#### 6.5.2 Wiring — the three things that silently break

```jsonc
// .azuredevops/claude/ado-mcp.json  — the server KEY here becomes the tool prefix
{
  "mcpServers": {
    "ado": {                                   // -> tools are mcp__ado__<toolname>
      "command": "npx",
      "args": ["-y", "<your-org-mcp-package>@<pinned-version>"],
      "env": {
        "ADO_ORG_URL": "$(System.TeamFoundationCollectionUri)",
        "ADO_PAT": "$(ADO_PAT)"                // from the locked variable group
      }
    }
  }
}
```

```bash
claude -p \
  --output-format json \
  --json-schema "`cat .azuredevops/claude/review-schema.json`" \
  --mcp-config .azuredevops/claude/ado-mcp.json \
  --strict-mcp-config \
  --tools "Read,Grep,Glob" \
  --allowedTools "mcp__ado__*" \
  --setting-sources "" \
  < prompt.txt > claude-out.json
```

1. **MCP tools require explicit allowlisting** **[verified by A/B test]**. Unlike built-in tools,
   which run freely in headless mode, an MCP call with no allow rule is **denied**.
2. **The allow glob must be anchored to a literal server prefix.** `mcp__ado__*` works.
   **`mcp__*` and `*` are silently skipped and auto-approve nothing** — the run completes,
   `is_error` is `false`, and **zero comments are posted**. This is the highest-risk failure mode in
   this variant because it looks like success.
3. **`--tools` does not cover MCP tools.** It replaces the *built-in* set only; MCP tools come from
   `--mcp-config`. You need both flags: `--tools` for capability, `--allowedTools` for MCP permission.

Also: `MCP_TIMEOUT` defaults to **30000 ms** for server startup, and config-validation failures
surface in `system/init`.`mcp_server_errors` while **the run still exits cleanly** — so check that
field, not just the exit code.

#### 6.5.3 The verification step this variant requires

Because the model decides whether to post, you must assert that it did. Add after the Claude step:

```bash
# permission_denials catches a bad allowlist; the thread count catches a silent no-op.
DENIALS=`jq '.permission_denials | length' claude-out.json`
if [ "$DENIALS" -gt 0 ]; then
  echo "##vso[task.logissue type=error]Blocked MCP calls — check --allowedTools anchoring:"
  jq -c '.permission_denials[]' claude-out.json
  exit 1
fi
```

> `permission_denials` is present and correctly populated on v2.1.226 but is **not documented**
> **[unverified as a stable contract]**. Pair it with a positive assertion — re-list the PR threads
> and confirm the expected count — rather than relying on it alone.

**Recommended hybrid, if your server passes items 1–3:** give the model the MCP server for *reads*
(`--allowedTools "mcp__ado__get_*,mcp__ado__list_*"`) so it can pull PR metadata and prior review
threads, and still emit findings as JSON for `post-review.mjs` to write. You get the org-approved
integration where it adds value and determinism where it matters.

---

## 7. One-time setup runbook

Ordered. Steps 1–3 need **Project Administrator**; step 2 needs the pipeline to have run once if job
authorization scope is project-level.

0. **AWS side (Bedrock) — do this first, §1.7.** ① Bedrock console → Model catalog → select an
   Anthropic model → **submit the use case form** (once per account; access is immediate).
   ② Create the IAM policy from §1.7.5 and attach it to the identity. ③ Mint a **Bedrock API key**
   (`AWS_BEARER_TOKEN_BEDROCK`). ④ Confirm your chosen model is available in the region:
   `aws bedrock list-inference-profiles --region us-east-1`.

1. **Variable group.** Pipelines → Library → **+ Variable group** → name `claude-review-secrets` →
   add `AWS_BEARER_TOKEN_BEDROCK`, click the **lock icon** → Save. Then **Pipeline permissions** →
   authorize this pipeline. *(Azure DevOps CLI variable-group commands are not supported on Server.)*

2. **Build service permission.** First determine the identity: Organization settings → Pipelines →
   Settings, and Project settings → Pipelines → Settings → *"Limit job authorization scope to current
   project"*. Then Project settings → **Repositories** → *your repo* → **Security** → select
   **`Project Collection Build Service ({Collection})`** *(or `{Project} Build Service ({Collection})`
   if project-scoped)* → set **Contribute to pull requests = Allow** and **Read = Allow** → Save.

   > Without this, thread creation returns **403**. This is the single most common setup failure.

3. **Branch policy.** Repos → Branches → hover target branch → **…** → **Branch policies** → **+**
   next to **Build validation**:
   - Build pipeline: the review pipeline
   - Trigger: **Automatic**
   - Policy requirement: **Optional** ← so a slow or failed review never blocks a merge
   - Build expiration: **Never** ← so target-branch churn does not re-run the reviewer

4. **Agent prerequisites.** Node 22+; egress to `bedrock-runtime.<region>.amazonaws.com` (§1.7);
   `HTTPS_PROXY`/`NO_PROXY` set machine-level on the agent host and the service restarted; the ADO
   Server host listed in the agent's `.proxybypass`.

5. **Commit** `.azuredevops/claude/{rubric.md,review-schema.json,post-review.mjs}` and the pipeline
   YAML; create the pipeline definition pointing at the YAML.

---

## 8. Implementation plan

### Phase 0 — De-risking spike (½ day) · **do this first**

Nothing below is worth building until these five answers exist. All are ~30 minutes total.

| # | Check | Command / action | Kills the plan if… |
|---|---|---|---|
| 0.1 | **Agent egress to Bedrock + model access** | On the agent: `CLAUDE_CODE_USE_BEDROCK=1 AWS_REGION=us-east-1 AWS_BEARER_TOKEN_BEDROCK=$KEY claude -p "say OK" --model us.anthropic.claude-sonnet-4-6 --tools "" --setting-sources "" --output-format json` | No egress to `bedrock-runtime.<region>.amazonaws.com` → PrivateLink (§1.7.6). A 400 naming the model → use case form not submitted, or wrong region prefix |
| **0.1b** | **Prompt caching actually active** | From 0.1's output: `jq '.usage.cache_creation_input_tokens'` | Zero → caching unavailable in that region (§1.7.2b). Not fatal, but re-price before committing |
| **0.1c** | **Streaming survives the proxy** | 0.1 succeeding *is* the test — a TLS-inspecting proxy fails with `Bedrock streaming response has content-type …` | Fix the gateway to pass `application/vnd.amazon.eventstream` through (§1.7.2c) |
| 0.2 | Server's real api-version | `curl -u :$PAT ".../_apis/git/repositories?api-version=9.9"` — read the max from the error | — (tells you what to pin) |
| 0.3 | **Post one PR-level and one file-anchored comment by hand** on a scratch PR with `post-review.mjs` `DRY_RUN=0` | see below | Auth/permission wrong → fix step 7.2 |
| 0.4 | npm reachability on the agent | `npm install -g @anthropic-ai/claude-code@2.1.226` | Blocked → use the native installer, or mirror the per-platform optional dep |
| 0.5 | Evaluate `listellm.claude-code-base-task@3` | install in a test project | If it works on-prem, **it may replace Phases 1–3 entirely** |
| **0.6** | **Run the §6.5.1 capability checklist against your org's MCP server** | `claude mcp list` + the tool-enumeration probe | Items 1–3 missing → write path stays Variant A. Either way this decides Variant A vs B **before** you build |

```bash
# 0.3 — the single most valuable 10 minutes in this plan
cat > review.json <<'JSON'
{"summary":"Spike.","assessment":"comment","findings":[
 {"file":"README.md","startLine":1,"endLine":2,"severity":"nit",
  "fingerprint":"spike-probe","body":"Spike: multi-line anchored comment."}]}
JSON
ADO_COLLECTION_URI=https://tfs.corp.local/tfs/DefaultCollection/ \
ADO_PROJECT=MyProject ADO_REPO_ID=MyRepo ADO_PR_ID=123 \
ADO_TOKEN=$PAT ADO_API_VERSION=7.0 node post-review.mjs
```

**Exit criterion:** both comment types visible in the PR UI, anchored to the right lines.

### Phase 1 — Core loop (1 day)
Commit the four files. Create the pipeline. Queue it **manually** against a test PR first
(temporarily relax the `Build.Reason` condition), so you iterate without touching branch policy.
**Done when:** a manual run produces a sticky summary + ≥1 correctly anchored inline comment.

### Phase 2 — Wire the trigger (½ day)
Restore the condition, add the branch policy (7.3). Push twice to the PR branch.
**Done when:** the second push adds **no duplicate** comments and **updates** the summary in place.

### Phase 3 — Harden (½–1 day)
Tune the rubric on 3–5 real PRs. Confirm cost per review. Add a 401 alert. Verify the three
**[unverified]** trigger behaviours from §2.1 empirically (reopen, target-branch change, auto-cancel).

### Phase 3.5 — Repackage for rollout (1–2 days) · only when going past 1–2 repos

Migrate from Option 1 to **Option 2 + 3** (§1.5.3). This is what makes it maintainable *and* closes
the "PR branch can edit the reviewer" hole.

1. Create a tooling repo (e.g. `devops-tooling`) holding `claude-review-template.yml` + the three
   `.azuredevops/claude/` files. Restrict write access.
2. Consumer repos shrink to:
   ```yaml
   resources:
     repositories:
       - repository: tooling
         type: git
         name: DevOps/devops-tooling
         ref: refs/tags/claude-review-v1     # pin — do not float on a branch
   extends:
     template: claude-review-template.yml@tooling
   ```
3. Build a container image (Node 22 + pinned Claude Code + scripts) and switch the job to
   `container:`. Removes the npm install from every run — the fix for air-gapped agents *and* the
   biggest single speed-up.
4. Investigate a **Required template check** to stop a PR editing the top-level YAML.
   **[unverified on Server]**

**Done when:** a consumer repo onboards with ~6 lines of YAML and a branch policy, and a PR that
edits `.azuredevops/` has no effect on the review that runs.

### Phase 4 — Deferred: the `@claude` mention trigger
Azure DevOps has a *"Pull request commented on"* service hook, but it **cannot queue a pipeline
directly**. It needs `Service Hook → relay endpoint → incoming-webhook pipeline resource`, which is a
deployed component with its own auth and availability story. **Skip for v1** — automatic-on-PR is
~90% of the value for ~30% of the work.

---

## 9. Questions that must be answered before coding

These are genuine blockers or fork-in-the-road decisions. **Q1 and Q2 gate everything.**

| # | Question | Why it matters |
|---|---|---|
| ~~Q1~~ | ~~Can the build agents reach `api.anthropic.com`?~~ | ✅ **ANSWERED — Phase 1 uses Amazon Bedrock (§1.7).** The remaining questions are: which AWS **region**, and can agents reach `bedrock-runtime.<region>.amazonaws.com` (probe 0.1) |
| **Q2** | **Which Azure DevOps Server build?** | Sets `api-version` (5.0/6.0/7.0/7.1). Probe 0.2 answers it in 30 seconds |
| ~~Q3~~ | ~~`ANTHROPIC_API_KEY` or `CLAUDE_CODE_OAUTH_TOKEN`?~~ | ✅ **Moot on Bedrock** — neither is used. Remaining choice is the AWS credential form (§1.7.4): **Bedrock API key** recommended for the POC |
| **Q3b** | **Which AWS region, and is prompt caching supported there?** | Sets the inference-profile prefix (`us.`/`eu.`/`apac.`) and materially changes cost. Probes 0.1/0.1b answer it |
| Q4 | Post as build service or a named bot user? | `System.AccessToken` is minted per run (no rotation) but comments appear as "Project Collection Build Service". A PAT gives a friendlier author name and **caps at one year** |
| Q5 | Internally-issued TLS cert on the Server? | Needs `NODE_EXTRA_CA_CERTS`. **Do not** reach for `NODE_TLS_REJECT_UNAUTHORIZED=0` — it disables verification process-wide |
| Q6 | Proxy details (`--proxyurl`, bypass list)? | Scripts do not inherit agent proxy config; must be set explicitly |
| **Q7** | **Which MCP server does your org use, and what is its exact package name, server key, env-var contract, and tool list?** | Determines Variant A vs B. The allowlist string is literally `mcp__<server-key>__<tool>` and an unanchored glob silently posts nothing — so the key must be fixed and hardcoded. Answer with the §6.5.1 probe |
| Q8 | Supply-chain review on npm packages? | Determines whether options D/E/F are even permissible |
| Q9 | Is `npx`/npm reachable at pipeline time? | If not, every MCP option dies and only the REST path survives |

---

## 10. Risks

| Risk | Likelihood | Mitigation |
|---|---|---|
| **💸 Unpinned model bills at Opus rates on Bedrock** | **High if forgotten** | Set `ANTHROPIC_MODEL` to a **full** inference-profile ID; never pass a bare alias. Pin `ANTHROPIC_DEFAULT_*_MODEL` too (§1.7.2a) |
| **💸 Bedrock prompt caching silently inactive in-region** | Medium | Probe 0.1b; the pipeline warns when cache tokens are 0. Re-price or change region |
| **Corporate proxy breaks Bedrock streaming** | Medium on-prem | Gateway must pass `application/vnd.amazon.eventstream` through unmodified (§1.7.2c) |
| No egress to `bedrock-runtime.*` | Medium | Probe 0.1 first; PrivateLink via Direct Connect/VPN (§1.7.6) |
| Bedrock model access not enabled on the AWS account | **High** — it is off by default | Submit the use case form once per account (§1.7.2d); runbook step 0 |
| Build service 403 on thread create | **High** | Runbook 7.2; probe 0.3 catches it in minutes |
| `fetchDepth` default breaks the diff | **High** if forgotten | Explicit `fetchDepth: 0` + the `rev-parse` assertion that fails with a named cause |
| Cost creep from re-runs on every push | Medium | Fingerprint dedupe; `maxDiffBytes`; `--max-budget-usd`; expiration **Never** |
| Duplicate comments across pushes | **High** without markers | Fingerprint + sticky summary — implemented, must be tested in Phase 2 |
| Prompt injection from PR content | Medium | `--tools "Read,Grep,Glob"` (no Bash/Write), `--setting-sources ""`, `IsFork` gate. **Note CLAUDE.md is attacker-controlled on a PR branch and is instructions** — accepted risk on an internal Server; revisit if external contributors exist |
| **PR branch can edit the reviewer itself** (Option 1 packaging) | **High past the POC** | PR builds run the YAML *and scripts* from the source branch. Move to a central template repo in Phase 3.5; add a Required template check. Accepted only for a POC with trusted contributors |
| Anthropic credential expiry silently stops reviews | **Realised on GitHub today** | `claude auth status` exits 0 and needs no API call — assert it in the pipeline; alert on `is_error` |
| Wrong `changeTrackingId` anchors comments to wrong files | Low | Only set on exact path match; degrade gracefully otherwise |
| **Variant B only: unanchored MCP allow glob → run "succeeds" and posts nothing** | **High** | Anchor to `mcp__<key>__*`; assert `permission_denials` is empty **and** re-list threads to confirm a positive count (§6.5.3) |

---

## 11. Appendix — API reference

**Endpoint (both comment types):**
```
POST {collectionUri}{project}/_apis/git/repositories/{repoId}/pullRequests/{prId}/threads?api-version=7.0
GET  … /threads                                   list (no server-side filter — dedupe client-side)
PATCH… /threads/{threadId}/comments/{commentId}   edit in place (sticky summary)
GET  … /iterations                                → max id = latest iteration
GET  … /iterations/{id}/changes                   → changeEntries[].changeTrackingId
```

**PR-level** — omit `threadContext`:
```json
{ "comments": [{ "parentCommentId": 0, "content": "Overall this looks good…", "commentType": 1 }],
  "status": 4 }
```

**File-anchored** — include it:
```json
{ "comments": [{ "parentCommentId": 0, "content": "Null check missing here.", "commentType": 1 }],
  "status": 1,
  "threadContext": {
    "filePath": "/src/main/kotlin/com/github/gbrowser/Foo.kt",
    "rightFileStart": { "line": 42, "offset": 1 },
    "rightFileEnd":   { "line": 44, "offset": 1 }
  },
  "pullRequestThreadContext": {
    "changeTrackingId": 7,
    "iterationContext": { "firstComparingIteration": 1, "secondComparingIteration": 3 }
  } }
```

`leftFileStart`/`leftFileEnd` target the base side — use for comments on deleted lines.

**Enums** — `CommentType`: `Unknown=0, Text=1, CodeChange=2, System=3`.
`CommentThreadStatus`: `Unknown=0, Active=1, Fixed=2, WontFix=3, Closed=4, ByDesign=5, Pending=6`.

**Predefined variables** (auto-exported as `SYSTEM_PULLREQUEST_PULLREQUESTID` etc.):
`System.PullRequest.PullRequestId` *(use this, not `PullRequestNumber` — that is GitHub-only in
effect)*, `System.PullRequest.SourceBranch` / `TargetBranch` *(full `refs/heads/…` refs on Azure
Repos — strip the prefix)*, `System.PullRequest.IsFork`, `Build.Repository.ID`, `System.TeamProject`,
`System.TeamFoundationCollectionUri`, `System.AccessToken`.
`System.PullRequest.SourceCommitId` is missing from the Server docs table **[unverified]** — prefer
`git rev-parse HEAD^2`.

**Doc view monikers** — always pin the query string, or Microsoft Learn silently shows you cloud-only
behaviour: `?view=azure-devops` = Services · `?view=azure-devops-2022` = Server 2022 ·
`?view=azure-devops-server` = current on-prem release.

---

## 12. Corrections to the earlier findings document

`claude-pr-review-azure-devops-port.md` contains three errors, all corrected above:

1. **§5.1 "GitHub App identity → PAT or `$(System.AccessToken)` … Easy"** understated the blocker: it
   did not establish that **the official ADO MCP server cannot reach on-prem at all**. That is the
   single most important fact in this port.
2. **§7 risk 2 — "7.1 on Server 2022, 7.0 on Server 2020"** is wrong on both counts.
   Correct: **2022 → 7.0**, **2020 → 6.0**, 2022.1 → 7.1, 2019 → 5.0.
3. **§6.1 — "`--allowedTools Edit,Read,Write` may be overriding rather than extending"** is **a false
   alarm**, now disproven by direct experiment. `--allowedTools` grants permission and never removes
   tools; `--tools` is the flag that replaces the tool set. The GitHub review failure was **purely**
   the expired `CLAUDE_CODE_OAUTH_TOKEN`.

Also worth carrying forward: §5.3's claim that `offset` starts at 0. Microsoft documents this
inconsistently across versions; **use `offset: 1`**, which is what every real implementation does.

---

## 13. References

- Azure DevOps REST — [PR Threads Create](https://learn.microsoft.com/en-us/rest/api/azure/devops/git/pull-request-threads/create?view=azure-devops-rest-7.1) · [PR Iteration Changes](https://learn.microsoft.com/en-us/rest/api/azure/devops/git/pull-request-iteration-changes/get?view=azure-devops-rest-7.1)
- [Branch policies (Build validation)](https://learn.microsoft.com/en-us/azure/devops/repos/git/branch-policies?view=azure-devops-2022)
- [`pr` trigger — notes the Azure Repos limitation](https://learn.microsoft.com/en-us/azure/devops/pipelines/yaml-schema/pr?view=azure-pipelines-2022)
- [Predefined variables](https://learn.microsoft.com/en-us/azure/devops/pipelines/build/variables?view=azure-devops-2022)
- Claude Code — [Headless](https://code.claude.com/docs/en/headless) · [CLI reference](https://code.claude.com/docs/en/cli-reference) · [GitLab CI/CD (closest official non-GitHub template)](https://code.claude.com/docs/en/gitlab-ci-cd)
- [`microsoft/azure-devops-mcp`](https://github.com/microsoft/azure-devops-mcp) — cloud-only; see `dist/index.js:55`
- [`Tiberriver256/mcp-server-azure-devops`](https://github.com/Tiberriver256/mcp-server-azure-devops) — on-prem MCP fallback
- `listellm.claude-code-base-task` (Azure DevOps Marketplace) · [`rios0rios0/code-guru`](https://github.com/rios0rios0/code-guru)
