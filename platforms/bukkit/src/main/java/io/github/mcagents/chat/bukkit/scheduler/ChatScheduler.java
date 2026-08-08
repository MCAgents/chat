package io.github.mcagents.chat.bukkit.scheduler;

import org.bukkit.entity.Player;

/**
 * Runs work on the thread that is allowed to touch a given player.
 *
 * <p>Implemented once per server family: the legacy {@code BukkitScheduler} on
 * SpigotMC and PaperMC, and the entity scheduler on Folia. The chat command
 * depends only on this, so the same handler is correct on all three.</p>
 */
public interface ChatScheduler {

    /**
     * Runs a task on the thread owning a player.
     *
     * <p>Called from a completed future, so the caller is normally an arbitrary
     * HTTP thread. An implementation must not assume otherwise.</p>
     *
     * <p>If the player has left, the task is dropped: on Folia a departed
     * entity has no owning region, and on every platform there is nobody left
     * to message.</p>
     *
     * @param player The player the work touches.
     * @param task The work to run.
     */
    void runFor(Player player, Runnable task);
}
