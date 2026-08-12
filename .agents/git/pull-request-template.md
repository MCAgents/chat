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
* No trailing period, no issue IDs, no emoji, no branch names, no session or run
  URLs.
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
* **No session links anywhere in the title, the body, or a review comment** — and no
  generated-by footer carrying one. Strip whatever your tooling appends before
  posting. Provenance names the tool, never the conversation. See
  [`../rules/no-session-links.md`](../rules/no-session-links.md).
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

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`../rules/discovery-protocol.md`](../rules/discovery-protocol.md).
