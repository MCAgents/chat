package io.github.mcagents.chat.mods.environment;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks something that belongs to the server half.
 *
 * <p>The loader-neutral spelling of Fabric's {@code @Environment(SERVER)} and
 * NeoForge's {@code @OnlyIn(Dist.DEDICATED_SERVER)}, for the same reasons given
 * on {@link ClientOnly}.</p>
 *
 * <p>Note the asymmetry, which is not a mistake: server classes <em>do</em>
 * exist on a client, because a client hosting a single player world runs server
 * logic. What this annotation promises is narrower than its client counterpart
 * — the code makes no client assumptions, never touches a screen, and is where
 * a decision must be made when a client cannot be trusted to make it.</p>
 *
 * <p>Retained at runtime so a test can assert that the server half is
 * marked.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface ServerOnly {
}
