package io.github.mcagents.chat.spigotmc;

import io.github.mcagents.chat.bukkit.AbstractChatPlugin;
import io.github.mcagents.chat.bukkit.scheduler.ChatScheduler;
import io.github.mcagents.chat.bukkit.scheduler.BukkitChatScheduler;

/**
 * The SpigotMC entry point.
 *
 * <p>Everything this plugin does lives in
 * {@link AbstractChatPlugin}. This class exists to supply the one thing that
 * genuinely differs between the Bukkit family platforms: which scheduler a
 * reply is handed to before it touches a player.</p>
 */
public final class SpigotMCChatPlugin extends AbstractChatPlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ChatScheduler createScheduler() {
        return new BukkitChatScheduler(this);
    }
}
