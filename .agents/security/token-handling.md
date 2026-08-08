---
name: token-handling
description: How API tokens are stored, rotated, logged, and surfaced in this repository — the rules that keep a paid credential from leaking.
---

# Token Handling

This repository exists to hold **API tokens that cost their owner money**, on
machines the owner frequently does not control: a rented game server, a shared
host, a player's laptop. A leaked token is not a bug report, it is somebody's
bill.

Read this before touching anything that reads, writes, logs, displays, or
transmits a token.

## Never log a token

Not at any level, not truncated, not "just the first four characters", not in a
stack trace, not in a debug flag someone has to remember to turn off.

* When a message needs to identify **which** token failed, use its position in the
  pool — "token 2 of 3" — never its value.
* When a message needs to identify a **vendor**, use the vendor code.
* Before adding a `toString()` to any type holding a token, make it redact the
  token. A record's generated `toString` prints every component, so a record that
  holds one is a leak until you override it.

The same applies to anything a player or console can see: a command's output, a
chat message, an error shown in game.

## Never send a token anywhere but its own vendor

A token belongs to exactly one service. Do not put it in a header for a different
vendor, a telemetry payload, a crash report, an issue template, or any diagnostic
bundle. If a diagnostic needs to say something about credentials, it says how many
are configured and whether they work — never what they are.

## Storage

* **Server side**, tokens live in the plugin's own configuration under the
  server's data folder. That file is the owner's; do not copy it, back it up
  elsewhere, or upload it.
* **Client side**, tokens live in the shared MCAgents file under the Minecraft
  directory, so several MCAgents mods read one set of credentials rather than each
  keeping its own.
* Write that file with the **narrowest permissions the platform allows**, and
  create it fresh rather than inheriting the permissions of whatever was there.
* Never commit a real token. Every example in configuration, documentation, or a
  test uses an obvious placeholder.

## Rotation and eviction

When a vendor rejects a token as invalid or exhausted — not merely rate limited —
the pool moves to the next one and **removes the dead token from storage**, so a
credential that will never work again is not retried on every request forever.

The distinction matters and is easy to get wrong:

* **Rejected** (HTTP 401, 403, or a vendor's explicit "this key is invalid or out
  of credit") — the token is dead. Evict it.
* **Rate limited** (HTTP 429) — the token is fine and came back too fast. **Never
  evict it.** Rotating to another token is reasonable; deleting this one is not.
* **Network failure, timeout, 5xx** — nothing was learned about the token. Do not
  evict.

Evicting a healthy token on a transient failure destroys something the user paid
for and cannot be undone from inside the game. When in doubt, keep it.

When the pool empties, the state is recorded in memory — *not set* versus
*expired* — so the next request fails with a message that tells the owner which
one happened, instead of a generic error.

## Reloading

Credentials must be replaceable **without restarting the server or rejoining the
world**. Anything that caches a token holds it behind something a reload can
replace, and a reload re-reads storage rather than merging into what is already
in memory — otherwise an evicted token comes back from the cache.

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
