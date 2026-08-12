---
name: root-index
description: Root router — the single entry point to every index this repository can reach, local and shared, plus the shared-override table.
---

# Root Index

## Indexes

| Index | Scope | Load when |
|---|---|---|
| [`agents-index.md`](agents-index.md) | This repository's instruction set | You need a rule specific to this repository. |
| `{shared}/index/root-index.md` | The shared instruction set | You need a branching, commit, pull request, planning, directory, or creator convention. |
| [`agent-wiki-index.md`](agent-wiki-index.md) | `.agents/wiki/` agent knowledge | You need an SOP, domain guideline, or operating context written for agents. |
| [`project-wiki-index.md`](project-wiki-index.md) | `wiki/` human documentation | You need to read or write documentation a person will read. |
| [`memory-index.md`](memory-index.md) | `.agents/memory/` dynamic state | You need prior task state, session history, or must record progress. |
| [`logs-index.md`](logs-index.md) | `wiki/logs/` versioned change logs | You need release history or must record a change. |

The shared row is reached over the `lxagents-agents-base` connector as
`agents://index/root-index.md` — there is no local copy of it, and there must never
be one.

## Shared overrides

| `name` | Local file | Replaces | Why |
|---|---|---|---|

*No overrides — this repository uses the shared set unchanged.*

**This file lists indexes only** — never rules, never documentation, never links to
leaf content. Adding, removing, or renaming any index updates the first table in the
same commit; adding or dropping an override updates the second table in the same
commit. Read exactly one branch per task, plus `memory-index.md`; do not preload the
others.
