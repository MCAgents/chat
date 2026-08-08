package io.github.mcagents.chat.api.token;

/**
 * Whether a vendor can currently be called, and if not, why not.
 *
 * <p>The distinction between {@link #NOT_SET} and {@link #EXPIRED} is the whole
 * reason this enum exists. Both mean "no usable credential", but they call for
 * opposite actions from the server owner — add a key, versus find out why the
 * keys stopped working — and a single "no token" state would hide that.</p>
 */
public enum TokenState {

    /**
     * At least one credential is configured and has not been rejected.
     */
    READY,

    /**
     * No credential was ever configured for this vendor.
     */
    NOT_SET,

    /**
     * Every configured credential was rejected by the vendor and evicted.
     */
    EXPIRED
}
