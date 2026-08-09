package io.github.mcagents.chat.mods.server;

import io.github.mcagents.chat.api.ChatException;
import io.github.mcagents.chat.common.ChatSettings;
import io.github.mcagents.chat.mods.ModChatService;
import io.github.mcagents.chat.mods.environment.ServerOnly;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The chat surface a dedicated server talks to.
 *
 * <p>Everything the client half does not have to do happens here:</p>
 *
 * <ul>
 *   <li><strong>One conversation per player</strong>, keyed on the identity the
 *       server authenticated — never on anything a client supplied, or a player
 *       could read someone else's conversation by choosing their session
 *       id.</li>
 *   <li><strong>Authorisation before anything is sent</strong>, because the
 *       person typing is not the person paying. See
 *       {@link ServerChatAuthority}.</li>
 *   <li><strong>Validation before anything is sent</strong>, because a message
 *       costs money whether or not it was worth asking. See
 *       {@link ChatInputPolicy}.</li>
 * </ul>
 *
 * <p>Both checks run <em>before</em> the message reaches the shared service, so
 * a refusal costs nothing: nothing is appended to the conversation, nothing
 * leaves the machine, and nothing is billed.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>{@link #ask(ChatCaller, String)} returns immediately and is safe to call
 * from a server tick. The future completes on an unspecified thread, so a
 * caller that touches the game must hop back onto the right scheduler.</p>
 */
@ServerOnly
public final class ServerChatService {

    /**
     * The shared, side-agnostic service underneath.
     */
    private final ModChatService shared;

    /**
     * What a message must satisfy to be worth sending.
     */
    private final ChatInputPolicy policy;

    /**
     * Who may ask. Replaced wholesale on reload, never mutated, so a request in
     * flight finishes under the rules it started with.
     */
    private volatile ServerChatAuthority authority;

    /**
     * Wraps the shared service for a server, with the default input policy.
     *
     * @param shared The shared service.
     * @throws NullPointerException When {@code shared} is {@code null}.
     */
    public ServerChatService(ModChatService shared) {
        this(shared, new ChatInputPolicy());
    }

    /**
     * Wraps the shared service for a server.
     *
     * @param shared The shared service.
     * @param policy What a message must satisfy.
     * @throws NullPointerException When either argument is {@code null}.
     */
    public ServerChatService(ModChatService shared, ChatInputPolicy policy) {
        this.shared = Objects.requireNonNull(shared, "shared cannot be null");
        this.policy = Objects.requireNonNull(policy, "policy cannot be null");
        this.authority = new ServerChatAuthority(shared.settings());
    }

    /**
     * Returns who may ask.
     *
     * @return The authority, never {@code null}.
     */
    public ServerChatAuthority authority() {
        return authority;
    }

    /**
     * Returns what a message must satisfy.
     *
     * @return The input policy, never {@code null}.
     */
    public ChatInputPolicy policy() {
        return policy;
    }

    /**
     * Explains why a caller's message would be refused.
     *
     * <p>Synchronous and cheap, so a command can answer the player immediately
     * rather than failing a future a moment later. Checks the caller first: a
     * player who may not chat at all should not be told their message was too
     * long, which would confirm that chat exists and is worth probing.</p>
     *
     * @param caller Who is asking, as the server knows them.
     * @param message What they typed.
     * @return The reason to show them, or empty when the message may be sent.
     * @throws NullPointerException When {@code message} is {@code null}.
     */
    public Optional<String> refusalFor(ChatCaller caller, String message) {
        Objects.requireNonNull(message, "message cannot be null");

        Optional<String> denied = authority.refusalFor(caller);
        if (denied.isPresent()) {
            return denied;
        }
        return policy.refusalFor(message);
    }

    /**
     * Sends a player's message and returns the reply text.
     *
     * <p>Refuses before sending anything when the caller may not ask or the
     * message may not be sent, so a refusal costs nothing at all.</p>
     *
     * @param caller Who is asking, as the server knows them.
     * @param message What they said.
     * @return A CompletableFuture containing the reply text. Fails with a
     *         {@link ChatException} of kind
     *         {@link ChatException.Kind#NOT_ALLOWED} when refused, and with the
     *         shared service's own failures otherwise.
     * @throws NullPointerException When {@code message} is {@code null}.
     */
    public CompletableFuture<String> ask(ChatCaller caller, String message) {
        Optional<String> refusal = refusalFor(caller, message);
        if (refusal.isPresent()) {
            return CompletableFuture.failedFuture(
                    new ChatException(ChatException.Kind.NOT_ALLOWED, refusal.get()));
        }

        // Cleaned rather than raw: the policy already decided this text is
        // acceptable, and sending the original would mean paying for whatever
        // it stripped.
        String cleaned = policy.clean(message).orElseThrow();
        return shared.ask(caller.uniqueId(), cleaned);
    }

    /**
     * Forgets one player's conversation.
     *
     * <p>Needs no permission. A player may always drop their own conversation —
     * it is theirs, forgetting it costs nothing, and it is the way out if they
     * are ever left waiting on a reply that never came.</p>
     *
     * @param caller Whose conversation to forget.
     * @return {@code true} when there was one to forget.
     * @throws NullPointerException When {@code caller} is {@code null}.
     */
    public boolean clear(ChatCaller caller) {
        Objects.requireNonNull(caller, "caller cannot be null");
        return shared.clear(caller.uniqueId());
    }

    /**
     * Reports whether a player is waiting on a reply.
     *
     * @param caller The player to check.
     * @return {@code true} when a request is in flight for them.
     * @throws NullPointerException When {@code caller} is {@code null}.
     */
    public boolean isWaiting(ChatCaller caller) {
        Objects.requireNonNull(caller, "caller cannot be null");
        return shared.isWaiting(caller.uniqueId());
    }

    /**
     * How many conversations are currently held in memory.
     *
     * @return The live session count, for a diagnostic line.
     */
    public int liveSessions() {
        return shared.liveSessions();
    }

    /**
     * Forgets every conversation and adopts new settings.
     *
     * <p>The authority is rebuilt from them, so an owner who closed chat to
     * players closes it for everyone already in a conversation too.</p>
     *
     * @param updated The settings to adopt.
     * @throws NullPointerException When {@code updated} is {@code null}.
     */
    public void reload(ChatSettings updated) {
        Objects.requireNonNull(updated, "updated cannot be null");

        shared.reload(updated);
        this.authority = authority.withSettings(updated);
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
