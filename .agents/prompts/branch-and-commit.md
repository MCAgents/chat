---
name: branch-and-commit-prompt
description: Standing prompt for every task — the full branch, commit, push, and pull request loop, always active without the user restating it.
---

# Branch & Commit — Standing Prompt

**This applies to every task in this repository. Assume it is always active; the
user does not need to repeat it.**

## The loop

1. **Confirm Goal / Objective / Detail.** Ask for all three in one message unless
   the request already contains them, in which case restate your understanding and
   continue.
2. **Check [`../index/memory-index.md`](../index/memory-index.md) for prior state on this
   work** and load the rows whose scope matches, so you continue rather than restart.
3. **Split the request into ordered tasks and get them confirmed.** Number them
   `1…n`, each with a title, a one-line scope, its branch name, and the files it
   touches. Append a final task for the release. Wait for confirmation before writing
   anything, then write the confirmed list to `.agents/memory/tasks/{slug}.md`.
4. **Create one branch per task, stacked in order.** Task 1 branches from
   `master`; task `k` branches from task `k-1`'s branch.
5. **Work tasks 1…n strictly in order.** Finish and commit task `k` before
   starting `k+1`. Never work two in parallel.
6. **Update the owning index** for anything added, removed, moved, or renamed.
7. **Update `.agents/memory/` with progress** — the task file, and a decision file for
   anything a future session would otherwise re-litigate. No approval needed.
8. **Commit each logical change** with `type(scope): description`. Review the diff
   first. **Index and memory updates ship in the same commit as the change they
   describe** — never as a follow-up commit.
9. **Push every branch** with `git push -u origin {branch}`.
10. **Open one pull request per branch**, using the pull request template.
11. **Ask the user before merging.** Wait for an explicit yes.
12. **Merge in order 1…n**, waiting for each merge to finish before the next, then close
    out the memory task file.

## Branch naming

```
{type}/{primary-noun}
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`,
`chore`, `revert`.

Examples: `feat/login`, `fix/schema-drift`, `docs/agents-setup`.

**Never commit to the default branch, and never use a tool-preset branch prefix**
(`claude/`, `codex/`, `cursor/`, …). If a branch violates the convention, recreate
it correctly and delete the wrong one, or present the options to the user.

## Commit messages

```
type(optional scope): description
```

Same type list. Scope is a module or subsystem — `docs(wiki):`, `feat(auth):`,
`chore(deps):`. Subject in imperative mood, plain text, no trailing period, no
links, no issue IDs. Optional body: short bullets on what and why. Commit each
logical change; never batch a session into one commit.

## Pull request titles

**Pull request titles are human-readable — never `feat:` / `fix:` / `chore:`.**
Write a plain capitalized phrase that says what the change is, e.g.
`Add new API for identity lookup`. Full rules and the required
Overview / Added / Modified / Deleted / Summary body:
[`../git/pull-request-template.md`](../git/pull-request-template.md).

## Authorities

* Intake, ordering, and merging: [`../planning/task-workflow.md`](../planning/task-workflow.md)
* What may be written to memory, and how: [`../rules/memory-policy.md`](../rules/memory-policy.md)
* Branch naming: [`../git/branching-strategy.md`](../git/branching-strategy.md)
* Commit format: [`../git/commit-conventions.md`](../git/commit-conventions.md)
* Pull request format: [`../git/pull-request-template.md`](../git/pull-request-template.md)
* Version changes require user approval: [`../rules/versioning.md`](../rules/versioning.md)

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
