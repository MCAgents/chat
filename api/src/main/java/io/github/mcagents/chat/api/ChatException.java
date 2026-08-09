package io.github.mcagents.chat.api;

/**
 * What every chat failure arrives as.
 *
 * <p>One type covers the lot — no backend, no credentials, a vendor rejection,
 * a bridge that could not resolve — because the chat surface handles them the
 * same way: tell the player something short, log the detail once, and carry on.
 * What distinguishes them is {@link #kind()}.</p>
 *
 * <p>Unchecked, because these failures surface as a failed
 * {@link java.util.concurrent.CompletableFuture} rather than at a call site.</p>
 */
public class ChatException extends RuntimeException {

    /**
     * Serialization identity. Fixed so a value serialized by one build
     * deserializes in the next.
     */
    private static final long serialVersionUID = 1L;

    /**
     * What went wrong, at the granularity the chat surface acts on.
     */
    public enum Kind {

        /**
         * The MCAgents core plugin is absent, or its API could not be resolved.
         * Nothing the player does will help; the server owner must install or
         * update core.
         */
        BACKEND_UNAVAILABLE,

        /**
         * The vendor could not answer.
         *
         * <p>One kind covers every remote failure, including a missing or
         * exhausted credential. This project cannot tell those apart and has no
         * reason to: it holds no credentials, so there is nothing it could do
         * differently. Core's own message says what actually went wrong and goes
         * to the console.</p>
         */
        VENDOR_ERROR,

        /**
         * The player already has a request in flight.
         *
         * <p>Not a failure of the service — a refusal by this project. Nothing
         * was sent, nothing was billed, and the earlier request is still on its
         * way. The only one of these kinds the player can act on: wait.</p>
         */
        ALREADY_WAITING,

        /**
         * The caller was not allowed to ask, or asked something that was not
         * worth sending.
         *
         * <p>A refusal made <strong>before</strong> anything reached a vendor:
         * nothing was sent and nothing was billed. Distinct from the kinds
         * above because it is the only one where the answer depends on
         * <em>who</em> asked, which is a question only a server has — a client
         * has one user, and they already own the credentials.</p>
         *
         * <p>The message carries the reason and is safe to show. It never names
         * a credential, a configured platform, or anything else a refused
         * caller has no business learning.</p>
         */
        NOT_ALLOWED
    }

    /**
     * What went wrong. Never {@code null}.
     */
    private final Kind kind;

    /**
     * Creates a failure of a kind.
     *
     * @param kind What went wrong.
     * @param message A description safe to log.
     */
    public ChatException(Kind kind, String message) {
        this(kind, message, null);
    }

    /**
     * Creates a failure wrapping a lower level one.
     *
     * @param kind What went wrong.
     * @param message A description safe to log.
     * @param cause The underlying failure, or {@code null}.
     */
    public ChatException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    /**
     * Returns what went wrong.
     *
     * @return The kind, never {@code null}.
     */
    public Kind kind() {
        return kind;
    }
}
