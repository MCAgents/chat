/**
 * The bridge to the <strong>MCAgents/core</strong> plugin.
 *
 * <p>Core owns every conversation with a language model vendor.
 * {@link io.github.mcagents.chat.bukkit.bridge.mcagents.core.MCAgentsBridge}
 * hands it a credential, hands it a prompt, and reads back the reply —
 * resolving core's provider, request builder, and response accessors
 * reflectively through that plugin's own classloader at enable time.</p>
 *
 * <p>Everything here is specific to core's API shape: the class names, the
 * method names, and the order the request builder is driven in. A rename on
 * core's side breaks this package and nothing else, which is precisely why it
 * has one of its own.</p>
 */
package io.github.mcagents.chat.bukkit.bridge.mcagents.core;
