# Modules

The build is a Gradle multi project defined in `settings.gradle`, with
`rootProject.name = mcagents-chat`. It has twelve modules, and every package
under them is prefixed `io.github.mcagents.chat`.

## The module graph

Each platform family lives in one folder, and the folder's `core` module holds
what the rest of that family shares. Nothing sits loose at the `platforms/`
level except the engine, which is not a family — it is the thing that bundles
them all.

```
api          pure Java contracts
 └── common  pure Java implementations

platforms/bukkit/core        shared Bukkit code (Spigot API) + the core bridge
 ├── platforms/bukkit/spigotmc   SpigotMC entry point
 ├── platforms/bukkit/papermc    PaperMC entry point
 └── platforms/bukkit/foliamc    Folia entry point

platforms/mods/core          shared mod code, both physical sides
 ├── platforms/mods/client       client-only half
 ├── platforms/mods/server       dedicated-server-only half
 ├── platforms/mods/neoforge     NeoForge entry point
 └── platforms/mods/fabric       Fabric entry point

platforms/engine             implements every module above
```

## What each module holds

| Module | Package | Purpose |
|---|---|---|
| `api` | `…chat.api` | Pure Java contracts: interfaces, records, enums, abstract types. No implementation, no platform type, and no MCAgents core type. |
| `common` | `…chat.common` | Pure Java implementations — credential pooling, conversation state, settings. Still no platform type. |
| `platforms:bukkit:core` | `…chat.bukkit` | What SpigotMC, PaperMC, and Folia share, compiled against the Spigot API. Also holds the reflective bridge to the core plugin. |
| `platforms:bukkit:spigotmc` | `…chat.spigotmc` | Only what SpigotMC needs on top of `platforms:bukkit:core`. |
| `platforms:bukkit:papermc` | `…chat.papermc` | Only what PaperMC needs on top of `platforms:bukkit:core`. |
| `platforms:bukkit:foliamc` | `…chat.foliamc` | Only what Folia needs on top of `platforms:bukkit:core`, which in practice means the regionised schedulers. |
| `platforms:mods:core` | `…chat.mods` | What every mod module shares — the same role `platforms:bukkit:core` plays for the server side — including the machinery that decides which physical side is running. |
| `platforms:mods:client` | `…chat.mods.client` | The client-only half: one local conversation, and replies rendered into the client's own chat. |
| `platforms:mods:server` | `…chat.mods.server` | The dedicated-server-only half: a conversation per player, and the checks that decide who may spend the owner's credit. |
| `platforms:mods:neoforge` | `…chat.neoforge` | Only what NeoForge needs on top of `platforms:mods:core`. |
| `platforms:mods:fabric` | `…chat.fabric` | Only what Fabric needs on top of `platforms:mods:core`. |
| `platforms:engine` | `…chat.engine` | The universal entry point. The one module that implements every other module, so a single artifact carries the whole chat surface. |

## Why the platform modules carry a qualified group

Both families hold a module named `core`, and Gradle identifies a project by
`group:name` — never by its path. On one shared group they would both be
`io.github.mcagents:core`, which is to say **the same module**, and Gradle would
resolve one away in favour of the other.

Nothing would fail. `./gradlew build` would stay green and the engine would
shade eleven modules and ship without the twelfth.

So the root `build.gradle` qualifies a platform module's group with its family
folder: `io.github.mcagents.bukkit:core` and `io.github.mcagents.mods:core` are
distinct modules, and the folders keep the names that read best. Only the
platform modules are affected and none of them is published, so no coordinate
anyone depends on changes.

If a third family is ever added, this is why its shared module may also be
called `core`.

## Why the mod family has two sides and the Bukkit family does not

A Bukkit plugin only ever runs on a server. A mod runs in two physically
different places — a client with a window and a player in front of it, and a
dedicated server with neither — and the classes that exist differ between them.
Client-only code reached on a dedicated server is not a logic error; it is a
`NoClassDefFoundError` that takes the server down.

