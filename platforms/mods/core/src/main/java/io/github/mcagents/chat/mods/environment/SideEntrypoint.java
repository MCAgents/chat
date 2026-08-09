package io.github.mcagents.chat.mods.environment;

/**
 * What each physical side offers the loader: a start, a stop, and a name.
 *
 * <p>The client and the server half each implement this once. Neither is ever
 * named by type outside its own module — {@link ModBootstrap} resolves the
 * implementation for the detected side by name — so this interface is the only
 * thing the rest of the mod code knows about either of them.</p>
 *
 * <h2>Implementations need a no-argument constructor</h2>
 *
 * <p>Because they are instantiated reflectively. Do the side check in that
 * constructor — {@link SideGuard#requireClient(String)} or its counterpart — so
 * a wiring mistake fails at the moment of construction, with a message, rather
 * than at whatever later moment the first side-bound class is touched.</p>
 */
public interface SideEntrypoint {

    /**
     * Returns the side this entry point belongs to.
     *
     * @return The side, never {@code null}.
     */
    PhysicalSide side();

    /**
     * Starts this side.
     *
     * <p>Called once, by the loader, with the context it assembled. Must not
     * block: a loader waiting on a network call stalls the game's startup.</p>
     *
     * @param context What the loader knows.
     * @throws NullPointerException When {@code context} is {@code null}.
     * @throws WrongSideException When the context names the other side.
     */
    void start(ModContext context);

    /**
     * Stops this side, releasing whatever {@link #start(ModContext)} took.
     *
     * <p>Must be safe to call when {@code start} was never called, and safe to
     * call twice: a loader shutting down after a failed start is exactly when
     * this matters.</p>
     */
    void stop();

    /**
     * Describes this entry point for a diagnostic line.
     *
     * @return A short description. Never {@code null}, and never containing a
     *         credential.
     */
    String describe();
}
