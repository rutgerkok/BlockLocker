package nl.rutgerkok.blocklocker;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

/**
 * This class is intended as an easy way for other plugin developers to hook
 * into this plugin. Binary compatibility for all methods in this class will be
 * kept for all future releases of the plugin, unless stated otherwise. In other
 * words: if your plugin uses only the methods in this class, it won't break for
 * any future releases of this plugin.
 *
 */
public final class BlockLockerAPIv2 {

    /**
     * Gets the owner of the given block.
     *
     * @param block
     *            The block.
     * @return The owner, or empty if the block is not protected.
     */
    @SuppressWarnings("deprecation")
    public static Optional<OfflinePlayer> getOwner(Block block) {
        Optional<Protection> protection = getPlugin().getProtectionFinder().findProtection(block);
        if (!protection.isPresent()) {
            return Optional.empty();
        }

        Profile owner = protection.get().getOwner().orElse(null);
        if (owner instanceof PlayerProfile) {
            Optional<UUID> uuid = ((PlayerProfile) owner).getUniqueId();
            if (uuid.isPresent()) {
                return Optional.of(Bukkit.getOfflinePlayer(uuid.get()));
            }

            // No uuid looked up yet
            getPlugin().getProtectionUpdater().update(protection.get(), false);
            return Optional.ofNullable(Bukkit.getOfflinePlayer(owner.getDisplayName()));
        }

        return Optional.empty();
    }

    /**
     * Gets the display name of the owner of the block.
     *
     * @param block
     *            The block.
     * @return The display name, or {@code Optional.empty()} if the block isn't
     *         protected. Unlike {@link #getOwner(Block)}, this method still
     *         returns the name of the owner even if the UUID of the owner is
     *         not yet known.
     */
    public static Optional<String> getOwnerDisplayName(Block block) {
        Optional<Protection> protection = getPlugin().getProtectionFinder().findProtection(block);
        if (!protection.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(protection.get().getOwnerDisplayName());
    }

    /**
     * Gets access to the plugin instance, allowing for much more control of the
     * plugin. <strong>Binary compatibility with future versions of the plugin
     * is not guaranteed.</strong>
     *
     * @return The plugin instance.
     */
    public static final BlockLockerPlugin getPlugin() {
        return (BlockLockerPlugin) JavaPlugin.getProvidingPlugin(BlockLockerAPIv2.class);
    }

    /**
     * Checks if the player is allowed in the protection.
     *
     * @param player
     *            The player to check.
     * @param block
     *            The block.
     * @param allowBypass
     *            If the player has the bypass permission, or if the protection
     *            is expired, the plugin will return true regardless of whether
     *            the player is listed on the signs.
     * @return True if
     *         <ul>
     *         <li>the block is protected and the player is allowed</li>
     *         <li>the block is unprotected</li>
     *         <li>{@code allowBypass} is set to true and the player has the
     *         bypass permission.</li>
     *         </ul>
     *         False otherwise.
     */
    public static boolean isAllowed(Player player, Block block, boolean allowBypass) {
        // Check admin bypass
        if (allowBypass && player.hasPermission(Permissions.CAN_BYPASS)) {
            return true;
        }

        Optional<Protection> protection = getPlugin().getProtectionFinder().findProtection(block);
        if (!protection.isPresent()) {
            return true;
        }

        // Check normal allowance
        PlayerProfile playerProfile = getPlugin().getProfileFactory().fromPlayer(player);
        if (protection.get().isAllowed(playerProfile)) {
            return true;
        }

        // Check expiration
        if (allowBypass) {
            Optional<Date> expireDate = getPlugin().getChestSettings().getChestExpireDate();
            if (expireDate.isPresent() && protection.get().isExpired(expireDate.get())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets whether the player is the owner of the protection. Only owners are
     * allowed to destroy the protection.
     *
     * @param player
     *            The player.
     * @param block
     *            The block.
     * @return True if the player is the owner, false if the player is not the
     *         owner or if the block is unprotected.
     */
    public static boolean isOwner(Player player, Block block) {
        Optional<Protection> protection = getPlugin().getProtectionFinder().findProtection(block);
        if (!protection.isPresent()) {
            return false;
        }
        PlayerProfile playerProfile = getPlugin().getProfileFactory().fromPlayer(player);
        return protection.get().isOwner(playerProfile);
    }

    /**
     * Checks if the given block is protected by the plugin.
     *
     * @param block
     *            The block to check.
     * @return True if the given block is protected, false otherwise.
     */
    public static boolean isProtected(Block block) {
        return getPlugin().getProtectionFinder().findProtection(block).isPresent();
    }

    private BlockLockerAPIv2() {
        // No instances
    }
}
