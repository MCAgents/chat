package io.github.mcagents.chat.mods;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.common.ChatService;
import io.github.mcagents.chat.common.ChatSettings;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * The mod side entry point, mirroring what the plugin's lifecycle does on a
 * server.
 *
 * <p>A loader module builds one of these with its logger and its own bridge to
 * MCAgents core, then calls {@link #ask(String)} from its chat command. The
 * loader-specific parts stay in the loader's module; this class holds everything
 * the two loaders share.</p>
 *
 * <p><strong>No credentials pass through here.</strong> MCAgents core owns the
 * shared {@code mcagents.json} under the Minecraft directory, along with the
 * rotation and the eviction. This class asks core what state the credentials are
 * in and reports it — nothing more.</p>
 *
 * <p>A client has exactly one player, so unlike the server there is no player
 * identity to key a conversation on. {@link #CLIENT_SESSION} stands in for it,
 * which keeps the shared {@link ChatService} unchanged rather than growing a
 * single-user special case.</p>
 */
public final class ModChatService {

    /**
     * The identity a client's single conversation is keyed on.
     *
     * <p>Fixed rather than random, so a conversation survives this object being
     * rebuilt by a reload.</p>
     */
    public static final UUID CLIENT_SESSION = UUID.nameUUIDFromBytes("mcagents-chat-client".getBytes());

    /**
     * Where problems are reported.
     */
    private final Logger logger;

    /**
     * The shared chat logic.
     */
    private final ChatService service;

    /**
     * Builds the mod side service.
     *
     * @param backend The loader's bridge to MCAgents core.
     * @param logger Where to report problems.
     * @param platform Which platform to talk to, as core names it.
     * @throws NullPointerException When the backend or logger is {@code null}.
     * @throws IllegalArgumentException When {@code platform} names no supported
     *                                  platform.
     */
    public ModChatService(AgentBackend backend, Logger logger, String platform) {
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
        this.service = new ChatService(
                Objects.requireNonNull(backend, "backend cannot be null"),
                ChatSettings.of(platform));

        report();
    }

    /**
     * Sends a message and returns the reply text.
     *
     * <p>Returns immediately. The future completes on an unspecified thread, so
     * a caller that touches the game must hop back onto the client thread
     * before showing the reply.</p>
     *
     * @param message What the player said.
     * @return A CompletableFuture containing the reply text, failing with a
     *         {@link io.github.mcagents.chat.api.ChatException}.
     */
    public CompletableFuture<String> ask(String message) {
        return service.ask(CLIENT_SESSION, message).thenApply(reply -> reply.text());
    }

    /**
     * Forgets the conversation, so the next message starts fresh.
     *
     * @return {@code true} when there was a conversation to forget.
     */
    public boolean clear() {
        return service.clearSession(CLIENT_SESSION);
    }

    /**
     * Asks MCAgents core to re-read its shared credential file.
     *
     * <p>This is what backs the mod's reload command. A token added by hand — or
     * by another MCAgents mod — becomes usable immediately, without restarting
     * the game.</p>
     *
     * @return Core's credential state afterwards, so the caller can report it.
     */
    public String reload() {
        String state = service.reload(service.settings());
        report();
        return state;
    }

    /**
     * Reports the credential state, so a player can see it in the game log
     * without running a command.
     */
    private void report() {
        switch (service.tokenState()) {
            case "READY" -> logger.info("MCAgents chat: tokens ready.");
            case "NOT_SET" -> logger.warning("MCAgents chat: MCAgents core has no token for "
                    + service.settings().vendorCode() + ". Add one to mcagents.json in your Minecraft "
                    + "directory, then run the chat reload command.");
            case "EXPIRED" -> logger.warning("MCAgents chat: every token MCAgents core had for "
                    + service.settings().vendorCode() + " was rejected and removed.");
            default -> logger.warning("MCAgents chat: could not read the credential state from MCAgents core.");
        }
    }
}
