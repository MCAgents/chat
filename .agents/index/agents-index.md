---
name: agents-index
description: Index of this repository's own instruction folders — the rules, security, compliance, and platform knowledge that belong to MCAgents/chat alone.
---

# Agents Index

**Scope:** `.agents/` instruction folders in this repository
**Parent:** [root-index](root-index.md)

This index is the **sole authority** that indexes this repository's instruction tree.
It does not manage `.agents/index/`, `.agents/wiki/`, or `.agents/memory/` — those are
reserved structural trees owned by [`root-index.md`](root-index.md),
[`agent-wiki-index.md`](agent-wiki-index.md), and
[`memory-index.md`](memory-index.md). Any instruction file added to or removed from
`.agents/` is reflected in this index **in the same commit**.

**It lists local files only.** Branching, commits, pull requests, the task workflow,
the directory architecture, and the creators are served by the `lxagents-agents-base`
connector and are reached through `agents://index/root-index.md` — never listed here,
never copied here.

## Rules

| File | Purpose |
|---|---|
| [`../rules/repository.md`](../rules/repository.md) | What is actually true about this repository right now, and the split with `MCAgents/core` that must hold. |
| [`../rules/change-propagation.md`](../rules/change-propagation.md) | A change to code or structure updates the docs, indexes, and memory it invalidates, in the same commit. |

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
