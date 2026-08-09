package io.github.mcagents.chat.mods.server;

import io.github.mcagents.chat.api.AgentPrompt;
import io.github.mcagents.chat.api.ChatException;
import io.github.mcagents.chat.common.ChatSettings;
import io.github.mcagents.chat.mods.ModChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ServerChatService}.
 *
 * <p>The interesting cases are the refusals, and specifically that a refusal
 * costs nothing: a request that should not have been made must not reach the
 * backend, because reaching it is what puts a charge on the server owner's
 * account.</p>
 */
@DisplayName("ServerChatService")
class ServerChatServiceTest {

    /**
     * A player with no permissions at all.
     */
    private static final ChatCaller PLAYER = new ChatCaller(UUID.randomUUID(), "Steve", 0);

    /**
     * A player at the operator level.
     */
    private static final ChatCaller OPERATOR =
            new ChatCaller(UUID.randomUUID(), "Alex", ChatCaller.OPERATOR_LEVEL);

    /**
     * A logger that discards, so a test run stays quiet.
     */
    private static Logger quietLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);
        return logger;
    }

    /**
     * Builds settings differing from the defaults only in who may chat.
     *
     * @param playerAllowed Whether ordinary players may chat.
     * @return The settings.
     */
    private static ChatSettings settings(boolean playerAllowed) {
        return new ChatSettings(
                "openai",
                "gpt-4o-mini",
                playerAllowed,
                ChatSettings.DEFAULT_SYSTEM_PROMPT,
                ChatSettings.DEFAULT_MAX_TURNS,
                Duration.ofMinutes(30),
                AgentPrompt.NO_MAX_TOKENS);
    }

    /**
     * Builds a server service over a backend.
     *
     * @param backend What to send to.
     * @param playerAllowed Whether ordinary players may chat.
     * @return The new service.
     */
    private static ServerChatService serviceOver(FakeBackend backend, boolean playerAllowed) {
        return new ServerChatService(
                new ModChatService(backend, quietLogger(), settings(playerAllowed)));
    }

    @Nested
    @DisplayName("authorisation")
    class Authorisation {

        @Test
        @DisplayName("refuses an ordinary player when chat is limited to operators")
        void refusesOrdinaryPlayers() {
            FakeBackend backend = FakeBackend.answering("ok");
            ServerChatService service = serviceOver(backend, false);

            CompletionException thrown = assertThrows(CompletionException.class,
                    () -> service.ask(PLAYER, "Where is diamond?").join());

            ChatException failure = assertInstanceOf(ChatException.class, thrown.getCause());
            assertEquals(ChatException.Kind.NOT_ALLOWED, failure.kind());
            // The point of refusing before sending: nothing was billed.
            assertTrue(backend.prompts().isEmpty(), "a refused request must never reach the backend");
        }

        @Test
        @DisplayName("lets an operator through")
        void allowsOperators() {
            ServerChatService service = serviceOver(FakeBackend.answering("Below y=16."), false);

            assertEquals("Below y=16.", service.ask(OPERATOR, "Where is diamond?").join());
        }

        @Test
        @DisplayName("lets everyone through once the owner opens it")
        void allowsPlayersWhenOpened() {
            ServerChatService service = serviceOver(FakeBackend.answering("ok"), true);

            assertEquals("ok", service.ask(PLAYER, "hello").join());
        }

        @Test
        @DisplayName("refuses a caller nothing identified")
        void refusesAnUnidentifiedCaller() {
            ServerChatService service = serviceOver(FakeBackend.answering("ok"), true);

            // Fails closed even with chat open to everyone: an absent caller is
            // a bug at the call site, not a permitted anonymous user.
            assertTrue(service.refusalFor(null, "hello").isPresent());
            assertThrows(CompletionException.class, () -> service.ask(null, "hello").join());
        }

        @Test
        @DisplayName("closes chat again for players already in a conversation")
        void reloadAppliesToExistingPlayers() {
            ServerChatService service = serviceOver(FakeBackend.answering("ok"), true);
            service.ask(PLAYER, "hello").join();

            service.reload(settings(false));

            assertTrue(service.refusalFor(PLAYER, "hello again").isPresent());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("refuses a message that is only whitespace")
        void refusesBlankMessages() {
            FakeBackend backend = FakeBackend.answering("ok");
            ServerChatService service = serviceOver(backend, true);

            assertTrue(service.refusalFor(OPERATOR, "   ").isPresent());
            assertThrows(CompletionException.class, () -> service.ask(OPERATOR, "  \n ").join());
            assertTrue(backend.prompts().isEmpty());
        }

        @Test
        @DisplayName("refuses a message longer than the bound")
        void refusesLongMessages() {
            FakeBackend backend = FakeBackend.answering("ok");
            ServerChatService service = serviceOver(backend, true);
            String tooLong = "x".repeat(ChatInputPolicy.DEFAULT_MAX_LENGTH + 1);

            assertTrue(service.refusalFor(OPERATOR, tooLong).isPresent());
            assertTrue(backend.prompts().isEmpty());
        }

        @Test
        @DisplayName("sends the cleaned message, not the raw one")
        void sendsTheCleanedMessage() {
            FakeBackend backend = FakeBackend.answering("ok");
            ServerChatService service = serviceOver(backend, true);

            service.ask(OPERATOR, "  Where is diamond?  ").join();

            // Paying for characters that were going to be stripped anyway would
            // be the one thing worse than stripping them.
            assertEquals("Where is diamond?", backend.lastMessage());
        }

        @Test
        @DisplayName("checks who is asking before what they asked")
        void checksTheCallerFirst() {
            ServerChatService service = serviceOver(FakeBackend.answering("ok"), false);

            Optional<String> refusal = service.refusalFor(PLAYER, "");

            // Telling a player their message was too short would confirm that
            // chat exists and is worth probing. They are simply not allowed.
            assertTrue(refusal.isPresent());
            assertFalse(refusal.get().contains("Say something"), refusal.get());
        }
    }

    @Nested
    @DisplayName("sessions")
    class Sessions {

        @Test
        @DisplayName("keeps one conversation per player, keyed on the server's own identity")
        void keepsOneConversationPerPlayer() {
            FakeBackend backend = FakeBackend.answering("ok");
            ServerChatService service = serviceOver(backend, true);

            service.ask(PLAYER, "one").join();
            service.ask(PLAYER, "two").join();
            service.ask(OPERATOR, "three").join();

            assertEquals(2, service.liveSessions());
            assertEquals(3, backend.prompts().get(1).history().size());
            assertEquals(1, backend.prompts().get(2).history().size());
        }

        @Test
        @DisplayName("lets a player drop their own conversation without permission")
        void clearNeedsNoPermission() {
            ServerChatService service = serviceOver(FakeBackend.answering("ok"), true);
            service.ask(PLAYER, "one").join();

            // It is theirs, forgetting it costs nothing, and it is the way out
            // if they are ever stuck waiting on a reply that never came.
            assertTrue(service.clear(PLAYER));
            assertFalse(service.isWaiting(PLAYER));
        }

        @Test
        @DisplayName("forgets every conversation on reload")
        void reloadForgetsEverything() {
            ServerChatService service = serviceOver(FakeBackend.answering("ok"), true);
            service.ask(PLAYER, "one").join();

            service.reload(settings(true));

            assertEquals(0, service.liveSessions());
        }

        @Test
        @DisplayName("refuses to be built or called without what it needs")
        void refusesNulls() {
            ServerChatService service = serviceOver(FakeBackend.answering("ok"), true);

            assertThrows(NullPointerException.class, () -> new ServerChatService(null));
            assertThrows(NullPointerException.class, () -> service.ask(OPERATOR, null));
            assertThrows(NullPointerException.class, () -> service.clear(null));
            assertThrows(NullPointerException.class, () -> service.isWaiting(null));
            assertThrows(NullPointerException.class, () -> service.reload(null));
        }
    }
}
