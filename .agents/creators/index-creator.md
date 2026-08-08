---
name: index-creator
description: Owns the shape of every INDEX.md in the repository — the canonical template, the split threshold, and the maintenance and audit rules.
---

# Index Creator

Owns the shape of **every `INDEX.md` in this repository**, so they all look the
same and an agent can route through them without reading ahead.

The repository uses a **hub-and-spoke index tree**: one root router, one index per
owned scope, each index small enough to load at no real cost.

## What an index may contain

**Pointer tables, a scope line, and a parent link. Nothing else.**

* An index **never explains a rule**.
* An index **never documents behavior**.
* An index **never carries prose** beyond a one-line purpose per row.

The moment an index starts teaching something, it has become the context bloat it
exists to prevent. Move that content into a real file and link to it.

## Split threshold — when a folder earns its own `INDEX.md`

* A folder gets its own `INDEX.md` when it holds **more than ~10 files**, or when
  it has **subfolders of its own**.
* Below that threshold, the parent index lists the folder's files **inline**. Do
  not create an index for a folder with three files — index sprawl costs more hops
  than it saves.
* When a folder crosses the threshold: move its rows out of the parent into a new
  child `INDEX.md`, replace them in the parent with a single **Child Indexes** row,
  and add the child to the root [`../../INDEX.md`](../../INDEX.md) — **all in one
  commit**.
* Every index names its parent. Every child index is reachable from the root.

## Canonical template

Copy this shape for every non-root index:

```
---
name: {scope}-index
description: Index of {scope} — {what an agent finds here}.
---

# {Scope} Index

**Scope:** `{directory this index owns}`
**Parent:** [{parent index name}]({relative path to parent INDEX.md})

## {Section — one per subfolder or topic}

| File | Purpose |
|---|---|
| [`{path}`]({path}) | {One line. What it is for, not what it says.} |

## Child Indexes

| Index | Scope | Load when |
|---|---|---|
| [`{sub}/INDEX.md`]({sub}/INDEX.md) | {what lives under `{sub}/`} | {the condition that makes this branch the right one} |
```

Drop the **Child Indexes** section when the scope has no child indexes.

## Root-index variant

The root `INDEX.md` is different, and deliberately so:

* **No `Scope` line, no `Parent` line** — it is the top of the tree.
* **No file table.** It links to indexes only, never to a leaf file.
* Its entire body is **a single Child Indexes table** covering every index in the
  repository, plus at most a two-line note.
* It is a **router only**: no rules, no documentation, no prose. It must never be
  used to dictate or write files inside any subtree.

## Maintenance rules

* A file **added, removed, moved, or renamed** updates its owning index **in the
  same commit**.
* An index **added, removed, or renamed** updates the root `INDEX.md` **in the
  same commit**.
* A `Purpose` cell is **one line** and never grows into a paragraph.
* Rows are sorted so the **most-used entries come first**.

## No orphans

* **Every `INDEX.md` must be reachable from the root `INDEX.md`.**
* **Every file in an indexed scope must appear in exactly one index** — not zero,
  not two.

## Audit procedure

Run this when asked to check the index tree, and after any bulk change:

1. Walk the repository tree and collect every file under each indexed scope.
2. Walk every `INDEX.md` and collect every row's target path.
3. Report **files missing from their index** — present on disk, absent from the
   index that owns them.
4. Report **rows pointing at files that no longer exist** — present in an index,
   absent from disk.
5. Report **indexes unreachable from the root**, and **files appearing in more
   than one index**.
6. Report **folders that crossed the split threshold without gaining an index**,
   and **child indexes that exist below the threshold**.
7. Present the findings. Fixing them is a normal change — it follows the Discovery
   Protocol below when it means adding or rewriting an instruction.

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
docs(index): split the skills folder into its own index

- move the skills rows out of .agents/INDEX.md
- add .agents/skills/INDEX.md
- register the new index in the root INDEX.md
```

## Standing reminders

* **Placement** is governed by
  [`../rules/directories.md`](../rules/directories.md), including its instruction
  to create a new folder when nothing fits — and to register that folder in the
  directory table and the owning index in the same commit.
* **Every file created or removed must be registered in the index that owns that
  scope, in the same commit.** For this creator, that rule is the whole job.
* **Any pull request this creator opens follows**
  [`../git/pull-request-template.md`](../git/pull-request-template.md) — a
  human-readable title, never a commit-style prefix — and **merging requires user
  approval** per [`../planning/task-workflow.md`](../planning/task-workflow.md).
* **Version changes require user approval** —
  [`../rules/versioning.md`](../rules/versioning.md). This includes creating a new
  `wiki/logs/{Major}/{Minor}/{Patch}/` directory, which also means a new row in
  the logs index.

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
