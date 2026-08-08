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
     * Supplies the credential the backend should use for a vendor from now on.
     *
     * <p>Called at startup, on reload, and whenever the token pool rotates.
     * Replacing a credential for a vendor already configured is expected and
     * must take effect for the next request.</p>
     *
     * <p>Implementations must never log, echo, or otherwise reveal
     * {@code apiKey}.</p>
     *
     * @param vendorCode The vendor to configure, as core names it.
     * @param apiKey The credential to use.
     * @return {@code true} when the backend accepted the credential. This says
     *         nothing about whether the vendor will accept it — no network call
     *         is made.
     */
    boolean useToken(String vendorCode, String apiKey);

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
