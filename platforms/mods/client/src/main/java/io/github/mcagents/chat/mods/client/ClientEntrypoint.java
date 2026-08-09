package io.github.mcagents.chat.mods.client;

import io.github.mcagents.chat.mods.ModChatService;
import io.github.mcagents.chat.mods.environment.ClientOnly;
import io.github.mcagents.chat.mods.environment.ModContext;
import io.github.mcagents.chat.mods.environment.PhysicalSide;
import io.github.mcagents.chat.mods.environment.SideEntrypoint;
import io.github.mcagents.chat.mods.environment.SideGuard;
import io.github.mcagents.chat.mods.environment.WrongSideException;

import java.util.Objects;

/**
 * The client half's entry point.
 *
 * <p>Starts one conversation for the one player who is there, and nothing else.
 * No identity to track, nobody to authorise, no input to police — the person
 * typing owns the machine and the credentials, so every check the server half
 * makes would be a check against themselves.</p>
 *
 * <h2>Lifecycle</h2>
 *
 * <p>A loader constructs this reflectively through
 * {@link io.github.mcagents.chat.mods.environment.ModBootstrap} and calls
 * {@link #start(ModContext)} once. Constructing it on a dedicated server throws
 * immediately rather than failing later somewhere less legible.</p>
 */
@ClientOnly
public final class ClientEntrypoint implements SideEntrypoint {

    /**
     * What this half is called in a failure message.
     */
    private static final String FEATURE = "The MCAgents chat client entry point";

    /**
     * The client chat surface, or {@code null} before {@link #start(ModContext)}
     * and after {@link #stop()}.
     */
    private volatile ClientChatService chat;

    /**
     * Refuses to exist anywhere but a client.
     *
     * @throws WrongSideException When constructed on a dedicated server.
     */
    public ClientEntrypoint() {
        SideGuard.requireClient(FEATURE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhysicalSide side() {
        return PhysicalSide.CLIENT;
    }

    /**
     * Opens the client's single conversation.
     *
     * @param context What the loader knows.
     * @throws NullPointerException When {@code context} is {@code null}.
     * @throws WrongSideException When the context names the other side.
     */
    @Override
    public void start(ModContext context) {
        Objects.requireNonNull(context, "context cannot be null");
        if (!context.side().isClient()) {
            throw new WrongSideException(FEATURE, PhysicalSide.CLIENT, context.side());
        }

        this.chat = new ClientChatService(ModChatService.from(context));

        context.logger().info("MCAgents chat ready on " + context.settings().vendorCode()
                + ". API tokens are managed by MCAgents core.");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Dropping the service drops the conversation with it, which is the
     * intended effect: a client shutting down has nothing worth keeping, and
     * the shared service holds no thread or handle to release.</p>
     */
    @Override
    public void stop() {
        this.chat = null;
    }

    /**
     * Returns the client chat surface.
     *
     * <p>The loader wires this into its own command tree: both loaders build
     * commands on Brigadier, whose types this module does not compile against,
     * so the wiring stays in the loader and the behaviour stays here.</p>
     *
     * @return The chat surface.
     * @throws IllegalStateException When called before {@link #start(ModContext)}
     *                               or after {@link #stop()}.
     */
    public ClientChatService chat() {
        ClientChatService current = chat;
        if (current == null) {
            throw new IllegalStateException(FEATURE + " has not been started.");
        }
        return current;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        ClientChatService current = chat;
        return current == null
                ? "MCAgents chat client (not started)"
                : "MCAgents chat client on " + current.shared().settings().vendorCode();
    }
}
