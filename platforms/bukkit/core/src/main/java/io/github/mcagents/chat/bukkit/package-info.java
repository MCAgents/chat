/**
 * Shared Bukkit platform code.
 *
 * <p>Everything the SpigotMC, PaperMC, and Folia entry points have in common
 * lives here, compiled against the Spigot API — the lowest platform of the
 * three, so this code runs on all of them.</p>
 *
 * <p>This is also where the reflective bridge to the MCAgents core plugin
 * lives. Folia has no single main thread, so nothing in this package may assume
 * one, reach for the legacy {@code BukkitScheduler}, or block a tick on a
 * language model call.</p>
 */
package io.github.mcagents.chat.bukkit;
