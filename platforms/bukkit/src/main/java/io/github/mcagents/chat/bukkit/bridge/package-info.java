/**
 * Bridges from this project's contracts to whatever actually serves them.
 *
 * <p>This package holds what every bridge shares — today
 * {@link io.github.mcagents.chat.bukkit.bridge.UnavailableBackend}, the fallback
 * used when no bridge could be built. Each concrete bridge lives in its own
 * package below, addressed by the project it binds to:</p>
 *
 * <pre>{@code bridge.{org}.{repo}}</pre>
 *
 * <ul>
 *   <li>{@code bridge.mcagents.core} — the MCAgents core plugin.</li>
 * </ul>
 *
 * <p>Organisation before repository, so a second integration from the same
 * organisation sits beside the first, and one from a different organisation
 * never collides with it. That matters more than it looks: repository names are
 * short and generic — a {@code core} or a {@code common} from two different
 * projects is entirely likely — and the organisation segment is what keeps them
 * apart without anyone having to invent a disambiguating name.</p>
 *
 * <p>The split is not decoration either. A bridge is a set of hardcoded class
 * and method names belonging to one external project, and those names move on
 * that project's schedule rather than this one's. Keeping each in its own
 * package means a break in one is contained.</p>
 *
 * <p>Nothing in these packages is compiled against the project it bridges to.
 * Classes are resolved at enable time through the target plugin's own
 * classloader, which is what lets this jar survive a release it was never built
 * against. Everything above works against
 * {@link io.github.mcagents.chat.api.AgentBackend} and never sees any of it.</p>
 */
package io.github.mcagents.chat.bukkit.bridge;
