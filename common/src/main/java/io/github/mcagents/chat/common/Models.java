package io.github.mcagents.chat.common;

import java.util.Map;

/**
 * The model each platform falls back to when configuration names none.
 *
 * <p>These are defaults, not the whole story: {@code model} in {@code config.yml}
 * overrides them. They exist so a server owner who never touches that key still
 * gets something sensible for the platform they picked, and so a blank value has
 * an obvious meaning rather than being an error.</p>
 *
 * <p>A model identifier is easy to get subtly wrong, and a wrong one produces a
 * vendor rejection that looks exactly like a bad credential. That is why
 * {@link #looksMismatched(String, String)} exists — see its documentation.</p>
 *
 * <p>The vendor codes match the ones MCAgents core uses, since they travel
 * across the bridge unchanged.</p>
 */
public final class Models {

    /**
     * The model used for each vendor, keyed by the vendor code core knows it
     * by.
     *
     * <p>OpenRouter namespaces its identifiers by provider; the other three use
     * their own bare names.</p>
     */
    private static final Map<String, String> BY_VENDOR = Map.of(
            "openrouter", "~deepseek/deepseek-v4-flash-latest",
            "openai", "gpt-4o-mini",
            "deepseek", "deepseek-chat",
            "anthropic", "claude-haiku-4-5-20251001");

    /**
     * Not instantiable — this class is a lookup table.
     */
    private Models() {
    }

    /**
     * Reports whether a model identifier obviously does not belong to a
     * platform.
     *
     * <p>OpenRouter namespaces every model — {@code ~deepseek/deepseek-v4-flash-latest},
     * {@code openai/gpt-4o-mini} — while the three direct vendors use bare
     * names. So a slug carrying a {@code ~} or a {@code /} sent straight to
     * OpenAI, DeepSeek, or Anthropic will be rejected as unknown, and a bare
     * name sent to OpenRouter usually will too.</p>
     *
     * <p>This is a warning and never a refusal. Vendors add naming conventions
     * without asking, and refusing to start over a slug this code has not heard
     * of would be worse than the mistake it prevents. It exists because that
     * rejection arrives looking exactly like a bad API key, and a server owner
     * can lose an afternoon to it.</p>
     *
     * @param vendorCode The platform the model will be sent to.
     * @param model The model identifier.
     * @return {@code true} when the two obviously disagree.
     */
    public static boolean looksMismatched(String vendorCode, String model) {
        if (vendorCode == null || model == null || model.isBlank()) {
            return false;
        }

        boolean namespaced = model.startsWith("~") || model.contains("/");
        boolean openRouter = "openrouter".equalsIgnoreCase(vendorCode.trim());
        return openRouter != namespaced;
    }

    /**
     * Returns the model a vendor falls back to when configuration names none.
     *
     * @param vendorCode The vendor code, matched without regard to case or
     *                   surrounding whitespace.
     * @return The default model identifier.
     * @throws IllegalArgumentException When no default is recorded for that
     *                                  vendor, which means the vendor code is
     *                                  wrong or this table was not updated
     *                                  when a vendor was added.
     */
    public static String forVendor(String vendorCode) {
        String normalized = vendorCode == null ? "" : vendorCode.trim().toLowerCase(java.util.Locale.ROOT);
        String model = BY_VENDOR.get(normalized);
        if (model == null) {
            throw new IllegalArgumentException("No default model is recorded for vendor: " + vendorCode
                    + ". Known vendors: " + BY_VENDOR.keySet());
        }
        return model;
    }

    /**
     * Reports whether a vendor code names a vendor this project supports.
     *
     * @param vendorCode The vendor code to check.
     * @return {@code true} when a model is configured for it.
     */
    public static boolean isKnown(String vendorCode) {
        return vendorCode != null && BY_VENDOR.containsKey(vendorCode.trim().toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * Lists every supported vendor code.
     *
     * @return An unmodifiable set of the vendor codes, for validating
     *         configuration and for command tab completion.
     */
    public static java.util.Set<String> vendors() {
        return BY_VENDOR.keySet();
    }
}
