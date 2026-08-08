/**
 * The reflective bridge to the MCAgents core plugin.
 *
 * <p>This is the <strong>only</strong> package in the project that knows core
 * exists. Everything above it works against
 * {@link io.github.mcagents.chat.api.AgentBackend}, so core's API shape is
 * confined to one class that can be replaced or repaired on its own.</p>
 *
 * <p>Nothing here is compiled against core. Classes are resolved at enable time
 * through the loaded core plugin's own classloader, which is what lets this jar
 * survive a core release it was never built against.</p>
 */
package io.github.mcagents.chat.bukkit.bridge;
