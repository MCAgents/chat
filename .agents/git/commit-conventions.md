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
* **No session links and no session trailers**, in the subject, the body, or a
  trailer — see [`../rules/no-session-links.md`](../rules/no-session-links.md). If
  your tooling appends one by default, strip it before committing. A
  `Co-Authored-By:` line naming a tool is fine; a line carrying a session identifier
  is not.
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

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`../rules/discovery-protocol.md`](../rules/discovery-protocol.md).
