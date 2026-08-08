# Modules

The build is a Gradle multi project defined in `settings.gradle`, with
`rootProject.name = mcagents-chat`. It has ten modules, and every package under
them is prefixed `io.github.mcagents.chat`.

## The module graph

```
api          pure Java contracts
 └── common  pure Java implementations

platforms/bukkit           shared Bukkit code (Spigot API) + the core bridge
 ├── platforms/spigotmc    SpigotMC entry point
 ├── platforms/papermc     PaperMC entry point
 └── platforms/foliamc     Folia entry point

platforms/mods             shared mod loader code + the shared credential file
 ├── platforms/neoforge    NeoForge entry point
 └── platforms/fabric      Fabric entry point

platforms/engine           implements every module above
```

## What each module holds

| Module | Package | Purpose |
|---|---|---|
| `api` | `…chat.api` | Pure Java contracts: interfaces, records, enums, abstract types. No implementation, no platform type, and no MCAgents core type. |
| `common` | `…chat.common` | Pure Java implementations — credential pooling, conversation state, settings. Still no platform type. |
| `platforms:bukkit` | `…chat.bukkit` | What SpigotMC, PaperMC, and Folia share, compiled against the Spigot API. Also holds the reflective bridge to the core plugin. |
| `platforms:spigotmc` | `…chat.spigotmc` | Only what SpigotMC needs on top of `platforms:bukkit`. |
| `platforms:papermc` | `…chat.papermc` | Only what PaperMC needs on top of `platforms:bukkit`. |
| `platforms:foliamc` | `…chat.foliamc` | Only what Folia needs on top of `platforms:bukkit`, which in practice means the regionised schedulers. |
| `platforms:mods` | `…chat.mods` | What the mod loaders share — the same role `platforms:bukkit` plays for the server side, including the shared credential file under the Minecraft directory. |
| `platforms:neoforge` | `…chat.neoforge` | Only what NeoForge needs on top of `platforms:mods`. |
| `platforms:fabric` | `…chat.fabric` | Only what Fabric needs on top of `platforms:mods`. |
| `platforms:engine` | `…chat.engine` | The universal entry point. The one module that implements every other module, so a single artifact carries the whole chat surface. |

## MCAgents/core is not a build dependency

This is the decision that shapes the build, so it is worth stating plainly:
**`chat` never compiles against `core`.**

`core` ships as its own plugin, already installed on the server. `chat` reaches
it through a **reflective bridge** in `platforms:bukkit` that resolves
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
(`platforms:bukkit` or `platforms:mods`) as **`compileOnly`**. They compile
against those classes but never bundle them.

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
| `gson` | `com.google.code.gson:gson` | `platforms:mods`, `neoforge`, `fabric`, `engine` |
| `spigot-api` | `org.spigotmc:spigot-api` | `platforms:bukkit`, `platforms:spigotmc` |
| `paper-api` | `io.papermc.paper:paper-api` | `platforms:papermc` |
| `folia-api` | `dev.folia:folia-api` | `platforms:foliamc`, `platforms:engine` |

All of them are `compileOnly` — every target platform provides them at runtime,
so the shaded jar carries none of them.

The engine compiles against the Folia API alone. Folia is a superset of Paper,
which is a superset of Spigot, and all three declare the same
`org.spigotmc:spigot-api` capability — Gradle rejects them as a mutually
exclusive conflict if declared together, so the superset is the only one that can
be named.

`platforms:mods`, `platforms:neoforge`, and `platforms:fabric` declare **no mod
loader coordinate yet**. Resolving `net.neoforged:neoforge` or the Fabric loader
requires a toolchain (ModDevGradle, Loom) that remaps Minecraft as part of the
build. Those arrive with the first real loader code rather than being scaffolded
ahead of it.

## Java version

Every module compiles on **Java 25**, set once in the root `build.gradle` through
a Gradle toolchain. See [`../environments/setup.md`](../environments/setup.md).
