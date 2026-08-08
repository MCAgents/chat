# 0.1.0

Released: 2026-08-08

The first functional release. `0.0.0` was a scaffold: the modules existed and
compiled, but nothing chatted.

## Upgrading

**API tokens have moved.** They are no longer configured in this plugin. Put them
in the MCAgents core plugin instead:

```
plugins/MCAgents/config.yml
```

Then run `/mcagents reload`. Tokens left in this plugin's `config.yml` are
ignored, and `/chat` will report that no token is configured until they are
moved.

This plugin requires **MCAgents core 0.2.0 or later**.

## Added

- **`/chat <message>`** on SpigotMC, PaperMC, and Folia — one jar, with the
  server fork detected at enable time and the matching scheduler installed.
- **`/chat clear`** to forget a conversation, and **`/chat reload`** to re-read
  configuration, which also asks core to re-read its credential file.
- **Permissions.** `mcagents.chat.use` gates chatting, and only takes effect when
  `player_allow: true`; `mcagents.chat.reload` gates reloading. The default is
  operators only — every message costs the server owner money.
- **Per-player conversations**, bounded and held in memory. Nothing is written to
  a database. Turns are dropped in pairs so the history always starts with a
  player turn, and an idle conversation is forgotten.
- **A documented `config.yml`** covering platform selection, who may chat, the
  system prompt, the reply cap, and the session bounds. A bad value falls back to
  its default with a warning rather than stopping the server from booting.
- **The reflective bridge** to MCAgents core, under
  `bridge.{org}.{repo}`, so a second integration can be added beside it. This
  project never compiles against core, so the jar survives a core release it was
  not built against.
- **The mod side service**, sharing all of the above; the loader entry points
  still need their toolchain.

## Changed

- **This project no longer handles credentials at all.** The token pool, the
  storage, and the rotation moved to MCAgents core — see *Upgrading*.

## Known gaps

- `platforms/neoforge` and `platforms/fabric` carry no loader code. Registering a
  client command needs ModDevGradle or Loom, which remap Minecraft during the
  build.
- Prompt cache hit counts are not reported, because core cannot yet read them
  from the vendors.
