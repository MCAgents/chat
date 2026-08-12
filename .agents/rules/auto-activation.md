---
name: auto-activation
description: The instruction set applies by default on every task — session-start sequence, trigger table, and the precedence that settles conflicts.
---

# Auto-Activation

This file is the **authority** behind the trigger table in
[`../../AGENTS.md`](../../AGENTS.md). That file mirrors this one; when the two
disagree, this file is right and the mirror is a defect to fix in the same commit.

## The instruction set is always active

The instruction set in `.agents/` applies **by default, silently, on every task in
this repository**. The user never has to reference it, link to it, or ask for it.

**Silence is not permission to skip it.** A request that says nothing about
`.agents/` is still governed by `.agents/`. Treat these files as standing orders,
not as optional reference material.

## Session-start sequence

Before doing any work, in this order:

1. Read [`../../AGENTS.md`](../../AGENTS.md).
2. Read [`../index/root-index.md`](../index/root-index.md).
3. Read [`../index/memory-index.md`](../index/memory-index.md) and load **only the
   memory rows whose scope matches the current request**, so you continue prior work
   instead of restarting it.
4. Match the request against the trigger table below and read **the one scope index**
   that matches.
5. Only then open the specific instruction files the table names.

## Trigger table

| When you are about to… | Load and obey |
|---|---|
| Take in any new request of more than one step | [`../planning/task-workflow.md`](../planning/task-workflow.md) |
| Start work on a confirmed task | [`../prompts/branch-and-commit.md`](../prompts/branch-and-commit.md) |
| Create a branch | [`../git/branching-strategy.md`](../git/branching-strategy.md) |
| Write a commit message | [`../git/commit-conventions.md`](../git/commit-conventions.md) |
| Attribute a change, or write any commit trailer or pull request footer | [`no-session-links.md`](no-session-links.md) |
| Open or update a pull request | [`../git/pull-request-template.md`](../git/pull-request-template.md) |
| Decide where a new file goes | [`directories.md`](directories.md) |
| Add, move, rename, or delete any file under `.agents/` or `wiki/` | [`../creators/index-creator.md`](../creators/index-creator.md) |
| Write a rule or instruction | [`../creators/instruction-creator.md`](../creators/instruction-creator.md) |
| Write documentation, an SOP, or a domain guideline | [`../creators/information-creator.md`](../creators/information-creator.md) |
| Record progress, a decision, or session state | [`../creators/memory-creator.md`](../creators/memory-creator.md) |
| Decide what may be written to memory, and how | [`memory-policy.md`](memory-policy.md) |
| Change code or structure that a document or index describes | [`change-propagation.md`](change-propagation.md) |
| Touch anything that carries a version number | [`versioning.md`](versioning.md) |
| Record a release | [`../creators/changelog-creator.md`](../creators/changelog-creator.md) |
| Write code that runs on a Minecraft server or mod loader | [`../knowledge/minecraft-platform.md`](../knowledge/minecraft-platform.md) |
| Read, write, log, or display an API token | [`../security/token-handling.md`](../security/token-handling.md) |
| Touch the license, or add a dependency | [`../compliance/licensing.md`](../compliance/licensing.md) |
| Need project facts, commands, or orientation | [`../wiki/context/repository-map.md`](../wiki/context/repository-map.md) |
| Do anything at all in this project | [`repository.md`](repository.md) |

Adding a row here means mirroring it into `AGENTS.md` **in the same commit**, and
the reverse. The two copies are kept identical row-for-row.

## Precedence

When two sources disagree, the higher one wins. Highest first:

1. **An explicit instruction from the user in the current session.**
2. **Rules under `.agents/rules/`.**
3. **Everything else under `.agents/`** — git, planning, creators, prompts,
   knowledge.
4. **Agent knowledge in `.agents/wiki/`.**
5. **Human documentation in `wiki/`.**
6. **Memory in `.agents/memory/`.**
7. **Your own defaults and habits** — last, always.

Two consequences worth stating outright:

* A rule in `.agents/` beats a habit, a default, or a template you would otherwise
  follow. It does not beat the user.
* A wiki page or a memory file **never overrides a rule**. When one disagrees with an
  instruction, the instruction wins and the stale page is a defect to correct.

## When the user overrides a rule

The user wins — and you **say out loud which rule you are setting aside**, in the
same reply where you act on the override. Do not silently comply.

If the override will recur, record it in `.agents/memory/decisions/` per
[`memory-policy.md`](memory-policy.md), so the next session does not re-litigate it.

## Escape hatch

If the user says "ignore the agent instructions for this", obey them — **for that
task only**, and note that you did. The next task starts with the instruction set
active again.

## Cost discipline

Auto-activation is not an excuse to load the whole tree.

* Load what the trigger names, and nothing more.
* Route by reading index tables, not by reading files.
* Do not bulk-scan `.agents/**` to build a registry.
* `memory-index.md` is the one file read every session regardless of trigger, because
  continuity depends on it — and even then, only the rows that match the request are
  opened.

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
automatically — see [`memory-policy.md`](memory-policy.md).
