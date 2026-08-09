package io.github.mcagents.chat.mods.environment;

/**
 * The check each side-bound entry point makes about where it is running.
 *
 * <p>One line at the top of a constructor, and a wiring mistake becomes a
 * {@link WrongSideException} naming the feature and both sides, instead of a
 * {@code NoClassDefFoundError} some frames later naming a Minecraft class.</p>
 *
 * <p>This is a diagnostic, not a security boundary. What actually keeps the
 * client half off a dedicated server is that nothing links it by type — see
 * {@link ModBootstrap}, which resolves an entry point by name for the detected
 * side and never mentions the other one.</p>
 *
 * <p>The two checks are not symmetric, and that is deliberate.
 * {@link #requireClient(String)} is the one that matters: client classes are
 * genuinely absent from a dedicated server.
 * {@link #requireDedicatedServer(String)} is narrower and rarer — most server
 * logic is perfectly at home inside a client hosting a single player world, so
 * guarding it as dedicated-server-only would break single player for nothing.
 * Reach for it only when something must not run in a client process at all.</p>
 */
public final class SideGuard {

    /**
     * Not instantiable — this class is a pair of assertions.
     */
    private SideGuard() {
    }

    /**
     * Refuses to continue unless this is a client.
     *
     * @param feature What is being reached, named so the failure says where to
     *                look.
     * @throws WrongSideException When called on a dedicated server.
     */
    public static void requireClient(String feature) {
        require(feature, PhysicalSide.CLIENT);
    }

    /**
     * Refuses to continue unless this is a dedicated server.
     *
     * @param feature What is being reached, named so the failure says where to
     *                look.
     * @throws WrongSideException When called on a client.
     */
    public static void requireDedicatedServer(String feature) {
        require(feature, PhysicalSide.DEDICATED_SERVER);
    }

    /**
     * Refuses to continue unless the current side matches.
     *
     * @param feature What is being reached.
     * @param required The side it belongs to.
     * @throws WrongSideException When the sides differ.
     */
    private static void require(String feature, PhysicalSide required) {
        PhysicalSide actual = ModEnvironment.current();
        if (actual != required) {
            throw new WrongSideException(feature, required, actual);
        }
    }
}
