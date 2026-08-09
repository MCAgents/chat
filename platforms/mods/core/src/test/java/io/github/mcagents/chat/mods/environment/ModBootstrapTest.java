package io.github.mcagents.chat.mods.environment;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.api.AgentPrompt;
import io.github.mcagents.chat.api.AgentReply;
import io.github.mcagents.chat.common.ChatSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ModBootstrap} and {@link ModContext} on the shared module's
 * own classpath.
 *
 * <p>Neither half is present here — {@code platforms:mods:core} depends on
 * neither — which makes this the right place to check what the bootstrap does
 * when a half is missing. That is not a hypothetical: a distribution built for
 * one side only is exactly this situation, and the failure has to say so rather
 * than surfacing as a bare {@code ClassNotFoundException}.</p>
 *
 * <p>The tests that start a half for real live in the two side modules, where
 * that half is on the classpath and the other still is not.</p>
 */
@DisplayName("ModBootstrap")
class ModBootstrapTest {

    /**
     * A backend that answers nothing, since no test here sends anything.
     */
    private static final AgentBackend BACKEND = new AgentBackend() {

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public CompletableFuture<AgentReply> send(AgentPrompt prompt) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public String describe() {
            return "a backend that answers nothing";
        }
    };

    @AfterEach
    void restore() {
        ModEnvironment.reset();
    }

    @Test
    @DisplayName("names a different entry point for each side")
    void namesOneEntrypointPerSide() {
        assertEquals(ModBootstrap.CLIENT_ENTRYPOINT, ModBootstrap.entrypointFor(PhysicalSide.CLIENT));
        assertEquals(ModBootstrap.DEDICATED_SERVER_ENTRYPOINT,
                ModBootstrap.entrypointFor(PhysicalSide.DEDICATED_SERVER));
    }

    @Test
    @DisplayName("names each entry point inside its own module's package")
    void namesTheRightPackages() {
        // The names are the seam. If one drifts from the class it points at,
        // the loader fails at startup with a missing class — so the shape is
        // pinned here, and the names are resolved for real in the side modules.
        assertTrue(ModBootstrap.CLIENT_ENTRYPOINT.startsWith("io.github.mcagents.chat.mods.client."),
                ModBootstrap.CLIENT_ENTRYPOINT);
        assertTrue(ModBootstrap.DEDICATED_SERVER_ENTRYPOINT.startsWith("io.github.mcagents.chat.mods.server."),
                ModBootstrap.DEDICATED_SERVER_ENTRYPOINT);
    }

    @Test
    @DisplayName("refuses a null side")
    void refusesNullSide() {
        assertThrows(NullPointerException.class, () -> ModBootstrap.entrypointFor(null));
        assertThrows(NullPointerException.class, () -> ModBootstrap.load(null));
    }

    @Test
    @DisplayName("explains which half is missing rather than throwing a bare reflection failure")
    void explainsAMissingHalf() {
        for (PhysicalSide side : PhysicalSide.values()) {
            IllegalStateException thrown =
                    assertThrows(IllegalStateException.class, () -> ModBootstrap.load(side));

            String message = thrown.getMessage();
            assertTrue(message.contains(ModBootstrap.entrypointFor(side)), message);
            assertTrue(message.contains(side.code()), message);
        }
    }

    @Test
    @DisplayName("refuses a null context")
    void refusesNullContext() {
        assertThrows(NullPointerException.class, () -> ModBootstrap.start(null));
    }

    @Test
    @DisplayName("builds a context that carries the side the loader reported")
    void contextCarriesTheSide() {
        ModContext context = ModContext.of(
                PhysicalSide.DEDICATED_SERVER, BACKEND, Logger.getAnonymousLogger(), "openai");

        assertEquals(PhysicalSide.DEDICATED_SERVER, context.side());
        assertEquals("openai", context.settings().vendorCode());
    }

    @Test
    @DisplayName("refuses a context that could not work")
    void refusesAnUnusableContext() {
        Logger logger = Logger.getAnonymousLogger();
        ChatSettings settings = ChatSettings.of("openai");

        assertThrows(NullPointerException.class, () -> new ModContext(null, BACKEND, logger, settings));
        assertThrows(NullPointerException.class,
                () -> new ModContext(PhysicalSide.CLIENT, null, logger, settings));
        assertThrows(NullPointerException.class,
                () -> new ModContext(PhysicalSide.CLIENT, BACKEND, null, settings));
        assertThrows(NullPointerException.class,
                () -> new ModContext(PhysicalSide.CLIENT, BACKEND, logger, null));
    }
}
