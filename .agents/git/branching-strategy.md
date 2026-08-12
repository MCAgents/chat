---
name: branching-strategy
description: How branches are named and created — one task per branch, {type}/{primary-noun}, branched off master, never a tool-preset prefix.
---

# Branching Strategy

## Rules

* **Branch off the default branch (`master`) for every task.** Never commit
  directly to it.
* **One task per branch, one pull request per branch.** Never put two tasks on one
  branch, and never reuse a branch across tasks.
* Keep branches short-lived and rebased on the default branch.

## Naming

```
{type}/{primary-noun}
```

The primary noun is the thing the task is about — short, lowercase, kebab-case.

Examples:

* `feat/login`
* `fix/schema-drift`
* `docs/agents-setup`
* `refactor/packet-codec`

## Allowed types

`feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`,
`chore`, `revert`

## Forbidden

* **No tool-preset prefixes.** `claude/`, `codex/`, `cursor/`, and anything like
  them are not branch types. A tool's default branch name is not this project's
  convention.
* **No session, run, or conversation identifiers in a branch name** — a generated
  session suffix is not a primary noun. See
  [`../rules/no-session-links.md`](../rules/no-session-links.md).
* If a branch already violates the convention, recreate it correctly and delete
  the wrong one — or present both options to the user and let them choose. Do not
  silently keep working on a misnamed branch.

## Stacked branches for multi-task work

When a request splits into several tasks, the branches stack in dependency order:
task 1 branches from `master`, task `k` branches from task `k-1`'s branch. That is
what keeps the merges conflict-free. See
[`../planning/task-workflow.md`](../planning/task-workflow.md) for the full
intake, ordering, and merge procedure.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`../rules/discovery-protocol.md`](../rules/discovery-protocol.md).
