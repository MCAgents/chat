package io.github.mcagents.chat.mods.store;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

/**
 * Finds the Minecraft directory the shared credential file lives in.
 *
 * <p>There is no fixed path. It differs by operating system, and launchers and
 * modpacks relocate it constantly — MultiMC, Prism, and the modern Mojang
 * launcher all give an instance its own directory. Hardcoding
 * {@code ~/.minecraft} would work for one user in three and silently write the
 * file somewhere nobody looks for the rest.</p>
 *
 * <p>Resolution runs in order, most explicit first:</p>
 *
 * <ol>
 *   <li>The {@code MCAGENTS_DIR} environment variable, when set. The escape
 *       hatch for anyone whose setup this class guesses wrong.</li>
 *   <li>The directory the loader supplied, when a mod passed one. A loader
 *       knows where its own instance lives, and it is right where guessing is
 *       not.</li>
 *   <li>The operating system's conventional location.</li>
 * </ol>
 */
public final class MinecraftDirectory {

    /**
     * The environment variable that overrides every other rule.
     */
    public static final String OVERRIDE_ENV = "MCAGENTS_DIR";

    /**
     * The file every MCAgents mod shares.
     *
     * <p>One name, one file, deliberately: a player configures a token once and
     * every MCAgents mod they install finds it, rather than each mod keeping its
     * own copy of the same key.</p>
     */
    public static final String FILE_NAME = "mcagents.json";

    /**
     * Not instantiable — this class is a resolver.
     */
    private MinecraftDirectory() {
    }

    /**
     * Resolves the shared credential file's path.
     *
     * @param loaderDirectory The directory the mod loader reported as the game
     *                        directory, or {@code null} when none is available.
     * @return The path to {@value #FILE_NAME}. The file itself may not exist
     *         yet; the parent directory is not created here.
     */
    public static Path resolveFile(Path loaderDirectory) {
        return resolveDirectory(loaderDirectory).resolve(FILE_NAME);
    }

    /**
     * Resolves the directory the shared credential file belongs in.
     *
     * @param loaderDirectory The directory the mod loader reported, or
     *                        {@code null}.
     * @return The resolved directory. Never {@code null} — the last fallback is
     *         the conventional path for the running operating system, whether or
     *         not it exists.
     */
    public static Path resolveDirectory(Path loaderDirectory) {
        Optional<Path> override = fromEnvironment();
        if (override.isPresent()) {
            return override.get();
        }
        if (loaderDirectory != null) {
            return loaderDirectory;
        }
        return conventionalDirectory();
    }

    /**
     * Reads the override environment variable.
     *
     * @return The overridden directory, or empty when the variable is unset or
     *         blank.
     */
    private static Optional<Path> fromEnvironment() {
        String value = System.getenv(OVERRIDE_ENV);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Paths.get(value.trim()));
    }

    /**
     * Returns the conventional Minecraft directory for the running operating
     * system.
     *
     * <p>Windows uses {@code %APPDATA%\.minecraft}; macOS uses
     * {@code ~/Library/Application Support/minecraft} — note the missing dot,
     * which is a genuine platform difference rather than a typo; everything else
     * uses {@code ~/.minecraft}.</p>
     *
     * @return The conventional directory, which may not exist.
     */
    private static Path conventionalDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home", ".");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Paths.get(appData, ".minecraft");
            }
            return Paths.get(home, "AppData", "Roaming", ".minecraft");
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return Paths.get(home, "Library", "Application Support", "minecraft");
        }
        return Paths.get(home, ".minecraft");
    }

    /**
     * Reports whether a resolved directory can actually be written to.
     *
     * <p>Worth checking before promising a player that their token was saved: a
     * read-only or non-existent instance directory produces a save that appears
     * to work and silently loses the credential.</p>
     *
     * @param directory The directory to check.
     * @return {@code true} when the directory exists and is writable, or can be
     *         created.
     */
    public static boolean isUsable(Path directory) {
        if (directory == null) {
            return false;
        }
        if (Files.isDirectory(directory)) {
            return Files.isWritable(directory);
        }
        Path parent = directory.getParent();
        return parent != null && Files.isDirectory(parent) && Files.isWritable(parent);
    }
}
