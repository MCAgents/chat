---
name: memory-index
description: Index of .agents/memory/ — ongoing task records, session logs, durable decisions, and the current state of the repository.
---

# Memory Index

**Scope:** `.agents/memory/`
**Parent:** [root-index](root-index.md)

This index owns `.agents/memory/` and only `.agents/memory/`. Any memory file
created or removed is reflected here **in the same commit** that creates or removes
it.

**Read this index at the start of every session** and load only the rows whose scope
matches the request, so you continue prior work instead of restarting it. That
standing exception to the one-branch routing rule is why the `Scope` column exists —
match on it before opening anything. Writing here needs no approval; see
`.agents/rules/memory-policy.md` and `.agents/creators/memory-creator.md`.

## State

| File | Scope | Purpose |
|---|---|---|
| [`../memory/state/repository-state.md`](../memory/state/repository-state.md) | Whole repository | What exists today, what the stack is, what is not built yet, and the next obvious step. |

## Tasks

| File | Scope | Purpose |
|---|---|---|
| [`../memory/tasks/agents-setup.md`](../memory/tasks/agents-setup.md) | `.agents/`, `wiki/` | The agent instruction, knowledge, and memory system: what was built, and the decisions behind it. |
