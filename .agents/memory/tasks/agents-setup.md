---
name: memory-tasks-agents-setup
description: Task record for the agent instruction, knowledge, and memory system — what was built, the decisions behind it, and what was left out.
status: done
---

# Task — Agent Instruction, Knowledge, and Memory System

**Goal.** Bring `MCAgents/chat` onto the centralized agent architecture: one flat
index folder, a separate agent wiki, and a committed memory tree — replacing the
scattered `INDEX.md` routing the repository used before. The same migration was
applied to `MCAgents/core` in parallel, so the two repositories stay consistent.

**Branch.** `docs/agents-setup`, from `master`.

## 2026-08-12

**Status: done.** Pushed and opened as a pull request. Not merged — merging needs
explicit user approval per `.agents/planning/task-workflow.md`.

**What changed structurally.**

- Four `INDEX.md` files deleted — `INDEX.md`, `.agents/INDEX.md`, `wiki/INDEX.md`,
  `wiki/logs/INDEX.md`. `INDEX.md` is now forbidden repository-wide.
- Six indexes created in `.agents/index/`: `root-index.md`, `agents-index.md`,
  `agent-wiki-index.md`, `project-wiki-index.md`, `memory-index.md`,
  `logs-index.md`.
- `.agents/wiki/context/repository-map.md` added — the agent orientation page.
- `.agents/memory/` seeded with `state/repository-state.md` and this file.
- `.agents/rules/auto-activation.md` and `.agents/rules/memory-policy.md` added.
- `.agents/creators/memory-creator.md` added as the fifth creator.
- `AGENTS.md` gained the auto-activation contract and the trigger table; the trigger
  table is mirrored from `auto-activation.md`, which is the source of truth.

**Decisions made, so a later session does not re-litigate them.**

- **No new version directory.** `wiki/logs/` already carries `0/0/0` through
  `0/4/0`, and `gradle.properties` says `0.4.0`. Creating a version directory is a
  version claim requiring user approval, so this task created none and bumped
  nothing.
- **The trigger table carries two rows `core` does not have** — one for
  `.agents/security/token-handling.md` and one for
  `.agents/compliance/licensing.md`. This repository's sharpest edges are
  credentials and the license, and an instruction with no trigger fires only when
  someone thinks to look for it.
- **`memory-policy.md` points at `token-handling.md`, not at platform knowledge.**
  Memory is committed to git, so the never-log rules apply to a memory file exactly
  as they apply to a log line. That was the one place chat's copy of the policy had
  to diverge from core's.
- **`change-propagation.md` was kept**, not folded into the new rules. It predates
  this architecture and carries a real rule the baseline does not — so it was
  extended with rows for memory and the agent wiki instead of being replaced.
- **`.agents/wiki/sop/` and `.agents/wiki/domain/` were not created.** Empty folders
  are forbidden, and nothing concrete needed either type yet. They are listed as
  reserved in `.agents/rules/directories.md`.
- **The branch is `docs/agents-setup`**, not the harness-suggested
  `docs/agents-setup-8cn24s`. The suffix was a generated token with no meaning and
  broke the `{type}/{primary-noun}` convention in
  `.agents/git/branching-strategy.md`.

**Stale facts corrected along the way.** `rules/repository.md` still described a
repository with no build system and no source — "when a build system is introduced",
"until source lands", "the ten modules". All three were wrong: the build, the source,
and twelve modules exist. Those sentences were in sections this task was rewriting
anyway.

**What was left unchanged, deliberately.** `.agents/git/branching-strategy.md`,
`commit-conventions.md`, and `pull-request-template.md` already matched the new
specification; they took only the Discovery Protocol's new memory-scope paragraph.
`.agents/security/token-handling.md` and `.agents/compliance/licensing.md` took the
same one-paragraph change and nothing else. `LICENSE` was not touched.
