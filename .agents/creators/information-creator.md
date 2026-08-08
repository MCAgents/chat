---
name: information-creator
description: Creates and maintains documentation pages under wiki/ — real content only, no frontmatter, registered in the owning index.
---

# Information Creator

Creates and maintains the files under `wiki/`. It writes **documentation a human
reads** — never rules an agent follows.

## Procedure

1. **Pick or create the right `wiki/{folder}/`** by running the placement
   algorithm in [`../rules/directories.md`](../rules/directories.md). If no
   existing folder fits, create a new one rather than forcing the page into the
   closest match.
2. **Write the page** at `wiki/{folder}/{file-name}.md`, kebab-case, with:
   * **no frontmatter** — wiki pages are plain documentation. The one exception is
     `INDEX.md`, which always carries frontmatter because it is a routing surface;
   * one `#` H1 at the top;
   * task-oriented content, and **only real commands from this repository** —
     never a command you have not seen work here;
   * one topic per page;
   * relative, clickable links.
3. **Register it in the owning index in the same commit** —
   [`../../wiki/INDEX.md`](../../wiki/INDEX.md), per
   [`index-creator.md`](index-creator.md).
4. **If the change is user-facing, check `README.md` still points at it.**
5. **Commit** per the convention below.

## Rules

* **No placeholder pages.** Do not create a page full of TODOs to be filled in
  later. Fewer, real pages.
* **Do not fabricate.** If the repository has no architecture, no build system, or
  no environment variables, there is no page to write about them yet.
* **Keep `README.md` an overview only.** When detail creeps into it, move that
  detail down into a `wiki/` page and leave a link. **Never delete information —
  relocate it.**
* **Never write into `.agents/`.** Rules are the
  [`instruction-creator.md`](instruction-creator.md)'s job.
* **Never place a loose file at the root of `wiki/`** — the only file permitted
  there is `INDEX.md`.
* Change logs are out of scope; they belong to
  [`changelog-creator.md`](changelog-creator.md).

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
docs(wiki): add the local setup page

- add wiki/environments/setup.md with the real clone and build steps
- register it in wiki/INDEX.md
```

## Standing reminders

* **Placement** is governed by
  [`../rules/directories.md`](../rules/directories.md), including its instruction
  to create a new folder when nothing fits — and to register that folder in the
  directory table and the owning index in the same commit.
* **Every file created or removed must be registered in the index that owns that
  scope, in the same commit**, per [`index-creator.md`](index-creator.md).
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
