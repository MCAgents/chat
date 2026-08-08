package io.github.mcagents.chat.common;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.api.AgentPrompt;
import io.github.mcagents.chat.api.AgentReply;
import io.github.mcagents.chat.api.ChatException;
import io.github.mcagents.chat.api.ChatTurn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * The single entry point to this project's chat behavior.
 *
 * <p>A platform module builds one of these with a backend and a token store,
 * then calls {@link #ask(UUID, String)} from its command handler. Everything
 * between — the session, the credential rotation, the prompt shape — happens
 * here, identically on every platform.</p>
 *
 * <h2>No credentials here</h2>
 *
 * <p>This project holds no API tokens. MCAgents core owns the credential file,
 * the pool, the rotation, and the eviction — including the decision that a
 * rejected key is dead while a rate limited one is merely busy. That decision is
 * destructive to get wrong, so it is written once, in core, rather than repeated
 * in every consumer.</p>
 *
 * <p>All this does is ask core what state a vendor's credentials are in, so a
 * command can tell a server owner to add a key or to find out why their keys
 * stopped working.</p>
 *
 * <h2>Prompt caching</h2>
 *
 * <p>No vendor-specific cache flag is sent. Three of the four supported vendors
 * cache automatically, and all of them key on a <em>common prefix</em>, so what
 * actually decides whether a cache hits is the shape of the prompt.
 * {@link #buildPrompt} therefore puts the stable framing instructions first and
 * the varying turns last, and the session's turn text is never rewritten
 * between requests. {@link AgentReply#cachedTokens()} reports what the vendor
 * actually served from cache.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>{@link #ask(UUID, String)} returns immediately and is safe to call from a
 * server tick. The returned future completes on an unspecified thread, so a
 * caller that touches the game must hop back onto the right scheduler.</p>
 */
public final class ChatService {

    /**
     * Where requests are sent — the reflective bridge to MCAgents core.
     */
    private final AgentBackend backend;

    /**
     * Every player's running conversation.
     */
    private final SessionStore sessions;

    /**
     * The settings shaping each request. Replaced wholesale on reload, never
     * mutated, so a request in flight finishes under what it started with.
     */
    private volatile ChatSettings settings;

    /**
     * Builds a service.
     *
     * @param backend Where to send requests.
     * @param settings The settings to start with.
     * @throws NullPointerException When any argument is {@code null}.
     */
    public ChatService(AgentBackend backend, ChatSettings settings) {
        this.backend = Objects.requireNonNull(backend, "backend cannot be null");
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        this.sessions = new SessionStore(settings.maxTurns(), settings.sessionIdleTimeout());
    }

    /**
     * Returns the settings currently in force.
     *
     * @return The settings, never {@code null}.
     */
    public ChatSettings settings() {
        return settings;
    }

    /**
     * Reports whether this vendor can currently be called, and if not, why not.
     *
     * @return Core's state name — {@code "READY"}, {@code "NOT_SET"}, or
     *         {@code "EXPIRED"}.
     */
    public String tokenState() {
        return backend.tokenState(settings.vendorCode());
    }

    /**
     * Sends a player's message and returns the model's reply.
     *
     * <p>The message is appended to the player's session before sending and the
     * reply is appended after, so the next message continues the conversation.
     * A failed request leaves the player's message in the session, so a retry
     * after a transient failure does not lose what they typed.</p>
     *
     * @param playerUuid Who is talking.
     * @param message What they said.
     * @return A CompletableFuture containing the reply, failing with a
     *         {@link ChatException} when the backend is unavailable, no
     *         credential is usable, or every credential was refused.
     * @throws NullPointerException When either argument is {@code null}.
     * @throws IllegalArgumentException When the message is blank.
     */
    public CompletableFuture<AgentReply> ask(UUID playerUuid, String message) {
        Objects.requireNonNull(playerUuid, "playerUuid cannot be null");
        Objects.requireNonNull(message, "message cannot be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message cannot be blank");
        }

        if (!backend.isAvailable()) {
            return CompletableFuture.failedFuture(new ChatException(
                    ChatException.Kind.BACKEND_UNAVAILABLE,
                    "The MCAgents core plugin is not available, so nothing can be asked."));
        }

        ChatSession session = sessions.get(playerUuid);
        session.append(ChatTurn.user(message));

        return backend.send(buildPrompt(session))
                .handle((reply, failure) -> {
                    if (failure != null) {
                        throw new CompletionException(asChatException(failure));
                    }
                    session.append(reply.asTurn());
                    return reply;
                });
    }

    /**
     * Forgets one player's conversation, so their next message starts fresh.
     *
     * @param playerUuid The player.
     * @return {@code true} when a conversation existed and has now been
     *         dropped.
     */
    public boolean clearSession(UUID playerUuid) {
        return sessions.clear(playerUuid);
    }

    /**
     * How many conversations are currently held in memory.
     *
     * @return The live session count.
     */
    public int liveSessions() {
        return sessions.size();
    }

    /**
     * Re-reads the credentials and adopts new settings, without a restart.
     *
     * <p>This is what backs {@code /chat reload}. Every conversation is
     * forgotten: the settings that shape a prompt may have changed, and
     * continuing a conversation half built under the old ones produces replies
     * nobody can explain.</p>
     *
     * <p>Also asks core to re-read its credential file, so a key the owner just
     * pasted into core's config becomes usable from this command too.</p>
     *
     * @param updated The settings to adopt. Changing the platform is fine here:
     *                nothing in this service is bound to a vendor any more.
     * @return Core's credential state afterwards, so the caller can report it.
     */
    public String reload(ChatSettings updated) {
        Objects.requireNonNull(updated, "updated cannot be null");

        this.settings = updated;
        sessions.clearAll();
        backend.reloadTokens();
        return backend.tokenState(updated.vendorCode());
    }

    /**
     * Turns a session into the prompt sent to the vendor.
     *
     * <p>The framing instructions go first and the turns after, oldest first.
     * That order is what lets a vendor's prompt cache match a common prefix
     * across a conversation's requests.</p>
     *
     * @param session The conversation to send.
     * @return The prompt.
     */
    private AgentPrompt buildPrompt(ChatSession session) {
        ChatSettings current = settings;
        List<ChatTurn> history = new ArrayList<>(session.history());

        return new AgentPrompt(
                current.vendorCode(),
                current.model(),
                current.systemPrompt(),
                history,
                current.maxTokens());
    }

    /**
     * Normalizes whatever a failed future carried into a {@link ChatException}.
     *
     * <p>A future's failure arrives wrapped in a {@link CompletionException},
     * so the useful cause is one level down.</p>
     *
     * @param failure What the future completed with.
     * @return The failure as a {@link ChatException}.
     */
    private ChatException asChatException(Throwable failure) {
        Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;

        if (cause instanceof ChatException chatFailure) {
            return chatFailure;
        }
        return new ChatException(ChatException.Kind.VENDOR_ERROR,
                cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage(), cause);
    }
}
