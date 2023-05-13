package nl.rutgerkok.blocklocker.impl.updater;

import java.net.URLConnection;

import org.bukkit.plugin.Plugin;

/**
 * Sets the correct user agent.
 *
 */
final class UserAgent {

    /**
     * Sets the correct user agent as used by this plugin on the request.
     *
     * @param plugin
     *            The plugin.
     * @param connection
     *            The request to set the user agent on.
     */
    static void setFor(Plugin plugin, URLConnection connection) {
        String agent = "Mozilla/5.0 (" + plugin.getName() + "/" + plugin.getDescription().getVersion() + ")";
        connection.setRequestProperty("User-Agent", agent);
    }

    private UserAgent() {

    }
}
