package io.github.mcagents.chat.bukkit.command;

import io.github.mcagents.chat.api.ChatException;
import io.github.mcagents.chat.bukkit.AbstractChatPlugin;
import io.github.mcagents.chat.bukkit.scheduler.ChatScheduler;
import io.github.mcagents.chat.common.ChatService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletionException;

/**
 * The {@code /chat} command.
 *
 * <p>Three forms:</p>
 *
 * <ul>
 *   <li>{@code /chat <message>} — send a message and get a reply.</li>
 *   <li>{@code /chat reload} — re-read {@code config.yml}, including new
 *       credentials, without restarting the server.</li>
 *   <li>{@code /chat clear} — forget the sender's conversation.</li>
 * </ul>
 *
 * <h2>Threading</h2>
 *
 * <p>{@link #onCommand} returns immediately. The reply arrives on an HTTP
 * thread and is handed to a {@link ChatScheduler} before it touches the player,
 * which on Folia means the scheduler owning that player's region.</p>
 */
public final class ChatCommand implements CommandExecutor, TabCompleter {

    /**
     * The permission an ordinary player needs to use {@code /chat}.
     *
     * <p>Only consulted when {@code player_allow} is {@code true} in
     * configuration; when it is {@code false}, operators alone can send a
     * message regardless of permissions.</p>
     */
    public static final String USE_PERMISSION = "mcagents.chat.use";

    /**
     * The permission needed for {@code /chat reload}.
     *
     * <p>Deliberately separate from {@link #USE_PERMISSION}: reloading rewrites
     * which credentials are live and drops every conversation on the server, so
     * it is an administrative action even where chatting is not.</p>
     */
    public static final String RELOAD_PERMISSION = "mcagents.chat.reload";

    /**
     * The plugin this command belongs to, asked for the current service on
     * every invocation so a reload takes effect immediately.
     */
    private final AbstractChatPlugin plugin;

    /**
     * Where replies are handed back to a legal thread.
     */
    private final ChatScheduler scheduler;

