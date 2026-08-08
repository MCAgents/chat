# 0.2.0

Released: 2026-08-08

This plugin becomes prompt-only. It no longer has any way to see, set, or reload
an API credential.

## Upgrading

Requires **MCAgents core 0.3.0 or later**.

Manage API tokens with core's command:

```
/agents                                   credential status for every platform
/agents <platform> token add <token>      store a token
/agents <platform> token remove <handle>  revoke one
/agents reload                            re-read the credential file
```

`/chat reload` now reloads **this plugin's settings only**.

## Removed

- `AgentBackend.tokenState` and `AgentBackend.reloadTokens`. The contract is a
  prompt in and a reply out.
- The `NO_TOKEN`, `TOKENS_EXPIRED`, `TOKEN_REJECTED`, and `RATE_LIMITED` failure
  kinds, leaving the two a prompt-only consumer can act on differently.

## Changed

- A player sees one message for every service failure — "the AI service could not
  answer that" — because this plugin cannot distinguish a missing credential from
  a rate limited one and a player could do nothing differently about either. The
  detail is not lost: core's own message goes to the console, and `/agents`
  reports credential status.
- `/chat reload` no longer touches credentials.

## Known gaps

- `platforms/neoforge` and `platforms/fabric` carry no loader code. Registering a
  client command needs ModDevGradle or Loom, which remap Minecraft during the
  build. Fabric additionally has no yarn mappings published for any Minecraft
  26.x version, so it cannot target the same version as the server side today.
