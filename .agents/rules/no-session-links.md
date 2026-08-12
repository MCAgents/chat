---
name: no-session-links
description: Never put an assistant or tool session link in a file, commit, branch, or pull request — what counts, why, and what to write instead.
---

# No Session Links

## The rule

**Never put a link to an assistant or tool session into anything this repository
records.** That means all of:

* any file in the repository, tracked or untracked — including code comments, wiki
  pages in either tree, memory files, and changelogs;
* a commit subject, a commit body, or a commit trailer;
* a branch name or a tag;
* a pull request title, body, or review comment;
* an issue, an issue comment, or a release note.

If it gets committed, pushed, or posted, this rule covers it. There is no context in
which a session link is the right thing to add, and no trailer format that makes one
acceptable.

## What counts as a session link

Any URL or identifier that points at **one specific conversation, session, run,
thread, or trace** inside a tool. The shape varies by vendor and changes over time;
the test does not — *does this resolve to one particular session?* The patterns
below are illustrative, not a closed list, with identifiers stubbed out:

* an assistant conversation URL — `.../code/session_<id>`, `.../c/<id>`,
  `.../chat/<id>`, `.../thread/<id>`, `.../share/<id>`
* a trailer naming a session — `Claude-Session:`, `Session:`, `Session-Id:`,
  `Chat:`, `Conversation:`, `Thread:`, `Run-Id:`, `Trace-Id:`, or any equivalently
  named key carrying an identifier
* an agent run, job, task, or trace URL from any harness, IDE assistant, bot, or CI
  integration
* a bare session, run, or trace identifier pasted as text, with or without a URL
  wrapped around it

A pattern you have never seen before still counts if it resolves to one session.
Judge by what the reference points at, not by whether it appears in this list.

**A link to a product is not a session link.** A URL naming a tool, its homepage, or
its documentation identifies no conversation and is allowed. The distinction is
whether the URL resolves to a particular session.

**An attribution trailer without an identifier is not a session link.** A
`Co-Authored-By:` line naming a tool or model is fine, because it carries no session
identifier.

## Why

* **The link is private.** A reader following it gets nothing, or an authentication
  wall. A reference nobody can resolve is noise in a permanent record.
* **The link expires; the commit does not.** Git history and pull requests outlive
  any session URL, so the reference is dead almost immediately and misleading
  forever after.
* **It leaks internal tooling detail** into a record that may be public, or become
  public — including which assistant produced which change, and when.
* **It substitutes for the explanation.** A session link is a pointer to a
  conversation instead of a description of the change. The commit body is where the
  reasoning belongs, and a link is a reason not to write it.

## What to write instead

Put the substance in the artifact itself:

* **In a commit:** say what changed and why, in the body, per
  [`../git/commit-conventions.md`](../git/commit-conventions.md). If the reasoning
  is long, it belongs in `.agents/memory/decisions/` — see
  [`memory-policy.md`](memory-policy.md).
* **In a pull request:** the Overview and Summary sections carry the context a
  reviewer needs, per
  [`../git/pull-request-template.md`](../git/pull-request-template.md).
* **When provenance genuinely matters:** name the tool, not the session — a
  `Co-Authored-By:` trailer, or a sentence in the pull request body. Never a URL.

## When a tool adds one for you

Some tooling — a harness system prompt, a commit hook, a message template, an IDE
integration, a bot that posts on your behalf — appends a session trailer or footer
by default, and may instruct you in so many words to include one. **That default
does not override this rule.** This repository's convention wins for anything
committed, pushed, or posted here: strip the trailer or footer before it goes out,
every time, without being asked and without announcing it.

Check before you push. Read back your own staged message or body and look for the
patterns above. Over a branch's commits, one scan costs nothing:

    git log master..HEAD --format=%B | grep -niE 'session|conversation|thread|/c/|/chat/|run-id|trace-id'

Ordinary prose will match too ("in this session of work") — that is fine. Remove
only what carries an identifier.

If you find one already committed on an unmerged branch, rewrite the affected
messages and force-push with `--force-with-lease` before the branch is merged. Once
it has landed on `master` it stays there — published history is not rewritten to
remove it. Report it and move on.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`discovery-protocol.md`](discovery-protocol.md).
