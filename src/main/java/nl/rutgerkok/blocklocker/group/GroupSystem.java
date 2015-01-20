package nl.rutgerkok.blocklocker.group;

import org.bukkit.entity.Player;

/**
 * Represents a group system on the server.
 *
 */
public abstract class GroupSystem {

    /**
     * Gets whether the given player is in a group with the given name. The
     * implementation should make an effort to make the group name case
     * insensitive, but this is not strictly required.
     *
     * @param player
     *            The player to check.
     * @param groupName
     *            The name of the group.
     *
     * @return True if the player is in the given group, false otherwise.
     */
    public abstract boolean isInGroup(Player player, String groupName);

    /**
     * Gets whether this group system must be kept when the plugin is reloaded
     * using the reload command of the plugin.
     *
     * <p>
     * When a group system is removed on reload, it must be re-added after the
     * reload. For group systems provided by other plugins this is problematic,
     * so they must return true. On the other hand, group systems included in
     * BlockLocker will be loaded again by BlockLocker, so they must return
     * false.
     *
     * @return True if the group system must be kept on reload, false otherwise.
     */
    public boolean keepOnReload() {
        // "return true" is the correct option for third-party plugins
        return true;
    }
}
