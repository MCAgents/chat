package io.github.mcagents.chat.mods.environment;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.common.ChatSettings;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Everything a side entry point is given when it starts.
 *
 * <p>Assembled by the loader, because only the loader knows any of it: which
 * side it is, which bridge to MCAgents core it managed to resolve, and which
 * logger to report through. Passing it as one value rather than four arguments
 * is what lets both halves share a single {@link SideEntrypoint} signature.</p>
 *
 * <p><strong>No credential is in here, and none can be.</strong> Core owns the
 * token file, loads it, rotates it, and evicts from it; this project sends a
 * prompt and receives a reply. Nothing on either side of the split has anything
 * to be trusted with.</p>
 *
 * @param side The physical side this process is. Comes from the loader rather
 *             than from {@link ModEnvironment} so the value the entry point
 *             acts on is the same one the bootstrap dispatched on.
 * @param backend The loader's bridge to MCAgents core.
 * @param logger Where to report problems.
 * @param settings The settings each request is shaped by.
 */
public record ModContext(
        PhysicalSide side,
        AgentBackend backend,
        Logger logger,
        ChatSettings settings) {

    /**
     * Validates the components.
     *
     * @throws NullPointerException When any component is {@code null}.
     */
    public ModContext {
        Objects.requireNonNull(side, "side cannot be null");
        Objects.requireNonNull(backend, "backend cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");
        Objects.requireNonNull(settings, "settings cannot be null");
    }

    /**
     * Builds a context using a platform's default settings.
     *
     * @param side The physical side this process is.
     * @param backend The loader's bridge to MCAgents core.
     * @param logger Where to report problems.
     * @param platform Which platform to talk to, as core names it.
     * @return The new context.
     * @throws IllegalArgumentException When {@code platform} names no supported
     *                                  platform.
     */
    public static ModContext of(PhysicalSide side, AgentBackend backend, Logger logger, String platform) {
        return new ModContext(side, backend, logger, ChatSettings.of(platform));
    }
}
