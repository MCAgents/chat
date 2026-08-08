package io.github.mcagents.chat.common;

import io.github.mcagents.chat.api.token.TokenState;
import io.github.mcagents.chat.api.token.TokenStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The credentials configured for one vendor, and which of them is in use.
 *
 * <p>A pool starts at the first credential and stays there. It moves on only
 * when a request fails, and what it does then depends entirely on why:</p>
 *
 * <ul>
 *   <li><strong>Rejected</strong> — the credential is dead. {@link #reject()}
 *       drops it from the pool <em>and from storage</em>, so a key that will
 *       never work again is not retried on every request forever.</li>
 *   <li><strong>Rate limited</strong> — the credential is fine and came too
 *       fast. {@link #rotate()} moves to the next one and <em>keeps</em> this
 *       one. Evicting here would destroy something the user paid for, and
 *       nothing inside the game can undo that.</li>
 * </ul>
 *
 * <p>When the last credential is rejected the pool reports
 * {@link TokenState#EXPIRED} rather than {@link TokenState#NOT_SET}, so the
 * message a server owner sees tells them their keys stopped working rather than
 * that they forgot to add one.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>Every method is synchronized. Rotation happens on whichever thread a
 * failed request completed on, which on Folia can be any region thread, and the
 * operations are compound — read the current credential, drop it, advance —
 * so an atomic field would look safe without being so.</p>
 */
public final class TokenPool {

    /**
     * The vendor these credentials belong to, as core names it.
     */
    private final String vendorCode;

    /**
     * Where the credentials are read from, and written back to on eviction.
     */
    private final TokenStore store;

    /**
     * The credentials still believed good, in the order they are tried. Never
     * {@code null}; empty means nothing usable is left.
     */
    private final List<String> tokens = new ArrayList<>();

    /**
     * Whether every credential this pool ever held has been rejected.
     *
     * <p>This is the only thing separating "expired" from "never configured"
     * once the list is empty, so it is remembered until the next
     * {@link #reload()}.</p>
     */
    private boolean exhausted;

    /**
     * Builds a pool for a vendor and loads its credentials.
     *
     * @param vendorCode The vendor these credentials belong to.
     * @param store Where to read them from.
     * @throws NullPointerException When either argument is {@code null}.
     */
    public TokenPool(String vendorCode, TokenStore store) {
        this.vendorCode = Objects.requireNonNull(vendorCode, "vendorCode cannot be null");
        this.store = Objects.requireNonNull(store, "store cannot be null");
        reload();
    }

    /**
     * Returns the vendor these credentials belong to.
     *
     * @return The vendor code, never {@code null}.
     */
    public String vendorCode() {
        return vendorCode;
    }

    /**
     * Returns the credential to use for the next request.
     *
     * @return The current credential, or empty when none is usable.
     */
    public synchronized Optional<String> current() {
        return tokens.isEmpty() ? Optional.empty() : Optional.of(tokens.get(0));
    }

    /**
     * Reports whether this vendor can currently be called, and if not, why not.
     *
     * @return {@link TokenState#READY} when a credential is available,
     *         {@link TokenState#EXPIRED} when every one was rejected, and
     *         {@link TokenState#NOT_SET} when none was ever configured.
     */
    public synchronized TokenState state() {
        if (!tokens.isEmpty()) {
            return TokenState.READY;
        }
        return exhausted ? TokenState.EXPIRED : TokenState.NOT_SET;
    }

    /**
     * How many credentials are still believed good.
     *
     * <p>Used to bound a retry loop and to report "token 2 of 3" without
     * revealing a credential.</p>
     *
     * @return The remaining count, never negative.
     */
    public synchronized int remaining() {
        return tokens.size();
    }

    /**
     * Drops the current credential permanently, because the vendor rejected it.
     *
     * <p>Removes it from the pool and from storage. Call this <strong>only</strong>
     * for an authentication failure or an explicit "this key is invalid" — never
     * for a rate limit, a timeout, or a server error, none of which say anything
     * about the credential.</p>
     *
     * @return The next usable credential, or empty when that was the last one.
     */
    public synchronized Optional<String> reject() {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }

        String dead = tokens.remove(0);
        exhausted = true;
        store.evict(vendorCode, dead);
        return current();
    }

    /**
     * Moves to the next credential while keeping the current one.
     *
     * <p>For a rate limit: the credential is healthy and simply busy. The
     * rotated credential goes to the back of the queue, so a pool of several
     * keys spreads load rather than hammering one.</p>
     *
     * @return The credential to try next, or empty when the pool is empty. With
     *         a single credential this returns that same one — there is nowhere
     *         else to go, and refusing to return it would be worse.
     */
    public synchronized Optional<String> rotate() {
        if (tokens.size() > 1) {
            tokens.add(tokens.remove(0));
        }
        return current();
    }

    /**
     * Re-reads the credentials from storage, discarding what is held.
     *
     * <p>This is what makes {@code /chat reload} work without a restart. It
     * replaces the list rather than merging into it, and clears the exhausted
     * flag, so a credential the owner has just fixed gets a fresh chance and an
     * evicted one does not come back.</p>
     */
    public synchronized void reload() {
        store.reload();

        tokens.clear();
        exhausted = false;
        for (String token : store.load(vendorCode)) {
            // A blank entry is a placeholder the owner never filled in, not a
            // credential. Treating it as one produces a rejection, which would
            // then silently delete the line they were about to edit.
            if (token != null && !token.isBlank()) {
                tokens.add(token.trim());
            }
        }
    }
}
