package io.github.mcagents.chat.mods.server;

import io.github.mcagents.chat.common.ChatSettings;
import io.github.mcagents.chat.mods.environment.ServerOnly;

import java.util.Objects;
import java.util.Optional;

/**
 * Decides who may spend the server owner's credit.
 *
 * <p>This is the truth check, and it belongs on the server for one reason: a
 * client cannot be believed. On a client, "may I ask?" has no adversary — the
 * player owns the machine and pays for the key. On a server the same question
 * is a spending decision made on someone else's behalf, and any part of the
 * answer a client could influence is not an answer at all.</p>
 *
 * <p>So the input is a {@link ChatCaller} built from server-side state, and the
 * rule is deliberately dull: either the owner opened chat to everyone, or the
 * caller is an operator. Rules that are easy to state are rules that are easy
 * to audit.</p>
 *
 * <h2>Refusals are values, not exceptions</h2>
 *
 * <p>{@link #refusalFor(ChatCaller)} returns the reason as an
 * {@link Optional}, because being refused is an ordinary outcome a command
 * shows the player, not an error with a stack trace.</p>
 */
@ServerOnly
public final class ServerChatAuthority {

    /**
     * The permission level a caller needs when chat is not open to everyone.
     */
    private final int operatorLevel;

    /**
     * The settings in force. Replaced by {@link #withSettings(ChatSettings)}
     * rather than mutated, so a decision in progress finishes under what it
     * started with.
     */
    private final ChatSettings settings;

    /**
     * Builds an authority over settings, at the operator level.
     *
     * @param settings The settings in force.
     * @throws NullPointerException When {@code settings} is {@code null}.
     */
    public ServerChatAuthority(ChatSettings settings) {
        this(settings, ChatCaller.OPERATOR_LEVEL);
    }

    /**
     * Builds an authority over settings, at a level.
     *
     * @param settings The settings in force.
     * @param operatorLevel The level a caller needs when chat is not open to
     *                      everyone.
     * @throws NullPointerException When {@code settings} is {@code null}.
     * @throws IllegalArgumentException When the level is below {@code 1}, which
     *                                  every player already meets and would
     *                                  make the setting meaningless.
     */
    public ServerChatAuthority(ChatSettings settings, int operatorLevel) {
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        if (operatorLevel < 1) {
            throw new IllegalArgumentException(
                    "operatorLevel must be at least 1, or the player_allowed setting would mean nothing");
        }
        this.operatorLevel = operatorLevel;
    }

    /**
     * Returns an authority over new settings.
     *
     * @param updated The settings to adopt.
     * @return A new authority. This one is unchanged.
     * @throws NullPointerException When {@code updated} is {@code null}.
     */
    public ServerChatAuthority withSettings(ChatSettings updated) {
        return new ServerChatAuthority(updated, operatorLevel);
    }

    /**
     * Returns the level a caller needs when chat is not open to everyone.
     *
     * @return The operator level.
     */
    public int operatorLevel() {
        return operatorLevel;
    }

    /**
     * Reports whether a caller may ask.
     *
     * @param caller Who is asking, as the server knows them. A {@code null}
     *               caller is one that was never identified, and is refused.
     * @return {@code true} when they may.
     */
    public boolean allows(ChatCaller caller) {
        return refusalFor(caller).isEmpty();
    }

    /**
     * Explains why a caller may not ask.
     *
     * @param caller Who is asking, as the server knows them.
     * @return The reason to show them, or empty when they may. The reason never
     *         names the platform, the model, or anything else a refused caller
     *         has no business learning.
     */
    public Optional<String> refusalFor(ChatCaller caller) {
        if (caller == null) {
            // Nothing identified the caller. Fail closed: an absent caller is a
            // bug at the call site, and letting it through would be the worst
            // possible reading of one.
            return Optional.of("AI chat is not available to you on this server.");
        }
        if (settings.playerAllowed() || caller.hasLevel(operatorLevel)) {
            return Optional.empty();
        }
        return Optional.of("AI chat is limited to operators on this server.");
    }
}
