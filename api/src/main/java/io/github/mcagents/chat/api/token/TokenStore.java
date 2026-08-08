package io.github.mcagents.chat.api.token;

import java.util.List;

/**
 * Where credentials are read from and written back to.
 *
 * <p>Implemented once per side: the server plugin's configuration file, and the
 * shared file under the Minecraft directory that several MCAgents mods read.
 * The pooling logic depends only on this interface, so neither storage shape
 * leaks into it.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>Implementations touch disk and must not be called from a server tick.
 * They must tolerate being called from several threads at once, since eviction
 * can happen on whichever thread a failed request completed on.</p>
 */
public interface TokenStore {

    /**
     * Reads the credentials configured for a vendor, in the order they should
     * be tried.
     *
     * @param vendorCode The vendor to read, as core names it.
     * @return The credentials, oldest-configured first. Empty when none are
     *         configured — never {@code null}.
     */
    List<String> load(String vendorCode);

    /**
     * Permanently removes a credential the vendor rejected.
     *
     * <p>Called only when a credential is known dead — a rejection, never a
     * rate limit and never a network failure. Deleting a healthy credential
     * destroys something the user paid for and cannot be undone from inside the
     * game, so an implementation should treat an unexpected call here as a bug
     * rather than a hint.</p>
     *
     * <p>Removing a credential that is not present is a no-op, not an error.</p>
     *
     * @param vendorCode The vendor the credential belongs to.
     * @param token The credential to remove.
     */
    void evict(String vendorCode, String token);

    /**
     * Re-reads the underlying storage, discarding anything cached.
     *
     * <p>This is what makes credentials replaceable without restarting the
     * server or rejoining the world. It must genuinely re-read rather than
     * merge into what is already in memory — otherwise an evicted credential
     * comes back from the cache.</p>
     */
    void reload();

    /**
     * Describes where this store keeps its credentials, for a diagnostic
     * message.
     *
     * <p>A path or a file name. Must never include a credential.</p>
     *
     * @return A short description, never {@code null}.
     */
    String describe();
}
