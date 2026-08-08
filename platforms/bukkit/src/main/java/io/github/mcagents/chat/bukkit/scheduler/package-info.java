/**
 * The scheduler abstraction that keeps replies on a legal thread.
 *
 * <p>A language model call completes on whatever thread the HTTP client
 * finished on. Touching a player from there is unsafe, and on Folia there is no
 * single main thread to hop back to — the correct target is the scheduler owning
 * that player's region. This package hides that difference so the command
 * handler does not have to know which server it is running on.</p>
 */
package io.github.mcagents.chat.bukkit.scheduler;
