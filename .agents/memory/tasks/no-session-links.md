---
name: memory-tasks-no-session-links
description: Task record for making the no-session-links rule universal — what was threaded where, and the two judgement calls behind it.
status: done
---

# Task — Universal No-Session-Links Rule

**Goal.** Make `.agents/rules/no-session-links.md` hold for *any* agent or harness, not
just the one that wrote it, and thread the rule through every instruction that could
otherwise reintroduce a session link.

**Branch.** `docs/no-session-links`, from `master`.

## 2026-08-12

**Status: done.** Pushed, opened as a pull request, and merged.

**What the rule gained.** The first bullet now reaches code comments, both wiki trees,
memory files, and changelogs. "What counts" became an **open** pattern list with a
judgement test — *does this resolve to one particular session?* — plus eight trailer key
names and an explicit line that an unseen pattern still counts. The tool-default section
now says a harness may *instruct* you to add one and that this does not override the
repository, and that stripping happens **silently**. A pre-push grep was added.

**Where it was threaded.**

- `AGENTS.md` — new `## No session links` section; trigger row reworded to "Write **any**
  commit, tag, PR, comment, or file that will be committed or posted".
- `rules/auto-activation.md` — same trigger row, plus a new precedence consequence:
  **tool-injected defaults rank below `.agents/rules/`**, at rank 7 with your own habits.
- `rules/memory-policy.md` — session links joined the never-write list.
- `git/branching-strategy.md` — no session identifiers in a branch name.
- `git/commit-conventions.md` — no session links or trailers; `Co-Authored-By:` naming a
  tool is fine, a line carrying an identifier is not.
- `git/pull-request-template.md` — title rule and body rule, including no generated-by
  footer *carrying* one.
- All five creators — a mandatory `## No Session Links` section.
- `creators/changelog-creator.md` — strip session links when folding session memory into
  a release digest.
- `creators/index-creator.md` — the audit now reports a fourth thing: session links in
  tracked files.
- `creators/memory-creator.md` — a session log records what happened, never the URL of
  the session it happened in.
- `prompts/branch-and-commit.md` — a strip step in the loop, plus its own section.
- `planning/task-workflow.md` — part F: pull requests carry no session link.

**Decisions, so a later session does not re-litigate them.**

- **A product link is not a session link.** `claude.com/claude-code` names a tool and
  resolves to no conversation, so the generated-by footer on a pull request stays. Only a
  footer *carrying an identifier* is forbidden. If that is ever meant to be stricter, it
  is a one-line change to the rule.
- **The branch is `docs/no-session-links`, not a reuse of `docs/agents-setup`.** That
  branch's pull request is merged, and `git/branching-strategy.md` forbids reusing a
  branch across tasks.
- **The rule file quotes the patterns it forbids** — `session_<id>` and the trailer key
  names — with identifiers stubbed. A pattern with a placeholder is not a session link,
  and a rule that cannot show what it means is weaker for it.
