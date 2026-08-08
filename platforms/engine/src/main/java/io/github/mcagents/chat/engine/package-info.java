/**
 * Universal entry point.
 *
 * <p>The engine is the one module that implements every other module, so the
 * single artifact it produces carries the whole chat surface. Which platform is
 * actually running is resolved here, at load time, rather than by shipping a
 * different artifact per platform.</p>
 */
package io.github.mcagents.chat.engine;
