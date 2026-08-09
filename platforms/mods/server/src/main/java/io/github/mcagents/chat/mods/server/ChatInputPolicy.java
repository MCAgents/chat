package io.github.mcagents.chat.mods.server;

import io.github.mcagents.chat.mods.environment.ServerOnly;

import java.util.Objects;
import java.util.Optional;

/**
 * What a server accepts as a message before it costs anyone money.
 *
 * <p>Three checks, each of which exists because of what it prevents rather than
 * for tidiness:</p>
 *
 * <ul>
 *   <li><strong>Blank.</strong> A prompt with nothing in it still costs the
 *       system prompt and the whole replayed conversation.</li>
 *   <li><strong>Too long.</strong> Tokens are billed by the thousand and a
 *       player can paste as much as they like. The bound is on the message, not
 *       on the conversation, because the conversation is already bounded by the
 *       session's turn limit.</li>
 *   <li><strong>Control characters.</strong> Stripped rather than refused. They
 *       arrive from a paste far more often than from an attack, and refusing a
 *       message over an invisible character is a mystery for the player.</li>
 * </ul>
 *
 * <h2>Why this is server-only</h2>
 *
 * <p>On a client the player is spending their own credit and typing into their
 * own game — there is nobody to protect them from. Every check here exists
 * because on a server the person typing is not the person paying.</p>
 */
@ServerOnly
public final class ChatInputPolicy {

    /**
     * The most characters one message may carry.
     *
     * <p>Vanilla chat cuts off at 256, so a message longer than this could not
     * have been typed into the chat box — it was pasted or sent by a modified
     * client, and neither is a reason to bill the owner for it.</p>
     */
    public static final int DEFAULT_MAX_LENGTH = 256;

    /**
     * The most characters this policy accepts.
     */
    private final int maxLength;

    /**
     * Builds a policy with the default bound.
     */
    public ChatInputPolicy() {
        this(DEFAULT_MAX_LENGTH);
    }

    /**
     * Builds a policy with a bound.
     *
     * @param maxLength The most characters one message may carry.
     * @throws IllegalArgumentException When the bound is not positive.
     */
    public ChatInputPolicy(int maxLength) {
        if (maxLength < 1) {
            throw new IllegalArgumentException("maxLength must be positive");
        }
        this.maxLength = maxLength;
    }

    /**
     * Returns the most characters this policy accepts.
     *
     * @return The bound.
     */
    public int maxLength() {
        return maxLength;
    }

    /**
     * Cleans a message and reports whether it may be sent.
     *
     * <p>Length is measured <strong>after</strong> cleaning, so padding a
     * message with invisible characters cannot push it over the bound, and
     * stripping them cannot push a legitimate message under it either.</p>
     *
     * @param message What the player typed.
     * @return The cleaned message, or empty when it may not be sent.
     * @throws NullPointerException When {@code message} is {@code null}.
     */
    public Optional<String> clean(String message) {
        Objects.requireNonNull(message, "message cannot be null");

        String cleaned = strip(message).strip();
        if (cleaned.isEmpty() || cleaned.length() > maxLength) {
            return Optional.empty();
        }
        return Optional.of(cleaned);
    }

    /**
     * Explains why a message may not be sent.
     *
     * @param message What the player typed.
     * @return The reason to show them, or empty when it may be sent.
     * @throws NullPointerException When {@code message} is {@code null}.
     */
    public Optional<String> refusalFor(String message) {
        Objects.requireNonNull(message, "message cannot be null");

        String cleaned = strip(message).strip();
        if (cleaned.isEmpty()) {
            return Optional.of("Say something to ask about.");
        }
        if (cleaned.length() > maxLength) {
            return Optional.of("That message is too long. Keep it under "
                    + maxLength + " characters.");
        }
        return Optional.empty();
    }

    /**
     * Removes control characters, keeping the ordinary text between them.
     *
     * @param message The message to clean.
     * @return The message without control characters.
     */
    private static String strip(String message) {
        StringBuilder cleaned = new StringBuilder(message.length());
        message.codePoints().forEach(codePoint -> {
            // Tabs and newlines are control characters too, and a pasted
            // paragraph is a reasonable question. They become spaces rather
            // than disappearing, so words do not run together.
            if (codePoint == '\t' || codePoint == '\n' || codePoint == '\r') {
                cleaned.append(' ');
            } else if (!Character.isISOControl(codePoint)) {
                cleaned.appendCodePoint(codePoint);
            }
        });
        return cleaned.toString();
    }
}
