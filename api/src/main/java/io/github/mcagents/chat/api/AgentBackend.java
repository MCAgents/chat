package io.github.mcagents.chat.api;

import java.util.concurrent.CompletableFuture;

/**
 * The one thing this project needs from MCAgents core, stated in this
 * project's own types.
 *
 * <p>Everything above this interface — sessions, token pooling, commands —
 * works against it and never against a core class. The single implementation
 * that knows core exists is the reflective bridge in a platform module, which
 * means the reflection lives in exactly one place, can be swapped without
 * touching anything else, and fails where it can be reported cleanly.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>{@link #send(AgentPrompt)} returns immediately and does its work off the
 * calling thread, so it is safe from a server tick. Nothing guarantees which
 * thread the returned future completes on — a callback that touches the game
 * must hop back onto the right scheduler, which on Folia is the one owning the
 * region.</p>
 */
public interface AgentBackend {

    /**
     * Reports whether the backend can currently serve a request.
     *
     * <p>{@code false} when core is absent or its API could not be resolved.
     * Synchronous and cheap: the chat surface calls it before every send so it
     * can answer the player immediately rather than failing a future.</p>
     *
     * @return {@code true} when a request would reach core.
     */
    boolean isAvailable();

    /**
     * Reports whether a vendor has a usable credential, and if not, why not.
     *
     * <p>This project holds no credentials. MCAgents core owns the token file,
     * the rotation, and the eviction; all this asks is what core currently
     * knows, so a command can tell a server owner to add a key or to find out
     * why their keys stopped working.</p>
     *
     * @param vendorCode The vendor to ask about, as core names it.
     * @return {@code "READY"}, {@code "NOT_SET"}, or {@code "EXPIRED"} — core's
     *         own state names, passed through as text because this module does
     *         not compile against core's enum. Any other value means the state
     *         could not be read.
     */
    String tokenState(String vendorCode);

    /**
     * Asks core to re-read its credential file.
     *
     * <p>Backs the reload command. The credentials themselves are core's, so
     * this is a request rather than an update — nothing here holds a token to
     * replace.</p>
     *
     * @return {@code true} when core reloaded.
     */
    boolean reloadTokens();

    /**
     * Sends one exchange and returns the model's reply.
     *
     * @param prompt What to ask, including the vendor, the model, and the full
     *               conversation.
     * @return A CompletableFuture containing the reply, failing with a
     *         {@link ChatException} when the backend is unavailable or the
     *         vendor refuses.
     * @throws NullPointerException When {@code prompt} is {@code null}.
     */
    CompletableFuture<AgentReply> send(AgentPrompt prompt);

    /**
     * Describes this backend for a diagnostic message.
     *
     * <p>Shown by the reload command so a server owner can tell a working
     * bridge from a stubbed one. Must never include a credential.</p>
     *
     * @return A short description, never {@code null}.
     */
    String describe();
}
