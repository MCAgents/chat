---
name: root-index
description: Root router — the single entry point to every index in this repository. Lists indexes only, never leaf content.
---

# Root Index

| Index | Scope | Load when |
|---|---|---|
| [`agents-index.md`](agents-index.md) | `.agents/` instruction set | You need a rule, convention, skill, persona, or creator before doing work. |
| [`memory-index.md`](memory-index.md) | `.agents/memory/` dynamic state | You need prior task state, session history, or must record progress. |
| [`agent-wiki-index.md`](agent-wiki-index.md) | `.agents/wiki/` agent knowledge | You need an SOP, domain guideline, or operating context written for agents. |
| [`project-wiki-index.md`](project-wiki-index.md) | `wiki/` human documentation | You need to read or write documentation a person will read. |
| [`logs-index.md`](logs-index.md) | `wiki/logs/` versioned change logs | You need release history or must record a change. |

**This file lists indexes only** — never rules, never documentation, never links to
leaf content. Adding, removing, or renaming any file in `.agents/index/` updates
this table in the same commit. Read exactly one branch per task, plus
`memory-index.md`; do not preload the others.
