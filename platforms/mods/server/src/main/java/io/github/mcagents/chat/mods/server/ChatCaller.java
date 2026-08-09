package io.github.mcagents.chat.mods.server;

import io.github.mcagents.chat.mods.environment.ServerOnly;

import java.util.Objects;
import java.util.UUID;

/**
 * Who is asking, as the <strong>server</strong> knows them.
 *
 * <p>Every field here must be read from server-side state — the player the
 * server authenticated, and the permission level the server assigned. None of
 * it may come from a packet. A client can claim any name and any level it
 * likes; believing the claim is the whole class of bug this record exists to
 * make visible, because a reviewer can see at the construction site where each
 * value came from.</p>
 *
 * <p>The identity is also what a conversation is keyed on, which is the second
 * reason it cannot be client-supplied: a caller who could choose their own
 * session id could read someone else's conversation.</p>
 *
 * @param uniqueId The player's identity, as the server authenticated it. Never
 *                 {@code null} — a caller with no identity has no conversation
 *                 to continue.
 * @param name What to call them in a message. Never blank.
 * @param permissionLevel The level the server assigned, on Minecraft's own
 *                        0 to 4 scale. A negative value is clamped to
 *                        {@code 0} rather than rejected — a malformed level
 *                        must fail closed, never open.
 */
@ServerOnly
public record ChatCaller(UUID uniqueId, String name, int permissionLevel) {

    /**
     * The level an operator holds, and what chatting requires when the server
     * has not opened it to everyone.
     *
     * <p>Minecraft's own scale: {@code 0} is everyone, {@code 2} is where
     * commands like {@code /gamemode} sit, and {@code 4} is full ownership.
     * Chat sits at {@code 2} rather than {@code 4} because it is a spending
     * decision the owner delegates, not an ownership one.</p>
     */
    public static final int OPERATOR_LEVEL = 2;

    /**
     * Validates and clamps the components.
     *
     * @throws NullPointerException When the identity or name is {@code null}.
     * @throws IllegalArgumentException When the name is blank.
     */
    public ChatCaller {
        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        name = name.trim();
        // Fail closed. A level that arrived negative was computed wrong, and
        // the safe reading of a wrong number is "no permissions at all".
        permissionLevel = Math.max(0, permissionLevel);
    }

    /**
     * Reports whether this caller holds at least a permission level.
     *
     * @param required The level to meet.
     * @return {@code true} when the assigned level is at least {@code required}.
     */
    public boolean hasLevel(int required) {
        return permissionLevel >= required;
    }
}
