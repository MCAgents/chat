# Local Setup

## Requirements

Only **git** is required today. The repository contains documentation and agent
instructions, so there is nothing to compile or install.

## Access

This repository is **proprietary**. Cloning it does not grant a license to use
the software — see [`../information/licensing.md`](../information/licensing.md).

## Get a working copy

```sh
git clone https://github.com/MCAgents/chat.git
cd chat
```

The default branch is `master`.

## Start a piece of work

Never commit to `master`. Branch from it, named `{type}/{primary-noun}`:

```sh
git checkout master
git pull origin master
git checkout -b docs/my-change
```

When the work is done:

```sh
git push -u origin docs/my-change
```

Then open one pull request for the branch. The branch types, commit message
format, pull request title and body rules, and the merge procedure are defined in
`.agents/` — start from [`../../AGENTS.md`](../../AGENTS.md).

## Build, test, and run

**There is no build system, test runner, or runnable entry point in this
repository yet.** There are no commands to document, and none should be invented
here.

When a build system is introduced, its real commands are recorded on this page in
the same change that introduces it.

## Environment variables

The repository reads no environment variables. When it does, they are documented
in `wiki/environments/env.md`, created at that time.

**Never put an API token in an environment variable committed to this
repository**, or in any example on this page. Tokens are configured at runtime —
see [`../../.agents/security/token-handling.md`](../../.agents/security/token-handling.md).
