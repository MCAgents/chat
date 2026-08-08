# Client Setup

Where to put your API token on a client.

**The file belongs to MCAgents core, not to this mod.** Core resolves it, reads
it, rotates the keys in it, and removes a dead one. This mod holds no credentials
and never touches the file — it asks core for a reply and is told what state the
credentials are in. Everything below describes core's file, documented here
because this is the mod you are installing.

## One file for every MCAgents mod

Credentials live in a single file:

```
<your Minecraft directory>/mcagents.json
```

One file, one name, deliberately. Configure a token once and **every MCAgents mod
you install finds it** — rather than each mod keeping its own copy of the same
key, and each needing to be updated separately when it changes.

## Where that file actually is

The Minecraft directory is not at a fixed path. It differs by operating system,
and launchers and modpacks relocate it constantly — MultiMC, Prism, and the
modern Mojang launcher each give an instance its own directory.

Resolution runs in this order, most explicit first:

1. **The `MCAGENTS_DIR` environment variable**, when set. The escape hatch if the
   rest guesses wrong for your setup.
2. **The directory the mod loader reports.** A loader knows where its own
   instance lives, and it is right where guessing is not — so a modpack or a
   custom launcher works without configuration.
3. **The conventional location for your operating system:**

| OS | Path |
|---|---|
| Windows | `%APPDATA%\.minecraft` |
| macOS | `~/Library/Application Support/minecraft` |
| Linux | `~/.minecraft` |

macOS has no leading dot on `minecraft`. That is a genuine platform difference,
not a typo.

The mod logs the resolved path on startup, so if you are unsure, look there
first.

## What the file looks like

Created empty on first run, with a section per platform so you can see where a
key goes without reading documentation:

```json
{
  "openrouter": { "token": [] },
  "openai": { "token": [] },
  "deepseek": { "token": [] },
  "anthropic": { "token": [] }
}
```

Add your key to the platform you use:

```json
{
  "openrouter": { "token": ["sk-or-v1-..."] }
}
```

A single key written as a bare string instead of a list works too — the guess is
reasonable enough that rejecting it would just look like the key was wrong.

## Multiple keys

Each platform takes a list, and supplying more than one is worth doing:

* When the service **rate limits** a key, the mod moves to the next and **keeps**
  the busy one.
* When the service **rejects** a key — expired, revoked, out of credit — the mod
  moves to the next **and deletes the dead key from `mcagents.json`**.

Only a rejection deletes a key. A rate limit, a timeout, or a service outage
never does.

## Applying a change

Use MCAgents core's own command — the same subcommands as the server:

```
/agents <platform> token add <token>
/agents <platform> token remove <handle>
```

Tab completion offers a masked handle such as `#2:a3f9` for `remove`, so choosing
which key to delete never puts the key itself on screen. Or edit
`mcagents.json` by hand and run `/agents reload`. Either way the change takes
effect **without restarting the game**.

## How the file stays safe to share

Several MCAgents mods may hold this file at once, so writing it carefully
matters:

* **Writes are atomic.** A new version is written to a temporary file beside it
  and moved into place, so another mod never reads a half-written file and
  concludes you have no credentials.
* **Writes re-read first.** If another mod added a key since this one last
  loaded, that key is preserved rather than overwritten.
* **A malformed file is never overwritten.** If the JSON does not parse, the mod
  reports it, treats your tokens as unset, and **leaves the file exactly as it
  is** — it holds your keys, and rewriting it would destroy them. Fix the syntax
  and reload.
* **Permissions are narrowed to your user** where the filesystem supports it.

## Keep it private

Anyone who can read `mcagents.json` can spend your credit. It is not encrypted,
and no software can make a file both readable by your mods and unreadable by
someone with access to your machine.

Do not include it in a modpack, upload it with a crash report, or paste it into a
support channel. Nothing in the mod ever logs a token — bug reports and logs are
safe to share; this file is not.
