package io.github.mcagents.chat.api;

import java.util.Objects;

/**
 * One turn in a conversation: who said it, and what they said.
 *
 * @param role The author of this turn, never {@code null}.
 * @param content The text of this turn, never {@code null}.
 */
public record ChatTurn(Role role, String content) {

    /**
     * Validates the components.
     *
     * @throws NullPointerException When either component is {@code null}.
     */
    public ChatTurn {
        Objects.requireNonNull(role, "role cannot be null");
        Objects.requireNonNull(content, "content cannot be null");
    }

    /**
     * Creates a turn authored by the player.
     *
     * @param content The text the player sent.
     * @return The new turn.
     */
    public static ChatTurn user(String content) {
        return new ChatTurn(Role.USER, content);
    }

    /**
     * Creates a turn authored by the model.
     *
     * @param content The text the model produced.
     * @return The new turn.
     */
    public static ChatTurn assistant(String content) {
        return new ChatTurn(Role.ASSISTANT, content);
    }
}
