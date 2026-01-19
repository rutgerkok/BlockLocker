package nl.rutgerkok.blocklocker;

import com.google.common.base.Optional;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is intended as an easy way for other plugin developers to hook into this plugin.
 * Binary compatibility for all methods in this class will be kept for all future releases of the
 * plugin, unless stated otherwise. In other words: if your plugin uses only the methods in this
 * class, it won't break for any future releases of this plugin.
 *
 * @deprecated This class uses google guava's Optional.class, a new Version was made that utilises
 *     java.util.Optional.
 * @see BlockLockerAPIv2
 */
@Deprecated
public final class BlockLockerAPI {

  /**
   * Gets the owner of the given block.
   *
   * @param block The block.
   * @return The owner, or empty if the block is not protected.
   */
  public static Optional<OfflinePlayer> getOwner(Block block) {
    return Optional.fromJavaUtil(BlockLockerAPIv2.getOwner(block));
  }

  /**
   * Gets the display name of the owner of the block.
   *
   * @param block The block.
   * @return The display name, or {@code Optional.absent()} if the block isn't protected. Unlike
   *     {@link #getOwner(Block)}, this method still returns the name of the owner even if the UUID
   *     of the owner is not yet known.
   */
  public static Optional<String> getOwnerDisplayName(Block block) {
    return Optional.fromJavaUtil(BlockLockerAPIv2.getOwnerDisplayName(block));
  }

  /**
   * Gets access to the plugin instance, allowing for much more control of the plugin.
   * <strong>Binary compatibility with future versions of the plugin is not guaranteed.</strong>
   *
   * @return The plugin instance.
   */
  public static final BlockLockerPlugin getPlugin() {
    return (BlockLockerPlugin) JavaPlugin.getProvidingPlugin(BlockLockerAPI.class);
  }

  /**
   * Checks if the player is allowed in the protection.
   *
   * @param player The player to check.
   * @param block The block.
   * @param allowBypass If the player has the bypass permission, or if the protection is expired,
   *     the plugin will return true regardless of whether the player is listed on the signs.
   * @return True if
   *     <ul>
   *       <li>the block is protected and the player is allowed
   *       <li>the block is unprotected
   *       <li>{@code allowBypass} is set to true and the player has the bypass permission.
   *     </ul>
   *     False otherwise.
   */
  public static boolean isAllowed(Player player, Block block, boolean allowBypass) {
    return BlockLockerAPIv2.isAllowed(player, block, allowBypass);
  }

  /**
   * Gets whether the player is the owner of the protection. Only owners are allowed to destroy the
   * protection.
   *
   * @param player The player.
   * @param block The block.
   * @return True if the player is the owner, false if the player is not the owner or if the block
   *     is unprotected.
   */
  public static boolean isOwner(Player player, Block block) {
    return BlockLockerAPIv2.isOwner(player, block);
  }

  /**
   * Checks if the given block is protected by the plugin.
   *
   * @param block The block to check.
   * @return True if the given block is protected, false otherwise.
   */
  public static boolean isProtected(Block block) {
    return BlockLockerAPIv2.isProtected(block);
  }

  private BlockLockerAPI() {
    // No instances
  }
}
