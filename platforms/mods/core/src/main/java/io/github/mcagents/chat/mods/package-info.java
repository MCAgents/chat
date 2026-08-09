/**
 * Shared mod code — every loader, and both physical sides.
 *
 * <p>Everything the mod modules have in common lives here, mirroring the role
 * {@code io.github.mcagents.chat.bukkit} plays for the server side. Two rules
 * decide what belongs:</p>
 *
 * <ul>
 *   <li><strong>Loader agnostic.</strong> Code needing a NeoForge or Fabric
 *       type belongs in that loader's own module.</li>
 *   <li><strong>Side agnostic.</strong> A client has one conversation and one
 *       person who is allowed to have it; a dedicated server has one per player
 *       and must decide who may. Code that differs between those belongs in
 *       {@code io.github.mcagents.chat.mods.client} or
 *       {@code io.github.mcagents.chat.mods.server}. A branch on the side
 *       inside a shared class is the shape this split exists to avoid.</li>
 * </ul>
 *
 * <p>The machinery that decides which side is running, and starts the matching
 * half without linking the other, is in
 * {@link io.github.mcagents.chat.mods.environment}.</p>
 */
package io.github.mcagents.chat.mods;
