package io.github.mcagents.chat.common;

import io.github.mcagents.chat.api.AgentPrompt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ChatSettings}.
 *
 * <p>Every check here fires at construction on purpose. A bad vendor code or a
 * blank model that survives into a request comes back as a vendor rejection,
 * which is indistinguishable from a bad credential by the time anyone sees
 * it.</p>
 */
@DisplayName("ChatSettings")
class ChatSettingsTest {

    /**
     * Builds settings differing from the defaults only where a test says so.
     *
     * @param vendorCode The vendor to talk to.
     * @param model The model identifier.
     * @param maxTurns The session bound.
     * @param idleTimeout How long a session may sit unused.
     * @return The new settings.
     */
    private static ChatSettings settings(String vendorCode, String model, int maxTurns, Duration idleTimeout) {
        return new ChatSettings(
                vendorCode,
                model,
                false,
                ChatSettings.DEFAULT_SYSTEM_PROMPT,
                maxTurns,
                idleTimeout,
                AgentPrompt.NO_MAX_TOKENS);
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("uses the platform's default model")
        void usesTheDefaultModel() {
            ChatSettings created = ChatSettings.of("openai");

            assertEquals("openai", created.vendorCode());
            assertEquals(Models.forVendor("openai"), created.model());
        }

        @Test
        @DisplayName("disallows ordinary players, because every message costs money")
        void disallowsPlayersByDefault() {
            assertFalse(ChatSettings.of("openai").playerAllowed());
        }

        @Test
        @DisplayName("applies the shared defaults for everything else")
        void appliesSharedDefaults() {
            ChatSettings created = ChatSettings.of("anthropic");

            assertEquals(ChatSettings.DEFAULT_SYSTEM_PROMPT, created.systemPrompt());
            assertEquals(ChatSettings.DEFAULT_MAX_TURNS, created.maxTurns());
            assertEquals(ChatSettings.DEFAULT_IDLE_TIMEOUT, created.sessionIdleTimeout());
            assertEquals(AgentPrompt.NO_MAX_TOKENS, created.maxTokens());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("refuses a vendor nothing can be sent to")
        void refusesUnknownVendor() {
            assertThrows(IllegalArgumentException.class,
                    () -> settings("mistral", "mistral-large", 20, Duration.ofMinutes(30)));
        }

        @Test
        @DisplayName("refuses a blank model")
        void refusesBlankModel() {
            assertThrows(IllegalArgumentException.class,
                    () -> settings("openai", "   ", 20, Duration.ofMinutes(30)));
        }

        @Test
        @DisplayName("refuses a session bound below two, which cannot hold an exchange")
        void refusesTooFewTurns() {
            assertThrows(IllegalArgumentException.class,
                    () -> settings("openai", "gpt-4o-mini", 1, Duration.ofMinutes(30)));
        }

        @Test
        @DisplayName("refuses an idle timeout that is not positive")
        void refusesNonPositiveIdleTimeout() {
            assertThrows(IllegalArgumentException.class,
                    () -> settings("openai", "gpt-4o-mini", 20, Duration.ZERO));
            assertThrows(IllegalArgumentException.class,
                    () -> settings("openai", "gpt-4o-mini", 20, Duration.ofMinutes(-1)));
        }

        @Test
        @DisplayName("refuses a null component")
        void refusesNulls() {
            assertThrows(NullPointerException.class,
                    () -> settings(null, "gpt-4o-mini", 20, Duration.ofMinutes(30)));
            assertThrows(NullPointerException.class,
                    () -> settings("openai", null, 20, Duration.ofMinutes(30)));
            assertThrows(NullPointerException.class,
                    () -> settings("openai", "gpt-4o-mini", 20, null));
        }
    }

    @Nested
    @DisplayName("normalization")
    class Normalization {

        @Test
        @DisplayName("lowercases and trims the vendor code so it travels as core names it")
        void normalizesTheVendorCode() {
            ChatSettings created = settings("  OpenAI ", "gpt-4o-mini", 20, Duration.ofMinutes(30));

            assertEquals("openai", created.vendorCode());
        }

        @Test
        @DisplayName("trims the model but leaves its case alone")
        void trimsTheModel() {
            // Model identifiers are case sensitive at the vendor, so only the
            // surrounding whitespace a configuration file collects is removed.
            ChatSettings created = settings("openai", "  GPT-4o-Mini  ", 20, Duration.ofMinutes(30));

            assertEquals("GPT-4o-Mini", created.model());
        }
    }
}
