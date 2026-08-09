package io.github.mcagents.chat.mods.environment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ModEnvironment} and {@link PhysicalSide}.
 *
 * <p>The one behaviour worth being certain about is the direction the guess
 * fails in. An unrecognised classpath must read as a dedicated server, so the
 * client half is never started somewhere its classes do not exist.</p>
 */
@DisplayName("ModEnvironment")
class ModEnvironmentTest {

    /**
     * The property value in force before a test replaced it, so it can be put
     * back — these tests share a JVM with every other test in the module.
     */
    private String previousProperty;

    @BeforeEach
    void captureAndClear() {
        previousProperty = System.getProperty(ModEnvironment.SIDE_PROPERTY);
        System.clearProperty(ModEnvironment.SIDE_PROPERTY);
        ModEnvironment.reset();
    }

    @AfterEach
    void restore() {
        if (previousProperty == null) {
            System.clearProperty(ModEnvironment.SIDE_PROPERTY);
        } else {
            System.setProperty(ModEnvironment.SIDE_PROPERTY, previousProperty);
        }
        ModEnvironment.reset();
    }

    @Nested
    @DisplayName("install")
    class Install {

        @Test
        @DisplayName("is believed over everything else")
        void winsOverProperty() {
            System.setProperty(ModEnvironment.SIDE_PROPERTY, "dedicated_server");

            ModEnvironment.install(PhysicalSide.CLIENT);

            assertEquals(PhysicalSide.CLIENT, ModEnvironment.current());
            assertTrue(ModEnvironment.isClient());
            assertFalse(ModEnvironment.isDedicatedServer());
        }

        @Test
        @DisplayName("overrides a side that was already detected")
        void overridesAnEarlierAnswer() {
            // A loader may install after something else has already asked. The
            // loader was told what it is; this class only guessed.
            assertEquals(PhysicalSide.DEDICATED_SERVER, ModEnvironment.current());

            ModEnvironment.install(PhysicalSide.CLIENT);

            assertEquals(PhysicalSide.CLIENT, ModEnvironment.current());
        }

        @Test
        @DisplayName("refuses a null side")
        void refusesNull() {
            assertThrows(NullPointerException.class, () -> ModEnvironment.install(null));
        }
    }

    @Nested
    @DisplayName("the system property")
    class PropertyOverride {

        @Test
        @DisplayName("is read when no loader installed a side")
        void isRead() {
            System.setProperty(ModEnvironment.SIDE_PROPERTY, "client");

            assertEquals(PhysicalSide.CLIENT, ModEnvironment.current());
        }

        @Test
        @DisplayName("accepts the short spelling of a dedicated server")
        void acceptsShortSpelling() {
            System.setProperty(ModEnvironment.SIDE_PROPERTY, "  SERVER  ");

            assertEquals(PhysicalSide.DEDICATED_SERVER, ModEnvironment.current());
        }

        @Test
        @DisplayName("falls through to detection when it names no side")
        void ignoresNonsense() {
            System.setProperty(ModEnvironment.SIDE_PROPERTY, "somewhere-else");

            // An unreadable override must not stop the game from starting.
            assertEquals(ModEnvironment.detect(), ModEnvironment.current());
        }
    }

    @Nested
    @DisplayName("detection")
    class Detection {

        @Test
        @DisplayName("answers dedicated server when the client marker is absent")
        void failsTowardsTheServer() {
            // No Minecraft on a unit test classpath, which is the same shape as
            // a dedicated server: the safe answer, since the client half is the
            // one that cannot run where its classes are missing.
            assertEquals(PhysicalSide.DEDICATED_SERVER, ModEnvironment.detect());
        }

        @Test
        @DisplayName("caches its answer")
        void cachesTheAnswer() {
            assertSame(ModEnvironment.current(), ModEnvironment.current());
        }
    }

    @Nested
    @DisplayName("PhysicalSide")
    class Sides {

        @Test
        @DisplayName("knows which of the two it is")
        void knowsItself() {
            assertTrue(PhysicalSide.CLIENT.isClient());
            assertFalse(PhysicalSide.CLIENT.isDedicatedServer());
            assertTrue(PhysicalSide.DEDICATED_SERVER.isDedicatedServer());
            assertFalse(PhysicalSide.DEDICATED_SERVER.isClient());
        }

        @Test
        @DisplayName("round-trips through its written name")
        void roundTripsThroughItsCode() {
            for (PhysicalSide side : PhysicalSide.values()) {
                assertEquals(Optional.of(side), PhysicalSide.fromName(side.code()));
                assertEquals(Optional.of(side), PhysicalSide.fromName(side.code().toUpperCase()));
            }
        }

        @Test
        @DisplayName("returns nothing for a name it does not know")
        void refusesUnknownNames() {
            assertEquals(Optional.empty(), PhysicalSide.fromName("integrated"));
            assertEquals(Optional.empty(), PhysicalSide.fromName("   "));
            assertEquals(Optional.empty(), PhysicalSide.fromName(null));
        }
    }
}
