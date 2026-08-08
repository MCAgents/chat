package io.github.mcagents.chat.bukkit;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.api.token.TokenState;
import io.github.mcagents.chat.bukkit.bridge.MCAgentsBridge;
import io.github.mcagents.chat.bukkit.bridge.UnavailableBackend;
import io.github.mcagents.chat.bukkit.command.ChatCommand;
import io.github.mcagents.chat.bukkit.config.ChatConfig;
import io.github.mcagents.chat.bukkit.config.YamlTokenStore;
import io.github.mcagents.chat.bukkit.scheduler.ChatScheduler;
import io.github.mcagents.chat.common.ChatService;
import io.github.mcagents.chat.common.ChatSettings;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The shared enable and disable lifecycle for every Bukkit family entry point.
 *
 * <p>A concrete platform supplies only its {@link ChatScheduler} — the one thing
 * that genuinely differs between SpigotMC, PaperMC, and Folia. Everything else
 * happens here, once.</p>
 *
 * <p>Enabling never fails. A missing or incompatible core plugin produces an
 * {@link UnavailableBackend}, so the plugin loads, the command exists, and a
 * player is told what is wrong instead of finding a command that does not
 * exist.</p>
 */
public abstract class AbstractChatPlugin extends JavaPlugin {

    /**
     * The name of the core plugin this one bridges to, as it appears in the
     * plugin manifest.
     */
    private static final String CORE_PLUGIN = "MCAgents";

    /**
     * The credential and settings file. Held so a reload can re-read it.
     */
    private YamlTokenStore store;

    /**
     * The chat entry point. Replaced only when the configured platform changes,
     * since a service owns a pool belonging to one vendor.
     */
    private ChatService service;

    /**
     * Where requests are sent. Resolved once at enable time.
     */
    private AgentBackend backend;

    /**
     * Supplies the scheduler this platform needs.
     *
     * <p>The single thing a concrete platform has to provide. On SpigotMC and
     * PaperMC this is the main thread scheduler; on Folia it must be the
     * regionised one, because the legacy scheduler is unsupported there.</p>
     *
     * @return The scheduler replies are handed to.
     */
    protected abstract ChatScheduler createScheduler();

    /**
     * {@inheritDoc}
     *
     * <p>Writes the default configuration if absent, resolves the bridge, builds
     * the service, and registers the command. Any failure short of a broken
     * configuration file leaves the plugin enabled with an unavailable
     * backend.</p>
     */
    @Override
    public void onEnable() {
        this.store = new YamlTokenStore(this);
        ChatSettings settings = ChatConfig.read(store.config(), getLogger());

        this.backend = resolveBackend();
        this.service = new ChatService(backend, store, settings);

        ChatCommand command = new ChatCommand(this, createScheduler());
        if (getCommand("chat") != null) {
            getCommand("chat").setExecutor(command);
            getCommand("chat").setTabCompleter(command);
        } else {
            getLogger().severe("The 'chat' command is missing from plugin.yml, so /chat will not work.");
        }

        report(settings);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Nothing to release: the HTTP clients belong to the core plugin, which
     * closes its own. Conversations are in memory and go with the process.</p>
     */
    @Override
    public void onDisable() {
        this.service = null;
        this.backend = null;
        this.store = null;
    }

    /**
     * Returns the live chat service.
     *
     * <p>Read on every command invocation rather than captured, so a reload
     * takes effect on the next message.</p>
     *
     * @return The service, never {@code null} while the plugin is enabled.
     */
    public ChatService chatService() {
        return service;
    }

    /**
     * Describes the backend for the reload command's output.
     *
     * @return A short description that never includes a credential.
     */
    public String backendDescription() {
        return backend == null ? "not initialised" : backend.describe();
    }

    /**
     * Re-reads configuration and applies it, without restarting the server.
     *
     * <p>Changing the platform rebuilds the service, because a service owns the
     * credential pool for one vendor. Everything else is applied in place.</p>
     *
     * @return The credential state after reloading, so the caller can report it.
     */
    public TokenState reloadChat() {
        store.reload();
        ChatSettings updated = ChatConfig.read(store.config(), getLogger());

        if (!updated.vendorCode().equals(service.settings().vendorCode())) {
            this.service = new ChatService(backend, store, updated);
            return service.tokenState();
        }
        return service.reload(updated);
    }

    /**
     * Logs what the plugin came up with, so a server owner can see the state
     * without running a command.
     *
     * @param settings The settings in force.
     */
    private void report(ChatSettings settings) {
        getLogger().info("Platform: " + settings.vendorCode() + " (" + settings.model() + ")");
        getLogger().info("Backend: " + backend.describe());
        getLogger().info("Players may chat: " + settings.playerAllowed());

        switch (service.tokenState()) {
            case READY -> getLogger().info("Tokens: ready.");
            case NOT_SET -> getLogger().warning("Tokens: none configured for " + settings.vendorCode()
                    + ". Add one to config.yml and run /chat reload.");
            case EXPIRED -> getLogger().warning("Tokens: every configured token has been rejected and removed.");
        }
    }

    /**
     * Resolves the bridge to the core plugin, or an unavailable backend
     * explaining why not.
     *
     * @return The backend to use.
     */
    private AgentBackend resolveBackend() {
        Plugin core = getServer().getPluginManager().getPlugin(CORE_PLUGIN);
        if (core == null || !core.isEnabled()) {
            getLogger().severe("The " + CORE_PLUGIN + " plugin is not installed or not enabled. "
                    + "Install it alongside this plugin; chat will report as unavailable until then.");
            return new UnavailableBackend("the " + CORE_PLUGIN + " plugin is not installed");
        }

        return MCAgentsBridge.resolve(this, core)
                .map(bridge -> (AgentBackend) bridge)
                .orElseGet(() -> new UnavailableBackend(
                        "the installed " + CORE_PLUGIN + " version is not compatible"));
    }
}
