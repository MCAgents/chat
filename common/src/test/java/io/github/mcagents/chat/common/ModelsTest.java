package io.github.mcagents.chat.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Models}.
 *
 * <p>The mismatch check is the part that earns its keep: sending a namespaced
 * slug to a direct vendor produces a rejection that looks exactly like a bad
 * API key, and a server owner can lose an afternoon to it.</p>
 */
@DisplayName("Models")
class ModelsTest {

    @Nested
    @DisplayName("forVendor")
    class ForVendor {

        @Test
        @DisplayName("returns a default for every supported vendor")
        void returnsDefaultForEveryVendor() {
            for (String vendor : Models.vendors()) {
                String model = Models.forVendor(vendor);

                assertFalse(model.isBlank(), "no default model recorded for " + vendor);
            }
        }

        @Test
        @DisplayName("ignores case and surrounding whitespace")
        void normalizesTheVendorCode() {
            assertEquals(Models.forVendor("openai"), Models.forVendor("  OpenAI  "));
        }

        @Test
        @DisplayName("refuses a vendor it has no default for")
        void refusesUnknownVendor() {
            assertThrows(IllegalArgumentException.class, () -> Models.forVendor("mistral"));
            assertThrows(IllegalArgumentException.class, () -> Models.forVendor(null));
        }
    }

    @Nested
    @DisplayName("isKnown")
    class IsKnown {

        @Test
        @DisplayName("agrees with the vendor list")
        void agreesWithVendorList() {
            Models.vendors().forEach(vendor -> assertTrue(Models.isKnown(vendor)));

            assertFalse(Models.isKnown("mistral"));
            assertFalse(Models.isKnown(null));
        }
    }

    @Nested
    @DisplayName("looksMismatched")
    class LooksMismatched {

        @Test
        @DisplayName("flags a bare name sent to OpenRouter")
        void flagsBareNameOnOpenRouter() {
            assertTrue(Models.looksMismatched("openrouter", "gpt-4o-mini"));
        }

        @Test
        @DisplayName("flags a namespaced slug sent to a direct vendor")
        void flagsNamespacedSlugOnDirectVendor() {
            assertTrue(Models.looksMismatched("openai", "openai/gpt-4o-mini"));
            assertTrue(Models.looksMismatched("deepseek", "~deepseek/deepseek-v4-flash-latest"));
        }

        @Test
        @DisplayName("accepts each vendor's own default")
        void acceptsEveryDefault() {
            for (String vendor : Models.vendors()) {
                assertFalse(Models.looksMismatched(vendor, Models.forVendor(vendor)),
                        "the default model for " + vendor + " is flagged as mismatched");
            }
        }

        @Test
        @DisplayName("says nothing when there is nothing to compare")
        void staysQuietWithoutInput() {
            assertFalse(Models.looksMismatched(null, "gpt-4o-mini"));
            assertFalse(Models.looksMismatched("openai", null));
            assertFalse(Models.looksMismatched("openai", "   "));
        }
    }
}
