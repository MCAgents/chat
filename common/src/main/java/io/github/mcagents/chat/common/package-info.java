/**
 * Root package of the MCAgents chat implementation.
 *
 * <p>Pure Java implementations of the contracts declared in
 * {@code io.github.mcagents.chat.api}: conversation state and settings.
 * Nothing here may reference a Minecraft server, a mod loader, or a MCAgents
 * core class — a platform module supplies the
 * {@link io.github.mcagents.chat.api.AgentBackend}, and everything in this
 * package works the same on every platform because of it.</p>
 *
 * <p>There are deliberately <strong>no credentials</strong> in this package, or
 * anywhere else in this project. MCAgents core owns the token file, the pool,
 * the rotation, and the eviction; this project asks core for a reply and is told
 * what state the credentials are in.</p>
 *
 * <p>{@link io.github.mcagents.chat.common.ChatService} is the entry point a
 * platform module builds and calls; {@code ChatSession}, {@code SessionStore},
 * {@code ChatSettings}, and {@code Models} are what it delegates to.</p>
 */
package io.github.mcagents.chat.common;
