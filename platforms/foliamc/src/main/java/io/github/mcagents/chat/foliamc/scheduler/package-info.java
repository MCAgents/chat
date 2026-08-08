/**
 * Folia's regionised scheduler implementation.
 *
 * <p>Separate from the Bukkit family's because Folia does not support the
 * legacy {@code BukkitScheduler} at all. A reply is run on the region that owns
 * the player, not on a global thread.</p>
 */
package io.github.mcagents.chat.foliamc.scheduler;
