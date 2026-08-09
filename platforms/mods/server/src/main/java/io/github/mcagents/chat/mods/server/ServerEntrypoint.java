package io.github.mcagents.chat.mods.server;

import io.github.mcagents.chat.mods.ModChatService;
import io.github.mcagents.chat.mods.environment.ModContext;
import io.github.mcagents.chat.mods.environment.PhysicalSide;
import io.github.mcagents.chat.mods.environment.ServerOnly;
import io.github.mcagents.chat.mods.environment.SideEntrypoint;
import io.github.mcagents.chat.mods.environment.WrongSideException;

import java.util.Objects;

/**
 * The server half's entry point.
 *
 * <p>Starts a conversation per player behind the checks that decide who may
 * have one — which is the whole difference from the client half, where there is
 * one player and nobody to check them against.</p>
 *
 * <p>No side guard is asserted in the constructor. Server logic runs on a
 * client too — a single player world is a server — and refusing to construct
 * this there would break single player for nothing. What must not happen is the
 * reverse, and that is guarded on the client half.</p>
 */
@ServerOnly
public final class ServerEntrypoint implements SideEntrypoint {

    /**
     * What this half is called in a failure message.
     */
    private static final String FEATURE = "The MCAgents chat server entry point";

    /**
     * The server chat surface, or {@code null} before {@link #start(ModContext)}
     * and after {@link #stop()}.
     */
    private volatile ServerChatService chat;

    /**
     * {@inheritDoc}
     */
    @Override
    public PhysicalSide side() {
        return PhysicalSide.DEDICATED_SERVER;
    }

    /**
     * Opens the per-player conversations and the checks around them.
     *
     * @param context What the loader knows.
     * @throws NullPointerException When {@code context} is {@code null}.
     * @throws WrongSideException Never from here — a client hosting a single
     *                            player world runs this half legitimately, so
     *                            no side is refused.
     */
    @Override
    public void start(ModContext context) {
        Objects.requireNonNull(context, "context cannot be null");

        ServerChatService started = new ServerChatService(ModChatService.from(context));
        this.chat = started;

        context.logger().info("MCAgents chat ready on " + context.settings().vendorCode()
                + ". " + (context.settings().playerAllowed()
                        ? "Every player may ask."
                        : "Only operators at level " + started.authority().operatorLevel() + " may ask.")
                + " API tokens are managed by MCAgents core.");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Dropping the service drops every conversation with it. That is the
     * intended effect on shutdown, and the shared service holds no thread or
     * handle to release.</p>
     */
    @Override
    public void stop() {
        this.chat = null;
    }

    /**
     * Returns the server chat surface.
     *
     * <p>The loader wires this into its own command tree, building the
     * {@link ChatCaller} from what the <strong>server</strong> knows about the
     * sender — never from anything the client sent.</p>
     *
     * @return The chat surface.
     * @throws IllegalStateException When called before {@link #start(ModContext)}
     *                               or after {@link #stop()}.
     */
    public ServerChatService chat() {
        ServerChatService current = chat;
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
        ServerChatService current = chat;
        return current == null
                ? "MCAgents chat server (not started)"
                : "MCAgents chat server on " + current.shared().settings().vendorCode()
                        + ", " + current.liveSessions() + " conversation(s) live";
    }
}
