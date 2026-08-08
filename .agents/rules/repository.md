---
name: repository-rules
description: Rules specific to the MCAgents/chat repository — what it is, what it depends on, and what must not be introduced into it.
---

# Repository Rules

This is a hub. It records what is true about **this** repository and links out to
the specialized rules rather than restating them.

## Current state — read this before assuming anything

`MCAgents/chat` is a **Gradle multi project build on Java 25**. The tracked files
are:

* `README.md` — project overview
* `LICENSE` — the MCAgents proprietary commercial license
* `settings.gradle`, `build.gradle`, `gradle.properties`, `gradlew`,
  `gradlew.bat`, `gradle/` — the build, the wrapper, and the version catalog
* `api/`, `common/`, `platforms/` — the ten modules, all packaged under
  `io.github.mcagents.chat`
* the `.agents/` instruction tree and the `wiki/` documentation tree

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

* **Do not fabricate architecture.** Until source lands, `wiki/` describes the
  repository as it is. No speculative architecture pages, no placeholder API
  reference, no TODO-filled documents.
* **Record commands only once they are real.** When a build system is introduced,
  add its actual build / test / run commands to
  [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md) and
  summarize them here. Never write a command you have not seen work in this repo.
* **A change carries its documentation with it** — see
  [`change-propagation.md`](change-propagation.md).
* **The default branch is `master`.** Branch from it, never commit to it directly —
  see [`../git/branching-strategy.md`](../git/branching-strategy.md).
* **The license is proprietary and fixed.** MCAgents, 2026. Do not change the
  license, the copyright holder, or the year without explicit user instruction —
  that is a legal statement, not a code change. See
  [`../compliance/licensing.md`](../compliance/licensing.md).
* **Credentials are the sharpest edge in this repository** — it exists to hold API
  tokens on machines their owner does not control. See
  [`../security/token-handling.md`](../security/token-handling.md) before touching
  anything that reads, writes, logs, or displays one.
* **Never bump the version yourself** — see [`versioning.md`](versioning.md).
* **Placement is not a judgment call.** New instructions and documents go where
  [`directories.md`](directories.md) says, including creating a new folder when
  none fits.
* **Keep `README.md` and `AGENTS.md` overviews.** Detail belongs in `wiki/`;
  rules belong in `.agents/`. If detail creeps into either overview, move it down
  rather than leaving it.

## When this file goes stale

The moment real source, a manifest, or a CI workflow lands in this repository,
this file's "Current state" section is wrong. Correcting it is part of the change
that introduces them — subject to the Discovery Protocol below, propose the
rewrite rather than silently reshaping the rules.

## Discovery Protocol

While working, if you notice an instruction worth adding — a new rule, or new
content for an existing instruction file — do NOT create or edit it yourself.
Collect the findings, and when the task is done present them to the user:

* one finding per message block, each in its own code block;
* include the proposed file path, `name`, `description`, and the full proposed
  body;
* explain in one line why it is worth adding.

Then let the user select which findings to apply. Create only the selected ones.
Never batch-apply, never apply silently.
