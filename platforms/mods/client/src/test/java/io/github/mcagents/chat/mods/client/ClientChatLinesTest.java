package io.github.mcagents.chat.mods.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ClientChatLines}.
 */
@DisplayName("ClientChatLines")
class ClientChatLinesTest {

    @Test
    @DisplayName("leaves a reply that already fits on one line")
    void leavesShortRepliesAlone() {
        assertEquals(List.of("Below y=16."), ClientChatLines.split("Below y=16."));
    }

    @Test
    @DisplayName("breaks between words")
    void breaksBetweenWords() {
        List<String> lines = ClientChatLines.split("diamonds spawn below y equals sixteen", 20, 10);

        assertEquals(List.of("diamonds spawn below", "y equals sixteen"), lines);
    }

    @Test
    @DisplayName("keeps every line within the width")
    void respectsTheWidth() {
        String reply = "Diamonds spawn between bedrock and y equals sixteen, "
                + "and deepslate diamond ore is most common around negative fifty nine.";

        ClientChatLines.split(reply, 24, 20)
                .forEach(line -> assertTrue(line.length() <= 24, "too long: " + line));
    }

    @Test
    @DisplayName("honours the line breaks the model wrote")
    void honoursExistingBreaks() {
        List<String> lines = ClientChatLines.split("first\nsecond\r\nthird", 50, 10);

        assertEquals(List.of("first", "second", "third"), lines);
    }

    @Test
    @DisplayName("drops blank paragraphs rather than drawing empty lines")
    void dropsBlankParagraphs() {
        assertEquals(List.of("one", "two"), ClientChatLines.split("one\n\n   \n\ntwo", 50, 10));
        assertTrue(ClientChatLines.split("   \n  ").isEmpty());
    }

    @Test
    @DisplayName("splits a single word too long to fit rather than letting it run off")
    void splitsAnUnbreakableWord() {
        List<String> lines = ClientChatLines.split("https://example.invalid/a/very/long/link", 12, 10);

        assertTrue(lines.size() > 1);
        lines.forEach(line -> assertTrue(line.length() <= 12, "too long: " + line));
    }

    @Test
    @DisplayName("marks a reply it had to cut short")
    void marksTruncation() {
        // Silently dropping the tail would leave a player unable to tell a
        // short answer from a cut-off one.
        List<String> lines = ClientChatLines.split("one two three four five six", 5, 3);

        assertEquals(3, lines.size());
        assertEquals(ClientChatLines.TRUNCATION_MARKER, lines.get(lines.size() - 1));
    }

    @Test
    @DisplayName("does not mark a reply that fitted exactly")
    void doesNotMarkAnExactFit() {
        List<String> lines = ClientChatLines.split("one two", 3, 2);

        assertEquals(List.of("one", "two"), lines);
    }

    @Test
    @DisplayName("refuses input it cannot draw")
    void refusesUnusableInput() {
        assertThrows(NullPointerException.class, () -> ClientChatLines.split(null));
        assertThrows(IllegalArgumentException.class, () -> ClientChatLines.split("hello", 0, 5));
        assertThrows(IllegalArgumentException.class, () -> ClientChatLines.split("hello", 20, 0));
    }
}
