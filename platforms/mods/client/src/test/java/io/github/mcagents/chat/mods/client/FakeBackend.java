package io.github.mcagents.chat.mods.client;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.api.AgentPrompt;
import io.github.mcagents.chat.api.AgentReply;
import io.github.mcagents.chat.api.ChatException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A backend that answers from memory and records what it was asked.
 *
 * <p>Local to this module's tests on purpose. A test double shared across
 * module boundaries would need a test-fixtures publication on {@code common},
 * which would ship an extra artifact for no runtime benefit — a copy of thirty
 * lines is the cheaper trade.</p>
 */
final class FakeBackend implements AgentBackend {

    /**
     * Every prompt this backend was sent, in order.
     */
    private final List<AgentPrompt> prompts = new ArrayList<>();

    /**
     * What to answer with, or {@code null} to fail instead.
     */
    private final String reply;

    /**
     * Whether this backend reports itself as usable.
     */
    private final boolean available;

    /**
     * Builds a backend that answers.
     *
     * @param reply What to answer with.
     */
    private FakeBackend(String reply, boolean available) {
        this.reply = reply;
        this.available = available;
    }

    /**
     * Builds a backend that answers every prompt with the same text.
     *
     * @param reply What to answer with.
     * @return The new backend.
     */
    static FakeBackend answering(String reply) {
        return new FakeBackend(reply, true);
    }

    /**
     * Builds a backend that reports itself unusable, as an absent core plugin
     * would.
     *
     * @return The new backend.
     */
    static FakeBackend unavailable() {
        return new FakeBackend(null, false);
    }

    /**
     * Builds a backend that accepts a prompt and then fails it, as a vendor
     * rejection does.
     *
     * @return The new backend.
     */
    static FakeBackend failing() {
        return new FakeBackend(null, true);
    }

    /**
     * Returns every prompt this backend was sent.
     *
     * @return The prompts, in order.
     */
    List<AgentPrompt> prompts() {
        return List.copyOf(prompts);
    }

    /**
     * Returns the text of the most recent user turn, which is what a caller
     * actually sent.
     *
     * @return The last message received.
     * @throws IllegalStateException When nothing was sent.
     */
    String lastMessage() {
        if (prompts.isEmpty()) {
            throw new IllegalStateException("nothing was sent");
        }
        List<io.github.mcagents.chat.api.ChatTurn> history =
                prompts.get(prompts.size() - 1).history();
        return history.get(history.size() - 1).content();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return available;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized CompletableFuture<AgentReply> send(AgentPrompt prompt) {
        prompts.add(prompt);
        if (reply == null) {
            return CompletableFuture.failedFuture(
                    new ChatException(ChatException.Kind.VENDOR_ERROR, "no reply configured"));
        }
        return CompletableFuture.completedFuture(AgentReply.of(reply));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        return "a fake backend";
    }
}
