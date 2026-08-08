package io.github.mcagents.chat.bukkit.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * The {@link ChatScheduler} for SpigotMC and PaperMC, where one main thread
 * owns every player.
 *
 * <p>Uses the legacy {@code BukkitScheduler}, which Folia does not support —
 * hence the separate Folia implementation. Nothing in this class may be reused
 * there.</p>
 */
public final class BukkitChatScheduler implements ChatScheduler {

    /**
     * The plugin the task is scheduled under, so the server can cancel it on
     * disable.
     */
    private final Plugin plugin;

    /**
     * Creates a scheduler.
     *
     * @param plugin The owning plugin.
     * @throws NullPointerException When {@code plugin} is {@code null}.
     */
    public BukkitChatScheduler(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Hops to the main thread. A player who has gone offline is skipped
     * rather than scheduled for.</p>
     */
    @Override
    public void runFor(Player player, Runnable task) {
        if (player == null || !player.isOnline()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
