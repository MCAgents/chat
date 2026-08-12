---
name: directory-architecture
description: Placement authority for all four trees — the centralized mandate, the two-wiki audience test, and creating a folder when none fits.
---

# Directory Architecture

This file decides **where a new file goes**. Nothing is placed by feel — run the
placement algorithm in section F.

## A. The core directory architecture mandate

All agent-related files must strictly follow this centralized structure. Do NOT
scatter `INDEX.md` files across various directories.

* **Indexes:** `.agents/index/{file-name}.md` (e.g. `root-index.md`)
  Replaces all scattered `INDEX.md` files. Acts as the centralized routing system.
* **Agent Wiki:** `.agents/wiki/{type}/{file-name}.md`
  The static knowledge base, SOPs, and domain guidelines specifically for agents.
* **Agent Memory:** `.agents/memory/{type}/{file-name}.md`
  Used to keep track of ongoing tasks, dynamic states, session logs, and agent
  memories.

### The human wiki stays where humans expect it

The mandate above governs **agent-related** files. Human-facing project documentation
is not an agent artifact and keeps its conventional home:

* **Project Wiki (humans):** `wiki/{folder}/{file-name}.md`
  The documentation a person reads — overview, architecture, guides, reference,
  environments, release logs. Plain markdown, no frontmatter, linked from
  `README.md`.

So the repository has **exactly two documentation trees, with different audiences**,
and never a third:

| Tree | Audience | Path | Frontmatter |
|---|---|---|---|
| Project Wiki | Humans — contributors, users, reviewers | `wiki/{folder}/{file-name}.md` | No |
| Agent Wiki | Agents — SOPs, domain guidelines, operating context | `.agents/wiki/{type}/{file-name}.md` | Yes |

### The audience test — apply it every time, before writing a page

* Would a new human contributor read this to understand or use the project?
  → `wiki/`.
* Is this a procedure, constraint, or framing that only exists so an agent behaves
  correctly? → `.agents/wiki/`.
* Both? Write the facts **once** in `wiki/` and have the `.agents/wiki/` page link to
  it. Never mirror content between the two trees — a duplicated fact is a fact that
  will go stale on one side.

Both trees are routed from `.agents/index/`. Neither contains an index file of its
own.

### What this mandate forbids

These are hard failures.

* **No `INDEX.md` anywhere in the repository** — not at the root, not in `.agents/`,
  not in `wiki/`, not in `.agents/wiki/`, not in any subfolder. Every index is a file
  inside `.agents/index/`.
* **No index outside `.agents/index/`.** A folder never carries its own index. When a
  scope earns an index, that index is a *new file in `.agents/index/`*, never a file
  placed inside the scope. This includes `wiki/` — the human tree is routed from
  [`../index/project-wiki-index.md`](../index/project-wiki-index.md).
* **No third documentation tree.** `wiki/` and `.agents/wiki/` are the only two. Do
  not create `docs/`, `documentation/`, or a second human wiki.
* **No memory outside `.agents/memory/`.** No scratch notes, task trackers, `TODO.md`,
  `NOTES.md`, `STATE.md`, or session logs anywhere else in the repository.
* **No loose files at the root of `.agents/`, `wiki/`, `.agents/wiki/`, or
  `.agents/memory/`.** Every page and memory file lives inside a folder or `{type}`.
  `.agents/index/` is the one flat folder, and it holds index files only.
* **Only three files belong at the repository root** as far as this architecture is
  concerned: `AGENTS.md`, `README.md`, `LICENSE`. `AGENTS.md` stays at the root
  because that is where agent tooling looks for it — it is an entry point, not an
  index. Build and tooling files at the root (`settings.gradle`, `gradlew`,
  `.gitignore`, …) are outside this rule's scope; it governs documentation and
  instruction files.

### The four trees, and what belongs in each

