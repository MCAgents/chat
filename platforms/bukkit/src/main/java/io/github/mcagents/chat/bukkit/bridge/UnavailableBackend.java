package io.github.mcagents.chat.bukkit.bridge;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.api.AgentPrompt;
import io.github.mcagents.chat.api.AgentReply;
import io.github.mcagents.chat.api.ChatException;

import java.util.concurrent.CompletableFuture;

/**
 * The backend used when MCAgents core is absent or could not be resolved.
 *
 * <p>Its existence is what lets the plugin enable cleanly without core. Without
 * it, every call site would need a null check and the plugin would either fail
 * to load or throw the first time a player typed a message — both worse than a
 * command that politely says the backend is unavailable.</p>
 */
public final class UnavailableBackend implements AgentBackend {

    /**
     * Why the real backend could not be built, phrased for a server owner.
     */
    private final String reason;

    /**
     * Creates an unavailable backend.
     *
     * @param reason Why the real backend could not be built. Shown in the
     *               reload command's output.
     */
    public UnavailableBackend(String reason) {
        this.reason = reason;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Always {@code false} — that is this class's entire purpose.</p>
     */
    @Override
    public boolean isAvailable() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Always unknown: with no backend there is nobody to ask about
     * credentials.</p>
     */
    @Override
    public String tokenState(String vendorCode) {
        return "UNKNOWN";
    }

    /**
     * {@inheritDoc}
     *
     * <p>Always {@code false}: there is nothing to reload.</p>
     */
    @Override
    public boolean reloadTokens() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Always fails, and never reaches a network.</p>
     */
    @Override
    public CompletableFuture<AgentReply> send(AgentPrompt prompt) {
        return CompletableFuture.failedFuture(
                new ChatException(ChatException.Kind.BACKEND_UNAVAILABLE, reason));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        return "unavailable — " + reason;
    }
}
