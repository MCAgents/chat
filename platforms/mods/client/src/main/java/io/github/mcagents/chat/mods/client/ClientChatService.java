package io.github.mcagents.chat.mods.client;

import io.github.mcagents.chat.mods.ModChatService;
import io.github.mcagents.chat.mods.environment.ClientOnly;
import io.github.mcagents.chat.mods.environment.SideGuard;
import io.github.mcagents.chat.mods.environment.WrongSideException;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The chat surface a client talks to.
 *
 * <p>A client has exactly one player, so there is no identity to key a
 * conversation on and nobody to authorise: the person typing owns the machine
 * and pays for the credentials. That is what makes this half so much smaller
 * than its server counterpart, and why the two are separate rather than one
 * class with a branch in it.</p>
 *
 * <p>{@link #CLIENT_SESSION} stands in for the missing player identity, which
 * keeps the shared {@link ModChatService} keyed on a session the same way on
 * both sides rather than growing a single-user special case.</p>
 */
@ClientOnly
public final class ClientChatService {

    /**
     * The identity the client's single conversation is keyed on.
     *
     * <p>Fixed rather than random, so a conversation survives this object being
     * rebuilt by a reload.</p>
     */
    public static final UUID CLIENT_SESSION = UUID.nameUUIDFromBytes("mcagents-chat-client".getBytes());

    /**
     * What this half is called in a failure message.
     */
    private static final String FEATURE = "The MCAgents client chat surface";

    /**
     * The shared, side-agnostic service underneath.
     */
    private final ModChatService shared;

    /**
     * Wraps the shared service for a client.
     *
     * @param shared The shared service.
     * @throws NullPointerException When {@code shared} is {@code null}.
     * @throws WrongSideException When constructed on a dedicated server.
     */
    public ClientChatService(ModChatService shared) {
        SideGuard.requireClient(FEATURE);
        this.shared = Objects.requireNonNull(shared, "shared cannot be null");
    }

    /**
     * Sends a message and returns the reply text.
     *
     * <p>Returns immediately. The future completes on an unspecified thread, so
     * a caller that touches the game must hop back onto the client thread
     * before drawing the reply.</p>
     *
     * @param message What the player said.
     * @return A CompletableFuture containing the reply text, failing with a
     *         {@link io.github.mcagents.chat.api.ChatException}.
     * @throws NullPointerException When {@code message} is {@code null}.
     * @throws IllegalArgumentException When the message is blank.
     */
    public CompletableFuture<String> ask(String message) {
        return shared.ask(CLIENT_SESSION, message);
    }

    /**
     * Forgets the conversation, so the next message starts fresh.
     *
     * @return {@code true} when there was a conversation to forget.
     */
    public boolean clear() {
        return shared.clear(CLIENT_SESSION);
    }

    /**
     * Reports whether a reply is already on its way.
     *
     * <p>Worth asking before sending: the shared service refuses a second
     * request rather than queueing it, and telling the player up front reads
     * better than a failed future a moment later.</p>
     *
     * @return {@code true} when a request is in flight.
     */
    public boolean isWaiting() {
        return shared.isWaiting(CLIENT_SESSION);
    }

    /**
     * Forgets the conversation and re-applies the settings.
     */
    public void reload() {
        shared.reload();
    }

    /**
     * Returns the shared service underneath.
     *
     * @return The shared service, never {@code null}.
     */
    public ModChatService shared() {
        return shared;
    }
}
