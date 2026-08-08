# chat

In-game chat for **MCAgents** — a Minecraft plugin and mod that lets a player talk
to a language model without leaving the game.

It does not talk to any vendor itself. [`MCAgents/core`](https://github.com/MCAgents/core)
owns that; this repository owns the experience around it — configuration,
credential storage, commands, permissions, and conversation state.

```
/chat What is the fastest way to find diamonds?
[AI] Branch mine at Y -59 with a fortune pickaxe, or check deepslate cave walls.
```

## What is here today

- A Gradle multi project build on **Java 25**: `api`, `common`, and eight
  `platforms/*` modules, all under `io.github.mcagents.chat`. See
  [`wiki/information/modules.md`](wiki/information/modules.md).
- `/chat`, `/chat clear`, and `/chat reload` on SpigotMC, PaperMC, and Folia,
  with permissions and a documented `config.yml`. See
  [`wiki/guides/server-setup.md`](wiki/guides/server-setup.md).
- **No credentials of its own.** API tokens live in MCAgents core, once, for the
  whole server or client — this project never holds, stores, or rotates one. See
  [`wiki/guides/client-setup.md`](wiki/guides/client-setup.md).
- Per-player conversations held in memory, with no database.
- A configurable model, defaulting to `~deepseek/deepseek-v4-flash-latest` on
  OpenRouter.
- An agent instruction set under `.agents/` — placement rules, git conventions,
  task workflow, token handling, licensing, platform knowledge, and the creator
  agents that maintain the trees.
- A documentation tree under `wiki/`.
- An `INDEX.md` router tree so a file can be found by reading a few small tables
  instead of scanning the repository.

## Quick start

```sh
git clone https://github.com/MCAgents/chat.git
cd chat
./gradlew build
```

The wrapper downloads Gradle and a Java 25 toolchain on the first run. You do not
need a copy of `MCAgents/core` to build — it is reached through a reflective
bridge at runtime, not compiled against. Full details:
[`wiki/environments/setup.md`](wiki/environments/setup.md).

## Documentation

Start at [`wiki/INDEX.md`](wiki/INDEX.md).

- [`wiki/information/overview.md`](wiki/information/overview.md) — what this
  repository is and how it is organized.
- [`wiki/information/modules.md`](wiki/information/modules.md) — the module
  graph, why core is not a build dependency, and the published artifacts.
- [`wiki/information/licensing.md`](wiki/information/licensing.md) — who is
  allowed to use the software, and under which terms.
- [`wiki/guides/server-setup.md`](wiki/guides/server-setup.md) — installing and
  configuring the plugin.
- [`wiki/guides/client-setup.md`](wiki/guides/client-setup.md) — where the mod
  keeps credentials.
- [`wiki/environments/setup.md`](wiki/environments/setup.md) — getting a working
  copy and starting a change.
- [`AGENTS.md`](AGENTS.md) — entry point for agents working in this repository.

## License

**Not open source.** Proprietary commercial software, © 2026 MCAgents. Use is
limited to MCAgents members, parties granted written permission, and buyers of a
license. Copying, reselling, claiming, reverse engineering, and redistribution are
prohibited.

See [`LICENSE`](LICENSE) and
[`wiki/information/licensing.md`](wiki/information/licensing.md).
