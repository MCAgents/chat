---
name: memory-state-repository-state
description: Current known state of MCAgents/chat — what exists, the stack, what is not built yet, and the next obvious step.
---

# Repository State

This file is **overwritten in place** and is always current. It is not a log — for
history, read `wiki/logs/`.

## 2026-08-12

**Stack.** Gradle multi project build, Java 25, group `io.github.mcagents`, packages
under `io.github.mcagents.chat`. Version `0.4.0`, carried by `project-version` in
`gradle.properties`. Default branch `master`. **Proprietary commercial license**,
MCAgents 2026 — not open source.

**What exists and works.**

- `api` and `common` — the chat contracts and the service layer: `ChatService`,
  `ChatSession`, `ChatSettings`, `Models`, `PendingRequests`, `SessionStore`.
- `/chat`, `/chat clear`, and `/chat reload` on SpigotMC, PaperMC, and Folia, behind
  permissions, with a documented `config.yml`.
- The **reflective bridge** to `MCAgents/core`, isolated in `MCAgentsBridge` inside
  `platforms/bukkit/core`. Nothing here compiles against `core`, and every bridge
  failure degrades to "backend unavailable" rather than crashing.
- Per-player conversations held in memory, no database.
- One request in flight per player — a second is refused, not queued.
- A configurable model, defaulting to `~deepseek/deepseek-v4-flash-latest` on
  OpenRouter.
- Twelve build modules, including the mod-side client and server split and the
  universal jar shaded by `platforms:engine`.
- A JUnit 5 harness with tests across `common`, the mod core, client, and server.

**What is not built yet.**

- **No CI pipeline.** `./gradlew test` is the only thing that runs the tests.
- **No loader toolchain.** `platforms/mods/neoforge` and `platforms/mods/fabric` hold
  no loader code; that needs ModDevGradle or Loom, which has not landed.
- **No credentials of its own, by design.** Tokens live in `core`. That is a rule, not
  a gap — see `.agents/security/token-handling.md`.

**Agent system.** The `.agents/` tree now follows the centralized architecture: all
indexes live in `.agents/index/`, agent knowledge in `.agents/wiki/`, and dynamic
state here in `.agents/memory/`. No `INDEX.md` exists anywhere in the repository.
See [`../tasks/agents-setup.md`](../tasks/agents-setup.md).

**Next obvious step.** Either a CI workflow — which would make `./gradlew test` run on
every pull request and let the "no CI" caveat come out of several files at once — or
the first real loader code, which brings its toolchain with it. Neither is started;
both are the user's call.
