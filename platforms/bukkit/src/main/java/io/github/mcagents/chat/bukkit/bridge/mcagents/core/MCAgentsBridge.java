package io.github.mcagents.chat.bukkit.bridge.mcagents.core;

import io.github.mcagents.chat.api.AgentBackend;
import io.github.mcagents.chat.api.AgentPrompt;
import io.github.mcagents.chat.api.AgentReply;
import io.github.mcagents.chat.api.ChatException;
import io.github.mcagents.chat.api.ChatTurn;
import io.github.mcagents.chat.api.Role;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Binds the pure {@link AgentBackend} contract to the live MCAgents core plugin.
 *
 * <p>Core owns every conversation with a language model vendor; this bridge
 * hands it a credential, hands it a prompt, and reads back the reply. It never
 * touches core's HTTP layer, its vendor dialects, or its client registry.</p>
 *
 * <p>The lookup is resolved reflectively through the core plugin's own
 * classloader. Bukkit's {@code depend} declaration guarantees core is loaded
 * first and makes its classes reachable, and resolving at runtime keeps this jar
 * from having to be recompiled against every core release — a method that moves
 * becomes a resolution failure at enable time with a readable message, rather
 * than a {@code NoSuchMethodError} in front of a player.</p>
 *
 * <p>Every failure path degrades rather than throws. A bridge that cannot
 * resolve reports {@link #isAvailable()} as {@code false}, which the chat
 * command turns into "the backend is unavailable" — the plugin still loads, and
 * the rest of the server is unaffected.</p>
 */
public final class MCAgentsBridge implements AgentBackend {

    /**
     * The core facade every call goes through. Core's own documentation names
     * it the single entry point, so this bridge deliberately reaches for
     * nothing else.
     */
    private static final String PROVIDER_CLASS = "io.github.mcagents.core.common.MCAgentsProvider";

    /**
     * The vendor enum, resolved from a code rather than a constant name.
     */
    private static final String VENDOR_CLASS = "io.github.mcagents.core.api.llm.LlmVendor";

    /**
     * The request record, built through its own nested builder.
     */
    private static final String REQUEST_CLASS = "io.github.mcagents.core.api.chat.ChatRequest";

    /**
     * Core's failure type, read to tell a dead credential from a rate limit.
     */
    private static final String EXCEPTION_CLASS = "io.github.mcagents.core.api.AgentException";

    /**
     * This plugin's logger, used for the one-shot failure reports.
     */
    private final Logger logger;

    /**
     * The core provider instance every request is sent through.
     */
    private final Object provider;

    /**
     * {@code LlmVendor.fromCode(String)} — resolves a configured platform name
     * to core's enum constant.
     */
    private final Method vendorFromCode;

    /**
     * {@code MCAgentsProvider.tokenState(LlmVendor)}.
     */
    private final Method tokenState;

    /**
     * {@code MCAgentsProvider.reloadTokens()}.
     */
    private final Method reloadTokens;

    /**
     * {@code MCAgentsProvider.chat(LlmVendor, ChatRequest)}.
     */
    private final Method chat;

    /**
     * {@code ChatRequest.builder(String)}.
     */
    private final Method requestBuilder;

    /**
     * The builder methods, in the order a prompt uses them.
     */
    private final Method builderSystem;
    private final Method builderUser;
    private final Method builderAssistant;
    private final Method builderMaxTokens;
    private final Method builderBuild;

    /**
     * {@code ChatResponse} accessors.
     */
    private final Method responseContent;
    private final Method responseFinishReason;
    private final Method responseUsage;

    /**
     * {@code TokenUsage} accessors.
     */
    private final Method usagePromptTokens;
    private final Method usageCompletionTokens;

    /**
     * {@code AgentException} accessors, used to classify a failure.
     */
    private final Class<?> exceptionClass;
    private final Method exceptionIsAuthFailure;
    private final Method exceptionIsRateLimited;

    /**
     * Whether a send failure has already been logged with its stack trace, so a
     * persistent problem does not flood the console on every message.
     */
    private volatile boolean sendFailureLogged;

    /**
     * Builds a bridge around already-resolved handles.
     *
     * <p>Private because a bridge is only ever produced by {@link #resolve},
     * which is what guarantees every handle is non-null.</p>
     *
     * @param logger This plugin's logger.
     * @param handles The resolved reflection handles.
     */
    private MCAgentsBridge(Logger logger, Handles handles) {
        this.logger = logger;
        this.provider = handles.provider;
        this.vendorFromCode = handles.vendorFromCode;
        this.tokenState = handles.tokenState;
        this.reloadTokens = handles.reloadTokens;
        this.chat = handles.chat;
        this.requestBuilder = handles.requestBuilder;
        this.builderSystem = handles.builderSystem;
        this.builderUser = handles.builderUser;
        this.builderAssistant = handles.builderAssistant;
        this.builderMaxTokens = handles.builderMaxTokens;
        this.builderBuild = handles.builderBuild;
        this.responseContent = handles.responseContent;
        this.responseFinishReason = handles.responseFinishReason;
        this.responseUsage = handles.responseUsage;
        this.usagePromptTokens = handles.usagePromptTokens;
        this.usageCompletionTokens = handles.usageCompletionTokens;
        this.exceptionClass = handles.exceptionClass;
        this.exceptionIsAuthFailure = handles.exceptionIsAuthFailure;
        this.exceptionIsRateLimited = handles.exceptionIsRateLimited;
    }

    /**
     * Resolves the bridge against the loaded MCAgents core plugin.
     *
     * <p>Called once at enable time; the resolved handles are cached for the
     * lifetime of the plugin. A single provider is created here and reused,
     * because core's provider owns the HTTP clients and creating one per
     * request would leak them.</p>
     *
     * @param plugin This plugin, used for its logger.
     * @param corePlugin The loaded MCAgents core plugin, used for its
     *                   classloader.
     * @return An Optional containing the bound backend, or empty when the core
     *         plugin does not expose the expected API.
     */
    public static Optional<MCAgentsBridge> resolve(Plugin plugin, Plugin corePlugin) {
        Logger logger = plugin.getLogger();
        try {
            ClassLoader loader = corePlugin.getClass().getClassLoader();

            Class<?> providerClass = Class.forName(PROVIDER_CLASS, false, loader);
            Class<?> vendorClass = Class.forName(VENDOR_CLASS, false, loader);
            Class<?> requestClass = Class.forName(REQUEST_CLASS, false, loader);
            Class<?> builderClass = Class.forName(REQUEST_CLASS + "$Builder", false, loader);
            Class<?> responseClass = Class.forName("io.github.mcagents.core.api.chat.ChatResponse", false, loader);
            Class<?> usageClass = Class.forName("io.github.mcagents.core.api.chat.TokenUsage", false, loader);
            Class<?> exceptionClass = Class.forName(EXCEPTION_CLASS, false, loader);

            Handles handles = new Handles();
            handles.vendorFromCode = vendorClass.getMethod("fromCode", String.class);
            handles.tokenState = providerClass.getMethod("tokenState", vendorClass);
            handles.reloadTokens = providerClass.getMethod("reloadTokens");
            handles.chat = providerClass.getMethod("chat", vendorClass, requestClass);
            handles.requestBuilder = requestClass.getMethod("builder", String.class);
            handles.builderSystem = builderClass.getMethod("system", String.class);
            handles.builderUser = builderClass.getMethod("user", String.class);
            handles.builderAssistant = builderClass.getMethod("assistant", String.class);
            handles.builderMaxTokens = builderClass.getMethod("maxTokens", int.class);
            handles.builderBuild = builderClass.getMethod("build");
            handles.responseContent = responseClass.getMethod("content");
            handles.responseFinishReason = responseClass.getMethod("finishReason");
            handles.responseUsage = responseClass.getMethod("usage");
            handles.usagePromptTokens = usageClass.getMethod("promptTokens");
            handles.usageCompletionTokens = usageClass.getMethod("completionTokens");
            handles.exceptionClass = exceptionClass;
            handles.exceptionIsAuthFailure = exceptionClass.getMethod("isAuthFailure");
            handles.exceptionIsRateLimited = exceptionClass.getMethod("isRateLimited");

            // The provider is the core plugin's own singleton, created when that
            // plugin enabled. Creating a second one here would give this plugin
            // its own client registry and its own credential pools — and the
            // credentials belong to core's configuration, not to this plugin's.
            handles.provider = providerClass.getField("instance").get(null);
            if (handles.provider == null) {
                throw new IllegalStateException("MCAgents core has not installed a provider yet");
            }

            return Optional.of(new MCAgentsBridge(logger, handles));
        } catch (ReflectiveOperationException | RuntimeException e) {
            logger.severe("Could not bind to the MCAgents core API ("
                    + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + "). Chat will report the backend as unavailable. "
                    + "The installed MCAgents version may be incompatible.");
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>A resolved bridge is always available: resolution is what could fail,
     * and it already succeeded. An unresolvable core produces no bridge at all,
     * which the plugin represents with an unavailable backend of its own.</p>
     */
    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads core's own {@code TokenState} enum and passes back its constant
     * name. This plugin holds no credentials, so there is nothing here to set —
     * only something to ask.</p>
     */
    @Override
    public String tokenState(String vendorCode) {
        try {
            Object vendor = vendorFromCode.invoke(null, vendorCode);
            Object state = tokenState.invoke(provider, vendor);
            return state instanceof Enum<?> constant ? constant.name() : "UNKNOWN";
        } catch (ReflectiveOperationException | RuntimeException e) {
            logSendFailureOnce(e);
            return "UNKNOWN";
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Asks core to re-read its own credential file. The file is core's, so
     * this plugin cannot and does not touch it.</p>
     */
    @Override
    public boolean reloadTokens() {
        try {
            reloadTokens.invoke(provider);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            logger.warning("MCAgents core could not reload its credentials ("
                    + e.getClass().getSimpleName() + ").");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<AgentReply> send(AgentPrompt prompt) {
        java.util.Objects.requireNonNull(prompt, "prompt cannot be null");
        try {
            Object vendor = vendorFromCode.invoke(null, prompt.vendorCode());
            Object request = buildRequest(prompt);

            Object future = chat.invoke(provider, vendor, request);
            if (!(future instanceof CompletableFuture<?> pending)) {
                return CompletableFuture.failedFuture(new ChatException(
                        ChatException.Kind.BACKEND_UNAVAILABLE,
                        "MCAgents core returned an unexpected type from chat()."));
            }

            return pending.handle((response, failure) -> {
                if (failure != null) {
                    throw new CompletionException(translate(failure));
                }
                return readReply(response);
            });
        } catch (ReflectiveOperationException | RuntimeException e) {
            return CompletableFuture.failedFuture(translate(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        return "MCAgents core (reflective bridge)";
    }

    /**
     * Builds core's request record through its nested builder.
     *
     * <p>The framing instructions are applied first and the turns after, oldest
     * first, preserving the order {@link AgentPrompt} documents — which is what
     * lets a vendor's prompt cache match a common prefix.</p>
     *
     * @param prompt What to ask.
     * @return Core's {@code ChatRequest}.
     * @throws ReflectiveOperationException When a builder call fails.
     */
    private Object buildRequest(AgentPrompt prompt) throws ReflectiveOperationException {
        Object builder = requestBuilder.invoke(null, prompt.model());

        if (prompt.hasSystemPrompt()) {
            builderSystem.invoke(builder, prompt.systemPrompt());
        }
        for (ChatTurn turn : prompt.history()) {
            if (turn.role() == Role.ASSISTANT) {
                builderAssistant.invoke(builder, turn.content());
            } else {
                builderUser.invoke(builder, turn.content());
            }
        }
        if (prompt.hasMaxTokens()) {
            builderMaxTokens.invoke(builder, prompt.maxTokens());
        }
        return builderBuild.invoke(builder);
    }

    /**
     * Reads core's response record into this project's reply.
     *
     * <p>Cached prompt tokens are reported as unknown: core's {@code TokenUsage}
     * carries prompt, completion, and total, and has no field for the cache hit
     * counts the vendors return. Automatic caching still happens — this is a
     * reporting gap, not a behavior one.</p>
     *
     * @param response Core's {@code ChatResponse}.
     * @return The reply.
     */
    private AgentReply readReply(Object response) {
        try {
            String text = (String) responseContent.invoke(response);
            String finishReason = (String) responseFinishReason.invoke(response);

            Object usage = responseUsage.invoke(response);
            int promptTokens = (int) usagePromptTokens.invoke(usage);
            int completionTokens = (int) usageCompletionTokens.invoke(usage);

            return new AgentReply(text, promptTokens, completionTokens, -1, finishReason);
        } catch (ReflectiveOperationException | RuntimeException e) {
            // The call succeeded and the reply is unreadable, which means the
            // response shape moved. Report the text as lost rather than
            // pretending the request failed.
            logSendFailureOnce(e);
            throw new CompletionException(new ChatException(
                    ChatException.Kind.BACKEND_UNAVAILABLE,
                    "MCAgents core returned a reply this version cannot read.", e));
        }
    }

    /**
     * Classifies a core failure into the kinds the rotation logic branches on.
     *
     * <p>This is the whole reason the bridge reads core's exception type: a
     * rejected credential must be evicted and a rate limited one must not, and
     * only core knows which happened.</p>
     *
     * @param failure Whatever core's future completed with.
     * @return The equivalent {@link ChatException}.
     */
    private ChatException translate(Throwable failure) {
        Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        if (cause instanceof InvocationTargetException invocation && invocation.getCause() != null) {
            cause = invocation.getCause();
        }

        String message = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();

        if (exceptionClass.isInstance(cause)) {
            try {
                if ((boolean) exceptionIsAuthFailure.invoke(cause)) {
                    return new ChatException(ChatException.Kind.TOKEN_REJECTED, message, cause);
                }
                if ((boolean) exceptionIsRateLimited.invoke(cause)) {
                    return new ChatException(ChatException.Kind.RATE_LIMITED, message, cause);
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                // Could not classify it. Fall through to VENDOR_ERROR, which
                // evicts nothing — the safe default when the cause is unclear.
                logSendFailureOnce(e);
            }
            return new ChatException(ChatException.Kind.VENDOR_ERROR, message, cause);
        }

        logSendFailureOnce(cause);
        return new ChatException(ChatException.Kind.VENDOR_ERROR, message, cause);
    }

    /**
     * Logs the first send failure with its stack trace and stays quiet
     * afterwards.
     *
     * @param error The failure to report.
     */
    private void logSendFailureOnce(Throwable error) {
        if (sendFailureLogged) {
            return;
        }
        sendFailureLogged = true;
        logger.log(Level.WARNING, "A chat request through MCAgents core failed. "
                + "This message is logged once; later failures are reported to the player only.", error);
    }

    /**
     * The reflection handles a bridge needs, collected during resolution.
     *
     * <p>A mutable holder rather than a twenty argument constructor. It never
     * escapes {@link #resolve}, and the bridge copies every field into a final
     * one, so nothing mutable is retained.</p>
     */
    private static final class Handles {
        private Object provider;
        private Method vendorFromCode;
        private Method tokenState;
        private Method reloadTokens;
        private Method chat;
        private Method requestBuilder;
        private Method builderSystem;
        private Method builderUser;
        private Method builderAssistant;
        private Method builderMaxTokens;
        private Method builderBuild;
        private Method responseContent;
        private Method responseFinishReason;
        private Method responseUsage;
        private Method usagePromptTokens;
        private Method usageCompletionTokens;
        private Class<?> exceptionClass;
        private Method exceptionIsAuthFailure;
        private Method exceptionIsRateLimited;
    }
}
