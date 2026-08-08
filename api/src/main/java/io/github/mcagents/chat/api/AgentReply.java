package io.github.mcagents.chat.api;

import java.util.Objects;

/**
 * What a language model returned, reduced to what the chat surface needs.
 *
 * @param text The reply to show the player, never {@code null}.
 * @param promptTokens Tokens the request consumed, or {@code -1} when the
 *                     vendor reported none.
 * @param completionTokens Tokens the reply consumed, or {@code -1} when the
 *                         vendor reported none.
 * @param cachedTokens Prompt tokens served from the vendor's cache, or
 *                     {@code -1} when the vendor reported none. This is the
 *                     only visible evidence that prompt caching is working, so
 *                     it is carried even though nothing in the chat flow
 *                     branches on it.
 * @param finishReason Why generation stopped, in the vendor's own words. Empty
 *                     when none was reported.
 */
public record AgentReply(
        String text,
        int promptTokens,
        int completionTokens,
        int cachedTokens,
        String finishReason) {

    /**
     * Validates the components.
     *
     * @throws NullPointerException When the text or finish reason is
     *                              {@code null}.
     */
    public AgentReply {
        Objects.requireNonNull(text, "text cannot be null");
        Objects.requireNonNull(finishReason, "finishReason cannot be null");
    }

    /**
     * Creates a reply carrying text and no usage figures, for a bridge that
     * could not read them.
     *
     * @param text The reply to show the player.
     * @return The new reply.
     */
    public static AgentReply of(String text) {
        return new AgentReply(text, -1, -1, -1, "");
    }

    /**
     * Reports whether any of the prompt was served from the vendor's cache.
     *
     * @return {@code true} when the vendor reported a non zero cache hit.
     */
    public boolean wasCached() {
        return cachedTokens > 0;
    }

    /**
     * Returns this reply as an assistant turn, ready to be appended to a
     * session's history.
     *
     * @return A {@link Role#ASSISTANT} turn holding {@link #text()}.
     */
    public ChatTurn asTurn() {
        return ChatTurn.assistant(text);
    }
}
