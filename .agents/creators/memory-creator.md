---
name: memory-creator
description: Creates and maintains dynamic state under .agents/memory/ — ungated writes, dated entries newest-first, registered in the memory index.
---

# Memory Creator

Creates and maintains the files under `.agents/memory/`. It writes **dynamic state** —
what is happening, what happened, what was decided. Never rules, never documentation.

## No approval gate

**This creator is exempt from the Discovery Protocol's approval requirement for its
own writes.** It does not ask before writing memory, and it does not defer memory to
the end of a branch.

The reason is plain: **memory that waits for permission is memory that never gets
written.** A session that starts with no memory restarts work instead of continuing
it, and the cost of that lands on the user. The gate exists to stop an agent from
quietly rewriting the rules it operates under; recording that a branch was pushed is
not that.

The gate still applies in full to instruction files — see the Discovery Protocol at
the bottom of this file.

## Procedure

1. **Pick the right `{type}`** — `state/`, `tasks/`, `sessions/`, or `decisions/`, per
   the table below and the placement algorithm in
   [`../rules/directories.md`](../rules/directories.md).
2. **Check [`../index/memory-index.md`](../index/memory-index.md) for an existing file
   on the same subject and extend it rather than creating a near-duplicate.** Two
   files on one subject means the next agent reads one and misses the other.
3. **Write or update the file** at `.agents/memory/{type}/{file-name}.md`: frontmatter,
   one `#` H1, then dated entries **newest first**.
4. **Register it in [`../index/memory-index.md`](../index/memory-index.md)** with its
   scope and a one-line purpose.
5. **Commit it in the same commit as the work it describes** — never as a follow-up.

## What to write, and where

| You have | Write it to | Shape |
|---|---|---|
| Ongoing or completed work | `tasks/{slug}.md` | Goal, plan, status, branches, pull requests. |
| What happened in a working session | `sessions/{yyyy-mm-dd}-{slug}.md` | What was done, what was learned, what was left. |
| A choice with consequences | `decisions/{slug}.md` | Context, options considered, the choice, the consequence. |
| The current live state of an area | `state/{area}.md` | Overwritten in place, always current. |

## When to write, without being asked

* At the end of a task.
* When a decision is made that a future session would otherwise re-litigate.
* When work is left unfinished — say exactly where it stopped and what comes next.
* When something surprising is learned about the codebase.
* When a branch or a pull request is opened.

## Never write to memory

* Secrets, API tokens, credentials, private keys.
* Customer data or personal data.
* Full file dumps, or pasted source that already lives in the repository.
* **Assistant or tool session links.** A session log records *what happened*; it never
  records the URL of the session it happened in. See
  [`../rules/no-session-links.md`](../rules/no-session-links.md).
* Anything you would not put in a public commit.

**Memory is committed to git like everything else.** It is not a private scratchpad,
and there is no deleting it after the fact.

## Entry format

```
---
name: memory-{type}-{topic}
description: {one line, ≤ 140 chars, routable without opening the file}
---

# {Title}

## {YYYY-MM-DD}

- {fact, with the file path, branch, or pull request number that proves it}
```

State facts: file paths, branch names, pull request numbers, commit subjects, commands
that ran and what they returned. **No speculation presented as fact** — label a guess
as a guess.

## Staleness check

Before trusting a memory file, compare its newest entry date against the repository's
current state. **If they disagree, the repository wins** — correct the memory file in
the same commit as your work. A memory file describing a state that no longer exists
is worse than no memory, because the next agent acts on it.

## Retention

* When a task ships, mark its `tasks/` file `status: done` with a closing entry.
* At each release, the `sessions/` files for that release are folded into one digest in
  the release's `wiki/logs/{Major}/{Minor}/{Patch}/` directory, and the originals and
  their index rows are deleted — that fold belongs to
  [`changelog-creator.md`](changelog-creator.md).
* `decisions/` files are permanent.
* `state/` files are overwritten in place and never accumulate history.

## Boundaries

**Memory is never normative.** A memory file may say "we currently do X"; it may never
say "always do X".

* If an entry starts sounding like a permanent rule, route it through
  [`instruction-creator.md`](instruction-creator.md) and the Discovery Protocol.
* If it is a durable fact about the project, route it through
  [`information-creator.md`](information-creator.md) — `wiki/` for humans,
  `.agents/wiki/` for agents.
* This creator **never writes rules and never writes wiki pages in either tree.**

Full policy: [`../rules/memory-policy.md`](../rules/memory-policy.md).

## Directory Mandate

* **Indexes** → `.agents/index/{scope}-index.md`. **No `INDEX.md` anywhere, ever.**
* **Agent wiki** → `.agents/wiki/{type}/{file-name}.md`, frontmatter required.
* **Agent memory** → `.agents/memory/{type}/{file-name}.md` — this creator's tree.
* **Human wiki** → `wiki/{folder}/{file-name}.md`, plain markdown, no frontmatter.

**Audience test:** a human contributor reads it → `wiki/`; it exists so an agent
behaves correctly → `.agents/wiki/`; both → write it once in `wiki/` and link from the
agent tree.

Placement, including **creating a new `{type}` when nothing fits**, is governed by
[`../rules/directories.md`](../rules/directories.md). Never place a loose file at the
root of `.agents/memory/`.

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

* Branch off the default branch (`master`) for every task; never commit directly to
  it.
* One task per branch, one pull request per branch.
* Naming: `{type}/{primary-noun}` — e.g. `feat/login`, `fix/schema-drift`,
  `docs/agents-setup`.
* Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`,
  `ci`, `chore`, `revert`.
* No tool-preset prefixes (`claude/`, `codex/`, `cursor/`, …). If a branch violates the
  convention, recreate it correctly and delete the wrong one, or present the options to
  the user.
* Keep branches short-lived and rebased on the default branch.
* For multi-task work, branches stack in dependency order — task 1 from `master`, task
  `k` from task `k-1`'s branch. See
  [`../planning/task-workflow.md`](../planning/task-workflow.md).

**Commits** — canonical file:
[`../git/commit-conventions.md`](../git/commit-conventions.md)

* Conventional Commits: `type(optional scope): description`.
* Same type list as above. Scope is a module or subsystem, e.g. `docs(wiki):`,
  `feat(auth):`, `chore(memory):`.
* Subject in imperative mood, plain text, no trailing period, no links, no
  issue-tracker IDs.
* Optional body: short bullets explaining what and why.
* Commit each logical change or group of related changes — never batch a whole session
  into one commit. Review the diff before every commit.
* **Index and memory updates ride in the same commit as the change they describe**,
  never in a follow-up commit.
* This format applies to **commit messages only. Pull request titles use a different
  format** — see [`../git/pull-request-template.md`](../git/pull-request-template.md).

Worked example:

```
chore(memory): record the platform module regrouping

- update .agents/memory/state/repository-state.md with the new module layout
- add the closing entry to .agents/memory/tasks/platform-structure.md
- register both rows in .agents/index/memory-index.md
```

## Standing reminders

* **Every file created, moved, or removed is registered in the index that owns that
  scope, in the same commit** — for this creator that is
  [`../index/memory-index.md`](../index/memory-index.md), per
  [`index-creator.md`](index-creator.md).
* **Any pull request this creator opens follows**
  [`../git/pull-request-template.md`](../git/pull-request-template.md) — a
  human-readable title, never a commit-style prefix — and **merging requires user
  approval** per [`../planning/task-workflow.md`](../planning/task-workflow.md).
* **Version changes require user approval** —
  [`../rules/versioning.md`](../rules/versioning.md). A memory file may record that a
  bump was proposed; it never performs one.

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
