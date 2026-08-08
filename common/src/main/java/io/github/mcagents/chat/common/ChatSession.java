package io.github.mcagents.chat.common;

import io.github.mcagents.chat.api.ChatTurn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * One player's running conversation.
 *
 * <p>History is bounded and the bound is not optional. Every turn is resent on
 * every request, so an unbounded session grows the prompt — and its cost — with
 * no ceiling, until the vendor rejects it for length. When the bound is reached
 * the oldest turns are dropped.</p>
 *
 * <p>Turns are dropped in <strong>pairs</strong>, oldest first, so the history
 * always begins with a player turn. A history starting with a model reply reads
 * as though the model spoke unprompted, and some vendors reject it outright.</p>
 *
 * <p>Nothing here is persisted. A session lives in memory for as long as the
 * player keeps talking and is gone when the server stops — this project has no
 * database, by design.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>Every method is synchronized. A player can send a second message while the
 * first is still in flight, and the reply is appended from whichever thread the
 * request completed on.</p>
 */
public final class ChatSession {

    /**
     * The turns so far, oldest first. Bounded by {@link #maxTurns}.
     */
    private final Deque<ChatTurn> turns = new ArrayDeque<>();

    /**
     * The most turns this session keeps. Always at least two, so a session can
     * hold one complete exchange.
     */
    private final int maxTurns;

    /**
     * When this session was last used, as a monotonic timestamp.
     *
     * <p>Read by {@link SessionStore} to expire idle sessions. Monotonic rather
     * than wall clock so a system clock change cannot make a session look
     * arbitrarily old or new.</p>
     */
    private long lastUsedNanos;

    /**
     * Creates an empty session.
     *
     * @param maxTurns The most turns to keep. Values below two are raised to
     *                 two, since a session that cannot hold one exchange is not
     *                 a session.
     */
    public ChatSession(int maxTurns) {
        this.maxTurns = Math.max(2, maxTurns);
        this.lastUsedNanos = System.nanoTime();
    }

    /**
     * Appends a turn, dropping the oldest pair when the bound is reached.
     *
     * @param turn The turn to append.
     * @throws NullPointerException When {@code turn} is {@code null}.
     */
    public synchronized void append(ChatTurn turn) {
        Objects.requireNonNull(turn, "turn cannot be null");

        turns.addLast(turn);
        lastUsedNanos = System.nanoTime();

        // Drop whole exchanges so the history keeps starting with a player
        // turn. Dropping one at a time would leave it starting with a reply.
        while (turns.size() > maxTurns) {
            turns.pollFirst();
            if (turns.size() > 1) {
                turns.pollFirst();
            }
        }
    }

    /**
     * Returns the conversation so far, oldest turn first.
     *
     * @return An immutable snapshot. Later turns do not appear in an
     *         already returned list.
     */
    public synchronized List<ChatTurn> history() {
        return List.copyOf(new ArrayList<>(turns));
    }

    /**
     * Forgets everything, starting the conversation over.
     */
    public synchronized void clear() {
        turns.clear();
        lastUsedNanos = System.nanoTime();
    }

    /**
     * How many turns this session currently holds.
     *
     * @return The turn count.
     */
    public synchronized int size() {
        return turns.size();
    }

    /**
     * How long this session has been idle.
     *
     * @param nowNanos The current {@link System#nanoTime()} reading.
     * @return Nanoseconds since the last append.
     */
    synchronized long idleNanos(long nowNanos) {
        return nowNanos - lastUsedNanos;
    }
}
