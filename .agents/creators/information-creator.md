---
name: information-creator
description: Creates and maintains both wiki trees — routes each page by audience, wiki/ for humans and .agents/wiki/ for agents, never mirrored.
---

# Information Creator

Creates and maintains **both** documentation trees. Its first job on every page is to
pick the right one.

| Tree | Audience | Path | Frontmatter |
|---|---|---|---|
| Project Wiki | Humans — contributors, users, reviewers | `wiki/{folder}/{file-name}.md` | No |
| Agent Wiki | Agents — SOPs, domain guidelines, operating context | `.agents/wiki/{type}/{file-name}.md` | Yes |

It writes **descriptive content** in either tree — never rules an agent follows, never
dynamic state.

## Route by audience

Apply the audience test from [`../rules/directories.md`](../rules/directories.md)
**before writing a line**:

* Would a new human contributor read this to understand or use the project?
  → `wiki/{folder}/{file-name}.md`, plain markdown, no frontmatter.
* Is this a procedure, constraint, or framing that exists only so an agent behaves
  correctly? → `.agents/wiki/{type}/{file-name}.md`, frontmatter required.
* Both? Write the facts **once** in `wiki/`, and have the `.agents/wiki/` page link to
  them.

**Never mirror content between the trees.** Facts live once, in `wiki/`; the agent page
links to them. If a page starts restating the human wiki, delete the restatement and
leave the link — a duplicated fact is a fact that will go stale on one side.

## Procedure

1. **Apply the audience test** and pick the tree.
2. **Pick or create the right `{folder}` or `{type}`** by running the placement
   algorithm in [`../rules/directories.md`](../rules/directories.md). If nothing fits,
   create a new one rather than forcing the page into the closest match.
3. **Write the page**, kebab-case, with:
   * **no frontmatter** for `wiki/` pages — they are plain documentation for people;
   * **frontmatter** for `.agents/wiki/` pages — a globally unique
     `agent-wiki-{type}-{topic}` `name`, and a one-line `description` ≤ 140 chars that
     an agent can route on without opening the body;
   * one `#` H1 at the top;
   * task-oriented content, and **only real commands from this repository** — never a
     command you have not seen work here;
   * one topic per page;
   * relative, clickable links.
4. **Register it in the owning index in the same commit** —
   [`../index/project-wiki-index.md`](../index/project-wiki-index.md) for `wiki/`,
   [`../index/agent-wiki-index.md`](../index/agent-wiki-index.md) for `.agents/wiki/` —
   per [`index-creator.md`](index-creator.md).
5. **If the change is user-facing, check `README.md` still points at it.**
6. **Commit** per the convention below.

## Rules

* **No placeholder pages.** Do not create a page full of TODOs to be filled in
  later. Fewer, real pages.
* **Do not fabricate.** If the repository has no architecture, no build system, or
  no environment variables, there is no page to write about them yet.
* **Keep `README.md` an overview only.** When detail creeps into it, move that
  detail down into a `wiki/` page and leave a link. **Never delete information —
  relocate it.**
* **Never write into the instruction folders.** Rules are the
  [`instruction-creator.md`](instruction-creator.md)'s job.
* **Never write memory.** Task status, session notes, and decisions belong in
  `.agents/memory/` and are the [`memory-creator.md`](memory-creator.md)'s job.
* **Never create a third documentation tree.** `wiki/` and `.agents/wiki/` are the only
  two. No `docs/`, no `documentation/`.
* **Never place a loose file at the root of `wiki/` or `.agents/wiki/`** — every page
  sits inside a `{folder}` or `{type}`. And never create an `INDEX.md`: every index is a
  file in `.agents/index/`.
* Change logs are out of scope; they belong to
  [`changelog-creator.md`](changelog-creator.md).

## Directory Mandate

* **Human wiki** → `wiki/{folder}/{file-name}.md`, plain markdown, no frontmatter — this
  creator's tree.
* **Agent wiki** → `.agents/wiki/{type}/{file-name}.md`, frontmatter required — also this
  creator's tree, and never a mirror of the other.
* **Indexes** → `.agents/index/{scope}-index.md`. **No `INDEX.md` anywhere, ever**, and
  no index inside the scope it describes.
* **Instructions** → `.agents/{folder}/{file}.md`, gated; **memory** →
  `.agents/memory/{type}/{file-name}.md`, ungated. Neither is this creator's to write.

**Audience test:** a human contributor reads it → `wiki/`; it exists so an agent behaves
correctly → `.agents/wiki/`; both → write it once in `wiki/` and link from the agent
tree.

Placement, including **creating a new folder or `{type}` when nothing fits**, is governed
by [`../rules/directories.md`](../rules/directories.md) — and a new folder is registered
in its tables and in the owning index in the same commit.

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
docs(wiki): add the local setup page

- add wiki/environments/setup.md with the real clone and build steps
- register it in .agents/index/project-wiki-index.md
```

## Standing reminders

* **Every file created, moved, renamed, or removed must be registered in the index that
  owns that scope, in the same commit**, per [`index-creator.md`](index-creator.md).
* **Any pull request this creator opens follows**
  [`../git/pull-request-template.md`](../git/pull-request-template.md) — a
  human-readable title, never a commit-style prefix — and **merging requires user
  approval** per [`../planning/task-workflow.md`](../planning/task-workflow.md).
* **Version changes require user approval** —
  [`../rules/versioning.md`](../rules/versioning.md). This includes creating a new
  `wiki/logs/{Major}/{Minor}/{Patch}/` directory.

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
