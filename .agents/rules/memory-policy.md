---
name: memory-policy
description: What may be written to .agents/memory/ and how — ungated writes, the four types, the never-write list, entry format, and retention.
---

# Memory Policy

## Memory is written automatically and needs no approval

This is the deliberate exception to the Discovery Protocol. Instructions are gated;
**memory is not.** Do not ask before writing memory, and do not defer it to the end
of a branch.

**An agent that finishes a meaningful unit of work and writes nothing to memory has
failed the task.** Memory that waits for permission is memory that never gets
written, and a session that starts with no memory restarts work instead of continuing
it.

## What to write, and where

Every memory file is `.agents/memory/{type}/{file-name}.md`.

| You have | Write it to | Shape |
|---|---|---|
| Ongoing or completed work | `tasks/{slug}.md` | Goal, plan, status, branches, pull requests. One file per task. |
| What happened in a working session | `sessions/{yyyy-mm-dd}-{slug}.md` | What was done, what was learned, what was left. |
| A choice with consequences | `decisions/{slug}.md` | Context, options considered, the choice, the consequence. |
| The current live state of an area | `state/{area}.md` | Overwritten in place, always current — never an append-only log. |

Pick the `{type}` that matches, and **extend an existing file on the same subject
rather than creating a near-duplicate** — check
[`../index/memory-index.md`](../index/memory-index.md) first.

## When to write, without being asked

* At the end of a task.
* When a decision is made that a future session would otherwise re-litigate.
* When work is left unfinished — say exactly where it stopped and what is next.
* When something surprising is learned about the codebase.
* When a branch or a pull request is opened.

## Never write to memory

* Secrets, API tokens, credentials, private keys.
* Customer data or personal data.
* Full file dumps, or pasted source that already lives in the repository.
* **Assistant or tool session links** — see
  [`no-session-links.md`](no-session-links.md). A session log records *what happened*;
  it never records the URL of the session it happened in.
* Anything you would not put in a public commit.

**Memory is committed to git like everything else.** It is not a private scratchpad,
and there is no such thing as deleting it after the fact. In this repository that
matters twice over: a token that reaches a memory file is a leaked token, and the
never-log rules in [`../security/token-handling.md`](../security/token-handling.md)
apply to memory exactly as they apply to a log line. That file is the authority; this
one adds no exception to it.

## Memory is never normative

A memory file may say **"we currently do X"**. It may never say **"always do X"**.

Recording state is the job; asserting a rule is not. A rule that deserves to be
permanent is proposed as an instruction through the Discovery Protocol below, and
lands under `.agents/{folder}/` — never as a memory entry that a later agent mistakes
for policy. Precedence is settled in
[`auto-activation.md`](auto-activation.md): an instruction always beats a memory
file.

## Entry format

Every memory file has:

1. Frontmatter — `name: memory-{type}-{topic}`, and a one-line `description`.
2. One `#` H1 title.
3. Dated entries, **newest first**, under `## {YYYY-MM-DD}` headings.

Inside an entry, state facts: file paths, branch names, pull request numbers, commit
subjects, commands that ran and what they returned. **No speculation presented as
fact** — if something is a guess, label it a guess.

## Staleness

Before trusting a memory file, check its newest entry date against the repository's
current state.

**If they disagree, the repository wins.** Correct the memory file in the same commit
as your work — a memory file describing a state that no longer exists is worse than
no memory at all, because the next agent will act on it.

## Retention

* When a task ships, mark its `tasks/` file `status: done` and add a closing entry.
* At each release, fold the `sessions/` files belonging to that release into **one
  digest** inside the release's `wiki/logs/{Major}/{Minor}/{Patch}/` directory, then
  delete the originals and their rows in
  [`../index/memory-index.md`](../index/memory-index.md). That fold is the
  [`../creators/changelog-creator.md`](../creators/changelog-creator.md)'s job.
* `decisions/` files are permanent. Do not prune them.
* `state/` files are overwritten in place and never accumulate history.

## Registration

**Every memory file appears in [`../index/memory-index.md`](../index/memory-index.md)
in the same commit that creates it.** An unregistered memory file will not be read,
which makes it worthless.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`discovery-protocol.md`](discovery-protocol.md).
