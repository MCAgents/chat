/**
 * Root package of the MCAgents chat implementation.
 *
 * <p>Pure Java implementations of the contracts declared in
 * {@code io.github.mcagents.chat.api}. Nothing here may reference a Minecraft
 * server, a mod loader, or a MCAgents core class — a platform module supplies
 * the {@link io.github.mcagents.chat.api.AgentBackend} and the
 * {@link io.github.mcagents.chat.api.token.TokenStore}, and everything in this
 * package works the same on every platform because of it.</p>
 *
 * <p>{@link io.github.mcagents.chat.common.ChatService} is the entry point a
 * platform module builds and calls; the rest — {@code TokenPool},
 * {@code ChatSession}, {@code SessionStore}, {@code ChatSettings},
 * {@code Models} — is what it delegates to.</p>
 */
package io.github.mcagents.chat.common;
