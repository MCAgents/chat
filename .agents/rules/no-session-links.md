---
name: no-session-links
description: Never put an assistant or tool session link in a file, commit, branch, or pull request — what counts, why, and what to write instead.
---

# No Session Links

## The rule

**Never put a link to an assistant or tool session into anything this repository
records.** That means all of:

* any file in the repository, tracked or untracked;
* a commit subject, a commit body, or a commit trailer;
* a branch name or a tag;
* a pull request title, body, or review comment;
* an issue, an issue comment, or a release note.

If it gets committed, pushed, or posted, this rule covers it. There is no context in
which a session link is the right thing to add, and no trailer format that makes one
acceptable.

## What counts as a session link

Any URL or identifier that points at **one specific conversation, session, run, or
thread** inside a tool. The shape varies by vendor; the test does not. Examples of the
pattern, with the identifier stubbed out:

* `claude.ai/code/session_<id>`
* a `Claude-Session:` commit trailer, or any equivalently named trailer
* `chatgpt.com/c/<id>`, `chat.openai.com/c/<id>`
* a Cursor, Copilot, Codex, or Gemini conversation or run URL
* an internal agent-run, job, or trace URL from any tooling

**A link to a product is not a session link.** `https://claude.com/claude-code` names a
tool; it identifies no conversation and is allowed. The distinction is whether the URL
resolves to a particular session.

**An attribution trailer without a URL is not a session link.** A
`Co-Authored-By:` line naming a tool or model is fine, because it carries no session
identifier.

## Why

* **The link is private.** A reader following it gets nothing, or an authentication
  wall. A reference nobody can resolve is noise in a permanent record.
* **The link expires; the commit does not.** Git history and pull requests outlive any
  session URL, so the reference is dead almost immediately and misleading forever after.
* **It leaks internal tooling detail** into a record that may be public, or become
  public — including which assistant produced which change, and when.
* **It substitutes for the explanation.** A session link is a pointer to a
  conversation instead of a description of the change. The commit body is where the
  reasoning belongs, and a link is a reason not to write it.

## What to write instead

Put the substance in the artifact itself:

* **In a commit:** say what changed and why, in the body, per
  [`../git/commit-conventions.md`](../git/commit-conventions.md). If the reasoning is
  long, it belongs in `.agents/memory/decisions/` — see
  [`memory-policy.md`](memory-policy.md).
* **In a pull request:** the Overview and Summary sections carry the context a reviewer
  needs, per
  [`../git/pull-request-template.md`](../git/pull-request-template.md).
* **When provenance genuinely matters:** name the tool, not the session — a
  `Co-Authored-By:` trailer, or a sentence in the pull request body. Never a URL.

## When a tool adds one for you

Some tooling appends a session trailer or footer by default. **That default does not
override this rule.** Strip it before committing or posting.

If you find one already committed on an unmerged branch, rewrite the messages to remove
it and force-push with `--force-with-lease` before the branch is merged. Once it is in
the default branch's history it stays there — history is not rewritten on `master` — so
the fix is to catch it beforehand. If one has already reached `master`, do not rewrite
history to remove it: report it and leave it.

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

**Scope of this gate:** it covers instruction files under `.agents/{folder}/`.
Documentation pages under `wiki/` and `.agents/wiki/` may be written when the facts
are real and verified. Memory under `.agents/memory/` is written freely and
automatically — see [`memory-policy.md`](memory-policy.md).
