/**
 * The {@code /chat} command and its tab completion.
 *
 * <p>The handler returns immediately and never blocks a tick on a model call.
 * The reply arrives on an HTTP thread and is handed to a
 * {@link io.github.mcagents.chat.bukkit.scheduler.ChatScheduler} before it
 * touches the player.</p>
 *
 * <p>A message shown to a player never contains a credential, a stack trace, or
 * a vendor URL — only one line saying what they can do about it.</p>
 */
package io.github.mcagents.chat.bukkit.command;
