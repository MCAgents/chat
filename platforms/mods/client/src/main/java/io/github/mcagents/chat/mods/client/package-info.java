/**
 * The client half of the mod side.
 *
 * <p>Physically client-only. Everything here runs on a machine with a window
 * and one player in front of it: a single conversation rather than one per
 * player, and replies rendered into the client's own chat. A dedicated server
 * must never load a class from this package, which is why nothing outside it
 * references one by type — the entry point is resolved by name, on the client
 * only.</p>
 *
 * <p>The server half lives in {@code io.github.mcagents.chat.mods.server} and
 * this package never depends on it. What both halves share belongs in
 * {@code io.github.mcagents.chat.mods} instead.</p>
 */
package io.github.mcagents.chat.mods.client;
