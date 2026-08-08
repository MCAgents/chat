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
 * <p>A platform module builds one of these with a backend, then calls
 * {@link #ask(UUID, String)} from its command handler. Everything
 * between — the session, the credential rotation, the prompt shape — happens
 * here, identically on every platform.</p>
 *
 * <h2>No credentials here</h2>
 *
 * <p>This project holds no API tokens and cannot see, set, or reload one. Core
 * owns the file, loads it automatically, rotates on a rate limit, and evicts on
 * a rejection. A prompt goes out and a reply or a failure comes back — that is
 * the whole relationship.</p>
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
 * <h2>One request at a time</h2>
 *
 * <p>A player may have only one request in flight. A second message sent while
 * the first is still being answered is refused rather than queued — each costs
 * the server owner money, the replies would arrive out of order, and both would
 * be built from the same history, so the second would be answered as though the
 * first had never been asked.</p>
 *
 * <p>The claim is released when the request completes, whatever the outcome.
 * Nothing here times it out: core bounds every request, so the future always
 * completes and the release always runs. See {@link PendingRequests}.</p>
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
     * Who currently has a request in flight.
     *
     * <p>One at a time per player. See {@link PendingRequests} for why this
     * needs no timer of its own.</p>
     */
    private final PendingRequests pending = new PendingRequests();

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

        if (!pending.tryAcquire(playerUuid)) {
            // Refused before anything is appended or sent: nothing is billed,
            // and the conversation is left exactly as the in-flight request
            // expects to find it.
            return CompletableFuture.failedFuture(new ChatException(
                    ChatException.Kind.ALREADY_WAITING,
                    "A reply is already on its way for this player."));
        }

        ChatSession session = sessions.get(playerUuid);
        session.append(ChatTurn.user(message));

        return backend.send(buildPrompt(session))
                .handle((reply, failure) -> {
                    // Released on every outcome — reply, failure, or core's
                    // timeout — because handle runs for all three. Attaching
                    // this to success alone would strand the player after the
                    // first error.
                    pending.release(playerUuid);

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
     * <p>Also releases any waiting state, so this doubles as the way out if a
     * player is ever left waiting on a request that never came back.</p>
     *
     * @return {@code true} when a conversation existed and has now been
     *         dropped.
     */
    public boolean clearSession(UUID playerUuid) {
        // Also releases any claim. Not a timeout — an escape hatch, so a player
        // is never permanently stuck behind a request that somehow never
        // completed.
        pending.release(playerUuid);
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
     * Reports whether a player is waiting on a reply.
     *
     * @param playerUuid The player to check.
     * @return {@code true} when a request is in flight for them.
     */
    public boolean isWaiting(UUID playerUuid) {
        return pending.isWaiting(playerUuid);
    }

    /**
     * How many requests are in flight across every player.
     *
     * @return The count, for a diagnostic line.
     */
    public int waitingCount() {
        return pending.size();
    }

    /**
     * Re-reads the credentials and adopts new settings, without a restart.
     *
     * <p>This is what backs {@code /chat reload}. Every conversation is
     * forgotten: the settings that shape a prompt may have changed, and
     * continuing a conversation half built under the old ones produces replies
     * nobody can explain.</p>
     *
     * <p>Credentials are untouched, because none are held here. A key added to
     * MCAgents core is picked up by core's own reload command.</p>
     *
     * @param updated The settings to adopt. Changing the platform is fine here:
     *                nothing in this service is bound to a vendor.
     */
    public void reload(ChatSettings updated) {
        Objects.requireNonNull(updated, "updated cannot be null");

        this.settings = updated;
        sessions.clearAll();
        pending.clear();
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
