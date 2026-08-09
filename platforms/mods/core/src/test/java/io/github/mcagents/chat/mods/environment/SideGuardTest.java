package io.github.mcagents.chat.mods.environment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SideGuard} and {@link WrongSideException}.
 *
 * <p>The message matters as much as the throw. The whole point of the guard is
 * that a wiring mistake produces something a reader can act on, instead of a
 * {@code NoClassDefFoundError} naming a Minecraft class nobody here wrote.</p>
 */
@DisplayName("SideGuard")
class SideGuardTest {

    @AfterEach
    void restore() {
        ModEnvironment.reset();
    }

    @Test
    @DisplayName("lets client code run on a client")
    void allowsClientOnClient() {
        ModEnvironment.install(PhysicalSide.CLIENT);

        assertDoesNotThrow(() -> SideGuard.requireClient("The client entry point"));
    }

    @Test
    @DisplayName("stops client code on a dedicated server")
    void refusesClientOnServer() {
        ModEnvironment.install(PhysicalSide.DEDICATED_SERVER);

        WrongSideException thrown = assertThrows(WrongSideException.class,
                () -> SideGuard.requireClient("The client entry point"));

        assertEquals(PhysicalSide.CLIENT, thrown.required());
        assertEquals(PhysicalSide.DEDICATED_SERVER, thrown.actual());
    }

    @Test
    @DisplayName("names the feature and both sides, so the message is actionable")
    void explainsItself() {
        ModEnvironment.install(PhysicalSide.DEDICATED_SERVER);

        WrongSideException thrown = assertThrows(WrongSideException.class,
                () -> SideGuard.requireClient("The MCAgents client entry point"));

        String message = thrown.getMessage();
        assertTrue(message.contains("The MCAgents client entry point"), message);
        assertTrue(message.contains(PhysicalSide.CLIENT.code()), message);
        assertTrue(message.contains(PhysicalSide.DEDICATED_SERVER.code()), message);
    }

    @Test
    @DisplayName("stops dedicated-server-only code on a client")
    void refusesDedicatedServerOnClient() {
        ModEnvironment.install(PhysicalSide.CLIENT);

        WrongSideException thrown = assertThrows(WrongSideException.class,
                () -> SideGuard.requireDedicatedServer("The console-only feature"));

        assertEquals(PhysicalSide.DEDICATED_SERVER, thrown.required());
        assertEquals(PhysicalSide.CLIENT, thrown.actual());
    }

    @Test
    @DisplayName("lets dedicated-server-only code run on a dedicated server")
    void allowsDedicatedServerOnServer() {
        ModEnvironment.install(PhysicalSide.DEDICATED_SERVER);

        assertDoesNotThrow(() -> SideGuard.requireDedicatedServer("The console-only feature"));
    }
}
