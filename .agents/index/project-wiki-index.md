---
name: project-wiki-index
description: Index of the wiki/ documentation tree — project information, setup guides, environments, and the versioned change logs.
---

# Project Wiki Index

**Scope:** `wiki/` (except `logs/`)
**Parent:** [root-index](root-index.md)

This index owns `wiki/` and only `wiki/`. It never writes into `.agents/`. Any page
added to or removed from `wiki/` is reflected here **in the same commit**.

Pages here are **plain markdown with no frontmatter**, written for a person who has
never seen the repository. Knowledge written for agents belongs to
[`agent-wiki-index.md`](agent-wiki-index.md) instead.

## Information

| File | Purpose |
|---|---|
| [`../../wiki/information/overview.md`](../../wiki/information/overview.md) | What this repository is, what it is for, and what it contains today. |
| [`../../wiki/information/modules.md`](../../wiki/information/modules.md) | The twelve build modules, why core is not a build dependency, and what is published. |
| [`../../wiki/information/mod-sides.md`](../../wiki/information/mod-sides.md) | How the mod half runs on a client and a dedicated server from one jar, without either side loading the other's code. |
| [`../../wiki/information/licensing.md`](../../wiki/information/licensing.md) | Who is allowed to use the software, and under which purchase terms. |

## Guides

| File | Purpose |
|---|---|
| [`../../wiki/guides/server-setup.md`](../../wiki/guides/server-setup.md) | Installing and configuring the plugin: platform, tokens, permissions, and the reload command. |
| [`../../wiki/guides/client-setup.md`](../../wiki/guides/client-setup.md) | Where the mod keeps credentials, and how the shared mcagents.json file works. |

## Environments

| File | Purpose |
|---|---|
| [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md) | Getting a local working copy, and building, testing, and publishing it. |

## Child Indexes

| Index | Scope | Load when |
|---|---|---|
| [`logs-index.md`](logs-index.md) | `wiki/logs/` versioned change logs | You need release history, or must record a change against a version. |
