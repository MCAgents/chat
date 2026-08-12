---
name: minecraft-platform-knowledge
description: Platform targets for chat — the server and mod families, Folia's threading constraints, and where a credential lives on each side.
---

# Minecraft Platform

`chat` ships to both sides of the ecosystem: a **server plugin** and a **client
mod**. They share logic but differ in almost everything about their environment,
and the differences are what this file exists to record.

## Supported platforms

Server side, three Bukkit platforms:

| Platform | What it is | API roots |
|---|---|---|
| **Spigot** | A CraftBukkit fork implementing the Bukkit API. | `org.bukkit.*`, `org.spigotmc.*` |
| **Paper** | A fork of Spigot. Superset of the Spigot API, plus its own. | Spigot roots, plus `io.papermc.paper.*` |
| **Folia** | A PaperMC fork of Paper with regionised multithreading. | Paper roots, plus the regionised schedulers |

Client side, two mod loaders:

| Loader | What it is |
|---|---|
| **NeoForge** | A Forge fork, and the mainline Forge-lineage loader. |
| **Fabric** | A lightweight loader with its own API. |

**Sponge, legacy Forge, and the proxies (Velocity, BungeeCord) are out of
scope.** Do not write code, documentation, or instructions targeting them until
that decision changes.

## The server hierarchy — compatibility runs one way

```
Bukkit API
 └── Spigot        implements Bukkit, adds the Spigot API
      └── Paper    implements Spigot, adds the Paper API
           └── Folia   implements Paper, changes the threading model
```

* Code written against the **Bukkit/Spigot API runs on all three**.
* Code touching **Paper-only API does not run on Spigot**. This fails at runtime —
  a missing class or method when the code path is first hit — **not** at compile
  time. A green build proves nothing about Spigot compatibility.
* **Folia runs Paper plugins only if they respect its threading model** and opt in
  explicitly.

**Rule:** target the lowest platform that provides what you need. Better still,
put the logic where it names no platform at all.

## Folia — the constraint that actually matters

Folia replaces "one main server thread" with **regionised multithreading**: the
world is split into independent regions, each ticked by its own thread.

What breaks:

* **`Bukkit.getScheduler()`** — the `BukkitScheduler` is unsupported on Folia.
  Treat any use of it as Folia-incompatible.
* **"I am on the main thread"** — there is no single main thread. Do not use
  `Bukkit.isPrimaryThread()` as proof that access is safe.
* **Static mutable state and shared collections** — several region threads may
  reach them at once. Unsynchronized shared state is a data race, not a style
  preference.
* **Cross-region access** — touching an entity, chunk, or block owned by another
  region from the wrong thread is unsafe. Schedule the work onto the owning region.

Use the regionised schedulers:

| Scheduler | Obtain with | Use for |
|---|---|---|
| Global region | `Bukkit.getGlobalRegionScheduler()` | Server-wide state — time, weather, global config. |
| Region | `Bukkit.getRegionScheduler()` | Work tied to a specific location or chunk. |
| Entity | `entity.getScheduler()` | Work that follows an entity between regions. |
| Async | `Bukkit.getAsyncScheduler()` | Off-thread work that touches no game state. |

**Opting in:** a plugin loads on Folia only when it declares
`folia-supported: true` in its manifest. That flag is a **claim that the code
honours the rules above** — not a switch that makes it true. Do not set it before
the code earns it.

## Why this matters here specifically

Every language model call is a network call taking seconds, not milliseconds. That
makes this project's core discipline non-negotiable:

* **Never block a tick on a model call.** `core` returns a
  `CompletableFuture` from every remote method; keep it that way all the way out
  to the command handler.
* **Never touch the game from a completion callback.** Nothing guarantees which
  thread a future finishes on. Sending a chat message, editing an entity, or
  reading a block from a callback must hop back onto the right scheduler first —
  the owning region's on Folia, the main thread on Spigot and Paper.
* A player can trigger a call from anywhere in the world, so on Folia the "right
  scheduler" is rarely the global one.

## Where a credential lives

The two sides differ, and the difference is not cosmetic:

| Side | Location | Why |
|---|---|---|
| Server | The plugin's own config under the server data folder | The server owner configures it once, for everyone on the server. |
| Client | A **shared** MCAgents file under the Minecraft directory | Several MCAgents mods must read one set of credentials rather than each keeping its own copy. |

The Minecraft directory is not at a fixed path — it varies by operating system and
is frequently relocated by a launcher or a modpack. Resolve it; never hardcode it.
See [`../security/token-handling.md`](../security/token-handling.md) for what may
and may not be done with a token once found.

## Not yet decided

Do not fill these in by guessing:

* The **Minecraft versions** `chat` targets. Until they are decided, write no
  version-specific code, no compatibility shims, and no documentation claiming a
  supported range.
* The **build system and API dependency coordinates** — none exist in this
  repository yet. See [`../rules/repository.md`](../rules/repository.md).

Record each one here in the same change that introduces it.

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

**Scope of this gate:** it covers instruction files under `.agents/{folder}/`.
Documentation pages under `wiki/` and `.agents/wiki/` may be written when the facts
are real and verified. Memory under `.agents/memory/` is written freely and
automatically — see [`../rules/memory-policy.md`](../rules/memory-policy.md).
