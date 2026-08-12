---
name: index-creator
description: Owns the shape of every index in .agents/index/ — the centralized mandate, the canonical template, the split threshold, and the audit.
---

# Index Creator

Owns the shape of **every index file in this repository**, so they all look the same and
an agent can route through them without reading ahead.

The repository uses a **hub-and-spoke index tree with a flat, centralized index
folder**: one root router, one index file per owned scope, all of them living side by
side in `.agents/index/`, each small enough to load without cost.

## The centralized-index mandate

* **Every index is a file in `.agents/index/`, named `{scope}-index.md`.**
* **`INDEX.md` is forbidden repository-wide** — not at the root, not in `.agents/`, not
  in `wiki/`, not in `.agents/wiki/`, not in any subfolder. There are no exceptions.
* **An index is never placed inside the scope it describes.** A folder does not carry
  its own index. When a scope earns one, that index is a *new file in `.agents/index/`*.
  This includes `wiki/` — the human tree is routed from
  [`project-wiki-index.md`](../index/project-wiki-index.md), which sits in
  `.agents/index/` like every other index.
* **`.agents/index/` is flat and holds index files only.** No subfolders, no rules, no
  documentation.

## What an index may contain

**Pointer tables, a scope line, and a parent link. Nothing else.**

* An index **never explains a rule**.
* An index **never documents behavior**.
* An index **never carries prose** beyond a one-line purpose per row.

The moment an index starts teaching something, it has become the context bloat it exists
to prevent. Move that content into a real file and link to it.

## The indexes that always exist

| File | `name` | Owns |
|---|---|---|
| [`../index/root-index.md`](../index/root-index.md) | `root-index` | Every other index. Nothing else. |
| [`../index/agents-index.md`](../index/agents-index.md) | `agents-index` | The instruction folders under `.agents/`. |
| [`../index/agent-wiki-index.md`](../index/agent-wiki-index.md) | `agent-wiki-index` | `.agents/wiki/`. |
| [`../index/project-wiki-index.md`](../index/project-wiki-index.md) | `project-wiki-index` | `wiki/`, except `logs/`. |
| [`../index/memory-index.md`](../index/memory-index.md) | `memory-index` | `.agents/memory/`. |
| [`../index/logs-index.md`](../index/logs-index.md) | `logs-index` | `wiki/logs/`, newest version first. |

**No index manages a scope it does not own.** Each scope index carries one `##` section
per folder or `{type}` it owns, with a `| File | Purpose |` table of relative links, and
states that any file added to or removed from its scope is reflected in the same commit.

## Split threshold — when a scope earns its own index file

* A folder or `{type}` earns its own `.agents/index/{scope}-index.md` when it holds
  **more than ~10 files**, or when it has **subfolders of its own**.
* Below that threshold, the parent index lists the files **inline**. Do not create an
  index for a folder with three files — index sprawl costs more hops than it saves.
* **Child index filenames are the scope path, kebab-joined:**

  | Scope | Index filename |
  |---|---|
  | `wiki/information/` | `project-wiki-information-index.md` |
  | `.agents/wiki/sop/` | `agent-wiki-sop-index.md` |
  | `.agents/memory/sessions/` | `memory-sessions-index.md` |

* When a scope crosses the threshold: move its rows out of the parent into the new child
  index file, replace them in the parent with a single **Child Indexes** row, and add the
  child to [`../index/root-index.md`](../index/root-index.md) — **all in one commit**.
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
**Parent:** [{parent index name}]({relative path, e.g. root-index.md})

## {Section — one per folder or type in scope}

| File | Purpose |
|---|---|
| [`{relative path}`]({relative path}) | {One line. What it is for, not what it says.} |

## Child Indexes

