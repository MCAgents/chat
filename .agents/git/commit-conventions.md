---
name: commit-conventions
description: Conventional Commits format for commit messages — type(scope) subject, imperative mood, one commit per logical change.
---

# Commit Conventions

## Format

```
type(optional scope): description
```

## Types

`feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`,
`chore`, `revert`

## Scope

The module or subsystem the change touches, in parentheses. Examples:
`docs(wiki):`, `feat(auth):`, `chore(deps):`, `fix(codec):`.

Scope is optional. Use it whenever the repository has more than one area a reader
could confuse.

## Subject

* Imperative mood — "add", not "added" or "adds".
* Plain text. No links, no issue-tracker IDs, no emoji.
* **No session link, in the subject, the body, or a trailer** — see
  [`../rules/no-session-links.md`](../rules/no-session-links.md).
* No trailing period.
* Say what the change does, specifically.

## Body

Optional. Short bullets explaining **what** changed and **why**. Skip it when the
subject already says everything.

## Committing discipline

* **Commit each logical change, or each group of genuinely related changes.**
  Never batch a whole working session into one commit.
* **Review the diff before every commit.** Read what you are about to record.
* A commit that mixes an unrelated fix into a feature is two commits.
* **Index and memory updates ride in the same commit as the change they describe**, never
  in a follow-up commit. A commit that adds a file without registering it, or that lands
  work without recording it, is incomplete — see
  [`../creators/index-creator.md`](../creators/index-creator.md) and
  [`../rules/memory-policy.md`](../rules/memory-policy.md).

## Worked example

```
docs(agents): add agent instruction architecture

- add the .agents/ tree with rules, git, planning, prompts, and creators
- add the placement algorithm that governs where new files go
- add the versioning rule forbidding self-service version bumps
```

## This format is for commits only

**Pull request titles use a different format.** A PR title is written for a human
scanning a list — never `feat:`, never `fix:`, never `chore:`. See
[`pull-request-template.md`](pull-request-template.md).

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
