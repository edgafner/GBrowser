#!/usr/bin/env bash
# Fails when this job's runner service is still running a node binary that no longer exists on disk.
# Copied from the sibling JirAI plugin's CI (2026-10-23 original); keep the two in sync.
#
# 🔴 Why: the GitHub runner updates itself between jobs and deletes the old `externals.<version>` directory, but the
# long-lived service parent (`runsvc.sh` -> `externals/node24/bin/node ./bin/RunnerService.js`) keeps running the deleted
# binary until the service is restarted. macOS privacy (TCC) cannot attribute the IDE's synthetic input to that
# responsible process any more (`tccd`: `proc_pidpath_audittoken() failed`), so every robot click is dropped while moves
# and screenshots still work. On azd-mac that turned every clicking UI job red from 2026-10-23 07:01Z until the service was
# restarted. The runner updates itself again, so this recurs on any macOS
# runner; the other preflight checks cannot see it.
#
# How: find the RunnerService.js process of THIS runner (its working directory is the runner root, which is RUNNER_TEMP
# minus /_work/_temp) and test that the executable it maps still exists. `lsof ... -d txt` still prints the path of a
# deleted binary, which is why `test -e` on it works. A runner with no such process (started with ./run.sh instead of the
# service) is not checked: a notice, exit 0.
#
# A check that cannot run says so with a ::warning:: naming what it saw, never a quiet pass.
#
# Usage: macos-runner-node-check.sh [runner-root]   (default: derived from RUNNER_TEMP)
set -euo pipefail
root="${1:-${RUNNER_TEMP:-}}"
case "$root" in */_work/_temp) root="${root%/_work/_temp}" ;; esac
if [ -z "$root" ] || [ ! -d "$root" ]; then
  echo "::warning::Cannot work out the runner root (RUNNER_TEMP='${RUNNER_TEMP:-}'); skipping the runner-binary check."
  exit 0
fi
root="$(cd "$root" && pwd -P)"
# 🔴 Absolute paths first (2026-10-23, found by the check's own first CI run): a runner service's job PATH can lack /usr/sbin, where lsof
# lives, and `2>/dev/null` hid the "command not found", so on azd-mac the check printed "not a service" and verified nothing.
lsof_bin=/usr/sbin/lsof; [ -x "$lsof_bin" ] || lsof_bin="$(command -v lsof || true)"
pgrep_bin=/usr/bin/pgrep; [ -x "$pgrep_bin" ] || pgrep_bin="$(command -v pgrep || true)"
if [ -z "$lsof_bin" ] || [ -z "$pgrep_bin" ]; then
  echo "::warning::lsof or pgrep not found (lsof='$lsof_bin', pgrep='$pgrep_bin', PATH='$PATH'); the runner-binary check did not run."
  exit 0
fi
checked=0
seen=""
# 🔴 `-a` is the fix (2026-10-23, `man pgrep`): macOS pgrep excludes its own ANCESTORS by default, and
# in a job RunnerService.js is one (service -> listener -> worker -> step -> this script), so without -a it can never match, and
# the first version printed "not a service" on azd-mac.
# The pattern is the process's real shape, `<...>/node <path>/RunnerService.js` ending the command line, because -a also matches any
# ANCESTOR shell whose command text merely mentions the file.
for pid in $($pgrep_bin -a -f '/node ([^ ]+/)?RunnerService[.]js$' || true); do
  # `|| true`: under pipefail, `head -1` closing the pipe early would otherwise make a good read look like a failure.
  cwd="$("$lsof_bin" -p "$pid" -a -d cwd -Fn 2>/dev/null | sed -n 's/^n//p' | head -1)" || true
  seen="$seen $pid:${cwd:-unreadable}"
  [ "$cwd" = "$root" ] || continue
  checked=1
  exe="$("$lsof_bin" -p "$pid" -a -d txt -Fn 2>/dev/null | sed -n 's/^n//p' | head -1)" || true
  if [ -n "$exe" ] && [ ! -e "$exe" ]; then
    echo "::error::The runner service on ${RUNNER_NAME:-this Mac} (pid $pid) is running a deleted binary, $exe. The runner updated itself under it, so macOS can no longer attribute robot clicks to it and every click would be lost. Restart the service, then rerun: cd $(printf '%q' "$root") && ./svc.sh stop && ./svc.sh start"
    exit 1
  fi
  echo "Runner service (pid $pid) runs $exe, which exists"
done
if [ "$checked" -eq 0 ]; then
  if [ -n "$seen" ]; then
    # Another runner's service, or this one with an unreadable working directory: say so loudly, it is the check not running.
    echo "::warning::RunnerService.js processes exist ($seen) but none has working directory $root; the runner-binary check did not run."
  else
    if [ "${ACTIONS_RUNNER_SVC:-}" = "1" ]; then
      # The service plist sets this (azd-mac: EnvironmentVariables ACTIONS_RUNNER_SVC=1), so a service must exist: not a pass.
      echo "::warning::ACTIONS_RUNNER_SVC=1 but no RunnerService.js process was found; the runner-binary check did not run."
    else
      echo "::notice::No RunnerService.js process; the runner at $root is not a service, so its binary is not checked."
    fi
  fi
fi
