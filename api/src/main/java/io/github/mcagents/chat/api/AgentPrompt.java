package io.github.mcagents.chat.api;

import java.util.List;
import java.util.Objects;

/**
 * Everything the bridge needs to ask a language model one question.
 *
 * <p>The field order matters for cost, not just for reading. The system prompt
 * and the oldest turns come first and stay byte-identical between requests,
 * because every vendor's prompt cache keys on a common prefix — a prompt whose
 * stable part is at the front gets cached; the same prompt with a timestamp at
 * the front never does. Keep anything that varies per request at the end.</p>
 *
 * @param vendorCode The vendor to ask, as core names it — for example
 *                   {@code "openrouter"}. A plain string rather than an enum
 *                   because the authoritative list lives in core, which this
 *                   module does not compile against.
 * @param model The model identifier to send to. Never blank.
 * @param systemPrompt The framing instructions, or an empty string for none.
 *                     Held separately so it can be placed first, where a cache
 *                     can reach it.
 * @param history The conversation, oldest turn first. Never empty, and always
 *                an unmodifiable copy.
 * @param maxTokens The upper bound on tokens to generate, or
 *                  {@link #NO_MAX_TOKENS} to leave it to the vendor.
 */
public record AgentPrompt(
        String vendorCode,
        String model,
        String systemPrompt,
        List<ChatTurn> history,
        int maxTokens) {

    /**
     * The value {@link #maxTokens()} takes when no bound was set.
     */
    public static final int NO_MAX_TOKENS = -1;

    /**
     * Validates the components and copies the history.
     *
     * @throws NullPointerException When any component is {@code null}.
     * @throws IllegalArgumentException When the vendor code or model is blank,
     *                                  or the history is empty.
     */
    public AgentPrompt {
        Objects.requireNonNull(vendorCode, "vendorCode cannot be null");
        Objects.requireNonNull(model, "model cannot be null");
        Objects.requireNonNull(systemPrompt, "systemPrompt cannot be null");
        Objects.requireNonNull(history, "history cannot be null");

        if (vendorCode.isBlank()) {
            throw new IllegalArgumentException("vendorCode cannot be blank");
        }
        if (model.isBlank()) {
            throw new IllegalArgumentException("model cannot be blank");
        }
        if (history.isEmpty()) {
            throw new IllegalArgumentException("history cannot be empty");
        }
        history = List.copyOf(history);
    }

    /**
     * Reports whether a bound on generated tokens was set.
     *
     * @return {@code true} when {@link #maxTokens()} is a real bound.
     */
    public boolean hasMaxTokens() {
        return maxTokens != NO_MAX_TOKENS;
    }

    /**
     * Reports whether framing instructions were supplied.
     *
     * @return {@code true} when {@link #systemPrompt()} holds something other
     *         than whitespace.
     */
    public boolean hasSystemPrompt() {
        return !systemPrompt.isBlank();
    }
}
