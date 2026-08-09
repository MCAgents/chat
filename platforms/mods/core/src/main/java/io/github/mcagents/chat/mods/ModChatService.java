package io.github.mcagents.chat.mods;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.common.ChatService;
import io.github.mcagents.chat.common.ChatSettings;
import io.github.mcagents.chat.mods.environment.ModContext;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * The mod side's chat service, shared by both physical sides.
 *
 * <p>What is left here after the client and server halves were separated out is
 * exactly what does <em>not</em> differ between them: build a service over the
 * bridge, send a message on a session, forget a session, reload. Every
 * difference — how many conversations there are, who is allowed to start one,
 * what a message may contain — belongs to a half.</p>
 *
 * <p>It is keyed on a session id rather than assuming one conversation, because
 * that assumption was only ever true on a client. A dedicated server has one
 * conversation per player, and building that on top of a single-conversation
 * service would mean unpicking it. See
 * {@code io.github.mcagents.chat.mods.client.ClientChatService} and
 * {@code io.github.mcagents.chat.mods.server.ServerChatService}.</p>
 *
 * <p><strong>No credentials pass through here.</strong> MCAgents core owns the
 * shared {@code mcagents.json}, along with the loading, the rotation, and the
 * eviction. This class cannot see a token, set one, or reload one — it sends a
 * prompt and receives a reply.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>{@link #ask(UUID, String)} returns immediately. The future completes on an
 * unspecified thread, so a caller that touches the game must hop back onto the
 * right thread before showing the reply — the client thread on a client, the
 * server thread on a server.</p>
 */
public final class ModChatService {

    /**
     * Where problems are reported.
     */
    private final Logger logger;

    /**
     * The shared chat logic.
     */
    private final ChatService service;

    /**
     * Builds the shared mod side service.
     *
     * @param backend The loader's bridge to MCAgents core.
     * @param logger Where to report problems.
     * @param settings The settings each request is shaped by.
     * @throws NullPointerException When any argument is {@code null}.
     */
    public ModChatService(AgentBackend backend, Logger logger, ChatSettings settings) {
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
        this.service = new ChatService(
                Objects.requireNonNull(backend, "backend cannot be null"),
                Objects.requireNonNull(settings, "settings cannot be null"));
    }

    /**
     * Builds the shared mod side service from what a loader assembled.
     *
     * @param context What the loader knows.
     * @return The new service.
     * @throws NullPointerException When {@code context} is {@code null}.
     */
    public static ModChatService from(ModContext context) {
        Objects.requireNonNull(context, "context cannot be null");
        return new ModChatService(context.backend(), context.logger(), context.settings());
    }

    /**
     * Returns the settings currently in force.
     *
     * @return The settings, never {@code null}.
     */
    public ChatSettings settings() {
        return service.settings();
    }

    /**
     * Returns where problems are reported.
     *
     * @return The logger, never {@code null}.
     */
    public Logger logger() {
        return logger;
    }

    /**
     * Sends a message on a session and returns the reply text.
     *
     * <p>Returns immediately. Nothing here decides whether the sender was
     * allowed to send it, or whether the message was worth sending — that is a
     * question with a different answer on each side, so each half answers it
     * before calling this.</p>
     *
     * @param session Which conversation this belongs to.
     * @param message What was said.
     * @return A CompletableFuture containing the reply text, failing with a
     *         {@link io.github.mcagents.chat.api.ChatException}.
     * @throws NullPointerException When either argument is {@code null}.
     * @throws IllegalArgumentException When the message is blank.
     */
    public CompletableFuture<String> ask(UUID session, String message) {
        return service.ask(session, message).thenApply(reply -> reply.text());
    }

    /**
     * Forgets one conversation, so the next message on it starts fresh.
     *
     * @param session Which conversation to forget.
     * @return {@code true} when there was one to forget.
     */
    public boolean clear(UUID session) {
        return service.clearSession(session);
    }

    /**
     * Reports whether a session is waiting on a reply.
     *
     * @param session The conversation to check.
     * @return {@code true} when a request is in flight for it.
     */
    public boolean isWaiting(UUID session) {
        return service.isWaiting(session);
    }

    /**
     * How many conversations are currently held in memory.
     *
     * @return The live session count, for a diagnostic line.
     */
    public int liveSessions() {
        return service.liveSessions();
    }

    /**
     * Forgets every conversation and re-applies the settings.
     *
     * <p>Only this mod's own settings and conversations. API tokens belong to
     * MCAgents core and are managed with its own command.</p>
     */
    public void reload() {
        service.reload(service.settings());
    }

    /**
     * Forgets every conversation and adopts new settings.
     *
     * @param updated The settings to adopt.
     * @throws NullPointerException When {@code updated} is {@code null}.
     */
    public void reload(ChatSettings updated) {
        service.reload(updated);
    }
}
