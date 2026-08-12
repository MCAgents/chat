---
name: repository-rules
description: Rules specific to the MCAgents/chat repository — what it is, what it depends on, and what must not be introduced into it.
---

# Repository Rules

This is a hub. It records what is true about **this** repository and links out to
the specialized rules rather than restating them.

## How this repository resolves its instructions

This repository is a **consumer** of the LXAgents shared instruction set. The set is
served by the **`lxagents-agents-base`** MCP connector and is read over `agents://`;
there is no checkout, no submodule, and nothing to keep in sync.

* **Universal conventions are not written here.** Branching, commit format, pull
  requests, the task workflow, the directory architecture, auto-activation,
  versioning, memory policy, no-session-links, the discovery protocol, and the five
  creators all come from the connector. A file that can be read from `agents://` must
  never exist in this repository as a copy.
* **This repository carries only what is its own** — the rules in this folder,
  `security/`, `compliance/`, `knowledge/`, every index in `.agents/index/`, the two
  wiki trees, and `.agents/memory/`.
* **A local file may override a shared one by `name`, whole-file.** It is a cost, not
  a feature, and it is only legitimate once it has a row and a stated reason in
  [`../index/root-index.md`](../index/root-index.md). There are none today.
* **If the connector is unreachable**, say so plainly, work from this local set and
  the user's instructions, and do not reconstruct the missing rules from memory or
  paste them in as a workaround.
* A rule you believe should be universal is proposed against the shared set, never
  written here — see `{shared}/rules/discovery-protocol.md`.

## Current state — read this before assuming anything

`MCAgents/chat` is a **Gradle multi project build on Java 25**. The tracked files
are:

* `README.md` — project overview
* `LICENSE` — the MCAgents proprietary commercial license
* `settings.gradle`, `build.gradle`, `gradle.properties`, `gradlew`,
  `gradlew.bat`, `gradle/` — the build, the wrapper, and the version catalog
* `api/`, `common/`, `platforms/` — the twelve modules, all packaged under
  `io.github.mcagents.chat`
* `.agents/` — the instruction folders, plus three reserved structural trees:
  `.agents/index/` (every index), `.agents/wiki/` (agent knowledge), and
  `.agents/memory/` (dynamic state)
* `wiki/` — the human documentation tree, including `wiki/logs/` release history

The build and tooling files at the root — `settings.gradle`, `gradlew`,
`.gitignore`, `.gitattributes` — sit outside the documentation architecture's scope.
It governs instruction and documentation files; it does not ask you to move a build
script.

The module graph and the published coordinates are documented once, in
[`../../wiki/information/modules.md`](../../wiki/information/modules.md). Do not
restate them here.

There is **no CI pipeline in this repository yet**. Do not describe one, do not
document commands for one, and do not write instructions that assume one.
Anything you would have to guess at is not a rule — it is a fabrication.

## What the project is

`chat` is the in-game chat surface for MCAgents: a Minecraft **plugin and mod**
that lets a player talk to a language model without leaving the game.

It does not talk to any vendor itself. `MCAgents/core` owns that — its
`MCAgentsProvider` is the single entry point to OpenRouter, OpenAI, DeepSeek, and
Anthropic. This repository owns everything around it: configuration, credential
storage, commands, permissions, and whatever conversation state the chat
experience needs.

That split is a rule, not an observation:

* **Never call a vendor's HTTP API from this repository.** If something cannot be
  expressed through `core`'s API, the fix is a change to `core`, proposed to the
  user — not a second HTTP client here.
* **Never compile against `core`.** It is not a Gradle dependency and must not
  become one. `chat` reaches the loaded core plugin through a **reflective
  bridge** resolved at enable time against that plugin's own classloader, so this
  jar builds with nothing published and survives a core release it was not
  compiled against.
* **Keep the bridge in one place.** Reflection into `core` belongs in the bridge
  class in its platform module and nowhere else. Everything above it works
  against this project's own `api` contracts, so the reflection is testable,
  replaceable, and fails in exactly one place.
* **Every bridge failure degrades, never crashes.** A missing core, an
  incompatible version, or a moved method is reported once and leaves the chat
  commands saying the backend is unavailable — it never prevents the plugin from
  loading or throws in front of a player.

## Rules

* **Do not fabricate architecture.** `wiki/` describes the repository as it is. No
  speculative architecture pages, no placeholder API reference, no TODO-filled
  documents, and no page describing a module that holds no code.
* **Record commands only once they are real.** The build, test, and publish commands
  live in [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md).
  Never write a command you have not seen work in this repository.
* **A change carries its documentation with it** — see
  [`change-propagation.md`](change-propagation.md).
* **The default branch is `master`.** Branch from it, never commit to it directly —
  see `{shared}/git/branching-strategy.md`.
* **The license is proprietary and fixed.** MCAgents, 2026. Do not change the
  license, the copyright holder, or the year without explicit user instruction —
  that is a legal statement, not a code change. See
  [`../compliance/licensing.md`](../compliance/licensing.md).
* **Credentials are the sharpest edge in this repository** — it exists to hold API
  tokens on machines their owner does not control. See
  [`../security/token-handling.md`](../security/token-handling.md) before touching
  anything that reads, writes, logs, or displays one.
* **Never bump the version yourself** — see `{shared}/rules/versioning.md`. The
  version carrier is `project-version` in `gradle.properties`, currently `0.4.0`.
  `wiki/logs/` carries `0/0/0` through `0/4/0`; creating another version directory
  requires user approval, exactly like editing that property.
* **Never create an `INDEX.md`.** Every index is a file in `.agents/index/`, named
  `{scope}-index.md`. This is absolute — see `{shared}/rules/directories.md`.
* **Write memory as you work.** Finishing a meaningful unit of work without recording
  it under `.agents/memory/` is an incomplete task, and needs no approval — see
  `{shared}/rules/memory-policy.md`. A token must never reach a memory file:
  [`../security/token-handling.md`](../security/token-handling.md) governs memory
  exactly as it governs a log line.
* **Placement is not a judgment call.** New instructions and documents go where
  [`directories.md`](directories.md) says, including creating a new folder when
  none fits.
* **Keep `README.md` and `AGENTS.md` overviews.** Detail belongs in `wiki/`; rules
  belong in `.agents/{folder}/`. If detail creeps into either overview, move it down
  rather than leaving it. `AGENTS.md` carries the auto-activation contract and the
  trigger table, never a rule body.

## When this file goes stale

This file's "Current state" and "What the project is" sections describe the
repository at a moment in time. The moment a CI workflow lands, a mod loader module
gains real code, or the split with `core` is deliberately changed, they are wrong.
Correcting them is part of the change that caused it — subject to the Discovery
Protocol below, propose the rewrite rather than silently reshaping the rules. The live
version of that snapshot is
[`../memory/state/repository-state.md`](../memory/state/repository-state.md), which is
updated freely; this file changes only by proposal.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
`{shared}/rules/discovery-protocol.md`.
