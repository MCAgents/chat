package io.github.mcagents.chat.common;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.api.AgentPrompt;
import io.github.mcagents.chat.api.AgentReply;
import io.github.mcagents.chat.api.ChatException;
import io.github.mcagents.chat.api.ChatTurn;
import io.github.mcagents.chat.api.token.TokenState;
import io.github.mcagents.chat.api.token.TokenStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 * <h2>Credential rotation</h2>
 *
 * <p>A request that fails because the vendor rejected the credential is retried
 * on the next one, and the rejected credential is deleted from storage. A
 * request that fails because of a rate limit is retried on the next credential
 * and the current one is <strong>kept</strong>. Anything else is not retried at
 * all, because nothing was learned about the credential.</p>
 *
 * <p>The retry is bounded by how many credentials are left, so a server whose
 * keys have all expired fails after one pass rather than looping.</p>
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
     * The credentials for the configured vendor, and which one is in use.
     */
    private final TokenPool tokens;

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
     * @param store Where the credentials live.
     * @param settings The settings to start with.
     * @throws NullPointerException When any argument is {@code null}.
     */
    public ChatService(AgentBackend backend, TokenStore store, ChatSettings settings) {
        this.backend = Objects.requireNonNull(backend, "backend cannot be null");
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        this.tokens = new TokenPool(settings.vendorCode(), Objects.requireNonNull(store, "store cannot be null"));
        this.sessions = new SessionStore(settings.maxTurns(), settings.sessionIdleTimeout());

        installCurrentToken();
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
     * @return The credential state.
     */
    public TokenState tokenState() {
        return tokens.state();
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

        ChatException credentialFailure = checkCredentials();
        if (credentialFailure != null) {
            return CompletableFuture.failedFuture(credentialFailure);
        }

        ChatSession session = sessions.get(playerUuid);
        session.append(ChatTurn.user(message));

        return send(session, tokens.remaining()).thenApply(reply -> {
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
     * @param updated The settings to adopt. Its vendor must match the one this
     *                service was built for — changing vendors means rebuilding
     *                the service, since the credential pool belongs to a vendor.
     * @return The credential state after reloading, so the caller can report it.
     * @throws IllegalArgumentException When {@code updated} names a different
     *                                  vendor.
     */
    public TokenState reload(ChatSettings updated) {
        Objects.requireNonNull(updated, "updated cannot be null");
        if (!updated.vendorCode().equals(tokens.vendorCode())) {
            throw new IllegalArgumentException("Changing the platform from " + tokens.vendorCode()
                    + " to " + updated.vendorCode() + " requires rebuilding the service");
        }

        this.settings = updated;
        tokens.reload();
        sessions.clearAll();
        installCurrentToken();
        return tokens.state();
    }

    /**
     * Sends the session's conversation, rotating credentials on failure.
     *
     * @param session The conversation to send.
     * @param attemptsLeft How many credentials may still be tried. Bounds the
     *                     recursion, so an exhausted pool fails after one pass.
     * @return A CompletableFuture containing the reply.
     */
    private CompletableFuture<AgentReply> send(ChatSession session, int attemptsLeft) {
        return backend.send(buildPrompt(session)).handle((reply, failure) -> {
            if (failure == null) {
                return CompletableFuture.completedFuture(reply);
            }

            ChatException chatFailure = asChatException(failure);
            Optional<String> next = switch (chatFailure.kind()) {
                // The credential is dead: drop it from the pool and from disk.
                case TOKEN_REJECTED -> tokens.reject();
                // The credential is healthy and busy: move on, but keep it.
                case RATE_LIMITED -> tokens.rotate();
                // Nothing was learned about the credential. Do not touch it.
                default -> Optional.empty();
            };

            if (next.isEmpty() || attemptsLeft <= 1) {
                return CompletableFuture.<AgentReply>failedFuture(exhaust(chatFailure));
            }

            backend.useToken(tokens.vendorCode(), next.get());
            return send(session, attemptsLeft - 1);
        }).thenCompose(future -> future);
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
     * Hands the current credential to the backend, if there is one.
     */
    private void installCurrentToken() {
        tokens.current().ifPresent(token -> backend.useToken(tokens.vendorCode(), token));
    }

    /**
     * Fails early when no credential is usable.
     *
     * @return The failure to report, or {@code null} when a credential is
     *         available.
     */
    private ChatException checkCredentials() {
        return switch (tokens.state()) {
            case READY -> null;
            case NOT_SET -> new ChatException(ChatException.Kind.NO_TOKEN,
                    "No token is configured for " + tokens.vendorCode() + ".");
            case EXPIRED -> new ChatException(ChatException.Kind.TOKENS_EXPIRED,
                    "Every token configured for " + tokens.vendorCode()
                            + " was rejected and removed. Add a working token and run the reload command.");
        };
    }

    /**
     * Reports a failure that used up the last credential as an exhaustion
     * rather than as the individual vendor error.
     *
     * <p>"Every token was rejected" is what the server owner has to act on; the
     * last vendor message is kept as the cause for the log.</p>
     *
     * @param failure The failure that ended the attempt.
     * @return The failure to hand back.
     */
    private ChatException exhaust(ChatException failure) {
        if (failure.kind() == ChatException.Kind.TOKEN_REJECTED
                && tokens.state() == TokenState.EXPIRED) {
            return new ChatException(ChatException.Kind.TOKENS_EXPIRED,
                    "Every token configured for " + tokens.vendorCode()
                            + " was rejected and removed. Add a working token and run the reload command.",
                    failure);
        }
        return failure;
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
