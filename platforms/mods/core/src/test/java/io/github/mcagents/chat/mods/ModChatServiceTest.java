package io.github.mcagents.chat.mods;

import io.github.mcagents.chat.api.ChatException;
import io.github.mcagents.chat.common.ChatSettings;
import io.github.mcagents.chat.mods.environment.ModContext;
import io.github.mcagents.chat.mods.environment.PhysicalSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ModChatService}, the half of the mod chat surface that is
 * the same on both sides.
 *
 * <p>What is checked here is exactly what does not differ between a client and
 * a server: that a session keeps its own conversation, that clearing one leaves
 * the others alone, and that nothing about who is asking is decided at this
 * level. Those decisions belong to the two halves and are tested there.</p>
 */
@DisplayName("ModChatService")
class ModChatServiceTest {

    /**
     * A logger that discards, so a test run stays quiet.
     */
    private static Logger quietLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);
        return logger;
    }

    /**
     * Builds a service over a backend.
     *
     * @param backend What to send to.
     * @return The new service.
     */
    private static ModChatService serviceOver(FakeBackend backend) {
        return new ModChatService(backend, quietLogger(), ChatSettings.of("openai"));
    }

    @Test
    @DisplayName("sends what it was given and returns the reply text")
    void sendsAndReturnsTheReply() throws Exception {
        FakeBackend backend = FakeBackend.answering("Diamonds are below y=16.");
        ModChatService service = serviceOver(backend);

        String reply = service.ask(UUID.randomUUID(), "Where is diamond?").get();

        assertEquals("Diamonds are below y=16.", reply);
        assertEquals("Where is diamond?", backend.lastMessage());
    }

    @Test
    @DisplayName("keeps one conversation per session")
    void keepsOneConversationPerSession() throws Exception {
        FakeBackend backend = FakeBackend.answering("ok");
        ModChatService service = serviceOver(backend);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        service.ask(first, "one").get();
        service.ask(first, "two").get();
        service.ask(second, "three").get();

        assertEquals(2, service.liveSessions());
        // The first session replays its own history and nobody else's, which is
        // the property a server depends on for one conversation per player:
        // its second question carries three turns, and the other session's
        // first question carries one.
        assertEquals(3, backend.prompts().get(1).history().size());
        assertEquals(1, backend.prompts().get(2).history().size());
    }

    @Test
    @DisplayName("forgets one session without touching the others")
    void clearsOneSession() throws Exception {
        ModChatService service = serviceOver(FakeBackend.answering("ok"));
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        service.ask(first, "one").get();
        service.ask(second, "two").get();

        assertTrue(service.clear(first));
        assertFalse(service.clear(first));
        assertEquals(1, service.liveSessions());
    }

    @Test
    @DisplayName("forgets everything on reload")
    void reloadForgetsEverything() throws Exception {
        ModChatService service = serviceOver(FakeBackend.answering("ok"));
        service.ask(UUID.randomUUID(), "one").get();

        service.reload();

        assertEquals(0, service.liveSessions());
    }

    @Test
    @DisplayName("adopts new settings on reload")
    void reloadAdoptsNewSettings() {
        ModChatService service = serviceOver(FakeBackend.answering("ok"));

        service.reload(ChatSettings.of("anthropic"));

        assertEquals("anthropic", service.settings().vendorCode());
    }

    @Test
    @DisplayName("fails rather than pretending when the backend is not there")
    void failsWithoutABackend() {
        ModChatService service = serviceOver(FakeBackend.unavailable());

        ExecutionException thrown = assertThrows(ExecutionException.class,
                () -> service.ask(UUID.randomUUID(), "anyone home?").get());

        ChatException failure = assertInstanceOf(ChatException.class, thrown.getCause());
        assertEquals(ChatException.Kind.BACKEND_UNAVAILABLE, failure.kind());
    }

    @Test
    @DisplayName("releases a session when its request completes, however it ended")
    void releasesTheSessionOnEveryOutcome() {
        UUID session = UUID.randomUUID();
        ModChatService failing = new ModChatService(
                FakeBackend.failing(), quietLogger(), ChatSettings.of("openai"));

        assertThrows(CompletionException.class, () -> failing.ask(session, "first").join());

        // A session left claimed after a failure would strand that player
        // behind a request that already ended.
        assertFalse(failing.isWaiting(session));
    }

    @Test
    @DisplayName("refuses what could never be a question")
    void refusesNonsense() {
        ModChatService service = serviceOver(FakeBackend.answering("ok"));

        assertThrows(NullPointerException.class, () -> service.ask(null, "hello"));
        assertThrows(NullPointerException.class, () -> service.ask(UUID.randomUUID(), null));
        assertThrows(IllegalArgumentException.class, () -> service.ask(UUID.randomUUID(), "   "));
    }

    @Test
    @DisplayName("refuses to be built without what it needs")
    void refusesToBeBuiltIncomplete() {
        assertThrows(NullPointerException.class,
                () -> new ModChatService(null, quietLogger(), ChatSettings.of("openai")));
        assertThrows(NullPointerException.class,
                () -> new ModChatService(FakeBackend.answering("ok"), null, ChatSettings.of("openai")));
        assertThrows(NullPointerException.class,
                () -> new ModChatService(FakeBackend.answering("ok"), quietLogger(), null));
        assertThrows(NullPointerException.class, () -> ModChatService.from(null));
    }

    @Test
    @DisplayName("can be built from what a loader assembled")
    void buildsFromAContext() {
        ModContext context = ModContext.of(
                PhysicalSide.DEDICATED_SERVER, FakeBackend.answering("ok"), quietLogger(), "deepseek");

        ModChatService service = ModChatService.from(context);

        assertEquals("deepseek", service.settings().vendorCode());
    }

    @Test
    @DisplayName("surfaces a vendor failure as a chat failure")
    void surfacesVendorFailures() {
        ModChatService service = serviceOver(FakeBackend.failing());

        CompletionException thrown = assertThrows(CompletionException.class,
                () -> service.ask(UUID.randomUUID(), "hello").join());

        ChatException failure = assertInstanceOf(ChatException.class, thrown.getCause());
        assertEquals(ChatException.Kind.VENDOR_ERROR, failure.kind());
    }
}
