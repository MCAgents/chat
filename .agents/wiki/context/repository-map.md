---
name: agent-wiki-context-repository-map
description: Orientation before touching MCAgents/chat — what lives where, the real build and test commands, the core bridge, and the gotchas.
---

# Repository Map

Read this before touching anything in `MCAgents/chat`. It is the agent-facing
orientation page: **where things are, what to run, and what will bite you.** The
underlying facts are documented once for humans in `wiki/` — this page links there
rather than restating them.

## What this repository is

A **Gradle multi project build on Java 25**, group `io.github.mcagents`, version
`0.4.0` in `gradle.properties`. It is the in-game chat surface for MCAgents: a plugin
and mod that lets a player talk to a language model without leaving the game.

It does **not** talk to any vendor. `MCAgents/core` owns that; this repository owns
the experience around it. Full description:
[`../../../wiki/information/overview.md`](../../../wiki/information/overview.md).

## What lives where

| Path | What it is |
|---|---|
| `api/` | The chat contracts — `AgentBackend`, `AgentPrompt`, `AgentReply`, `ChatTurn`, `Role`. |
| `common/` | `ChatService`, `ChatSession`, `ChatSettings`, `Models`, `PendingRequests`, `SessionStore`. |
| `platforms/bukkit/core/` | The shared plugin: the `/chat` command, config, scheduler abstraction, and the core bridge. |
| `platforms/bukkit/core/…/bridge/` | **The only place reflection into `core` is allowed.** |
| `platforms/bukkit/{spigotmc,papermc,foliamc}/` | One entry point per server platform; Folia adds its own scheduler. |
| `platforms/mods/core/` | What every mod module shares, including the side-guard machinery. |
| `platforms/mods/{client,server}/` | The two physical sides, which the loaders never mix. |
| `platforms/mods/{neoforge,fabric}/` | Loader entry points. Plain Java modules today — no loader toolchain yet. |
| `platforms/engine/` | Shades everything into the universal `MCAgentsChat-{version}.jar`. |
| `gradle/libs.versions.toml` | Every dependency coordinate. Never declare one inline in a module. |
| `.agents/` | The instruction, index, agent-wiki, and memory trees. |
| `wiki/` | Human documentation, including `wiki/logs/` release history. |

The module graph and why `core` is not a build dependency are documented once, in
[`../../../wiki/information/modules.md`](../../../wiki/information/modules.md).

## Entry points

| Entry point | Where | Notes |
|---|---|---|
| `AbstractChatPlugin` | `platforms/bukkit/core/…/bukkit/` | Base for the three Bukkit plugins. |
| `MCAgentsBridge` | `platforms/bukkit/core/…/bridge/mcagents/core/` | The reflective bridge to the loaded core plugin. Reflection lives here and nowhere else. |
| `ChatCommand` | `platforms/bukkit/core/…/command/` | `/chat`, `/chat clear`, `/chat reload`. |
| `SideEntrypoint` / `ModBootstrap` | `platforms/mods/core/…/mods/environment/` | Mod-side bootstrap and the client/server split. |

## Commands that actually work here

```sh
./gradlew build                            # every module, plus tests
./gradlew test                             # tests only
./gradlew :common:test                     # one module
./gradlew test --tests '*ChatSettings*'    # one class
./gradlew :platforms:engine:shadowJar      # the universal jar alone
./gradlew publishToMavenLocal              # a release dry run
./gradlew clean
```

Building needs **no copy of `MCAgents/core`** — the bridge is reflective, resolved at
runtime, so nothing here compiles against it. Full setup, publishing, and environment
variables:
[`../../../wiki/environments/setup.md`](../../../wiki/environments/setup.md).

## Generated paths — leave them alone

* `build/` and `{module}/build/` — every artifact, report, and test result.
* `{module}/build/libs/` — the per-module jars and the universal
  `MCAgentsChat-{version}.jar`.
* `{module}/build/reports/tests/test/index.html` — the HTML test report.
* `.gradle/` — Gradle's own state.

None of these are tracked. Do not commit them, and do not document a path under
`build/` as though it were source.

## Gotchas

* **Never call a vendor's HTTP API from here.** If something cannot be expressed
  through `core`'s API, the fix is a change to `core`, proposed to the user — not a
  second HTTP client in this repository.
* **Never compile against `core`.** It is not a Gradle dependency and must not become
  one. Adding it would break the guarantee that this jar survives a core release it
  was never compiled against.
* **Every bridge failure degrades, never crashes.** A missing core, an incompatible
  version, or a moved method is reported once and leaves the chat commands saying the
  backend is unavailable. It never blocks plugin load and never throws in front of a
  player.
* **This repository holds no credentials of its own.** Tokens live in `core`, once,
  for the whole server or client. Read
  [`../../security/token-handling.md`](../../security/token-handling.md) before
  touching anything that reads, writes, logs, or displays one — including memory
  files.
* **There is no CI pipeline.** `./gradlew test` is the only thing that runs the tests,
  so run it before opening a pull request.
* **A green build proves nothing about Spigot compatibility.** Paper-only API fails at
  *runtime* on Spigot, when the code path is first hit — never at compile time.
* **Never assume a main thread.** On Folia there is no single main thread, and
  `Bukkit.getScheduler()` is unsupported — which is why the scheduler is abstracted
  behind `ChatScheduler`. See
  [`../../knowledge/minecraft-platform.md`](../../knowledge/minecraft-platform.md).
* **`compileOnly` does not reach tests.** A module with tests repeats `api` and
  `common` as `testImplementation` in its own build file.
* **The software is proprietary.** Cloning grants no license. See
  [`../../compliance/licensing.md`](../../compliance/licensing.md) before touching
  `LICENSE` or adding a dependency.

## Before you write a file

Placement is not a judgment call — run the algorithm in
[`../../rules/directories.md`](../../rules/directories.md). Four trees, and the tree
is not negotiable:

* rules → `.agents/{folder}/{file}.md` (gated)
* indexes → `.agents/index/{scope}-index.md` (**never an `INDEX.md`**)
* knowledge → `.agents/wiki/{type}/` for agents, `wiki/{folder}/` for humans
* state → `.agents/memory/{type}/` (ungated)

Current repository state, and what is in flight:
[`../../memory/state/repository-state.md`](../../memory/state/repository-state.md).
