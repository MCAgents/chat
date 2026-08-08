package io.github.mcagents.chat.api;

/**
 * Who authored a {@link ChatTurn}.
 *
 * <p>Deliberately this project's own enum rather than a MCAgents core type.
 * Nothing in this module compiles against core — the bridge translates these
 * constants into whatever core expects at runtime.</p>
 */
public enum Role {

    /**
     * A turn authored by the player.
     */
    USER("user"),

    /**
     * A turn authored by the model, replayed as context on a later request.
     */
    ASSISTANT("assistant");

    /**
     * The identifier this role is passed across the bridge as. Fixed per
     * constant so renaming a constant cannot change what core receives.
     */
    private final String code;

    /**
     * Binds a role to the identifier it travels as.
     *
     * @param code The bridge identifier, never {@code null}.
     */
    Role(String code) {
        this.code = code;
    }

    /**
     * Returns the identifier this role is passed across the bridge as.
     *
     * @return The bridge identifier, for example {@code "assistant"}.
     */
    public String code() {
        return code;
    }
}