So the mod family splits by **physical side** rather than by loader:
`platforms:mods:client` and `platforms:mods:server` never depend on each other,
and neither is named by type from anywhere else. The loaders — NeoForge and
Fabric — sit beside them and differ only in how they announce an entry point,
which is a much smaller difference than the one between the two sides.

How the right half is started without linking the other is on
[`mod-sides.md`](mod-sides.md).

## MCAgents/core is not a build dependency

This is the decision that shapes the build, so it is worth stating plainly:
**`chat` never compiles against `core`.**

`core` ships as its own plugin, already installed on the server. `chat` reaches
it through a **reflective bridge** in `platforms:bukkit:core` that resolves
`MCAgentsProvider` against the loaded core plugin's own classloader at enable
time, caching the resolved method handles for the plugin's lifetime.

Three things follow from that, and all three are the point:

* **The build needs nothing published.** No GitHub Packages credentials, no
  `publishToMavenLocal` in a sibling checkout, no coordinate to keep in step.
* **This jar survives a core release** it was not compiled against. A method that
  moves is a resolution failure at enable time with a clear message, not a
  `NoSuchMethodError` in front of a player.
* **A missing or incompatible core degrades rather than crashes.** The bridge
  reports it once and the chat commands say so, instead of the plugin failing to
  load.

The Bukkit manifest declares a dependency on the core plugin, which is what
guarantees it loads first and makes its classes reachable.

## Why the module dependencies are `compileOnly`

Every module below the engine declares `api`, `common`, and its family core
(`platforms:bukkit:core` or `platforms:mods:core`) as **`compileOnly`**. They
compile against those classes but never bundle them.

`platforms:engine` is the one exception: it declares every module as
`implementation` and shades them into `MCAgentsChat-{version}.jar`. That is what
keeps exactly one copy of each class in the distributed artifact.

## Published artifacts

Only `api` and `common` carry a publication:

| Module | Coordinates |
|---|---|
| `api` | `io.github.mcagents:mcagents-chat-api` |
| `common` | `io.github.mcagents:mcagents-chat-common` |

The `artifactId` is qualified with the root project name deliberately. Without
it this project would publish `io.github.mcagents:api`, colliding with `core`'s
own module in the same group.

The platform modules are distributed as plugin and mod artifacts rather than
libraries, so they are not published to a Maven repository.

## Dependency coordinates

Every coordinate lives in `gradle/libs.versions.toml`, so a version bump is one
change in one file:

| Catalog entry | Coordinate | Used by |
|---|---|---|
| `gson` | `com.google.code.gson:gson` | `platforms:mods:neoforge`, `platforms:mods:fabric`, `platforms:engine` |
| `spigot-api` | `org.spigotmc:spigot-api` | `platforms:bukkit:core`, `platforms:bukkit:spigotmc` |
| `paper-api` | `io.papermc.paper:paper-api` | `platforms:bukkit:papermc` |
| `folia-api` | `dev.folia:folia-api` | `platforms:bukkit:foliamc`, `platforms:engine` |

All of them are `compileOnly` — every target platform provides them at runtime,
so the shaded jar carries none of them.

The engine compiles against the Folia API alone. Folia is a superset of Paper,
which is a superset of Spigot, and all three declare the same
`org.spigotmc:spigot-api` capability — Gradle rejects them as a mutually
exclusive conflict if declared together, so the superset is the only one that can
be named.

No module under `platforms/mods` declares a **mod loader coordinate yet**.
Resolving `net.neoforged:neoforge` or the Fabric loader requires a toolchain
(ModDevGradle, Loom) that remaps Minecraft as part of the build. Those arrive
with the first real loader code rather than being scaffolded ahead of it.
Everything the mod family holds today is plain Java, which is also what keeps
it testable without a game running.

## Java version

Every module compiles on **Java 25**, set once in the root `build.gradle` through
a Gradle toolchain. See [`../environments/setup.md`](../environments/setup.md).
