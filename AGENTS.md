---
name: agents-entry-point
description: Entry point for agents working in MCAgents/chat — reading order, routing protocol, placement, and the standing conventions.
---

# AGENTS

`MCAgents/chat` is the in-game chat surface for MCAgents: a Minecraft plugin and
mod that lets a player talk to a language model from inside the game. It builds
on [`MCAgents/core`](https://github.com/MCAgents/core), which owns the provider
API; this repository owns the chat experience around it. See
[`wiki/information/overview.md`](wiki/information/overview.md).

This file is an **overview**. It contains no rules of its own; it tells you where
the rules are.

## Reading order (mandatory)

1. Read `AGENTS.md`.
2. Read the root [`INDEX.md`](INDEX.md) — **and nothing else at this stage**.
3. From its routing table, pick the **one** index whose scope matches the task,
   and read that index.
4. If that index delegates to a child index, follow **the one branch** that
   matches.
5. Only then open the specific file(s) you need.

## Routing protocol (context discipline)

Route by reading index tables, not by reading files.

* Do **NOT** load every `INDEX.md`.
* Do **NOT** bulk-scan `.agents/**` to build a registry.
* Do **NOT** read an instruction body until that instruction has been selected.

Each index row's purpose text is what you route on; the file body is what you load
after choosing. This is the whole point of the index tree — never defeat it by
reading ahead.

## Iron rule (separation of concerns)

* `AGENTS.md` and `README.md` are **overviews** and must never carry detailed rules
  or documentation.
* The root `INDEX.md` is a **router only**. It lists other indexes. It must never
  contain rules, documentation, prose, or direct links to leaf content, and it
  must never be used to dictate or write files inside any subtree.
* `.agents/INDEX.md` is the **sole authority** that indexes and manages
  `.agents/`; nothing outside `.agents/` may dictate or write files inside it.
* `wiki/INDEX.md` indexes `wiki/` and must never write into `.agents/`.

## Placement

* New instructions go to `.agents/{folder}/{file}.md`, per
  [`.agents/rules/directories.md`](.agents/rules/directories.md).
* New documentation goes to `wiki/{folder}/{file-name}.md`.
* New or updated indexes follow
  [`.agents/creators/index-creator.md`](.agents/creators/index-creator.md).

## Discovery Protocol

> While working, if you find an instruction worth adding — a new rule, or content
> that belongs in an existing instruction file — you must NOT create or edit it on
> your own. Present each finding to the user separately, each in its own code
> block, including the proposed file path, `name`, `description`, and full body.
> Let the user select which ones to apply. Create only what the user selects.

## Standing conventions

Always active. The user never has to restate them.

* **A change carries its documentation with it.** When code or project structure
  changes, update the files that describe it — `wiki/` pages and the owning
  `INDEX.md` — in the same commit, and propose the `.agents/` instructions the
  change made wrong rather than rewriting them yourself.
  [`.agents/rules/change-propagation.md`](.agents/rules/change-propagation.md).
* Task intake, decomposition, branch order, and merge order —
  [`.agents/planning/task-workflow.md`](.agents/planning/task-workflow.md).
* Branch naming —
  [`.agents/git/branching-strategy.md`](.agents/git/branching-strategy.md).
* Commit messages —
  [`.agents/git/commit-conventions.md`](.agents/git/commit-conventions.md).
* Pull request title and body —
  [`.agents/git/pull-request-template.md`](.agents/git/pull-request-template.md).
* The standing prompt that ties them together —
  [`.agents/prompts/branch-and-commit.md`](.agents/prompts/branch-and-commit.md).

## Licensing

**This repository is not open source.** It is under a proprietary commercial
license — see [`LICENSE`](LICENSE) and
[`.agents/compliance/licensing.md`](.agents/compliance/licensing.md). Never
change the license, relicense any file, or add a dependency whose terms conflict
with it.

## Version rule

**Never change the project version without explicit user approval** — see
[`.agents/rules/versioning.md`](.agents/rules/versioning.md).
