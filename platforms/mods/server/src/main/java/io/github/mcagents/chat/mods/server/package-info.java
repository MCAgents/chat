/**
 * The dedicated server half of the mod side.
 *
 * <p>Physically server-only. Everything here runs where there is no window and
 * no local player: a conversation per player rather than one for the machine,
 * and the checks deciding who is allowed to spend the server owner's credit.
 * A client may load these classes when it hosts a single player world, but
 * they never assume a screen.</p>
 *
 * <p>The client half lives in {@code io.github.mcagents.chat.mods.client} and
 * this package never depends on it. What both halves share belongs in
 * {@code io.github.mcagents.chat.mods} instead.</p>
 */
package io.github.mcagents.chat.mods.server;
