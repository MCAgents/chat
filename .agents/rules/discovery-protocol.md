---
name: discovery-protocol
description: Instructions are proposed, never self-applied — how to collect a finding, how to present it, and what this gate does and does not cover.
---

# Discovery Protocol

This file is the **canonical home** of the discovery protocol. Every other file that
mentions it either reproduces it deliberately (see the last section) or links here.

## The protocol

While working, if you notice an instruction worth adding — a new rule, or new
content for an existing instruction file — do NOT create or edit it yourself.
Collect the findings, and when the task is done present them to the user:

* one finding per message block, each in its own code block;
* include the proposed file path, `name`, `description`, and the full proposed
  body;
* explain in one line why it is worth adding.

Then let the user select which findings to apply. Create only the selected ones.
Never batch-apply, never apply silently.

## Scope of the gate

**It covers instruction files under `.agents/{folder}/`.** Creating one, rewriting one,
or adding a rule to an existing one all require a proposal first.

It does **not** cover:

| Tree | Treatment |
|---|---|
| `wiki/` and `.agents/wiki/` | Write freely when the facts are real and verified — see [`../creators/information-creator.md`](../creators/information-creator.md). |
| `.agents/memory/` | Write freely and automatically, no approval — see [`memory-policy.md`](memory-policy.md). |
| `.agents/index/` | Update in the same commit as whatever it indexes — see [`../creators/index-creator.md`](../creators/index-creator.md). |
| `wiki/logs/` | The log body is written freely; the **version directory** is a version claim and needs approval — see [`versioning.md`](versioning.md). |

An index row, a wiki page, and a memory entry are all descriptive. Only a rule is
normative, and only the normative tree is gated.

## Why the gate exists

The instruction set is what every future session in this repository obeys before doing
anything. An agent that edits its own standing orders mid-task changes the rules it is
being judged against, and does it in a diff nobody was reading for that. The gate keeps
one thing true: **the rules change when a person decides they change.**

Two consequences worth naming:

* **The gate holds even when the instruction is provably wrong.** A stale rule is
  reported, not quietly corrected — see
  [`change-propagation.md`](change-propagation.md). Silently rewriting a rule you
  believe is wrong is exactly the failure this prevents, because you may be the one who
  is wrong.
* **The gate is not a reason to stay silent.** Noticing something and saying nothing is
  as much a failure as self-applying it. Collect it, present it, let the user choose.

## What a finding looks like

````
Path:        .agents/{folder}/{file}.md
name:        {kebab-case-id}
description: {one line, ≤ 140 chars, routable without opening the file}

{the full proposed body, exactly as it would be written}

Worth adding because: {one line}
````

One finding per block. Do not merge two findings into one block, and do not summarize a
body you have not actually written.

## Where this block is reproduced inline, and why

Six files carry the protocol verbatim rather than linking here:
[`../../AGENTS.md`](../../AGENTS.md) and the five creators in
[`../creators/`](../creators/).

That duplication is **deliberate, not an oversight.** Those six are loaded standalone —
`AGENTS.md` is the session's first read, and a creator is often the only file open when
an instruction is about to be written. A pointer at that moment costs a hop precisely
when the rule matters most. The creators repeat the branch and commit conventions for
the same reason.

Everywhere else, this file is linked, not copied. **When the protocol changes, update
this file and those six — and nothing else.** If you find the block pasted into a
seventh file, that is drift: replace it with a link.
