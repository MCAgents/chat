/**
 * Shared mod loader code.
 *
 * <p>Everything the NeoForge and Fabric entry points have in common lives
 * here, mirroring the role {@code io.github.mcagents.chat.bukkit} plays for
 * the server side. This is where the shared credential file under the
 * Minecraft directory is resolved and read, so that several MCAgents mods use
 * one set of credentials rather than each keeping its own.</p>
 */
package io.github.mcagents.chat.mods;
