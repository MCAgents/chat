---
name: versioning-rules
description: Never change the project version without explicit user approval — covers every version carrier, tags, and wiki/logs version directories.
---

# Versioning Rules

## The rule

**Never change the project version on your own initiative.** Always ask the user
first, and wait for an explicit answer before touching anything below. There is no
"obvious" bump and no bump too small to ask about.

## What counts as a version change

Every version carrier that exists — or comes to exist — in this repository:

* manifest versions: `package.json`, `pyproject.toml`, `Cargo.toml`, `pom.xml`,
  `gradle.properties`, `build.gradle`, `composer.json`, `*.csproj`;
* plain carriers: a `VERSION` file, a `__version__` constant, a version constant
  in source;
* packaging and deployment manifests: chart versions, image tags, plugin or mod
  descriptors, manifest files;
* git tags and release drafts;
* **creating a new `wiki/logs/{Major}/{Minor}/{Patch}/` directory** — that
  directory *is* a version claim, so it is covered by this rule exactly like a
  manifest edit.

## How to propose a bump

When a change looks like it warrants a bump, do not make it. Propose it:

1. State the **current version** and where it is recorded.
2. State the **proposed version**.
3. State whether it is **major, minor, or patch**, and why — which change forces
   that level.
4. List **every file that would change**, including any new `wiki/logs/` directory.

Then stop and wait for an explicit answer.

## Never

* Never re-tag an existing version.
* Never rewrite a released version's log directory to change history. Corrections
  go into the next version's log.
* Never treat a user's approval of one bump as standing approval for the next.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`discovery-protocol.md`](discovery-protocol.md).
