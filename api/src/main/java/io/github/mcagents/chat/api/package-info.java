/**
 * Root package of the MCAgents chat API.
 *
 * <p>Everything here is pure Java: interfaces, records, and enums that describe
 * what the chat surface does, never how it does it. No type in this package or
 * below it may reference a Minecraft server, a mod loader, or a MCAgents core
 * class.</p>
 *
 * <p>That last exclusion is the point. This project reaches
 * {@code MCAgents/core} through a reflective bridge against the loaded core
 * plugin rather than by compiling against it, so the contracts here are stated
 * in this project's own types and a platform module adapts them.
 * {@link io.github.mcagents.chat.api.AgentBackend} is that seam.</p>
 *
 * <p>Implementations live in {@code io.github.mcagents.chat.common}.</p>
 */
package io.github.mcagents.chat.api;
