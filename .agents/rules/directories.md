---
name: directory-architecture
description: Placement authority for the .agents/ and wiki/ trees — which folder a new file belongs in, and how to create one when none fits.
---

# Directory Architecture

This file decides **where a new file goes**. Every instruction lives at
`.agents/{folder}/{file}.md`. Every documentation page lives at
`wiki/{folder}/{file-name}.md`. Nothing is placed by feel — run the placement
algorithm below.

Naming is kebab-case, lowercase, no spaces, in both trees. `INDEX.md` is the one
uppercase exception, everywhere it appears.

## A. `.agents/` folders

The table below is the **baseline**, not a closed set. Only folders marked
*present* exist in this repository today; the rest are reserved names to use when
the project grows into them.

| Folder | Holds | Status |
|---|---|---|
| `rules/` | Repository-wide rules and the directory architecture itself. | Present |
| `git/` | Branching strategy, commit format, pull request format. | Present |
| `creators/` | The instruction / information / changelog / index creator agents. | Present |
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

A reserved folder is created the first time a real file needs it — not before.
Do not scaffold empty folders.

## B. `wiki/` folders

| Folder | Holds | Status |
|---|---|---|
| `information/` | What the project is, architecture, features, concepts. | Present |
| `environments/` | Setup, runtime, configuration, containers, CI environments. | Present |
| `logs/` | Versioned change logs — see the changelog creator. | Present |
| `guides/` | Task-oriented how-tos. | Reserved |
| `reference/` | Commands, config keys, API surface, schema. | Reserved |

## C. Placement algorithm

Follow these steps in order for every new file.

1. **Match the subject to an existing folder.** Pick the folder whose topic
   actually contains the new file's subject — not the one that is merely closest.
2. **If no existing folder fits, create a new one.** Do NOT force the file into
   the nearest folder, and do NOT rename the file to pretend it fits. Create a
   folder named as a plain topic noun, lowercase kebab-case — for example
   `.agents/observability/`, `wiki/integrations/` — and put the file there.
3. **Register every new folder in the same commit.** Add it to the table above
   AND to the index that owns that scope
   ([`../INDEX.md`](../INDEX.md) for `.agents/`,
   [`../../wiki/INDEX.md`](../../wiki/INDEX.md) for `wiki/`). If the new folder
   needs its own index, register that index in the root
   [`INDEX.md`](../../INDEX.md) too.
4. **Extend before duplicating.** If an existing file already covers the subject,
   extend that file instead of adding a near-duplicate — subject to the Discovery
   Protocol below: propose the change, do not self-apply it.
5. **Never place a loose file at the root** of `.agents/` or of `wiki/`. The only
   file permitted at either root is `INDEX.md`.
6. **Depth is `{tree}/{folder}/{file}.md`.** Go deeper only for `wiki/logs/`,
   which has its own required `{Major}/{Minor}/{Patch}/` shape.

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
