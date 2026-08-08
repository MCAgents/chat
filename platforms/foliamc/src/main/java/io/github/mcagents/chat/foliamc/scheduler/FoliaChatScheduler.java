package io.github.mcagents.chat.foliamc.scheduler;

import io.github.mcagents.chat.bukkit.scheduler.ChatScheduler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * The {@link ChatScheduler} for Folia, where no single main thread exists.
 *
 * <p>Uses the player's own entity scheduler, which runs the task on whichever
 * region currently owns that player and follows them if they move between
 * regions mid-flight. The legacy {@code BukkitScheduler} is unsupported on
 * Folia, so the SpigotMC implementation cannot be reused here.</p>
 */
public final class FoliaChatScheduler implements ChatScheduler {

    /**
     * The plugin the task is scheduled under.
     */
    private final Plugin plugin;

    /**
     * Creates a scheduler.
     *
     * @param plugin The owning plugin.
     * @throws NullPointerException When {@code plugin} is {@code null}.
     */
    public FoliaChatScheduler(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * <p>The retired callback is deliberately a no-op: a player who logged out
     * before the reply arrived has nowhere to receive it, which is an ordinary
     * outcome rather than an error.</p>
     */
    @Override
    public void runFor(Player player, Runnable task) {
        if (player == null || !player.isOnline()) {
            return;
        }
        player.getScheduler().run(plugin, scheduled -> task.run(), null);
    }
}
