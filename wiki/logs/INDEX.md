---
name: logs-index
description: Index of the versioned change logs under wiki/logs/ — every released version, newest first.
---

# Logs Index

**Scope:** `wiki/logs/`
**Parent:** [wiki-index](../INDEX.md)

Every version directory is `wiki/logs/{Major}/{Minor}/{Patch}/`, numeric segments
only — no `v` prefix, no zero padding. A version directory holds `CHANGELOG.md`
by default, and may hold `MIGRATION.md`, `BREAKING.md`, `UPGRADE.md`, or
`NOTES.md` beside it.

**Creating a new version directory is a version claim** and requires user
approval — see
[`../../.agents/rules/versioning.md`](../../.agents/rules/versioning.md).

## Versions

| Version | Summary | Documents |
|---|---|---|
| [`0/3/0/`](0/3/0/CHANGELOG.md) | The model becomes a config.yml setting, defaulting to ~deepseek/deepseek-v4-flash-latest. | `CHANGELOG.md` |
| [`0/2/0/`](0/2/0/CHANGELOG.md) | Prompt only: every token method removed, credentials managed entirely with core's /agents command. | `CHANGELOG.md` |
| [`0/1/0/`](0/1/0/CHANGELOG.md) | First functional release: /chat on all three server platforms, permissions, sessions, and credentials delegated to core. | `CHANGELOG.md` |
| [`0/0/0/`](0/0/0/CHANGELOG.md) | Initial scaffold: agent instruction system, documentation structure, and the commercial license. | `CHANGELOG.md` |
