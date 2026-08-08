package io.github.mcagents.chat.engine;

import io.github.mcagents.chat.bukkit.AbstractChatPlugin;
import io.github.mcagents.chat.bukkit.scheduler.BukkitChatScheduler;
import io.github.mcagents.chat.bukkit.scheduler.ChatScheduler;
import io.github.mcagents.chat.foliamc.scheduler.FoliaChatScheduler;

/**
 * The universal entry point: one jar that runs on SpigotMC, PaperMC, and Folia.
 *
 * <p>Which server is running is decided at enable time rather than by shipping a
 * different artifact per platform, so a server owner installs one file and does
 * not have to know which fork they run.</p>
 */
public final class MCAgentsChatEnginePlugin extends AbstractChatPlugin {

    /**
     * The class Folia adds and Paper does not.
     *
     * <p>Detection is by class presence rather than by a server version string:
     * a version string is a formatted name that forks rewrite freely, while this
     * class exists exactly when the regionised schedulers do.</p>
     */
    private static final String FOLIA_MARKER = "io.papermc.paper.threadedregions.RegionizedServer";

    /**
     * {@inheritDoc}
     *
     * <p>Installs the regionised scheduler on Folia and the main thread
     * scheduler everywhere else. Choosing wrongly here is not a cosmetic
     * mistake: the legacy scheduler throws on Folia, and every reply would fail
     * on its way back to the player.</p>
     */
    @Override
    protected ChatScheduler createScheduler() {
        if (isFolia()) {
            getLogger().info("Detected Folia. Using the regionised entity scheduler.");
            return new FoliaChatScheduler(this);
        }
        getLogger().info("Detected a SpigotMC or PaperMC server. Using the main thread scheduler.");
        return new BukkitChatScheduler(this);
    }

    /**
     * Reports whether the running server is Folia.
     *
     * @return {@code true} when Folia's regionised server class is present.
     */
    private boolean isFolia() {
        try {
            Class.forName(FOLIA_MARKER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
