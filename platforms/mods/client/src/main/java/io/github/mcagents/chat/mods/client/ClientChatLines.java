package io.github.mcagents.chat.mods.client;

import io.github.mcagents.chat.mods.environment.ClientOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Turns a model's reply into lines a client chat box can draw.
 *
 * <p>Local rendering, and nothing else: no state, no I/O, no game types. A
 * model answers in paragraphs; a chat box shows short lines and truncates
 * rather than wrapping, so a reply that is not split is a reply that is partly
 * unread.</p>
 *
 * <h2>Why this is client-only</h2>
 *
 * <p>A dedicated server writes replies to a player's chat too, but it does not
 * know that player's chat width, GUI scale, or whether they have the box
 * expanded — a client knows all three about itself. Wrapping on the server
 * would be guessing on someone else's behalf. The difference is small, and that
 * is the point: a small difference between the sides still belongs on one side,
 * not behind a branch inside shared code.</p>
 */
@ClientOnly
public final class ClientChatLines {

    /**
     * How many characters fit on a line of the client's chat box at the default
     * scale and width.
     *
     * <p>An approximation, and deliberately conservative: Minecraft's font is
     * proportional, so no character count is exactly right. Wrapping a little
     * early costs a line; wrapping late costs the end of the sentence.</p>
     */
    public static final int DEFAULT_WIDTH = 50;

    /**
     * The most lines one reply may occupy.
     *
     * <p>A model asked for brevity still occasionally writes an essay, and an
     * essay pushes every other message out of the chat box. The tail is dropped
     * with a marker rather than silently, so a player can tell the difference
     * between a short answer and a truncated one.</p>
     */
    public static final int DEFAULT_MAX_LINES = 12;

    /**
     * What replaces the dropped tail of an over-long reply.
     */
    public static final String TRUNCATION_MARKER = "…";

    /**
     * Not instantiable — this class is a formatter.
     */
    private ClientChatLines() {
    }

    /**
     * Splits a reply into lines at the default width and line bound.
     *
     * @param reply The reply text.
     * @return The lines to draw, in order.
     * @throws NullPointerException When {@code reply} is {@code null}.
     */
    public static List<String> split(String reply) {
        return split(reply, DEFAULT_WIDTH, DEFAULT_MAX_LINES);
    }

    /**
     * Splits a reply into lines.
     *
     * <p>Breaks between words where it can, and honours the line breaks the
     * model already wrote. A single word longer than the width is split rather
     * than left to run off the edge.</p>
     *
     * @param reply The reply text.
     * @param width The most characters to put on one line.
     * @param maxLines The most lines to produce, including the truncation
     *                 marker.
     * @return The lines to draw, in order. Empty for a reply that was only
     *         whitespace.
     * @throws NullPointerException When {@code reply} is {@code null}.
     * @throws IllegalArgumentException When the width or the line bound is not
     *                                  positive.
     */
    public static List<String> split(String reply, int width, int maxLines) {
        Objects.requireNonNull(reply, "reply cannot be null");
        if (width < 1) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (maxLines < 1) {
            throw new IllegalArgumentException("maxLines must be positive");
        }

        List<String> lines = new ArrayList<>();
        for (String paragraph : reply.split("\\R")) {
            String remaining = paragraph.strip();
            if (remaining.isEmpty()) {
                continue;
            }
            while (!remaining.isEmpty()) {
                if (lines.size() == maxLines) {
                    return truncated(lines);
                }

                if (remaining.length() <= width) {
                    lines.add(remaining);
                    break;
                }

                int split = remaining.lastIndexOf(' ', width);
                if (split <= 0) {
                    // One long word — a URL or an identifier. Splitting it
                    // beats losing the rest of the reply off the right edge.
                    split = width;
                }
                lines.add(remaining.substring(0, split).stripTrailing());
                remaining = remaining.substring(split).stripLeading();
            }
        }
        return lines;
    }

    /**
     * Replaces the last line with the truncation marker.
     *
     * @param lines The lines produced so far, at the bound.
     * @return The lines to draw, ending in the marker.
     */
    private static List<String> truncated(List<String> lines) {
        List<String> capped = new ArrayList<>(lines);
        capped.set(capped.size() - 1, TRUNCATION_MARKER);
        return capped;
    }
}
