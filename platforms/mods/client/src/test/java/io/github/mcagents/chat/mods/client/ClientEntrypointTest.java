package io.github.mcagents.chat.mods.client;

import io.github.mcagents.chat.mods.ModChatService;
import io.github.mcagents.chat.mods.environment.ClientOnly;
import io.github.mcagents.chat.mods.environment.ModBootstrap;
import io.github.mcagents.chat.mods.environment.ModContext;
import io.github.mcagents.chat.mods.environment.ModEnvironment;
import io.github.mcagents.chat.mods.environment.PhysicalSide;
import io.github.mcagents.chat.mods.environment.SideEntrypoint;
import io.github.mcagents.chat.mods.environment.WrongSideException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ClientEntrypoint} and {@link ClientChatService}.
 *
 * <p><strong>The server half is not on this test's classpath</strong> —
 * {@code platforms:mods:client} does not depend on
 * {@code platforms:mods:server}, and neither do these tests. So every pass here
 * is also a demonstration that the client half runs with the other half
 * entirely absent, which is the property the whole split exists for.</p>
 */
@DisplayName("ClientEntrypoint")
class ClientEntrypointTest {

    /**
     * A logger that discards, so a test run does not print startup lines.
     */
    private static Logger quietLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);
        return logger;
    }

    /**
     * Builds a client context over a backend.
     *
     * @param backend What to send to.
     * @return The context a loader would have assembled.
     */
    private static ModContext clientContext(FakeBackend backend) {
        return ModContext.of(PhysicalSide.CLIENT, backend, quietLogger(), "openai");
    }

    @AfterEach
    void restore() {
        ModEnvironment.reset();
    }

    @Test
    @DisplayName("is marked as client-only")
    void isMarked() {
        // Cheap, and it keeps the convention from rotting: a class that moves
        // into this module without the marker fails here rather than in review.
        assertNotNull(ClientEntrypoint.class.getAnnotation(ClientOnly.class));
        assertNotNull(ClientChatService.class.getAnnotation(ClientOnly.class));
        assertNotNull(ClientChatLines.class.getAnnotation(ClientOnly.class));
    }

    @Test
    @DisplayName("refuses to exist on a dedicated server")
    void refusesToBeConstructedOnAServer() {
        ModEnvironment.install(PhysicalSide.DEDICATED_SERVER);

        assertThrows(WrongSideException.class, ClientEntrypoint::new);
        assertThrows(WrongSideException.class, () -> new ClientChatService(
                ModChatService.from(clientContext(FakeBackend.answering("ok")))));
    }

    @Test
    @DisplayName("is what the bootstrap resolves for the client side")
    void isResolvedByName() {
        ModEnvironment.install(PhysicalSide.CLIENT);

        SideEntrypoint loaded = ModBootstrap.start(clientContext(FakeBackend.answering("ok")));

        // The bootstrap names this class as a string and never as a type. This
        // is the only place both ends of that seam exist at once, so it is the
        // only place the name can be checked against the class.
        assertInstanceOf(ClientEntrypoint.class, loaded);
        assertEquals(ClientEntrypoint.class.getName(), ModBootstrap.CLIENT_ENTRYPOINT);
        assertEquals(PhysicalSide.CLIENT, loaded.side());
    }

    @Test
    @DisplayName("reports the server half as missing instead of failing obscurely")
    void reportsTheAbsentServerHalf() {
        ModEnvironment.install(PhysicalSide.CLIENT);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> ModBootstrap.load(PhysicalSide.DEDICATED_SERVER));

        assertTrue(thrown.getMessage().contains(ModBootstrap.DEDICATED_SERVER_ENTRYPOINT),
                thrown.getMessage());
    }

    @Test
    @DisplayName("asks on one fixed session, since a client has one player")
    void asksOnOneSession() {
        ModEnvironment.install(PhysicalSide.CLIENT);
        ClientEntrypoint entrypoint = new ClientEntrypoint();
        entrypoint.start(clientContext(FakeBackend.answering("Below y=16.")));

        String reply = entrypoint.chat().ask("Where is diamond?").join();

        assertEquals("Below y=16.", reply);
        assertFalse(entrypoint.chat().isWaiting());
        assertTrue(entrypoint.chat().clear());
    }

    @Test
    @DisplayName("keeps the same conversation across a rebuild")
    void keepsTheSessionStable() {
        // Fixed rather than random, so a reload does not silently start a new
        // conversation the player did not ask for.
        assertEquals(ClientChatService.CLIENT_SESSION, ClientChatService.CLIENT_SESSION);
        assertEquals(ClientChatService.CLIENT_SESSION,
                java.util.UUID.nameUUIDFromBytes("mcagents-chat-client".getBytes()));
    }

    @Test
    @DisplayName("refuses to start on the other side")
    void refusesAServerContext() {
        ModEnvironment.install(PhysicalSide.CLIENT);
        ClientEntrypoint entrypoint = new ClientEntrypoint();

        // Constructed on a client, handed a server's context: a wiring mistake
        // rather than a real dedicated server, and it must not be papered over.
        assertThrows(WrongSideException.class, () -> entrypoint.start(ModContext.of(
                PhysicalSide.DEDICATED_SERVER, FakeBackend.answering("ok"), quietLogger(), "openai")));
    }

    @Test
    @DisplayName("says it has not started rather than handing out a null")
    void refusesToServeBeforeStarting() {
        ModEnvironment.install(PhysicalSide.CLIENT);
        ClientEntrypoint entrypoint = new ClientEntrypoint();

        assertThrows(IllegalStateException.class, entrypoint::chat);
        assertTrue(entrypoint.describe().contains("not started"));
    }

    @Test
    @DisplayName("can be stopped twice, and before it ever started")
    void stopsIdempotently() {
        ModEnvironment.install(PhysicalSide.CLIENT);
        ClientEntrypoint entrypoint = new ClientEntrypoint();

        entrypoint.stop();
        entrypoint.start(clientContext(FakeBackend.answering("ok")));
        entrypoint.stop();
        entrypoint.stop();

        assertTrue(entrypoint.describe().contains("not started"));
    }

    @Test
    @DisplayName("refuses to be built without what it needs")
    void refusesToBeBuiltIncomplete() {
        ModEnvironment.install(PhysicalSide.CLIENT);
        ClientEntrypoint entrypoint = new ClientEntrypoint();

        assertThrows(NullPointerException.class, () -> entrypoint.start(null));
        assertThrows(NullPointerException.class, () -> new ClientChatService(null));
    }
}
