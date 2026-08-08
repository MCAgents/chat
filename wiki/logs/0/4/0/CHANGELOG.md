# 0.4.0

Released: 2026-08-08

A player can have only one reply on the way at a time.

## Upgrading

Requires **MCAgents core 0.4.0 or later**.

No configuration changes. Nothing to migrate.

## Added

- **One request in flight per player.** Sending `/chat` again while the first
  reply is still coming is refused:

  ```
  You already have a reply on the way. Wait for it before asking again.
  ```

  Nothing is sent and nothing is billed for the refused message.

  It is refused rather than queued because each request costs the server owner
  money, two replies would arrive out of order, and both would be built from the
  same conversation history — so the second would be answered as though the first
  had never been asked.
- `ChatException.Kind.ALREADY_WAITING`, and `ChatService.isWaiting` /
  `waitingCount` for anything that wants to show the state.

## Changed

- `/chat clear` now releases the wait as well as the conversation, so a player is
  never permanently stuck behind a request that somehow never returned.
- `/chat reload` clears every wait.

## Why this plugin still has no timeout

The wait is released when the request completes, whatever the outcome — a reply,
a failure, or a timeout. Nothing here times it out, because MCAgents core already
bounds every request with `request_timeout_seconds`: the future always completes,
so the release always runs.

Adding a timer here would mean two places defining how long a request may take,
and this one would have to duplicate core's to be correct. If a request ever does
hang, the fix belongs in core, not here.

## Known gaps

- `platforms/neoforge` and `platforms/fabric` still carry no loader code.
