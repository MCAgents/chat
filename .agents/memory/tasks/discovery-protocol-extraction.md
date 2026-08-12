---
name: memory-tasks-discovery-protocol-extraction
description: Task record for extracting the discovery protocol into its own rule — why it was duplicated, and which six copies were kept on purpose.
status: done
---

# Task — Extract the Discovery Protocol Into Its Own Rule

**Goal.** The discovery protocol had been pasted verbatim into ~20 files per repository,
including ones it had nothing to do with — `no-session-links.md`, `versioning.md`,
`minecraft-platform.md`. One rule, twenty copies, in a repository whose own
`directories.md` says facts live once.

**Branch.** `docs/discovery-protocol`, from `master`.

## 2026-08-12

**Status: done.** Pushed, opened as a pull request, and merged.

**What changed.** Added `.agents/rules/discovery-protocol.md` as the canonical home, and
replaced the pasted block in **15 files** with a short `## Changing this rule` pointer
that says the same thing in three lines, in terms of the file the reader is actually in.

**The canonical file says more than the old block did.** It now states *why* the gate
exists — an agent that edits its own standing orders changes the rules it is judged
against — and two consequences that were never written down: the gate holds even when the
instruction is provably wrong (you may be the one who is wrong), and the gate is not a
reason to stay silent. It also carries a scope table naming what is **not** gated: both
wiki trees, memory, indexes, and a log body as against a version directory.

**Decisions, so a later session does not re-litigate them.**

- **Six files keep the block verbatim, on purpose:** `AGENTS.md` and the five creators.
  `AGENTS.md` is the session's first read; a creator is often the only file open when an
  instruction is about to be written, and a pointer there costs a hop exactly when the
  rule matters most. AGENTS-SETUP §5.1h and §6.9 require both. The creators already
  repeat the branch and commit conventions for the same reason.
- **The canonical file documents that duplication explicitly**, so nobody later
  "de-duplicates" it and breaks the standalone reads. It also says a *seventh* inline
  copy is drift and should become a link.
- **`rules/` was the right tree**, not `docs/` or a new folder. The protocol is a
  repository-wide normative rule about who may write instructions, which is what
  `rules/` holds.
- **The pointer is not a bare link.** It reads "This file is an instruction, so it is not
  yours to edit on your own initiative — even when you are confident it is wrong." A bare
  `see also` would have lost the part that actually binds.

**Verification.** 60 mechanical checks pass in both repositories, including three new
ones: the canonical file exists, the protocol appears inline in exactly the seven allowed
files, and every other instruction file reaches the authority by link.
