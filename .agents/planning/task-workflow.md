---
name: task-workflow
description: How a request becomes tasks, stacked branches, and merged pull requests — intake, decomposition, ordering, execution, and merge approval.
---

# Task Workflow

This file is the authority for turning a request into work. It is always active;
the user does not need to invoke it.

## A. Intake — Goal, Objective, Detail

Before starting work, ask the user for three things **in one message**:

* **Goal** — the outcome they want, and why it matters.
* **Objective** — the concrete, checkable result that means the work is done.
* **Detail** — constraints, scope boundaries, affected areas, and anything that
  must not change.

Rules:

* If the request already contains all three, **do not ask again**. Restate your
  understanding in a short block and continue.
* If the user declines to answer, state the assumptions you will work under and
  get a yes **before writing any file**.

## B. Understand the request, then split it into tasks

* Read the request and decide whether it is one task or several. Split it when the
  parts touch different areas, can be reviewed independently, or must land in a
  particular order.
* **A single, self-contained request stays a single task.** Do not manufacture
  tasks to look thorough.
* Present the task list **before doing any work**, numbered `1…n`, each with:
  * a human-readable title,
  * a one-line scope,
  * the branch name,
  * the files or areas it will touch.
* **Order the list by dependency.** If task B builds on task A, A comes first. Two
  tasks that touch the same file are never independent — sequence them.
* Wait for the user to confirm the list. If they change it, re-present the
  renumbered list before starting.

## C. One branch per task, stacked in order

* Every task gets its own branch, named per
  [`../git/branching-strategy.md`](../git/branching-strategy.md).
* **Task 1 branches from the default branch (`master`). Task `k` branches from
  task `k-1`'s branch**, not from the default branch. Stacking this way is what
  keeps the merges conflict-free — each branch already contains everything before
  it.
* Never put two tasks on one branch. Never reuse a branch across tasks.
* Do not reorder or renumber tasks after the branches exist without telling the
  user first.

## D. Execute strictly in order 1…n

* Work the tasks in numeric order. **Finish, verify, and commit task `k` before
  starting task `k+1`.**
* **Never work two tasks in parallel** — that is exactly what produces the merge
  conflicts this ordering exists to prevent.
* If task `k` invalidates an assumption behind a later task, **stop**, update the
  plan, and tell the user rather than silently reworking the list.

## E. Pull requests and merging

* When all tasks are done, push every branch, then open **one pull request per
  branch** — never one pull request covering several tasks.
* Pull request `1` targets the default branch; pull request `k` targets task
  `k-1`'s branch. State the chain in each body, e.g.
  `Merge order: 2 of 4 — merges after #<previous PR>`.
* Title and body follow
  [`../git/pull-request-template.md`](../git/pull-request-template.md).
* **Ask the user before merging anything, and wait for an explicit yes.** Never
  merge on your own initiative, and never enable auto-merge without being asked.
* Once approved, merge in order `1…n`. Wait for each merge to finish before
  starting the next, and re-target the next pull request's base branch if the
  platform does not do it automatically.
* If a merge conflict appears, resolve it when the correct resolution is
  unambiguous. When resolving it would mean choosing between two behaviors, stop
  and ask, naming the conflicting files.
* **Report the final state**: which pull requests merged, in what order, and
  anything left open.

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
