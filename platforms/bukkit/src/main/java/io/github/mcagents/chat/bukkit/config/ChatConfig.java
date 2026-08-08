package io.github.mcagents.chat.bukkit.config;

import io.github.mcagents.chat.common.ChatSettings;
import io.github.mcagents.chat.common.Models;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Turns {@code config.yml} into {@link ChatSettings}.
 *
 * <p>Every value has a default, and a value the owner got wrong falls back to
 * that default with a warning rather than preventing the plugin from loading. A
 * server that stops booting because of one mistyped line is a worse outcome than
 * one that boots and says what it ignored.</p>
 */
public final class ChatConfig {

    /**
     * Not instantiable — this class is a single reader.
     */
    private ChatConfig() {
    }

    /**
     * Reads the settings from a parsed configuration.
     *
     * @param config The parsed {@code config.yml}.
     * @param logger Where to report a value that had to be defaulted.
     * @return The settings, never {@code null}.
     */
    public static ChatSettings read(FileConfiguration config, Logger logger) {
        String platform = config.getString("platform", "openrouter");
        String normalized = platform == null ? "" : platform.trim().toLowerCase(Locale.ROOT);

        if (!Models.isKnown(normalized)) {
            logger.warning("config.yml sets platform: \"" + platform + "\", which is not a supported platform. "
                    + "Falling back to openrouter. Supported platforms: " + Models.vendors());
            normalized = "openrouter";
        }

        String model = config.getString("model", "");
        if (model == null || model.isBlank()) {
            model = Models.forVendor(normalized);
            logger.info("config.yml sets no model, so " + normalized + " will use " + model + ".");
        } else {
            model = model.trim();
        }

        // A wrong model is rejected by the vendor in a way that reads exactly
        // like a bad API key, so say something now rather than let the owner
        // hunt for a credential problem that does not exist.
        if (Models.looksMismatched(normalized, model)) {
            logger.warning("config.yml sets platform: \"" + normalized + "\" with model: \"" + model
                    + "\", which look mismatched. OpenRouter models are namespaced "
                    + "(~deepseek/deepseek-v4-flash-latest); openai, deepseek, and anthropic take bare names "
                    + "(gpt-4o-mini). Using it anyway — if every request fails, this is why.");
        }

        boolean playerAllowed = config.getBoolean("player_allow", false);

        String systemPrompt = config.getString("system_prompt", ChatSettings.DEFAULT_SYSTEM_PROMPT);
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = ChatSettings.DEFAULT_SYSTEM_PROMPT;
        }

        int maxTurns = config.getInt("session.max_turns", ChatSettings.DEFAULT_MAX_TURNS);
        if (maxTurns < 2) {
            logger.warning("config.yml sets session.max_turns below 2, which cannot hold one exchange. "
                    + "Using " + ChatSettings.DEFAULT_MAX_TURNS + ".");
            maxTurns = ChatSettings.DEFAULT_MAX_TURNS;
        }

        int idleMinutes = config.getInt("session.idle_timeout_minutes",
                (int) ChatSettings.DEFAULT_IDLE_TIMEOUT.toMinutes());
        if (idleMinutes < 1) {
            logger.warning("config.yml sets session.idle_timeout_minutes below 1, which would expire every "
                    + "conversation immediately. Using " + ChatSettings.DEFAULT_IDLE_TIMEOUT.toMinutes() + ".");
            idleMinutes = (int) ChatSettings.DEFAULT_IDLE_TIMEOUT.toMinutes();
        }

        int maxTokens = config.getInt("max_tokens", io.github.mcagents.chat.api.AgentPrompt.NO_MAX_TOKENS);
        if (maxTokens != io.github.mcagents.chat.api.AgentPrompt.NO_MAX_TOKENS && maxTokens < 1) {
            logger.warning("config.yml sets max_tokens below 1. Leaving the limit to the platform.");
            maxTokens = io.github.mcagents.chat.api.AgentPrompt.NO_MAX_TOKENS;
        }

        return new ChatSettings(normalized, model, playerAllowed, systemPrompt, maxTurns,
                Duration.ofMinutes(idleMinutes), maxTokens);
    }
}
