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
         * No token is configured for the selected vendor.
         */
        NO_TOKEN,

        /**
         * Every configured token was rejected and evicted. Distinct from
         * {@link #NO_TOKEN} because the fix is different: one means "add a
         * key", the other means "your keys stopped working".
         */
        TOKENS_EXPIRED,

        /**
         * The vendor rejected the credential itself — an authentication
         * failure, or an explicit "this key is invalid or out of credit".
         *
         * <p>The one kind that means a credential is dead and should be
         * evicted. Nothing else does.</p>
         */
        TOKEN_REJECTED,

        /**
         * The vendor rate limited the request.
         *
         * <p>Kept separate from {@link #TOKEN_REJECTED} on purpose: the
         * credential is fine and merely came too fast. Rotating to another is
         * reasonable; evicting this one destroys something the user paid
         * for.</p>
         */
        RATE_LIMITED,

        /**
         * The vendor was reached and refused the request for some other
         * reason, or could not be reached at all. Says nothing about the
         * credential, so never evict on this.
         */
        VENDOR_ERROR
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
