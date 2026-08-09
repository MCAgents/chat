package io.github.mcagents.chat.mods.environment;

import java.util.Locale;
import java.util.Optional;

/**
 * Which physical machine the code is running on.
 *
 * <p><strong>Physical, not logical.</strong> This is the distinction that
 * decides whether a class exists at all — a dedicated server jar has no
 * rendering classes, so touching one there is a {@code NoClassDefFoundError}
 * rather than a bug you can catch. It is not the same question as "is this the
 * integrated server thread inside a client", which is a logical side and
 * changes nothing about what is on the classpath.</p>
 *
 * <p>A client hosting a single player world is {@link #CLIENT}: everything a
 * server needs is present there too. Only a dedicated server is
 * {@link #DEDICATED_SERVER}, and only there is the client half genuinely
 * missing.</p>
 */
public enum PhysicalSide {

    /**
     * A machine with a window, a keyboard, and a player in front of it.
     *
     * <p>Every class exists here, including the ones a dedicated server does
     * not have. A single player world runs its server logic on this side.</p>
     */
    CLIENT("client"),

    /**
     * A headless server process.
     *
     * <p>No window, no local player, and no rendering classes on the
     * classpath.</p>
     */
    DEDICATED_SERVER("dedicated_server");

    /**
     * The name this side is written as in configuration and in the override
     * property. Fixed per constant so renaming a constant cannot change what a
     * launch script has to say.
     */
    private final String code;

    /**
     * Binds a side to the name it is written as.
     *
     * @param code The written name, never {@code null}.
     */
    PhysicalSide(String code) {
        this.code = code;
    }

    /**
     * Returns the name this side is written as.
     *
     * @return The written name, for example {@code "dedicated_server"}.
     */
    public String code() {
        return code;
    }

    /**
     * Reports whether this is the client side.
     *
     * @return {@code true} for {@link #CLIENT}.
     */
    public boolean isClient() {
        return this == CLIENT;
    }

    /**
     * Reports whether this is a dedicated server.
     *
     * @return {@code true} for {@link #DEDICATED_SERVER}.
     */
    public boolean isDedicatedServer() {
        return this == DEDICATED_SERVER;
    }

    /**
     * Reads a side from a written name.
     *
     * <p>Accepts {@code server} as well as {@code dedicated_server}, because a
     * launch script will be written by hand and the shorter spelling is the one
     * people reach for.</p>
     *
     * @param name What was written, in any case and with any surrounding
     *             whitespace.
     * @return The side, or empty when the name matches none. Empty rather than
     *         an exception: an unreadable override falls back to detection,
     *         which is better than refusing to start.
     */
    public static Optional<PhysicalSide> fromName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("server")) {
            return Optional.of(DEDICATED_SERVER);
        }
        for (PhysicalSide side : values()) {
            if (side.code.equals(normalized)) {
                return Optional.of(side);
            }
        }
        return Optional.empty();
    }
}
