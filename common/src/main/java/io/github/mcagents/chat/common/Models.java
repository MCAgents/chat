package io.github.mcagents.chat.common;

import java.util.Map;

/**
 * The model each vendor is asked for.
 *
 * <p>Fixed in code, deliberately, and not exposed in configuration. A model
 * identifier is not a preference — a wrong one produces a vendor rejection that
 * looks exactly like a bad credential, and diagnosing that from a server log is
 * miserable. Changing a model is a release of this project, where it can be
 * tested.</p>
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
            "openrouter", "openai/gpt-4o-mini",
            "openai", "gpt-4o-mini",
            "deepseek", "deepseek-chat",
            "anthropic", "claude-haiku-4-5-20251001");

    /**
     * Not instantiable — this class is a lookup table.
     */
    private Models() {
    }

    /**
     * Returns the model configured for a vendor.
     *
     * @param vendorCode The vendor code, matched without regard to case or
     *                   surrounding whitespace.
     * @return The model identifier.
     * @throws IllegalArgumentException When no model is configured for that
     *                                  vendor, which means the vendor code is
     *                                  wrong or this table was not updated
     *                                  when a vendor was added.
     */
    public static String forVendor(String vendorCode) {
        String normalized = vendorCode == null ? "" : vendorCode.trim().toLowerCase(java.util.Locale.ROOT);
        String model = BY_VENDOR.get(normalized);
        if (model == null) {
            throw new IllegalArgumentException("No model is configured for vendor: " + vendorCode
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
