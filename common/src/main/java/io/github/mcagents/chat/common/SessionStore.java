package io.github.mcagents.chat.common;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every player's running conversation, and the rule that eventually forgets it.
 *
 * <p>Sessions are created on first use and expire after a period of silence.
 * The expiry is what keeps this from being a memory leak dressed up as a
 * feature: without it, a busy server accumulates one growing history per player
 * who ever typed a message, and never releases any of them.</p>
 *
 * <p>Sweeping is done lazily, on access, rather than on a timer. A server that
 * has stopped being used stops accumulating sessions on its own, and there is
 * no scheduled task to register, cancel, or get wrong on Folia.</p>
 */
public final class SessionStore {

    /**
     * The live sessions, keyed by player.
     */
    private final Map<UUID, ChatSession> sessions = new ConcurrentHashMap<>();

    /**
     * How many turns each new session keeps.
     */
    private final int maxTurns;

    /**
     * How long a session may sit unused before it is forgotten, in nanoseconds.
     */
    private final long idleTimeoutNanos;

    /**
     * Creates a store.
     *
     * @param maxTurns The most turns each session keeps.
     * @param idleTimeout How long a session may sit unused. Must be positive.
     * @throws IllegalArgumentException When {@code idleTimeout} is zero or
     *                                  negative, which would expire every
     *                                  session the moment it was created.
     */
    public SessionStore(int maxTurns, Duration idleTimeout) {
        Objects.requireNonNull(idleTimeout, "idleTimeout cannot be null");
        if (idleTimeout.isZero() || idleTimeout.isNegative()) {
            throw new IllegalArgumentException("idleTimeout must be positive");
        }
        this.maxTurns = maxTurns;
        this.idleTimeoutNanos = idleTimeout.toNanos();
    }

    /**
     * Returns a player's session, creating it if this is their first message.
     *
     * <p>Sweeps expired sessions first, so the returned session is never a
     * stale one that happened to survive.</p>
     *
     * @param playerUuid The player.
     * @return Their session, never {@code null}.
     * @throws NullPointerException When {@code playerUuid} is {@code null}.
     */
    public ChatSession get(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid cannot be null");

        sweep();
        return sessions.computeIfAbsent(playerUuid, key -> new ChatSession(maxTurns));
    }

    /**
     * Forgets one player's conversation.
     *
     * @param playerUuid The player.
     * @return {@code true} when a session existed and has now been dropped.
     */
    public boolean clear(UUID playerUuid) {
        return sessions.remove(playerUuid) != null;
    }

    /**
     * Forgets every conversation.
     *
     * <p>Called on reload: settings that shape a prompt may have changed, and
     * continuing a conversation half built under the old ones produces replies
     * nobody can explain.</p>
     */
    public void clearAll() {
        sessions.clear();
    }

    /**
     * How many conversations are currently held.
     *
     * @return The live session count, after sweeping expired ones.
     */
    public int size() {
        sweep();
        return sessions.size();
    }

    /**
     * Drops every session that has been idle past the timeout.
     */
    private void sweep() {
        long now = System.nanoTime();
        sessions.values().removeIf(session -> session.idleNanos(now) > idleTimeoutNanos);
    }
}
