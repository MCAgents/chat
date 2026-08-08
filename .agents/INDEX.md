---
name: agents-index
description: Index of the .agents/ instruction set — rules, git conventions, planning, security, compliance, prompts, and the creator agents.
---

# Agents Index

**Scope:** `.agents/`
**Parent:** [root-index](../INDEX.md)

This index is the **sole authority** for `.agents/`. Nothing outside `.agents/`
may dictate or write files inside this tree. Any file added to or removed from
`.agents/` is reflected in this index **in the same commit**.

## Rules

| File | Purpose |
|---|---|
| [`rules/change-propagation.md`](rules/change-propagation.md) | A change to code or structure updates the docs and indexes it invalidates, in the same commit. |
| [`rules/directories.md`](rules/directories.md) | Where a new file goes, and how to create a folder when none fits. |
| [`rules/versioning.md`](rules/versioning.md) | Never change the project version without user approval. |
| [`rules/repository.md`](rules/repository.md) | What is actually true about this repository right now. |

## Git

| File | Purpose |
|---|---|
| [`git/branching-strategy.md`](git/branching-strategy.md) | Branch naming, one task per branch, stacked branches. |
| [`git/commit-conventions.md`](git/commit-conventions.md) | Conventional Commit format for commit messages. |
| [`git/pull-request-template.md`](git/pull-request-template.md) | Pull request title rules and the required body sections. |

## Planning

| File | Purpose |
|---|---|
| [`planning/task-workflow.md`](planning/task-workflow.md) | Intake, task decomposition, branch order, execution order, merge approval. |

## Security

| File | Purpose |
|---|---|
| [`security/token-handling.md`](security/token-handling.md) | How API tokens are stored, rotated, evicted, and kept out of logs. |

## Compliance

| File | Purpose |
|---|---|
| [`compliance/licensing.md`](compliance/licensing.md) | The proprietary license, who may use the software, and which dependency licenses are allowed. |

## Knowledge

| File | Purpose |
|---|---|
| [`knowledge/minecraft-platform.md`](knowledge/minecraft-platform.md) | Server and mod targets, Folia threading, and where a credential lives on each side. |

## Prompts

| File | Purpose |
|---|---|
| [`prompts/branch-and-commit.md`](prompts/branch-and-commit.md) | Standing prompt: the full branch, commit, push, and pull request loop. |

## Creators

| File | Purpose |
|---|---|
| [`creators/instruction-creator.md`](creators/instruction-creator.md) | Creates and maintains instruction files under `.agents/`. |
| [`creators/information-creator.md`](creators/information-creator.md) | Creates and maintains documentation pages under `wiki/`. |
| [`creators/index-creator.md`](creators/index-creator.md) | Owns the shape, split threshold, and audit of every `INDEX.md`. |
| [`creators/changelog-creator.md`](creators/changelog-creator.md) | Creates and maintains versioned change logs under `wiki/logs/`. |
