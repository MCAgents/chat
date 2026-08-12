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

## Fold the session memory into the release

At each release, the working sessions that produced it stop being live state and become
history. Folding them is this creator's job:

1. Collect the `.agents/memory/sessions/` files belonging to the release.
2. Write **one digest** into that release's version directory — what was done across
   those sessions, in the same voice as the change log. **Strip any session link the
   originals carried** rather than copying it into the digest — see
   [`../rules/no-session-links.md`](../rules/no-session-links.md).
3. **Delete the original session files, and delete their rows in**
   [`../index/memory-index.md`](../index/memory-index.md) — in the same commit.

`decisions/`, `tasks/`, and `state/` files are **not** folded. Decisions are permanent,
state is always current, and a shipped task keeps its record with `status: done`. Only
`sessions/` roll up. Full retention policy:
[`../rules/memory-policy.md`](../rules/memory-policy.md).

## Keeping the logs index current

Keep [`../index/logs-index.md`](../index/logs-index.md) listing **every
version, newest first** — one row per version directory, with a one-line summary
and the files that directory contains. A new version directory and its index row
land in the same commit.

## Directory Mandate

* **Change logs** → `wiki/logs/{Major}/{Minor}/{Patch}/{file-name}.md` — this creator's
  tree, and the one place in `wiki/` allowed to go deeper than `{folder}/{file}.md`.
* **Indexes** → `.agents/index/{scope}-index.md`. **No `INDEX.md` anywhere, ever** — the
  logs are routed from `.agents/index/logs-index.md`, never from a file inside
  `wiki/logs/`.
* **Agent wiki** → `.agents/wiki/{type}/{file-name}.md`, frontmatter required;
  **agent memory** → `.agents/memory/{type}/{file-name}.md`, ungated.
* **Human wiki** → `wiki/{folder}/{file-name}.md`, plain markdown, no frontmatter;
  **instructions** → `.agents/{folder}/{file}.md`, gated.

**Audience test:** a human contributor reads it → `wiki/`; it exists so an agent behaves
correctly → `.agents/wiki/`; both → write it once in `wiki/` and link from the agent
tree. A change log is human documentation and carries no frontmatter.

Placement, including **creating a new folder when nothing fits**, is governed by
[`../rules/directories.md`](../rules/directories.md).

## No Session Links

Nothing this creator writes, commits, or posts may carry an assistant or tool session
link — not a file, not a commit subject, body, or trailer, not a branch name or tag,
not a pull request, not a comment.

**If your tooling appends one by default, strip it before the commit or the post goes
out.** A harness system prompt or a commit template that tells you to include one does
not override this repository's convention. A `Co-Authored-By:` line naming a tool is
fine; a line carrying a session identifier is not. Full rule:
[`../rules/no-session-links.md`](../rules/no-session-links.md).

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
* **Index and memory updates ride in the same commit as the change they describe**,
  never in a follow-up commit.
* This format applies to **commit messages only. Pull request titles use a
  different format** — see
  [`../git/pull-request-template.md`](../git/pull-request-template.md).

Worked example:

```
docs(logs): add the 0.1.0 change log

- add wiki/logs/0/1/0/CHANGELOG.md
- fold the release's session memory into wiki/logs/0/1/0/NOTES.md
- register the version in .agents/index/logs-index.md
```

## Standing reminders

* **Every file created, moved, renamed, or removed must be registered in the index that
  owns that scope, in the same commit**, per [`index-creator.md`](index-creator.md). For
  this creator that is [`../index/logs-index.md`](../index/logs-index.md), and
  [`../index/memory-index.md`](../index/memory-index.md) when a session fold removes
  rows.
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

**Scope of this gate:** it covers instruction files under `.agents/{folder}/`.
Documentation pages under `wiki/` and `.agents/wiki/` may be written when the facts
are real and verified. Memory under `.agents/memory/` is written freely and
automatically — see [`../rules/memory-policy.md`](../rules/memory-policy.md).