| Index | Scope | Load when |
|---|---|---|
| [`{scope}-index.md`]({scope}-index.md) | {what lives under that scope} | {the condition that makes this branch the right one} |
```

Drop the **Child Indexes** section when the scope has no child indexes.

## Root-index variant

The root index is different, and deliberately so:

* **No `Scope` line, no `Parent` line** — it is the top of the tree.
* **No file table.** It links to indexes only, never to a leaf file.
* Its entire body is **a single routing table** covering every index in the repository,
  with a **Load when** column so an agent can choose one branch without opening the
  others, plus at most a two-line note.
* It is a **router only**: no rules, no documentation, no prose.

## Relative links

Index files sit in `.agents/index/`, so:

| Target | Link shape |
|---|---|
| A sibling index | `project-wiki-index.md` — a bare filename |
| The instruction tree | `../rules/directories.md` — climb one level |
| The agent wiki | `../wiki/context/repository-map.md` — climb one level |
| Agent memory | `../memory/state/repository-state.md` — climb one level |
| The human wiki | `../../wiki/information/overview.md` — climb two levels |
| A root file | `../../README.md` — climb two levels |

Getting this wrong produces an index full of dead links, which is worse than no index.
Check every link you write.

## Maintenance rules

* A file **added, removed, moved, or renamed** updates its owning index **in the same
  commit**.
* An index **added, removed, or renamed** updates
  [`../index/root-index.md`](../index/root-index.md) **in the same commit**.
* A `Purpose` cell is **one line** and never grows into a paragraph.
* Rows are sorted so the **most-used entries come first**.

## No orphans

* **Every index file must be reachable from `root-index.md`.**
* **Every file under `.agents/` and `wiki/` must appear in exactly one index** — not
  zero, not two.

## Audit procedure

Run this when asked to check the index tree, and after any bulk change:

1. Walk `.agents/` and `wiki/` and collect every file under each indexed scope.
2. Walk every index in `.agents/index/` and collect every row's target path.
3. Report **files missing from their index** — present on disk, absent from the index
   that owns them.
4. Report **index rows pointing at files that no longer exist**.
5. Report **any `INDEX.md` that has appeared anywhere in the repository**, and **any
   index file living outside `.agents/index/`**. Both are hard failures.
6. Report **indexes unreachable from the root**, and **files appearing in more than one
   index**.
7. Report **scopes that crossed the split threshold without gaining an index**, and
   **child indexes that exist below the threshold**.
8. Present the findings. Fixing them is a normal change — it follows the Discovery
   Protocol below when it means adding or rewriting an instruction.

## Directory Mandate

* **Indexes** → `.agents/index/{scope}-index.md`, flat, pointers only — this creator's
  tree. **No `INDEX.md` anywhere, ever.**
* **Instructions** → `.agents/{folder}/{file}.md`, normative and gated.
* **Agent wiki** → `.agents/wiki/{type}/{file-name}.md`, frontmatter required;
  **agent memory** → `.agents/memory/{type}/{file-name}.md`, ungated.
* **Human wiki** → `wiki/{folder}/{file-name}.md`, plain markdown, no frontmatter.

**Audience test:** a human contributor reads it → `wiki/`; it exists so an agent behaves
correctly → `.agents/wiki/`; both → write it once in `wiki/` and link from the agent
tree.

Placement, including **creating a new folder when nothing fits**, is governed by
[`../rules/directories.md`](../rules/directories.md) — and a new folder is registered in
its tables and in the owning index in the same commit. When a new folder earns its own
index, that index goes in `.agents/index/` and is registered in `root-index.md`.

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
docs(index): split the skills folder into its own index

- move the skills rows out of .agents/index/agents-index.md
- add .agents/index/agents-skills-index.md
- register the new index in .agents/index/root-index.md
```

## Standing reminders

* **Every file created, moved, renamed, or removed must be registered in the index that
  owns that scope, in the same commit.** For this creator, that rule is the whole job.
* **Any pull request this creator opens follows**
  [`../git/pull-request-template.md`](../git/pull-request-template.md) — a
  human-readable title, never a commit-style prefix — and **merging requires user
  approval** per [`../planning/task-workflow.md`](../planning/task-workflow.md).
* **Version changes require user approval** —
  [`../rules/versioning.md`](../rules/versioning.md). This includes creating a new
  `wiki/logs/{Major}/{Minor}/{Patch}/` directory, which also means a new row in
  [`../index/logs-index.md`](../index/logs-index.md).

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
