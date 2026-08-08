# chat

In-game chat for **MCAgents** — a Minecraft plugin and mod that lets a player talk
to a language model without leaving the game.

It does not talk to any vendor itself. [`MCAgents/core`](https://github.com/MCAgents/core)
owns that; this repository owns the experience around it — configuration,
credential storage, commands, permissions, and conversation state.

The repository is at the setup stage: it currently contains the agent instruction
set and the documentation structure, and no source code yet.

## What is here today

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
```

Only git is required — there is nothing to build or install yet. Full details:
[`wiki/environments/setup.md`](wiki/environments/setup.md).

## Documentation

Start at [`wiki/INDEX.md`](wiki/INDEX.md).

- [`wiki/information/overview.md`](wiki/information/overview.md) — what this
  repository is and how it is organized.
- [`wiki/information/licensing.md`](wiki/information/licensing.md) — who is
  allowed to use the software, and under which terms.
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
