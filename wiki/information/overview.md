# Overview

`chat` is the in-game chat surface for **MCAgents**: a Minecraft plugin and mod
that lets a player talk to a language model without leaving the game.

It does not talk to any language model vendor itself. That belongs to
[`MCAgents/core`](https://github.com/MCAgents/core), whose `MCAgentsProvider` is
the single entry point to OpenRouter, OpenAI, DeepSeek, and Anthropic. This
repository owns everything around it: configuration, credential storage,
commands, permissions, and the conversation state the chat experience needs.

That division is deliberate. A vendor's wire format changes in one repository, and
every MCAgents project picks up the fix.

`chat` does not compile against `core` either. It reaches the loaded core plugin
through a **reflective bridge** at runtime, so this project builds on its own and
survives a core release it was never compiled against — see
[`modules.md`](modules.md).

## Current state

The `api`, `common`, `platforms/bukkit`, and `platforms/mods` modules carry the
chat surface. The four entry point modules — `spigotmc`, `papermc`, `foliamc`,
and `engine` — are wired on the server side; `neoforge` and `fabric` are
scaffolded but hold no loader code yet, since that needs a toolchain the build
does not have. What exists today is:

| Path | What it is |
|---|---|
| `README.md` | Project overview and quick start. |
| `AGENTS.md` | Entry point for agents: reading order, routing, standing conventions. |
| `INDEX.md` | Root router pointing at every index in the repository. |
| `LICENSE` | The MCAgents proprietary commercial license. |
| `settings.gradle`, `build.gradle`, `gradle.properties` | The Gradle multi project build configuration. |
| `gradlew`, `gradlew.bat`, `gradle/` | The Gradle wrapper and the version catalog. |
| `api/`, `common/`, `platforms/` | The ten build modules — see [`modules.md`](modules.md). |
| `.agents/` | The agent instruction set — rules, git conventions, planning, security, compliance, platform knowledge, prompts, creators. |
| `wiki/` | This documentation tree. |

There is no CI pipeline in the repository at this point. Build, test, and publish
commands are on [`../environments/setup.md`](../environments/setup.md).

## Licensing

**This software is not open source.** Use is limited to three categories of user
under a paid or granted license. See [`licensing.md`](licensing.md) and the
[`LICENSE`](../../LICENSE) file.

## How the repository is organized

The project separates three kinds of content, and keeps them strictly apart:

* **Instructions** — rules an agent follows — live under `.agents/`, one topic per
  file, at `.agents/{folder}/{file}.md`.
* **Documentation** — pages a human reads — live under `wiki/`, at
  `wiki/{folder}/{file-name}.md`. This page is one of them.
* **Overviews** — `README.md` and `AGENTS.md` — stay short and point elsewhere.
  Detail is never left in them; it is moved down into `wiki/`.

Navigation runs through a tree of `INDEX.md` files: the root `INDEX.md` lists the
indexes, each index lists what its own scope contains. The point is that an agent
can find the one file it needs by reading a few small tables, rather than loading
the whole repository. Start at [`INDEX.md`](../../INDEX.md).

## Change logs

Released versions are recorded under `wiki/logs/{Major}/{Minor}/{Patch}/`. The
current version is `0.3.0` — see [`../logs/INDEX.md`](../logs/INDEX.md).
