package io.github.mcagents.chat.bukkit.config;

import io.github.mcagents.chat.api.token.TokenStore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The credential store backed by the plugin's own {@code config.yml}.
 *
 * <p>Credentials live under a per-vendor {@code token} list, so a server owner
 * can configure several keys for one vendor and the pool will work through them.
 * The file is the server owner's; nothing here copies it, uploads it, or writes
 * anything to it except the eviction of a key the vendor has rejected.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>Both {@link #reload()} and {@link #evict(String, String)} touch disk and
 * must not be called from a server tick. Eviction can arrive on whichever thread
 * a failed request completed on, so both are synchronized.</p>
 */
public final class YamlTokenStore implements TokenStore {

    /**
     * The plugin whose data folder holds the file.
     */
    private final Plugin plugin;

    /**
     * The configuration file itself, under the plugin's data folder.
     */
    private final File file;

    /**
     * The parsed configuration. Replaced wholesale by {@link #reload()} rather
     * than merged into, so an evicted credential cannot come back from a stale
     * in-memory copy.
     */
    private FileConfiguration config;

    /**
     * Opens the store, writing the bundled defaults if the file is missing.
     *
     * @param plugin The plugin whose data folder holds the file.
     * @throws NullPointerException When {@code plugin} is {@code null}.
     */
    public YamlTokenStore(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.file = new File(plugin.getDataFolder(), "config.yml");

        plugin.saveDefaultConfig();
        reload();
    }

    /**
     * Returns the parsed configuration, for reading settings other than
     * credentials.
     *
     * @return The configuration currently in memory, never {@code null}.
     */
    public synchronized FileConfiguration config() {
        return config;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads {@code {vendor}.token}, accepting either a list or a single
     * string — a server owner who writes one key without a dash has made a
     * reasonable guess, and rejecting it would look like the key was wrong.</p>
     */
    @Override
    public synchronized List<String> load(String vendorCode) {
        if (vendorCode == null || vendorCode.isBlank()) {
            return List.of();
        }

        String path = vendorCode.trim().toLowerCase(Locale.ROOT) + ".token";
        List<String> tokens = new ArrayList<>();

        if (config.isList(path)) {
            for (String token : config.getStringList(path)) {
                if (token != null && !token.isBlank()) {
                    tokens.add(token.trim());
                }
            }
        } else {
            String single = config.getString(path);
            if (single != null && !single.isBlank()) {
                tokens.add(single.trim());
            }
        }
        return tokens;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Rewrites the vendor's list without the rejected credential and saves.
     * The rest of the file — comments included, since Bukkit's YAML writer keeps
     * the header — is otherwise left alone.</p>
     *
     * <p>Nothing about the credential is logged. The message names the vendor
     * and how many keys remain, which is what a server owner needs.</p>
     */
    @Override
    public synchronized void evict(String vendorCode, String token) {
        if (vendorCode == null || token == null) {
            return;
        }

        String path = vendorCode.trim().toLowerCase(Locale.ROOT) + ".token";
        List<String> remaining = new ArrayList<>(load(vendorCode));
        if (!remaining.remove(token.trim())) {
            // Already gone — an eviction racing a reload, most likely. Not an
            // error, and rewriting the file would be pointless work.
            return;
        }

        config.set(path, remaining);
        try {
            config.save(file);
            plugin.getLogger().warning("A " + vendorCode + " token was rejected and has been removed from config.yml. "
                    + remaining.size() + " token(s) remain for that platform.");
        } catch (IOException e) {
            plugin.getLogger().warning("A " + vendorCode + " token was rejected but config.yml could not be written ("
                    + e.getMessage() + "). It will be retried until the file is writable.");
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Re-reads the file from disk into a fresh configuration. Deliberately
     * not {@code plugin.reloadConfig()}: that merges the bundled defaults back
     * in, which would resurrect a placeholder token line the owner had removed.</p>
     */
    @Override
    public synchronized void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String describe() {
        return file.getPath();
    }
}
