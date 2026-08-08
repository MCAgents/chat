---
name: changelog-creator
description: Creates and maintains versioned change logs under wiki/logs/ — numeric version directories, approved bumps only, history never rewritten.
---

# Changelog Creator

Creates and maintains the files under `wiki/logs/`.

## Path shape

```
wiki/logs/{Major}/{Minor}/{Patch}/{file-name}.md
```

Examples:

* `wiki/logs/1/0/0/CHANGELOG.md`
* `wiki/logs/1/0/1/CHANGELOG.md`
* `wiki/logs/1/1/0/CHANGELOG.md`
* `wiki/logs/2/0/0/CHANGELOG.md`

Rules:

* **Numeric directory segments only** — no `v` prefix, no zero padding.
* The directory shape exists so a version can hold **more than one document**.
  `CHANGELOG.md` is the default; `MIGRATION.md`, `BREAKING.md`, `UPGRADE.md`, and
  `NOTES.md` may live beside it in the same version directory.

## `CHANGELOG.md` content

Sections, in this order — **omit the ones that are empty**:

`Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, `Security`

Include the **release date** and a **one-line summary** at the top.

## Creating a version directory requires approval

**A new `wiki/logs/{Major}/{Minor}/{Patch}/` directory is a version claim.**
Creating one requires explicit user approval, exactly like editing a manifest
version — see [`../rules/versioning.md`](../rules/versioning.md).

Ask which version applies **before** creating the directory. Never infer it from
the size of the change.

## Never rewrite history

* Never edit a released version's log to change what it says happened.
* Corrections go into the **next** version's log.
* Never re-tag a released version.

## Keeping the logs index current

Keep [`../../wiki/logs/INDEX.md`](../../wiki/logs/INDEX.md) listing **every
version, newest first** — one row per version directory, with a one-line summary
and the files that directory contains. A new version directory and its index row
land in the same commit.

## Branch & Commit Convention

This convention applies to **every commit this creator makes**.

**Branching** — canonical file:
[`../git/branching-strategy.md`](../git/branching-strategy.md)

* Branch off the default branch (`master`) for every task; never commit directly
  to it.
* One task per branch, one pull request per branch.
* Naming: `{type}/{primary-noun}` — e.g. `feat/login`, `fix/schema-drift`,
  `docs/agents-setup`.
* Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`,
  `build`, `ci`, `chore`, `revert`.
* No tool-preset prefixes (`claude/`, `codex/`, `cursor/`, …). If a branch
  violates the convention, recreate it correctly and delete the wrong one, or
  present the options to the user.
* Keep branches short-lived and rebased on the default branch.
* For multi-task work, branches stack in dependency order — task 1 from `master`,
  task `k` from task `k-1`'s branch. See
  [`../planning/task-workflow.md`](../planning/task-workflow.md).

**Commits** — canonical file:
[`../git/commit-conventions.md`](../git/commit-conventions.md)

* Conventional Commits: `type(optional scope): description`.
* Same type list as above. Scope is a module or subsystem, e.g. `docs(wiki):`,
  `feat(auth):`, `chore(deps):`.
* Subject in imperative mood, plain text, no trailing period, no links, no
  issue-tracker IDs.
* Optional body: short bullets explaining what and why.
* Commit each logical change or group of related changes — never batch a whole
  session into one commit. Review the diff before every commit.
* This format applies to **commit messages only. Pull request titles use a
  different format** — see
  [`../git/pull-request-template.md`](../git/pull-request-template.md).

Worked example:

```
docs(logs): add the 0.1.0 change log

- add wiki/logs/0/1/0/CHANGELOG.md
- register the version in wiki/logs/INDEX.md
```

## Standing reminders

* **Placement** is governed by
  [`../rules/directories.md`](../rules/directories.md), including its instruction
  to create a new folder when nothing fits — and to register that folder in the
  directory table and the owning index in the same commit. `wiki/logs/` is the one
  tree that goes deeper than `{tree}/{folder}/{file}.md`, and only in the
  `{Major}/{Minor}/{Patch}/` shape above.
* **Every file created or removed must be registered in the index that owns that
  scope, in the same commit**, per [`index-creator.md`](index-creator.md).
* **Any pull request this creator opens follows**
  [`../git/pull-request-template.md`](../git/pull-request-template.md) — a
  human-readable title, never a commit-style prefix — and **merging requires user
  approval** per [`../planning/task-workflow.md`](../planning/task-workflow.md).
* **Version changes require user approval** —
  [`../rules/versioning.md`](../rules/versioning.md). For this creator that is the
  central rule, not a footnote.

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