    /**
     * Creates the command handler.
     *
     * @param plugin The owning plugin.
     * @param scheduler Where to hop before touching a player.
     * @throws NullPointerException When either argument is {@code null}.
     */
    public ChatCommand(AbstractChatPlugin plugin, ChatScheduler scheduler) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(usage(label));
            return true;
        }

        String first = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1 && first.equals("reload")) {
            return reload(sender);
        }
        if (args.length == 1 && first.equals("clear")) {
            return clear(sender);
        }
        return send(sender, String.join(" ", args));
    }

    /**
     * Re-reads configuration and reports what the plugin now sees.
     *
     * @param sender Who ran the command.
     * @return Always {@code true}; the handler has reported its own outcome.
     */
    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission(RELOAD_PERMISSION) && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to reload MCAgents chat.");
            return true;
        }

        try {
            String state = plugin.reloadChat();
            sender.sendMessage(ChatColor.GREEN + "MCAgents chat reloaded.");
            sender.sendMessage(ChatColor.GRAY + "  platform: " + ChatColor.WHITE
                    + plugin.chatService().settings().vendorCode());
            sender.sendMessage(ChatColor.GRAY + "  backend: " + ChatColor.WHITE
                    + plugin.chatService().settings().model() + " via " + plugin.backendDescription());
            sender.sendMessage(ChatColor.GRAY + "  tokens: " + ChatColor.WHITE + describe(state));
        } catch (RuntimeException e) {
            // A reload that throws must not take the plugin down with it — the
            // previous settings are still live and still working.
            sender.sendMessage(ChatColor.RED + "Reload failed: " + e.getMessage());
            plugin.getLogger().warning("Reloading MCAgents chat failed: " + e.getMessage());
        }
        return true;
    }

    /**
     * Forgets the sender's conversation.
     *
     * @param sender Who ran the command.
     * @return Always {@code true}.
     */
    private boolean clear(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only a player has a conversation to clear.");
            return true;
        }

        boolean had = plugin.chatService().clearSession(player.getUniqueId());
        sender.sendMessage(ChatColor.GRAY + (had
                ? "Conversation cleared. The next message starts fresh."
                : "You had no conversation to clear."));
        return true;
    }

    /**
     * Sends a message and arranges for the reply.
     *
     * @param sender Who ran the command.
     * @param message What they said.
     * @return Always {@code true}.
     */
    private boolean send(CommandSender sender, String message) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only a player can chat. The console has no conversation.");
            return true;
        }
        if (!mayChat(player)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use MCAgents chat.");
            return true;
        }

        ChatService service = plugin.chatService();
        sender.sendMessage(ChatColor.DARK_GRAY + "..." );

        service.ask(player.getUniqueId(), message)
                .whenComplete((reply, failure) -> scheduler.runFor(player, () -> {
                    if (failure != null) {
                        player.sendMessage(ChatColor.RED + explain(failure));
                        return;
                    }
                    player.sendMessage(ChatColor.AQUA + "[AI] " + ChatColor.WHITE + reply.text());
                }));
        return true;
    }

    /**
     * Decides whether a player may send a message.
     *
     * <p>{@code player_allow: false} — the default — means operators only,
     * whatever permissions say. It is a spending control as much as a
     * permission: every message costs the server owner money.</p>
     *
     * @param player The player.
     * @return {@code true} when they may chat.
     */
    private boolean mayChat(Player player) {
        if (player.isOp()) {
            return true;
        }
        return plugin.chatService().settings().playerAllowed() && player.hasPermission(USE_PERMISSION);
    }

    /**
     * Turns a failure into one line a player can act on.
     *
     * <p>Never includes a credential, a stack trace, or a vendor URL.</p>
     *
     * @param failure What the request completed with.
     * @return The message to show.
     */
    private String explain(Throwable failure) {
        Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;

        if (cause instanceof ChatException chatFailure) {
            return switch (chatFailure.kind()) {
                case BACKEND_UNAVAILABLE ->
                        "AI chat is unavailable — the MCAgents core plugin is missing or incompatible.";
                case NO_TOKEN ->
                        "AI chat has no API token configured. An administrator must add one to config.yml.";
                case TOKENS_EXPIRED ->
                        "Every configured API token has been rejected. An administrator must add a working one.";
                case RATE_LIMITED ->
                        "The AI service is rate limiting requests. Try again in a moment.";
                case TOKEN_REJECTED, VENDOR_ERROR ->
                        "The AI service could not answer that. Try again shortly.";
            };
        }
        return "The AI service could not answer that. Try again shortly.";
    }

    /**
     * Describes a credential state for the reload output.
     *
     * @param state Core's state name.
     * @return A short phrase.
     */
    private String describe(String state) {
        return switch (state) {
            case "READY" -> "ready";
            case "NOT_SET" -> "not set — add one to plugins/MCAgents/config.yml";
            case "EXPIRED" -> "expired — every token was rejected and removed";
            default -> "unknown — MCAgents core did not answer";
        };
    }

    /**
     * Builds the usage lines.
     *
     * @param label How the command was invoked.
     * @return The lines to show.
     */
    private String usage(String label) {
        return ChatColor.GRAY + "/" + label + " <message>" + ChatColor.DARK_GRAY + " — ask the AI\n"
                + ChatColor.GRAY + "/" + label + " clear" + ChatColor.DARK_GRAY + " — forget your conversation\n"
                + ChatColor.GRAY + "/" + label + " reload" + ChatColor.DARK_GRAY + " — re-read config.yml";
    }

    /**
     * {@inheritDoc}
     *
     * <p>Completes the two subcommands only. A message is free text, so
     * suggesting anything for it would be noise.</p>
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        List<String> options = new ArrayList<>();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        if ("clear".startsWith(prefix)) {
            options.add("clear");
        }
        if ("reload".startsWith(prefix) && (sender.hasPermission(RELOAD_PERMISSION) || sender.isOp())) {
            options.add("reload");
        }
        return options;
    }
}
