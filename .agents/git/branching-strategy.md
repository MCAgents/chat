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

## Discovery Protocol

While working, if you notice an instruction worth adding — a new rule, or new
content for an existing instruction file — do NOT create or edit it yourself.
Collect the findings, and when the task is done present them to the user:

* one finding per message block, each in its own code block;
* include the proposed file path, `name`, `description`, and the full proposed
  body;
* explain in one line why it is worth adding.

Then let the user select which findings to apply. Create only the selected ones.
Never batch-apply, never apply silently.

**Scope of this gate:** it covers instruction files under `.agents/{folder}/`.
Documentation pages under `wiki/` and `.agents/wiki/` may be written when the facts
are real and verified. Memory under `.agents/memory/` is written freely and
automatically — see [`../rules/memory-policy.md`](../rules/memory-policy.md).
