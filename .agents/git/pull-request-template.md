---
name: pull-request-template
description: Pull request format — human-readable titles, never commit-style prefixes, plus the required Overview/Added/Modified/Deleted/Summary body.
---

# Pull Request Template

## Title

A pull request title is written **for a human scanning a list of pull requests**,
not for a parser.

* **Never use a Conventional Commit prefix.** No `feat:`, no `fix:`, no `chore:`,
  no `docs:`, no `refactor:`. That convention belongs to commit messages alone —
  see [`commit-conventions.md`](commit-conventions.md).
* Write a plain, capitalized phrase, verb-first where it reads naturally.
* No trailing period, no issue IDs, no emoji, no branch names.
* **No session link**, in the title or anywhere in the body — see
  [`../rules/no-session-links.md`](../rules/no-session-links.md).
* Say what the change **is**, specifically. "Update files" is not a title.
* Lead with `Breaking Change` when the pull request breaks an existing contract.

Examples of the expected shape:

* `Breaking Change: replace the session token format`
* `Add new API for identity lookup`
* `Delete the legacy migration scripts`
* `Update the Docker environment to Node 22`
* `Fundamental rework of the storage layer`

## Body

Use exactly these sections, in this order:

```
# Overview

{contents}

# Added

- {contents}
- {contents}

# Modified

- {contents}
- {contents}

# Deleted

- {contents}
- {contents}

# Summary

{contents}
```

## Body rules

* **Overview** — two to five sentences: what this pull request does, and why it
  exists. Enough that a reviewer who has not read the task can follow it.
* **Added / Modified / Deleted** — one bullet per real change, each naming the
  file, module, endpoint, or surface it touches, then what changed about it. No
  vague bullets ("various fixes"), and no bullet that just restates the heading.
* **Keep the heading order.** Drop a section only when it is genuinely empty — do
  not keep a heading with "None" or "N/A" under it.
* **Summary** — the reviewer's takeaway: blast radius, risk, what to test, and
  what follows this pull request.
* **Never leave a `{contents}` placeholder**, and never ship an empty section.
* When the pull request is part of an ordered chain, add the merge-order line
  required by [`../planning/task-workflow.md`](../planning/task-workflow.md), e.g.
  `Merge order: 2 of 4 — merges after #<previous PR>`.

## Relationship to `.github/pull_request_template.md`

If this repository ever adds `.github/pull_request_template.md`, **this file stays
the source of truth for wording**. Mirror these headings into that file rather
than maintaining two different shapes.

## Merging

Opening a pull request is not permission to merge it. **Ask the user and wait for
an explicit yes** — see
[`../planning/task-workflow.md`](../planning/task-workflow.md).

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
