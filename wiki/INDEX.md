---
name: wiki-index
description: Index of the wiki/ documentation tree — project information, environment setup, and the versioned change logs.
---

# Wiki Index

**Scope:** `wiki/`
**Parent:** [root-index](../INDEX.md)

This index owns `wiki/` and only `wiki/`. It must never write into `.agents/`.
Any page added to or removed from `wiki/` is reflected here **in the same commit**.

## Information

| File | Purpose |
|---|---|
| [`information/overview.md`](information/overview.md) | What this repository is, what it is for, and what it contains today. |
| [`information/modules.md`](information/modules.md) | The ten build modules, why core is not a build dependency, and what is published. |
| [`information/licensing.md`](information/licensing.md) | Who is allowed to use the software, and under which purchase terms. |

## Guides

| File | Purpose |
|---|---|
| [`guides/server-setup.md`](guides/server-setup.md) | Installing and configuring the plugin: platform, tokens, permissions, and the reload command. |
| [`guides/client-setup.md`](guides/client-setup.md) | Where the mod keeps credentials, and how the shared mcagents.json file works. |

## Environments

| File | Purpose |
|---|---|
| [`environments/setup.md`](environments/setup.md) | Getting a local working copy, and building, testing, and publishing it. |

## Child Indexes

| Index | Scope | Load when |
|---|---|---|
| [`logs/INDEX.md`](logs/INDEX.md) | Versioned change logs under `wiki/logs/` | You need release history, or must record a change against a version. |
