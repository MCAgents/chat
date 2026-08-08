/**
 * The credential file every MCAgents mod shares.
 *
 * <p>{@link io.github.mcagents.chat.mods.store.MinecraftDirectory} finds the
 * Minecraft directory — which is not at a fixed path, differs by operating
 * system, and is relocated freely by launchers and modpacks — and
 * {@link io.github.mcagents.chat.mods.store.SharedTokenStore} reads and writes
 * {@code mcagents.json} inside it.</p>
 *
 * <p>One file for every MCAgents mod, on purpose: a player configures a token
 * once and every mod finds it, rather than each keeping its own copy of the same
 * key. Several mods may therefore hold the file at once, which is why writes go
 * through a temporary sibling and are moved into place atomically.</p>
 *
 * <p>Nothing here may log, echo, or otherwise reveal a token.</p>
 */
package io.github.mcagents.chat.mods.store;
