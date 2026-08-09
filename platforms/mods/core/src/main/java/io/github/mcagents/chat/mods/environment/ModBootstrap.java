package io.github.mcagents.chat.mods.environment;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Starts exactly one physical side, and never links the other.
 *
 * <p>This class is the whole reason a single universal jar can carry both
 * halves. It names each entry point as a <strong>string</strong>, not as a
 * type, and resolves only the one matching the side actually running. A
 * dedicated server therefore never loads, links, or verifies a client class —
 * not because the class was stripped from the jar, but because nothing ever
 * asked for it.</p>
 *
 * <p>That is also why neither {@code platforms:mods:client} nor
 * {@code platforms:mods:server} appears in this module's dependencies. If it
 * did, the compiler would happily let someone write
 * {@code new ClientEntrypoint()} here, and the property above would quietly
 * stop being true.</p>
 *
 * <h2>What a loader does</h2>
 *
 * <pre>{@code
 * ModEnvironment.install(PhysicalSide.CLIENT);   // what the loader was told
 * SideEntrypoint running = ModBootstrap.start(
 *         ModContext.of(PhysicalSide.CLIENT, gameDirectory, logger));
 * }</pre>
 */
public final class ModBootstrap {

    /**
     * The client half's entry point, named rather than referenced.
     *
     * <p>Kept in step with the class by
     * {@code ModBootstrapTest} on the client module's own classpath, which is
     * the only place both this constant and that class exist.</p>
     */
    public static final String CLIENT_ENTRYPOINT =
            "io.github.mcagents.chat.mods.client.ClientEntrypoint";

    /**
     * The server half's entry point, named rather than referenced.
     */
    public static final String DEDICATED_SERVER_ENTRYPOINT =
            "io.github.mcagents.chat.mods.server.ServerEntrypoint";

    /**
     * Not instantiable — this class is a dispatcher.
     */
    private ModBootstrap() {
    }

    /**
     * Returns the class name of the entry point for a side.
     *
     * @param side The side to look up.
     * @return The fully qualified class name.
     * @throws NullPointerException When {@code side} is {@code null}.
     */
    public static String entrypointFor(PhysicalSide side) {
        Objects.requireNonNull(side, "side cannot be null");
        return switch (side) {
            case CLIENT -> CLIENT_ENTRYPOINT;
            case DEDICATED_SERVER -> DEDICATED_SERVER_ENTRYPOINT;
        };
    }

    /**
     * Loads the entry point for a side, without starting it.
     *
     * @param side The side to load.
     * @return The entry point, freshly constructed.
     * @throws NullPointerException When {@code side} is {@code null}.
     * @throws IllegalStateException When the half is not on the classpath, or
     *                               cannot be constructed. The message names
     *                               the class, because the usual cause is a
     *                               distribution built without that half.
     * @throws WrongSideException When the half refuses the side it was loaded
     *                            on, thrown by its own constructor.
     */
    public static SideEntrypoint load(PhysicalSide side) {
        String className = entrypointFor(side);
        try {
            Class<?> type = Class.forName(className, true, ModBootstrap.class.getClassLoader());
            return (SideEntrypoint) type.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new IllegalStateException(className + " is not on the classpath, so the "
                    + side.code() + " half cannot start. This build does not carry it.", e);
        } catch (InvocationTargetException e) {
            // The constructor threw. Its own failure is the useful one — a side
            // guard refusal in particular — so it is rethrown rather than
            // buried under a reflection message.
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(className + " could not be constructed.", cause);
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException(className + " is not a usable "
                    + SideEntrypoint.class.getSimpleName() + ".", e);
        }
    }

    /**
     * Loads and starts the half matching the context's side.
     *
     * @param context What the loader knows, including which side it is.
     * @return The started entry point, so the loader can stop it later.
     * @throws NullPointerException When {@code context} is {@code null}.
     * @throws IllegalStateException When that half is not on the classpath.
     */
    public static SideEntrypoint start(ModContext context) {
        Objects.requireNonNull(context, "context cannot be null");

        SideEntrypoint entrypoint = load(context.side());
        entrypoint.start(context);
        return entrypoint;
    }
}
