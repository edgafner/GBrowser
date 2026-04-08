---
description: Create a single branch combining all open Dependabot PRs to reduce CI costs
allowed-tools: Bash, Read
---

# Combine Dependabot PRs

Create a unified branch with all open Dependabot dependency updates, so they merge as one PR instead of triggering CI separately for each.

## Steps

1. **Fetch latest main and list open Dependabot PRs**

```bash
git fetch origin main
gh pr list --author "app/dependabot" --state open --json number,title,headRefName --jq '.[] | "\(.number)\t\(.headRefName)\t\(.title)"'
```

If no open Dependabot PRs, report "No open Dependabot PRs found" and stop.

2. **Create a new branch from origin/main**

```bash
git checkout -b feature/combine-dependabot-updates origin/main
```

3. **Merge each Dependabot branch into the combined branch**

For each Dependabot PR, merge its branch:

```bash
git fetch origin <branch-name>
git merge origin/<branch-name> --no-edit
```

If a merge conflict occurs:
- Try to resolve it (usually version conflicts in `gradle/libs.versions.toml` — take the newer version)
- If unresolvable, skip that PR and report it

4. **Verify the build compiles**

```bash
./gradlew compileKotlin --no-scan
```

If compilation fails, identify which dependency caused it and consider excluding that PR.

5. **Push and create PR**

```bash
git push -u origin feature/combine-dependabot-updates
```

Create PR with title: `chore(deps): combine Dependabot updates`

PR body should list all included PRs:
```
## Combined Dependabot Updates

Merges the following dependency PRs into a single update:
- #NNN: title
- #NNN: title
...

This reduces CI costs by running checks once instead of per-PR.
```

6. **Report which PRs were included and which were skipped**

## Rules

- Always compile after merging to catch incompatibilities early
- If a dependency update breaks the build, exclude it and note in the PR description
- Use `--no-scan` with all Gradle commands
- Do NOT close the individual Dependabot PRs until the combined PR is merged
