package io.github.mcagents.chat.common;

import java.time.Duration;
import java.util.Objects;

/**
 * The settings that shape a chat request, read from configuration.
 *
 * <p>Immutable: a reload builds a new instance rather than mutating this one,
 * so a request already in flight finishes under the settings it started with
 * instead of seeing half of an update.</p>
 *
 * @param vendorCode Which vendor to talk to, as core names it. Validated
 *                   against {@link Models#vendors()} at construction, because a
 *                   typo here would otherwise surface as an unexplained vendor
 *                   rejection much later.
 * @param model The model identifier to send to, read from configuration. Never
 *              blank — a blank value in the file is replaced by the platform's
 *              default before it reaches here.
 * @param playerAllowed Whether ordinary players may use the chat command. When
 *                      {@code false} — the default — only operators can. This
 *                      is a cost control before it is a permission: every
 *                      message spends the server owner's credit.
 * @param systemPrompt The framing instructions sent with every request. Kept
 *                     stable between requests on purpose: it sits at the front
 *                     of the prompt, which is exactly the part a vendor's cache
 *                     keys on.
 * @param maxTurns The most turns a session keeps before the oldest are dropped.
 * @param sessionIdleTimeout How long a session may sit unused before it is
 *                           forgotten.
 * @param maxTokens The upper bound on tokens to generate, or
 *                  {@link io.github.mcagents.chat.api.AgentPrompt#NO_MAX_TOKENS}
 *                  to leave it to the vendor.
 */
public record ChatSettings(
        String vendorCode,
        String model,
        boolean playerAllowed,
        String systemPrompt,
        int maxTurns,
        Duration sessionIdleTimeout,
        int maxTokens) {

    /**
     * The framing instructions used when configuration supplies none.
     *
     * <p>Short deliberately: it is prepended to every request, so every word
     * here is paid for on every message by every player.</p>
     */
    public static final String DEFAULT_SYSTEM_PROMPT =
            "You are a helpful assistant inside a Minecraft server. "
                    + "Answer briefly — a few sentences at most, in plain text with no markdown.";

    /**
     * The session bound used when configuration supplies none.
     */
    public static final int DEFAULT_MAX_TURNS = 20;

    /**
     * How long a session may sit unused when configuration supplies nothing.
     */
    public static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofMinutes(30);

    /**
     * Validates the components.
     *
     * @throws NullPointerException When any reference component is {@code null}.
     * @throws IllegalArgumentException When the vendor is unknown, the turn
     *                                  bound is below two, or the idle timeout
     *                                  is not positive.
     */
    public ChatSettings {
        Objects.requireNonNull(vendorCode, "vendorCode cannot be null");
        Objects.requireNonNull(model, "model cannot be null");
        Objects.requireNonNull(systemPrompt, "systemPrompt cannot be null");
        Objects.requireNonNull(sessionIdleTimeout, "sessionIdleTimeout cannot be null");

        if (!Models.isKnown(vendorCode)) {
            throw new IllegalArgumentException("Unknown platform: " + vendorCode
                    + ". Supported platforms: " + Models.vendors());
        }
        if (model.isBlank()) {
            throw new IllegalArgumentException("model cannot be blank");
        }
        if (maxTurns < 2) {
            throw new IllegalArgumentException("maxTurns must be at least 2");
        }
        if (sessionIdleTimeout.isZero() || sessionIdleTimeout.isNegative()) {
            throw new IllegalArgumentException("sessionIdleTimeout must be positive");
        }

        vendorCode = vendorCode.trim().toLowerCase(java.util.Locale.ROOT);
        model = model.trim();
    }

    /**
     * Builds settings for a vendor with every other value defaulted.
     *
     * @param vendorCode Which vendor to talk to.
     * @return The new settings, using that platform's default model and with
     *         players disallowed.
     */
    public static ChatSettings of(String vendorCode) {
        return new ChatSettings(
                vendorCode,
                Models.forVendor(vendorCode),
                false,
                DEFAULT_SYSTEM_PROMPT,
                DEFAULT_MAX_TURNS,
                DEFAULT_IDLE_TIMEOUT,
                io.github.mcagents.chat.api.AgentPrompt.NO_MAX_TOKENS);
    }

}
