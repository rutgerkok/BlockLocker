package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.location.LocationChecker;
import org.bukkit.block.Block;

/**
 * Used to determine what blocks can be protected.
 *
 * @see LocationChecker For blocks that can be protected, but not anywhere, for example because of a
 *     claim.
 */
public interface ProtectableBlocksSettings {

  /**
   * Gets whether the block can be protected by any protection type (container, door, etc.)
   *
   * @param block Block to check. Note: if the block can be protected not because of it's
   *     type/structure, but because of where it is located, it is better to check this using a
   *     {@link LocationChecker} instead.
   * @return True if the block can be protected by the given type, false otherwise.
   */
  boolean canProtect(Block block);

  /**
   * Gets whether the material can be protected by the given protection type.
   *
   * @param type Protection type that must be checked for being able to protect this type. Note: if
   *     the block can be protected not because of it's type/structure, but because of where it is
   *     located, it is better to check this using a {@link LocationChecker} instead.
   * @param block Block to check.
   * @return True if the block can be protected by the given type, false otherwise.
   */
  boolean canProtect(ProtectionType type, Block block);
}
