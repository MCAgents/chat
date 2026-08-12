---
name: agents-index
description: Index of the .agents/ instruction set — rules, git conventions, planning, security, compliance, prompts, and the creator agents.
---

# Agents Index

**Scope:** `.agents/` instruction folders
**Parent:** [root-index](root-index.md)

This index is the **sole authority** that indexes the instruction tree. It does not
manage `.agents/index/`, `.agents/wiki/`, or `.agents/memory/` — those are
reserved structural folders owned by [`root-index.md`](root-index.md),
[`agent-wiki-index.md`](agent-wiki-index.md), and
[`memory-index.md`](memory-index.md). Any instruction file added to or removed
from `.agents/` is reflected in this index **in the same commit**.

## Rules

| File | Purpose |
|---|---|
| [`../rules/auto-activation.md`](../rules/auto-activation.md) | When each instruction fires without being asked, and which rule wins a conflict. |
| [`../rules/directories.md`](../rules/directories.md) | Where a new file goes across all four trees, and how to create a folder when none fits. |
| [`../rules/discovery-protocol.md`](../rules/discovery-protocol.md) | Instructions are proposed, never self-applied — how to collect a finding and what the gate covers. |
| [`../rules/memory-policy.md`](../rules/memory-policy.md) | What may be written to `.agents/memory/`, in what format, and what never goes there. |
| [`../rules/no-session-links.md`](../rules/no-session-links.md) | Never put an assistant or tool session link in a file, commit, branch, or pull request. |
| [`../rules/change-propagation.md`](../rules/change-propagation.md) | A change to code or structure updates the docs, indexes, and memory it invalidates, in the same commit. |
| [`../rules/versioning.md`](../rules/versioning.md) | Never change the project version without user approval. |
| [`../rules/repository.md`](../rules/repository.md) | What is actually true about this repository right now. |

## Git

| File | Purpose |
|---|---|
| [`../git/branching-strategy.md`](../git/branching-strategy.md) | Branch naming, one task per branch, stacked branches. |
| [`../git/commit-conventions.md`](../git/commit-conventions.md) | Conventional Commit format for commit messages. |
| [`../git/pull-request-template.md`](../git/pull-request-template.md) | Pull request title rules and the required body sections. |

## Planning

| File | Purpose |
|---|---|
| [`../planning/task-workflow.md`](../planning/task-workflow.md) | Intake, task decomposition, branch order, execution order, memory recording, merge approval. |

## Security

| File | Purpose |
|---|---|
| [`../security/token-handling.md`](../security/token-handling.md) | How API tokens are stored, rotated, evicted, and kept out of logs. |

## Compliance

| File | Purpose |
|---|---|
| [`../compliance/licensing.md`](../compliance/licensing.md) | The proprietary license, who may use the software, and which dependency licenses are allowed. |

## Knowledge

| File | Purpose |
|---|---|
| [`../knowledge/minecraft-platform.md`](../knowledge/minecraft-platform.md) | Server and mod targets, Folia threading, and where a credential lives on each side. |

## Prompts

| File | Purpose |
|---|---|
| [`../prompts/branch-and-commit.md`](../prompts/branch-and-commit.md) | Standing prompt: the full branch, commit, push, and pull request loop. |

## Creators

| File | Purpose |
|---|---|
| [`../creators/instruction-creator.md`](../creators/instruction-creator.md) | Creates and maintains instruction files under `.agents/{folder}/`. |
| [`../creators/information-creator.md`](../creators/information-creator.md) | Creates and maintains both wiki trees, routing each page by audience. |
| [`../creators/memory-creator.md`](../creators/memory-creator.md) | Creates and maintains dynamic state under `.agents/memory/`. |
| [`../creators/index-creator.md`](../creators/index-creator.md) | Owns the template, split threshold, and audit of every index in `.agents/index/`. |
| [`../creators/changelog-creator.md`](../creators/changelog-creator.md) | Creates and maintains versioned change logs under `wiki/logs/`. |
