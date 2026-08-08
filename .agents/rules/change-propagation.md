---
name: change-propagation
description: A change to code or structure updates the docs and indexes it invalidates, in the same commit, and proposes the stale instructions.
---

# Change Propagation

## The rule

**When code or project structure changes, every file that describes it changes
too.**

A commit that leaves a document describing the old state is not finished. It is a
commit plus a defect, and the defect is worse than a stale comment: the next agent
routes on these documents and will act on what they say.

This is not a cleanup pass to run later. It is part of the change.

## What "describes it" means

Work outward from what you touched. Each of these is only in scope when the change
actually invalidated it — an untouched page is not a chore.

| You changed | Then also update |
|---|---|
| A module, package, or the build | The `wiki/information/` page describing the module layout |
| A build, test, run, or publish command | [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md) |
| A public type, method, or its behavior | The `wiki/reference/` page covering it |
| How a consumer is meant to use something | The `wiki/guides/` page covering it |
| What the project is or does | [`../../wiki/information/overview.md`](../../wiki/information/overview.md), and `README.md` if the summary there is now wrong |
| Any file added, removed, moved, or renamed in an indexed scope | The `INDEX.md` owning that scope — see [`../creators/index-creator.md`](../creators/index-creator.md) |
| A new folder under `wiki/` or `.agents/` | The folder table in [`directories.md`](directories.md), plus the owning index |
| Something an instruction in `.agents/` asserts | That instruction — **by proposal, see below** |

## Which commit

**The same one.** Not a follow-up commit, not the end of the branch, not a
separate pull request. Reviewers read the diff to decide whether the change is
right, and a diff that changes behavior without changing its documentation reads
as though nothing was documented in the first place.

The one thing that may lag is a `wiki/logs/` entry, which is a version claim and
belongs to [`versioning.md`](versioning.md).

## The `.agents/` exception

`wiki/` pages, `README.md`, and every `INDEX.md` are yours to update directly.

**`.agents/` instructions are not.** When a change makes an instruction wrong, do
not rewrite it — that is exactly what the Discovery Protocol exists to prevent, and
it applies no less when the instruction is provably stale. Collect the finding, and
present the proposed rewrite to the user when the task is done: the file path, the
`name`, the `description`, and the full proposed body, one per code block. Create
only what the user selects.

So: update the documentation yourself, propose the instruction changes, and say in
the pull request body which instructions you left stale and why.

## Before you commit

Ask, and answer honestly:

1. Does any page still describe the behavior I just changed?
2. Did I add or remove a file in an indexed scope without touching its index?
3. Does an instruction in `.agents/` now assert something untrue?
4. Would a reader who trusted these documents be misled by this commit?

A "yes" to 1 or 2 means the commit is not ready. A "yes" to 3 means a Discovery
finding to present. A "yes" to 4 means both, and means say so out loud rather than
hoping it is noticed.

## This is a convention, not automation

Nothing enforces this rule mechanically. It holds because the reading order in
[`../../AGENTS.md`](../../AGENTS.md) puts it in front of an agent before the work
starts, and because pull request review catches what slips past. If this rule ever
needs teeth — a CI check that fails a commit touching `api/` without touching
`wiki/`, or a hook that refuses one adding a file without touching its index — that
is a real change to propose, not something this file can assert into being.

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
