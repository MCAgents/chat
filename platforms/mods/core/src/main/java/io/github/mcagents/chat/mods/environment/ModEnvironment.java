package io.github.mcagents.chat.mods.environment;

import java.util.Objects;
import java.util.Optional;

/**
 * Which physical side this process is, resolved once and answered cheaply.
 *
 * <p>Three sources, most authoritative first:</p>
 *
 * <ol>
 *   <li><strong>What the loader said.</strong> A loader knows its own
 *       distribution and is told before it loads a single mod class.
 *       {@link #install(PhysicalSide)} is how a loader entry point passes that
 *       on, and it is always right.</li>
 *   <li><strong>The {@value #SIDE_PROPERTY} system property.</strong> The
 *       escape hatch, and what a test uses when it is not exercising
 *       {@link #install(PhysicalSide)} itself.</li>
 *   <li><strong>A classpath probe.</strong> {@value #CLIENT_MARKER_CLASS} is
 *       present in a client distribution and absent from a dedicated server
 *       one, which is exactly the difference this class exists to detect.</li>
 * </ol>
 *
 * <p>The probe is last on purpose. It is a guess about someone else's jar, and
 * a guess that is wrong in the safe direction — an unrecognised classpath reads
 * as a dedicated server, so the client half is not started somewhere it cannot
 * run.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>The resolved side is held in a {@code volatile} field and never changes
 * after a loader installs it, so any thread may ask at any time.</p>
 */
public final class ModEnvironment {

    /**
     * The system property that overrides detection.
     *
     * <p>Values are the {@link PhysicalSide#code()} names, plus {@code server}
     * as a spelling of {@code dedicated_server}. An unreadable value is
     * ignored rather than fatal — see {@link PhysicalSide#fromName(String)}.</p>
     */
    public static final String SIDE_PROPERTY = "mcagents.side";

    /**
     * The class whose presence means this is a client distribution.
     *
     * <p>The launcher's own entry point. It exists in every client jar and in
     * no dedicated server jar, and unlike a rendering class it is not something
     * a mod would ever pull onto a server classpath by accident.</p>
     */
    public static final String CLIENT_MARKER_CLASS = "net.minecraft.client.main.Main";

    /**
     * The resolved side, or {@code null} until something asks or a loader
     * installs one.
     */
    private static volatile PhysicalSide side;

    /**
     * Not instantiable — this class is a single resolved answer.
     */
    private ModEnvironment() {
    }

    /**
     * Records the side the loader reported.
     *
     * <p>Call this first, from the loader entry point, before anything else
     * runs. It overrides both the property and the probe, because a loader is
     * told what it is and this class can only guess.</p>
     *
     * @param reported The side the loader reported.
     * @throws NullPointerException When {@code reported} is {@code null}.
     */
    public static void install(PhysicalSide reported) {
        side = Objects.requireNonNull(reported, "reported cannot be null");
    }

    /**
     * Forgets the resolved side, so the next question resolves again.
     *
     * <p>For tests. Nothing in a running game should call this: the physical
     * side of a process does not change while it runs.</p>
     */
    public static void reset() {
        side = null;
    }

    /**
     * Returns the side this process is running on.
     *
     * @return The resolved side, never {@code null}.
     */
    public static PhysicalSide current() {
        PhysicalSide known = side;
        if (known == null) {
            known = resolve();
            side = known;
        }
        return known;
    }

    /**
     * Reports whether this process is a client.
     *
     * @return {@code true} on a client, including one hosting a single player
     *         world.
     */
    public static boolean isClient() {
        return current().isClient();
    }

    /**
     * Reports whether this process is a dedicated server.
     *
     * @return {@code true} on a dedicated server.
     */
    public static boolean isDedicatedServer() {
        return current().isDedicatedServer();
    }

    /**
     * Resolves the side from the property, then from the classpath.
     *
     * @return The resolved side, never {@code null}.
     */
    private static PhysicalSide resolve() {
        Optional<PhysicalSide> overridden = fromProperty();
        if (overridden.isPresent()) {
            return overridden.get();
        }
        return detect();
    }

    /**
     * Reads the override system property.
     *
     * @return The overridden side, or empty when the property is unset, blank,
     *         or names no side.
     */
    private static Optional<PhysicalSide> fromProperty() {
        try {
            return PhysicalSide.fromName(System.getProperty(SIDE_PROPERTY));
        } catch (SecurityException e) {
            // A security manager refusing the read is not a reason to fail to
            // start. Fall through to the probe.
            return Optional.empty();
        }
    }

    /**
     * Detects the side by looking for the client marker class.
     *
     * @return {@link PhysicalSide#CLIENT} when the marker is on the classpath,
     *         {@link PhysicalSide#DEDICATED_SERVER} otherwise.
     */
    static PhysicalSide detect() {
        try {
            // Deliberately not initializing the class: presence is the whole
            // question, and running a Minecraft class's static initializer here
            // would be a side effect nobody asked for.
            Class.forName(CLIENT_MARKER_CLASS, false, ModEnvironment.class.getClassLoader());
            return PhysicalSide.CLIENT;
        } catch (ClassNotFoundException | LinkageError e) {
            return PhysicalSide.DEDICATED_SERVER;
        }
    }
}
