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
| Any file added, removed, moved, or renamed in an indexed scope | The index in `.agents/index/` owning that scope — see `{shared}/creators/index-creator.md` |
| A new folder under `wiki/` or `.agents/` | The folder tables in `{shared}/rules/directories.md`, plus the owning index |
| What is live, broken, or in flight in an area | [`../memory/state/repository-state.md`](../memory/state/repository-state.md), and the `tasks/` file for the work — see `{shared}/rules/memory-policy.md` |
| Orientation an agent relies on — layout, commands, entry points, gotchas | [`../wiki/context/repository-map.md`](../wiki/context/repository-map.md) |
| Something an instruction in `.agents/` asserts | That instruction — **by proposal, see below** |

## Which commit

**The same one.** Not a follow-up commit, not the end of the branch, not a
separate pull request. Reviewers read the diff to decide whether the change is
right, and a diff that changes behavior without changing its documentation reads
as though nothing was documented in the first place.

The one thing that may lag is a `wiki/logs/` entry, which is a version claim and
belongs to `{shared}/rules/versioning.md`.

## The `.agents/` exception

`wiki/` pages, `.agents/wiki/` pages, `README.md`, every index in `.agents/index/`, and
everything under `.agents/memory/` are yours to update directly. Memory in particular
needs no approval — see `{shared}/rules/memory-policy.md`.

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

1. Does any page in either wiki tree still describe the behavior I just changed?
2. Did I add or remove a file in an indexed scope without touching its index?
3. Did I finish a meaningful unit of work without writing anything to
   `.agents/memory/`?
4. Does an instruction in `.agents/` now assert something untrue?
5. Would a reader who trusted these documents be misled by this commit?

A "yes" to 1, 2, or 3 means the commit is not ready. A "yes" to 4 means a Discovery
finding to present. A "yes" to 5 means both, and means say so out loud rather than
hoping it is noticed.

## This is a convention, not automation

Nothing enforces this rule mechanically. It holds because the auto-activation contract in
[`../../AGENTS.md`](../../AGENTS.md) puts it in front of an agent before the work starts —
see `{shared}/rules/auto-activation.md` — and because pull request review catches
what slips past. If this rule ever
needs teeth — a CI check that fails a commit touching `api/` without touching
`wiki/`, or a hook that refuses one adding a file without touching its index — that
is a real change to propose, not something this file can assert into being.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
`{shared}/rules/discovery-protocol.md`.
