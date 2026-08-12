---
name: instruction-creator
description: Creates and maintains instruction files under .agents/ — one topic per file, valid frontmatter, registered in the owning index.
---

# Instruction Creator

Creates and maintains the instruction files under `.agents/{folder}/`. It writes
**rules an agent follows** — never documentation, never dynamic state, never prose
about the project.

## Procedure

1. **Confirm the instruction does not already exist.** Read
   [`../index/agents-index.md`](../index/agents-index.md) first and route from its
   tables. Do not bulk-scan `.agents/**`.
2. **Choose or create the right folder** by running the placement algorithm in
   [`../rules/directories.md`](../rules/directories.md). If no existing folder
   fits the subject, create a new one — do not force the file into the closest
   folder and do not rename the file to pretend it fits.
3. **Write the file** at `.agents/{folder}/{file}.md`, kebab-case, with:
   * valid frontmatter — a globally unique kebab-case `name`, and a `description`
     of one line, ≤ 140 chars, written so an agent can route on it without opening
     the body;
   * one `#` H1 immediately after the frontmatter;
   * exactly one topic. If the file needs two H1-level subjects, it is two files;
   * rules written in the imperative and testable — a reader must be able to tell
     whether they complied;
   * relative, clickable links.
4. **Register it in [`../index/agents-index.md`](../index/agents-index.md) in the same
   commit** — see [`index-creator.md`](index-creator.md).
5. **If it introduces a new automatic behavior, add its trigger row** to
   [`../rules/auto-activation.md`](../rules/auto-activation.md) and **mirror that row
   into [`../../AGENTS.md`](../../AGENTS.md)**, in the same commit. An instruction with
   no trigger fires only when someone thinks to look for it.
6. **Commit** per the convention below.

## Refusals

This creator must refuse to:

* **write documentation into the instruction folders** — project explanation, how-tos,
  and reference pages belong in `wiki/`, agent SOPs and domain guidelines belong in
  `.agents/wiki/`, and both are the
  [`information-creator.md`](information-creator.md)'s job;
* **write dynamic state** — task status, session notes, and decisions belong in
  `.agents/memory/` and are the [`memory-creator.md`](memory-creator.md)'s job. A rule
  that describes what is currently happening is not a rule;
* **put rules in `AGENTS.md`** — that file is an entry point and an activation
  contract, not a rule body;
* **put rules in any index** — an index carries pointer tables, a scope line, and a
  parent link, nothing else;
* **place a loose file at the root of `.agents/`** — every instruction sits inside a
  `{folder}`;
* **write into the reserved structural folders** `.agents/index/`, `.agents/wiki/`, and
  `.agents/memory/` — an instruction file never belongs in any of them.

## Directory Mandate

* **Instructions** → `.agents/{folder}/{file}.md`, normative and gated — this creator's
  tree.
* **Indexes** → `.agents/index/{scope}-index.md`. **No `INDEX.md` anywhere, ever.**
* **Agent wiki** → `.agents/wiki/{type}/{file-name}.md`, frontmatter required;
  **agent memory** → `.agents/memory/{type}/{file-name}.md`, ungated.
* **Human wiki** → `wiki/{folder}/{file-name}.md`, plain markdown, no frontmatter.

**Audience test:** a human contributor reads it → `wiki/`; it exists so an agent behaves
correctly → `.agents/wiki/`; both → write it once in `wiki/` and link from the agent
tree. Never mirror content between the two.

Placement, including **creating a new folder when nothing fits**, is governed by
[`../rules/directories.md`](../rules/directories.md) — and a new folder is registered in
its tables and in the owning index in the same commit.

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
docs(agents): add the packet validation rule

- add .agents/security/packet-validation.md
- register it in .agents/index/agents-index.md
- add its trigger row to .agents/rules/auto-activation.md and AGENTS.md
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

Authority: [`../rules/discovery-protocol.md`](../rules/discovery-protocol.md). This
block is reproduced here on purpose — a creator is often the only file open when an
instruction is about to be written.
