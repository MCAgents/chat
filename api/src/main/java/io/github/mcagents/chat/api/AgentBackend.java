package io.github.mcagents.chat.api;

import java.util.concurrent.CompletableFuture;

/**
 * The one thing this project needs from MCAgents core: send a prompt, get a
 * reply.
 *
 * <p><strong>There is deliberately no credential method here.</strong> MCAgents
 * core owns the token file, loads it automatically, rotates on a rate limit, and
 * evicts on a rejection — none of which this project can see, set, or trigger.
 * A consumer sends a prompt and gets an answer or a failure; that is the entire
 * surface, and it is why nothing in this repository has to be trusted with a
 * key.</p>
 *
 * <p>Everything above this interface — sessions, commands — works against it and
 * never against a core class. The single implementation
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
