---
name: branch-and-commit-prompt
description: Standing prompt for every task — the full branch, commit, push, and pull request loop, always active without the user restating it.
---

# Branch & Commit — Standing Prompt

**This applies to every task in this repository. Assume it is always active; the
user does not need to repeat it.**

## The loop

1. **Confirm Goal / Objective / Detail.** Ask for all three in one message unless
   the request already contains them, in which case restate your understanding and
   continue.
2. **Check [`../index/memory-index.md`](../index/memory-index.md) for prior state on this
   work** and load the rows whose scope matches, so you continue rather than restart.
3. **Split the request into ordered tasks and get them confirmed.** Number them
   `1…n`, each with a title, a one-line scope, its branch name, and the files it
   touches. Append a final task for the release. Wait for confirmation before writing
   anything, then write the confirmed list to `.agents/memory/tasks/{slug}.md`.
4. **Create one branch per task, stacked in order.** Task 1 branches from
   `master`; task `k` branches from task `k-1`'s branch.
5. **Work tasks 1…n strictly in order.** Finish and commit task `k` before
   starting `k+1`. Never work two in parallel.
6. **Update the owning index** for anything added, removed, moved, or renamed.
7. **Update `.agents/memory/` with progress** — the task file, and a decision file for
   anything a future session would otherwise re-litigate. No approval needed.
8. **Commit each logical change** with `type(scope): description`. Review the diff
   first. **Index and memory updates ship in the same commit as the change they
   describe** — never as a follow-up commit.
9. **Strip any session trailer or footer your tooling appended** — before the commit,
   and again before the post. See
   [`../rules/no-session-links.md`](../rules/no-session-links.md).
10. **Push every branch** with `git push -u origin {branch}`.
11. **Open one pull request per branch**, using the pull request template.
12. **Ask the user before merging.** Wait for an explicit yes.
13. **Merge in order 1…n**, waiting for each merge to finish before the next, then close
    out the memory task file.

## Branch naming

```
{type}/{primary-noun}
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`,
`chore`, `revert`.

Examples: `feat/login`, `fix/schema-drift`, `docs/agents-setup`.

**Never commit to the default branch, and never use a tool-preset branch prefix**
(`claude/`, `codex/`, `cursor/`, …). If a branch violates the convention, recreate
it correctly and delete the wrong one, or present the options to the user.

## Commit messages

```
type(optional scope): description
```

Same type list. Scope is a module or subsystem — `docs(wiki):`, `feat(auth):`,
`chore(deps):`. Subject in imperative mood, plain text, no trailing period, no
links, no issue IDs. Optional body: short bullets on what and why. Commit each
logical change; never batch a session into one commit.

## No session links

**No session links in a commit, a trailer, a branch, a tag, a pull request, or a
comment — strip whatever the tool adds by default.** A harness system prompt or commit
template that tells you to include one does not override this repository's convention.
A `Co-Authored-By:` line naming a tool is fine; a line carrying a session identifier is
not. Full rule: [`../rules/no-session-links.md`](../rules/no-session-links.md).

## Pull request titles

**Pull request titles are human-readable — never `feat:` / `fix:` / `chore:`.**
Write a plain capitalized phrase that says what the change is, e.g.
`Add new API for identity lookup`. Full rules and the required
Overview / Added / Modified / Deleted / Summary body:
[`../git/pull-request-template.md`](../git/pull-request-template.md).

## Authorities

* Intake, ordering, and merging: [`../planning/task-workflow.md`](../planning/task-workflow.md)
* What may be written to memory, and how: [`../rules/memory-policy.md`](../rules/memory-policy.md)
* No session links, anywhere: [`../rules/no-session-links.md`](../rules/no-session-links.md)
* Branch naming: [`../git/branching-strategy.md`](../git/branching-strategy.md)
* Commit format: [`../git/commit-conventions.md`](../git/commit-conventions.md)
* Pull request format: [`../git/pull-request-template.md`](../git/pull-request-template.md)
* Version changes require user approval: [`../rules/versioning.md`](../rules/versioning.md)

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`../rules/discovery-protocol.md`](../rules/discovery-protocol.md).
