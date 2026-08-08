/**
 * Credential storage contracts.
 *
 * <p>Where a token lives differs completely between the two sides — server
 * configuration on one, a shared file under the Minecraft directory on the
 * other — so the pooling logic works against
 * {@link io.github.mcagents.chat.api.token.TokenStore} and neither side's
 * storage leaks into it.</p>
 *
 * <p>Nothing in this package may log, echo, or otherwise reveal a token. See
 * the token handling rules in the repository's agent instruction set.</p>
 */
package io.github.mcagents.chat.api.token;
