---
name: memory-tasks-agents-rewrite
description: Re-pointing the instruction set at the lxagents-agents-base connector — what was deleted, what survived, and the judgement calls behind it.
---

# Task — Rewrite the agent instructions against the shared set

**Goal.** Stop carrying a private copy of the organization's instruction set. Resolve
everything universal through the `lxagents-agents-base` MCP connector, and leave in
this repository only what is genuinely its own.

**Branch.** `docs/agents-rewrite`, from `master`.

**Mode.** B — consumer. The remote is `MCAgents/chat`, not `LXAgents/mcp-server`, and
the connector resolves.

## Why

The `.agents/` tree was built before the shared set was served over MCP, so it held a
full local copy of it. A local file overrides a shared one by frontmatter `name`,
whole-file, and then never receives a shared fix. Every one of the sixteen copies had
already drifted from the version the connector serves — silently, with nothing to
signal it. That is the failure the connector exists to prevent.

## What was audited

Ran the duplicate-instruction audit against `agents://manifest.json` (24 shared files,
server version `0.2.0`). Matched by frontmatter `name` first, since `name` is the
override key, then by sha256 of the normalized body, then by path.

- **16 stale copies** — matched a shared file by both `name` and path, body differed,
  and no override was declared anywhere. `.agents/index/root-index.md` had no override
  table at all, so none of them could have been legitimate.
- **0 exact duplicates.** Every copy had drifted, which is the point.
- **5 local-only files** kept: `rules/repository.md`, `rules/change-propagation.md`,
  `security/token-handling.md`, `compliance/licensing.md`,
  `knowledge/minecraft-platform.md`.
- Memory, both wiki trees, and every index were never candidates.

The user approved all sixteen deletions in one decision, and asked that the Minecraft
platform knowledge stay where it is.

## What was deleted

`creators/` (all five), `git/` (all three), `planning/task-workflow.md`,
`prompts/branch-and-commit.md`, and six rules — `auto-activation`, `directories`,
`discovery-protocol`, `memory-policy`, `no-session-links`, `versioning`. The four
folders emptied by that are gone; a consuming repository carries none of them.

## What was preserved rather than regenerated

- **All memory** carried across untouched. The three earlier task records still name
  files that no longer exist locally — deliberately. They are records of what happened
  then, not routing tables, and rewriting them would falsify them.
- **`change-propagation.md`** kept. It reads like a shared rule but has no shared
  counterpart, so deleting it would have lost a real rule.
- **The Minecraft trigger row** from the deleted `auto-activation` copy → the local
  trigger table in `AGENTS.md`.
- **The root build-files placement fact** from the deleted `directories` copy →
  `rules/repository.md`, phrased as what it actually is: a fact about this repository,
  not a placement rule.
- **`wiki/`** untouched. Nothing needed moving; the human tree was already correct.

Nothing was dropped as untraceable.

## Decisions

- **The trigger table is split in two.** The first table mirrors
  `agents://rules/auto-activation.md` row for row so the mirror stays mechanically
  checkable against its source; this repository's four extra triggers sit in a second
  table below it. Mixing them would have made "row-for-row" unverifiable.
- **The licensing rule body left `AGENTS.md`.** The entry point carries no rule
  bodies. The proprietary license survives there as a fact about what this repository
  is, and the rule stays in `compliance/licensing.md` where the trigger table points.
- **The override table is empty and says so.** This repository takes the shared set
  unchanged. An empty table is the statement; a missing one is what let sixteen
  undeclared copies stand.
- **Cross-set links are prose, not relative paths.** `{shared}/rules/directories.md`,
  never `../rules/directories.md` — a link across sets cannot know where the other set
  sits on disk.
- **No version bump.** `project-version` stays `0.4.0` and no new `wiki/logs/`
  directory was created. Both are version claims and need explicit approval.
- **Branch naming.** The harness proposed a `claude/`-prefixed branch with a generated
  suffix. Tool-preset prefixes are forbidden, and the user named `docs/agents-rewrite`
  directly; the harness default was set aside and the user told.

## State after this task

`.agents/` holds 17 files: 6 indexes, 5 local instructions across `rules/` (two),
`security/`, `compliance/` and `knowledge/`, 1 agent wiki page, and 5 memory files. No
`INDEX.md`. Nothing the connector serves exists here as a file.
