package io.github.mcagents.chat.common;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who currently has a request in flight.
 *
 * <p>One at a time, per player. A second message sent while the first is still
 * being answered is refused rather than queued, for three reasons: each request
 * costs the server owner money, the replies would arrive out of order and read
 * as nonsense, and both would be built from the same conversation history —
 * so the second would be answered as though the first had never been asked.</p>
 *
 * <h2>Why there is no timer here</h2>
 *
 * <p>A lock that is never released locks a player out for good, so something has
 * to guarantee release. That something is MCAgents core: it bounds every request
 * with its own timeout, so the future always completes — with a reply, a failure,
 * or a timeout — and the release always runs.</p>
 *
 * <p>This project therefore holds no timer and no timeout setting of its own.
 * Adding one would mean two places defining how long a request may take, and the
 * one here would have to duplicate core's to be correct.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>A request is acquired on whichever thread the command ran on and released
 * on whichever thread the reply completed on — different threads by definition,
 * and on Folia potentially different regions. {@link ConcurrentHashMap} gives the
 * atomic add and remove that needs, and {@link #tryAcquire(UUID)} is a single
 * atomic operation rather than a check followed by a set, so two commands racing
 * cannot both win.</p>
 */
public final class PendingRequests {

    /**
     * The players with a request in flight.
     *
     * <p>A set rather than a count: one is the limit, so there is nothing to
     * count, and a count could drift out of step with reality if a release ever
     * ran twice.</p>
     */
    private final Set<UUID> inFlight = ConcurrentHashMap.newKeySet();

    /**
     * Creates an empty register.
     */
    public PendingRequests() {
        // Intentionally empty — the set is initialised at its declaration.
    }

    /**
     * Claims the right to send, if the player has nothing already in flight.
     *
     * <p>Atomic: two commands arriving together cannot both succeed.</p>
     *
     * @param playerUuid Who is asking.
     * @return {@code true} when the caller may send, {@code false} when a
     *         request is already running for that player.
     * @throws NullPointerException When {@code playerUuid} is {@code null}.
     */
    public boolean tryAcquire(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid cannot be null");
        return inFlight.add(playerUuid);
    }

    /**
     * Releases a player's claim.
     *
     * <p>Idempotent, so a release that somehow runs twice is harmless. Must be
     * called on every outcome — reply, failure, and timeout alike — which is why
     * callers attach it to the future's completion rather than to its success.</p>
     *
     * @param playerUuid Who was asking.
     */
    public void release(UUID playerUuid) {
        if (playerUuid != null) {
            inFlight.remove(playerUuid);
        }
    }

    /**
     * Reports whether a player is currently waiting on a reply.
     *
     * @param playerUuid The player to check.
     * @return {@code true} when a request is in flight for them.
     */
    public boolean isWaiting(UUID playerUuid) {
        return playerUuid != null && inFlight.contains(playerUuid);
    }

    /**
     * How many requests are in flight across every player.
     *
     * @return The count, for a diagnostic line.
     */
    public int size() {
        return inFlight.size();
    }

    /**
     * Forgets every claim.
     *
     * <p>Used on reload and shutdown. A request already in flight still
     * completes and its reply is still delivered — this only means the player is
     * not held back from sending another in the meantime, which is the right
     * trade when the alternative is a player stuck behind a claim nothing will
     * release.</p>
     */
    public void clear() {
        inFlight.clear();
    }
}
