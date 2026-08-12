---
name: licensing-rules
description: This repository is proprietary — who may use it, what the license forbids, and which dependency licenses may be introduced.
---

# Licensing

**This repository is not open source.** It is licensed under the MCAgents
proprietary commercial license in [`../../LICENSE`](../../LICENSE). Treat that
file as a legal statement, not documentation.

## The license is fixed

Do not change the license type, the copyright holder (`MCAgents`), or the year
(`2026`) without explicit user instruction. Do not add an alternate license, a
dual-license note, or an `SPDX-License-Identifier` header that contradicts it.

An agent may **quote** the license and **explain** it. An agent may not
**rewrite** it, soften it, or add exceptions to it.

## Who may use this software

Three categories, and no others — Section 1 of the license is the authority:

1. Owners and members of the MCAgents organization.
2. Users or organizations expressly granted permission **in writing** by MCAgents.
3. Buyers holding a valid paid license, under one of three terms: one-time
   limited duration, subscription, or one-time perpetual.

Documentation and in-game messaging must not imply anyone else may use it. Do not
write a README, a wiki page, or a command response that describes this software as
free, open source, or freely redistributable.

## What the license forbids

Copying beyond personal backups, reselling or sublicensing, claiming authorship,
reverse engineering, redistributing to any third party, modifying beyond
configuration, transferring the license, and building a competing product from it.

The practical consequence for an agent working here: **do not publish this
repository's source anywhere**, and do not paste substantial portions of it into
an external service, an issue on another repository, or a public gist.

## Dependencies

Every dependency added to this project must be compatible with shipping inside a
proprietary, commercially licensed artifact.

* **Acceptable**: permissive licenses — MIT, Apache-2.0, BSD-2/3-Clause, ISC,
  and the Minecraft platform APIs this project compiles against.
* **Not acceptable without explicit user approval**: any copyleft license — GPL,
  AGPL, LGPL, SSPL, and anything with a source-disclosure or network-use clause.
  These conflict directly with Section 3 of the license.
* **Ask first** for anything unfamiliar, dual-licensed, or "source available".

When you add a dependency, record its license in the dependency's own
documentation page. When you are unsure, propose it to the user rather than
adding it.

## Third party services

The software calls language model vendors that bill their own users under their
own terms. Section 4 of the license disclaims that relationship. Do not write
documentation or code implying MCAgents provides, resells, or is responsible for
a vendor's service, quota, or billing.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
`{shared}/rules/discovery-protocol.md`.
