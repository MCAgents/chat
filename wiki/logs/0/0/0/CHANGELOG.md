# 0.0.0

Released: 2026-08-08

The initial scaffold. Establishes the agent instruction system, the documentation
structure, and the license this repository ships under. No source code yet.

## Added

- `AGENTS.md` — the agent entry point: mandatory reading order, the routing
  protocol that keeps context small, the separation-of-concerns iron rule,
  placement, the discovery protocol, and the standing conventions.
- `INDEX.md` — the root router, listing every index in the repository and nothing
  else.
- `LICENSE` — the MCAgents proprietary commercial license (MCAgents, 2026):
  three permitted categories of user, three purchase terms, and the restrictions
  on copying, reselling, claiming, reverse engineering, and redistribution.
- `.agents/rules/` — `change-propagation.md` (a change updates the docs it
  invalidates, in the same commit), `directories.md` (the placement algorithm for
  both trees), `versioning.md` (no self-service version bumps), and
  `repository.md` (what is true about this repository, and the rule that vendor
  calls belong to `MCAgents/core`).
- `.agents/git/` — `branching-strategy.md`, `commit-conventions.md`, and
  `pull-request-template.md`.
- `.agents/planning/task-workflow.md` — intake, task decomposition, stacked
  branches, in-order execution, and merge approval.
- `.agents/security/token-handling.md` — how API tokens are stored, rotated,
  evicted, and kept out of logs, including the rejected-versus-rate-limited
  distinction that decides whether a token is deleted.
- `.agents/compliance/licensing.md` — who may use the software, what the license
  forbids, and which dependency licenses may be introduced.
- `.agents/knowledge/minecraft-platform.md` — the server and mod targets, Folia's
  threading constraints, why a model call must never block a tick, and where a
  credential lives on each side.
- `.agents/prompts/branch-and-commit.md` — the standing branch, commit, push, and
  pull request loop.
- `.agents/creators/` — the instruction, information, index, and changelog
  creators, each embedding the branch and commit convention and the discovery
  protocol in full.
- `.agents/INDEX.md` and `wiki/INDEX.md` — the scope indexes.
- `wiki/information/overview.md` — what the repository is and how it is organized.
- `wiki/information/licensing.md` — the license in plain language.
- `wiki/environments/setup.md` — getting a working copy, and an explicit statement
  that no build system exists yet.
- `wiki/logs/INDEX.md` and this change log.

## Changed

- `README.md` — rewritten as an overview that routes to `wiki/`, replacing the
  single-line placeholder.
