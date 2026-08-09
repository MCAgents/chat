package io.github.mcagents.chat.mods.server;

import io.github.mcagents.chat.mods.environment.ModBootstrap;
import io.github.mcagents.chat.mods.environment.ModContext;
import io.github.mcagents.chat.mods.environment.ModEnvironment;
import io.github.mcagents.chat.mods.environment.PhysicalSide;
import io.github.mcagents.chat.mods.environment.ServerOnly;
import io.github.mcagents.chat.mods.environment.SideEntrypoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ServerEntrypoint}.
 *
 * <p><strong>The client half is not on this test's classpath.</strong>
 * {@code platforms:mods:server} does not depend on
 * {@code platforms:mods:client}, and neither do these tests — so this module's
 * test JVM is in exactly the position a dedicated server is in. Every pass here
 * is a demonstration that the server half boots and serves with no client class
 * present anywhere. That is the claim the split makes, and this is where it is
 * checked rather than asserted.</p>
 */
@DisplayName("ServerEntrypoint")
class ServerEntrypointTest {

    /**
     * A logger that discards, so a test run does not print startup lines.
     */
    private static Logger quietLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);
        return logger;
    }

    /**
     * Builds a server context over a backend.
     *
     * @param backend What to send to.
     * @return The context a loader would have assembled.
     */
    private static ModContext serverContext(FakeBackend backend) {
        return ModContext.of(PhysicalSide.DEDICATED_SERVER, backend, quietLogger(), "openai");
    }

    @AfterEach
    void restore() {
        ModEnvironment.reset();
    }

    @Test
    @DisplayName("is marked as server-only")
    void isMarked() {
        assertNotNull(ServerEntrypoint.class.getAnnotation(ServerOnly.class));
        assertNotNull(ServerChatService.class.getAnnotation(ServerOnly.class));
        assertNotNull(ServerChatAuthority.class.getAnnotation(ServerOnly.class));
        assertNotNull(ChatInputPolicy.class.getAnnotation(ServerOnly.class));
        assertNotNull(ChatCaller.class.getAnnotation(ServerOnly.class));
    }

    @Test
    @DisplayName("starts on a dedicated server with no client class in sight")
    void startsWithoutTheClientHalf() {
        ModEnvironment.install(PhysicalSide.DEDICATED_SERVER);

        SideEntrypoint loaded = ModBootstrap.start(serverContext(FakeBackend.answering("ok")));

        assertInstanceOf(ServerEntrypoint.class, loaded);
        assertEquals(ServerEntrypoint.class.getName(), ModBootstrap.DEDICATED_SERVER_ENTRYPOINT);
        assertEquals(PhysicalSide.DEDICATED_SERVER, loaded.side());
    }

    @Test
    @DisplayName("reports the client half as missing instead of failing obscurely")
    void reportsTheAbsentClientHalf() {
        ModEnvironment.install(PhysicalSide.DEDICATED_SERVER);

        // The situation a client-stripped distribution is in. It must name the
        // class and the side, not surface as a bare ClassNotFoundException.
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> ModBootstrap.load(PhysicalSide.CLIENT));

        assertTrue(thrown.getMessage().contains(ModBootstrap.CLIENT_ENTRYPOINT), thrown.getMessage());
    }

    @Test
    @DisplayName("also starts inside a client, because a single player world is a server")
    void startsOnAClientToo() {
        ModEnvironment.install(PhysicalSide.CLIENT);
        ServerEntrypoint entrypoint = new ServerEntrypoint();

        // No guard refuses this, on purpose: server logic belongs wherever a
        // world is hosted, and a client hosts one.
        entrypoint.start(serverContext(FakeBackend.answering("ok")));

        assertNotNull(entrypoint.chat());
    }

    @Test
    @DisplayName("serves a conversation per player behind the checks")
    void servesBehindTheChecks() {
        ServerEntrypoint entrypoint = new ServerEntrypoint();
        entrypoint.start(serverContext(FakeBackend.answering("Below y=16.")));
        ChatCaller operator = new ChatCaller(UUID.randomUUID(), "Alex", ChatCaller.OPERATOR_LEVEL);

        assertEquals("Below y=16.", entrypoint.chat().ask(operator, "Where is diamond?").join());
        assertTrue(entrypoint.chat().refusalFor(
                new ChatCaller(UUID.randomUUID(), "Steve", 0), "hello").isPresent());
    }

    @Test
    @DisplayName("says it has not started rather than handing out a null")
    void refusesToServeBeforeStarting() {
        ServerEntrypoint entrypoint = new ServerEntrypoint();

        assertThrows(IllegalStateException.class, entrypoint::chat);
        assertTrue(entrypoint.describe().contains("not started"));
    }

    @Test
    @DisplayName("can be stopped twice, and before it ever started")
    void stopsIdempotently() {
        ServerEntrypoint entrypoint = new ServerEntrypoint();

        entrypoint.stop();
        entrypoint.start(serverContext(FakeBackend.answering("ok")));
        entrypoint.stop();
        entrypoint.stop();

        assertTrue(entrypoint.describe().contains("not started"));
    }

    @Test
    @DisplayName("refuses to start on nothing")
    void refusesANullContext() {
        assertThrows(NullPointerException.class, () -> new ServerEntrypoint().start(null));
    }
}