| Tree | Path shape | Nature | Who writes it |
|---|---|---|---|
| Instructions | `.agents/{folder}/{file}.md` | Normative. Rules an agent must obey. | Only with user approval — the Discovery Protocol. |
| Index | `.agents/index/{scope}-index.md` | Routing. Pointers only. | Same commit as whatever it indexes. |
| Project Wiki | `wiki/{folder}/{file-name}.md` | Human documentation. Facts about the project. | Freely, when the facts are real. |
| Agent Wiki | `.agents/wiki/{type}/{file-name}.md` | Agent knowledge. SOPs, domain guidelines, context. | Freely, when the facts are real. |
| Memory | `.agents/memory/{type}/{file-name}.md` | Dynamic state. Tasks, sessions, decisions. | Freely and automatically, no approval needed. |

The distinction that matters most: **instructions are normative and gated, memory is
dynamic and ungated, the two wikis are descriptive and in between.** Never record
dynamic task state as an instruction, and never let a wiki or memory file assert a
rule. When a wiki or memory page disagrees with an instruction, the instruction wins,
always — see [`auto-activation.md`](auto-activation.md).

## Naming

All file and folder names under `.agents/` and `wiki/` are **kebab-case**, lowercase,
no spaces.

`CHANGELOG.md` and its siblings in a version directory (`MIGRATION.md`,
`BREAKING.md`, `UPGRADE.md`, `NOTES.md`) are the only uppercase filenames permitted.
`INDEX.md` is **not** permitted anywhere. Index filenames always end in `-index.md`.

One topic per file. If a file needs two H1-level subjects, it is two files.

## B. `.agents/` instruction folders

The table below is the **baseline**, not a closed set. Only folders marked *present*
exist in this repository today; the rest are reserved names to use when the project
grows into them.

| Folder | Holds | Status |
|---|---|---|
| `rules/` | Repository-wide rules and the directory architecture itself. | Present |
| `git/` | Branching strategy, commit format, pull request format. | Present |
| `creators/` | The instruction / information / changelog / index / memory creators. | Present |
| `prompts/` | Standing prompt templates and few-shot examples. | Present |
| `planning/` | Task intake, breakdown, ordering, estimation, prioritization. | Present |
| `knowledge/` | Domain context an agent needs to reason correctly. | Present |
| `security/` | Security policy, secret handling, vulnerability prevention. | Present |
| `compliance/` | Licensing, legal, and privacy policy. | Present |
| `docs/` | Rules for writing README, wiki, and index files. | Reserved |
| `skills/` | Step-by-step procedures for recurring tasks. | Reserved |
| `tools/` | Tool definitions and schemas. | Reserved |
| `personas/` | Roles and behaviors to adopt. | Reserved |
| `ethics/` | Safety boundaries and constraints. | Reserved |
| `architecture/` | System design guidelines and structural constraints. | Reserved |
| `api/` | API design standards and specification guidelines. | Reserved |
| `database/` | Schema design, migrations, query constraints. | Reserved |
| `performance/` | Performance, memory, and bottleneck guidelines. | Reserved |
| `dependencies/` | Package management and version-update policy. | Reserved |
| `deploy/` | Deployment, environments, containerization. | Reserved |
| `workflows/` | CI/CD automation rules. | Reserved |
| `testing/` | Test strategy, coverage, fixtures. | Reserved |

`index/`, `wiki/`, and `memory/` are **reserved structural folders** inside
`.agents/`, not instruction folders. **Never put an instruction file in them.**

A reserved folder is created the first time a real file needs it — not before. Do not
scaffold empty folders.

## C. `wiki/` folders — human documentation

Plain markdown at `wiki/{folder}/{file-name}.md`, **no frontmatter**.

| Folder | Holds | Status |
|---|---|---|
| `information/` | What the project is, architecture, features, concepts. | Present |
| `environments/` | Setup, runtime, configuration, containers, CI environments. | Present |
| `guides/` | Task-oriented how-tos for people. | Present |
| `reference/` | Commands, config keys, API surface, schema. | Reserved |
| `logs/` | Versioned change logs — the one folder allowed extra depth. | Present |

## D. `.agents/wiki/` types — agent knowledge

Frontmatter required at `.agents/wiki/{type}/{file-name}.md`.

