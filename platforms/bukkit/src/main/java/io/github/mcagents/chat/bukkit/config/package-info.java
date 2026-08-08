/**
 * Server side configuration.
 *
 * <p>Reads {@code config.yml} from the plugin's data folder: which platform to
 * talk to, whether ordinary players may use the command, and the credentials for
 * each vendor. The file belongs to the server owner — nothing here copies it,
 * uploads it, or writes to it except to remove a credential the vendor has
 * rejected.</p>
 */
package io.github.mcagents.chat.bukkit.config;
