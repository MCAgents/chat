# Mod Sides

How the mod half of `chat` runs on a client and on a dedicated server from one
jar, without either side loading the other's code.

This page covers the mod family only. The Bukkit family has no equivalent
problem: a plugin only ever runs on a server.

## The problem this solves

A mod runs in two physically different processes:

| | Client | Dedicated server |
|---|---|---|
| Window, keyboard, local player | Yes | No |
| Client classes on the classpath | Yes | **No** |
| Hosts a world | Sometimes (single player) | Always |
| How many conversations | One | One per player |
| Who is spending | The person typing | Someone other than the person typing |

The second row is the one that bites. Client classes are not merely unused on a
dedicated server — they are **absent from the distribution**. Touching one there
is not a logic error you can catch and log; it is a `NoClassDefFoundError` while
the class is being linked, and it takes the server down at whatever moment the
code path is first reached. A green build proves nothing about it.

The last two rows are why the split is not only about crashes. On a client, "may
I ask?" has no adversary: one player, their own game, and credentials they
configured and pay for. On a server the same question is a spending decision
made on someone else's behalf, and every message costs the owner money.

**Neither side holds a credential.** MCAgents core owns the token file on both,
along with the loading, the rotation, and the eviction — see
[`../guides/client-setup.md`](../guides/client-setup.md). What differs is who is
allowed to make core spend one.

## The shape

```
platforms/mods/core      shared, both sides, no side-specific assumptions
 ├── platforms/mods/client   client only
 ├── platforms/mods/server   server only
 ├── platforms/mods/neoforge NeoForge entry point
 └── platforms/mods/fabric   Fabric entry point
```

`client` and `server` **never depend on each other**, and nothing else names
either of them by type. That is not a convention — it is the mechanism.

## Three parts

### 1. Which side is this?

`ModEnvironment` answers, from three sources, most authoritative first:

1. **What the loader said.** `ModEnvironment.install(side)` — a loader is told
   its distribution before it loads a single mod class, so this is never a
   guess. A loader entry point calls it first, before anything else runs.
2. **The `mcagents.side` system property.** The escape hatch. Accepts `client`,
   `dedicated_server`, or `server`. An unreadable value is ignored rather than
   fatal.
3. **A classpath probe** for `net.minecraft.client.main.Main`, which exists in
   every client distribution and no dedicated server one.

The probe is last because it is a guess about someone else's jar. It fails
towards `DEDICATED_SERVER`: an unrecognised classpath reads as a server, so the
client half is never started where its classes might not exist.

### 2. Starting one half without linking the other

`ModBootstrap` holds each entry point as a **string**, not as a type:

```java
ModEnvironment.install(PhysicalSide.CLIENT);
SideEntrypoint running = ModBootstrap.start(
        ModContext.of(PhysicalSide.CLIENT, backend, logger, "openrouter"));
```

It resolves only the name matching the side actually running. A dedicated server
therefore never loads, links, or verifies a client class — not because the class
was stripped from the jar, but because nothing ever asked for it. Both halves
travel in the single universal jar and only one is ever touched.

`platforms:mods:core` depends on neither half, which is what keeps that true: if
it did, someone could write `new ClientEntrypoint()` there and the compiler
would allow it.

### 3. Failing legibly when the wiring is wrong

`SideGuard.requireClient(...)` at the top of a client-only constructor turns a
mistake into a `WrongSideException` naming the feature and both sides, instead
of a `NoClassDefFoundError` several frames later naming a Minecraft class nobody
here wrote.

This is a diagnostic, not a security boundary. The boundary is part 2.

## The markers

`@ClientOnly` and `@ServerOnly` are the loader-neutral spelling of Fabric's
`@Environment` and NeoForge's `@OnlyIn`. Neither of those can be used yet:
resolving them needs a loader toolchain that remaps Minecraft, and the mod
family deliberately compiles without one so it stays plain Java — and testable
without a game. When a loader module gains that toolchain, these are what it
maps across.

Marking a class does not make it safe. Putting it in the right module and never
linking it does.

## The asymmetry, which is deliberate

`@ClientOnly` means the class does not exist elsewhere. `@ServerOnly` cannot
mean that, because **server classes do exist on a client** — a client hosting a
single player world runs server logic. So:

* `SideGuard.requireClient` is the check that matters, and the client entry
  point asserts it.
* The server entry point asserts nothing, on purpose. Refusing to construct it
  on a client would break single player for no reason.
* `SideGuard.requireDedicatedServer` exists for the narrow case of something
  that genuinely must not run inside a client process. Reach for it rarely.

## What lives on each side

`ModChatService` holds what does not differ: send a message on a session, forget
a session, reload. Everything below differs, and therefore does not live there.

| | Client | Dedicated server |
|---|---|---|
| Class | `ClientChatService` | `ServerChatService` |
| Conversations | One, on a fixed session id | One per player, keyed on the identity the **server** authenticated |
| Who may ask | The person at the keyboard | `ServerChatAuthority`: everyone, or operators only, per the `player_allowed` setting |
| What may be asked | Anything they type | `ChatInputPolicy`: non-blank, within the length bound, control characters stripped |
| Rendering | `ClientChatLines` wraps a reply to the client's own chat width | Nothing — the server does not know a player's chat width |

Both server checks run **before** the message reaches the shared service, so a
refusal costs nothing: nothing is appended to the conversation, nothing leaves
the machine, and nothing is billed.

## Where a caller comes from

`ChatCaller` carries the identity, the name, and the permission level — and
every one of them must be read from **server-side state**. None may come from a
packet. A client can claim any name and any level it likes, and believing the
claim is the whole class of bug the record exists to make visible: a reviewer
can see at the construction site where each value came from.

The identity is also what a conversation is keyed on, which is the second reason
it cannot be client-supplied. A caller who could choose their own session id
could read someone else's conversation.

A permission level that arrives negative is clamped to `0` rather than rejected.
A level computed wrong must fail closed, and it must not crash a command handler
either.

## Testing it

The two halves are tested in their own modules, and the module graph is the
isolation: `platforms:mods:server` does not depend on `platforms:mods:client`,
so its test JVM is in exactly the position a dedicated server is in. Every
passing test there is a demonstration that the server half boots and serves with
no client class present anywhere — the claim the split makes, checked rather
than asserted. The client module's tests are the mirror image.

See [`../environments/setup.md`](../environments/setup.md) for how to run them.
