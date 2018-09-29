package nl.rutgerkok.blocklocker.location;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Used to check if protections can be created at the given location.
 *
 */
public interface LocationChecker {

    /**
     * Checks whether people can protect chests here.
     *
     * @param player
     *            The player placing the chest.
     * @param block
     *            The location.
     * @throws IllegalLocationException
     *             If the player is not allowed to place a chest here. The exception
     *             should include a message why.
     */
    void checkLocation(Player player, Block block) throws IllegalLocationException;

    /**
     * Gets whether this location checker must be kept when the plugin is reloaded
     * using the reload command of the plugin.
     *
     * <p>
     * When a location checker is removed on reload, it must be re-added after the
     * reload. For location checkers provided by other plugins this is problematic,
     * so they must return true. On the other hand, group systems included in
     * BlockLocker will be loaded again by BlockLocker, so they must return false.
     *
     * @return True if the group system must be kept on reload, false otherwise.
     */
    default boolean keepOnReload() {
        return true;
    }
}