| Type | Holds | Status |
|---|---|---|
| `context/` | Orientation an agent needs before touching code: what lives where, build/test commands, entry points, gotchas. | Present |
| `sop/` | Standard operating procedures an agent follows step by step. | Reserved |
| `domain/` | Domain vocabulary, business rules, external-system behavior an agent must respect. | Reserved |

**Facts live once, in `wiki/`.** An `.agents/wiki/` page carries the agent-specific
procedure or framing and links to the human page for the underlying facts. If you
catch yourself pasting the same paragraph into both trees, the page belongs in `wiki/`
and the agent page should be a link.

## E. `.agents/memory/` types

| Type | Holds | Lifetime | Status |
|---|---|---|---|
| `state/` | Current dynamic state of an area — what is live, broken, in flight. | Overwritten in place, always current. | Present |
| `tasks/` | One file per ongoing or completed task: goal, plan, status, branches, pull requests. | Until the task ships, then archived. | Present |
| `sessions/` | `{yyyy-mm-dd}-{slug}.md` — what happened in a working session. | Rolled into a digest each release. | Reserved |
| `decisions/` | One file per durable decision: context, options, choice, consequence. | Permanent. | Reserved |

Format, retention, and the never-write list are in
[`memory-policy.md`](memory-policy.md).

## F. Placement algorithm

Follow these steps in order for every new file.

1. **Classify first.** Is the new file **normative** (instruction), **routing**
   (index), **human documentation** (`wiki/`), **agent knowledge**
   (`.agents/wiki/`), or **dynamic state** (memory)? The answer picks the tree, and
   **the tree is not negotiable.**
2. **If it is documentation, apply the audience test** from section A before choosing
   a tree. When both audiences want it, it goes in `wiki/` and the agent tree links to
   it.
3. **Pick the existing folder or `{type}`** whose topic actually contains the
   subject — not the one that is merely closest.
4. **If nothing fits, do NOT force it into the closest one and do NOT rename the file
   to pretend it fits.** Create a new folder or `{type}` that fits: lowercase
   kebab-case, a plain topic noun — for example `.agents/observability/`,
   `wiki/integrations/`, `.agents/wiki/playbooks/`, `.agents/memory/incidents/` — and
   put the file there.
5. **Register every new folder in the same commit.** Add it to the tables in this file
   AND to the index that owns that scope. If the new folder earns its own index,
   create it at `.agents/index/{scope}-index.md` and register that index in
   [`../index/root-index.md`](../index/root-index.md) too. **The tables above are a
   baseline, not a closed set.**
6. **Extend before duplicating.** If an existing file already covers the subject,
   extend that file instead of adding a near-duplicate — subject to the Discovery
   Protocol for instructions: propose, do not self-apply.
7. **Never place a loose file at the root** of `.agents/`, `wiki/`, `.agents/wiki/`,
   or `.agents/memory/`. Every file sits inside a folder or `{type}`.
8. **Depth** is `.agents/{folder}/{file}.md`, `wiki/{folder}/{file}.md`, and
   `.agents/{tree}/{type}/{file}.md`. The only exception is `wiki/logs/`, which has
   its own required `{Major}/{Minor}/{Patch}/` shape.
9. **Never create an `INDEX.md`. Never create a third documentation tree.**

## Which index owns what

| Scope | Owning index |
|---|---|
| `.agents/` instruction folders | [`../index/agents-index.md`](../index/agents-index.md) |
| `.agents/wiki/` | [`../index/agent-wiki-index.md`](../index/agent-wiki-index.md) |
| `.agents/memory/` | [`../index/memory-index.md`](../index/memory-index.md) |
| `wiki/` except `logs/` | [`../index/project-wiki-index.md`](../index/project-wiki-index.md) |
| `wiki/logs/` | [`../index/logs-index.md`](../index/logs-index.md) |
| `.agents/index/` itself | [`../index/root-index.md`](../index/root-index.md) |

No index manages a scope it does not own. Shape, split threshold, and audit are in
[`../creators/index-creator.md`](../creators/index-creator.md).

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
automatically — see [`memory-policy.md`](memory-policy.md).
