# Server Setup

How to get `/chat` working on a SpigotMC, PaperMC, or Folia server.

## Install

Two plugins, both required:

1. **MCAgents core** — does the talking to the AI service.
2. **MCAgents Chat** — `MCAgentsChat-{version}.jar`, the chat surface.

Drop both in `plugins/` and start the server. Chat declares `depend: [MCAgents]`,
so core always loads first.

One jar covers all three server platforms. It detects Folia at enable time and
installs the regionised scheduler; on SpigotMC and PaperMC it uses the main
thread. The log line at startup says which it picked.

If core is missing or too old, the plugin still loads — `/chat` replies that the
backend is unavailable, and the console says why. Nothing else on the server is
affected.

## Configure

`plugins/MCAgentsChat/config.yml` is written on first start.

### Pick a platform

```yaml
platform: "openrouter"
```

One of `openrouter`, `openai`, `deepseek`, or `anthropic`. Exactly one is active;
the other token sections are ignored.

The **model is fixed in code** per platform and is not configurable. A mistyped
model produces a rejection that looks exactly like a bad key, and telling those
apart from a server log is miserable.

### Add tokens

```yaml
openrouter:
  token:
    - "sk-or-v1-..."
    - "sk-or-v1-..."
```

Each platform takes a **list**. More than one is worth having:

* When the service **rate limits** a key, the plugin moves to the next and
  **keeps** the busy one.
* When the service **rejects** a key — expired, revoked, out of credit — the
  plugin moves to the next **and deletes the dead key from `config.yml`**, so it
  is not retried on every request forever.

Only a rejection deletes a key. A rate limit, a timeout, or a service outage
never does.

When every key has been rejected, `/chat` says so specifically — "every token was
rejected" rather than "no token configured" — because the two call for different
fixes.

> Keep `config.yml` private. Anyone who can read it can spend your credit.

### Decide who may chat

```yaml
player_allow: false
```

`false` (the default) means **operators only**, whatever permissions say.
`true` means any player holding `mcagents.chat.use` may use it.

This is a spending control before it is a permission — every message costs you
money at your provider. Turn it on deliberately.

### Tune the conversation

```yaml
system_prompt: "You are a helpful assistant inside a Minecraft server. Answer briefly - a few sentences at most, in plain text with no markdown."
max_tokens: -1
session:
  max_turns: 20
  idle_timeout_minutes: 30
```

| Key | What it does |
|---|---|
| `system_prompt` | Sent with every message. Keep it short — every word is paid for on every message by every player. Keep it **stable**, too: providers charge less for a repeated prompt prefix, and changing this line means paying full price again. |
| `max_tokens` | Caps one reply. `-1` leaves it to the platform. |
| `session.max_turns` | How many messages a conversation remembers. The whole conversation is resent every time, so a bigger number means a longer, costlier prompt. Minimum 2. |
| `session.idle_timeout_minutes` | How long a player can be silent before their conversation is forgotten. Minimum 1. |

A value that is out of range falls back to its default **with a warning** rather
than stopping the server from booting.

Conversations live in memory only. Nothing is written to a database, and
everything is forgotten when the server stops.

## Commands

| Command | Who | What it does |
|---|---|---|
| `/chat <message>` | Operators, or players when `player_allow: true` | Ask the AI. |
| `/chat clear` | Any player | Forget your own conversation and start fresh. |
| `/chat reload` | `mcagents.chat.reload`, or op | Re-read `config.yml`. |

`/chat reload` is what makes a new token live **without restarting the server or
rejoining the world**. It re-reads the file from disk, replaces the credentials,
drops every conversation, and reports what it now sees:

```
MCAgents chat reloaded.
  platform: openrouter
  backend: openai/gpt-4o-mini via MCAgents core (reflective bridge)
  tokens: ready
```

Conversations are dropped on reload on purpose: settings that shape a prompt may
have changed, and continuing a conversation half-built under the old ones
produces replies nobody can explain.

## Permissions

| Permission | Default | Grants |
|---|---|---|
| `mcagents.chat.use` | `false` | Using `/chat <message>` — **only when `player_allow: true`**. |
| `mcagents.chat.reload` | `op` | Running `/chat reload`. |

`mcagents.chat.reload` is deliberately separate from `mcagents.chat.use`:
reloading changes which credentials are live and drops every conversation on the
server, so it stays administrative even where chatting is not.

## What a player sees when something is wrong

Never a stack trace, a token, or a vendor URL — one line saying what can be done:

| Situation | Message |
|---|---|
| Core missing or incompatible | AI chat is unavailable — the MCAgents core plugin is missing or incompatible. |
| No token configured | AI chat has no API token configured. An administrator must add one to config.yml. |
| Every token rejected | Every configured API token has been rejected. An administrator must add a working one. |
| Rate limited | The AI service is rate limiting requests. Try again in a moment. |
| Anything else | The AI service could not answer that. Try again shortly. |

The console gets the detail, logged once rather than on every message.
