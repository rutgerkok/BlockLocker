package nl.rutgerkok.blocklocker;

import java.util.Optional;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/** Finds a {@link Protection} in the world. */
public interface ProtectionFinder {

  /**
   * Gets the protection the sign is placed against. The contents of the sign is not checked; in
   * other words: the sign doesn't have to be part of the protection (yet).
   *
   * @param signBlock The sign.
   * @return The protection, or empty if the block is not a sign or not placed against a protection.
   */
  Optional<Protection> findExistingProtectionForNewSign(Block signBlock);

  /**
   * Gets the protection the given block is part of. If the block is a sign, the sign must already
   * be filled in.
   *
   * @param block The block to search at.
   * @return The protection, if any.
   */
  Optional<Protection> findProtection(Block block);

  /**
   * Gets the protection the given block is part of. If the block is a sign, the sign must already
   * be filled in.
   *
   * @param block The block to search at.
   * @param searchMode The search mode.
   * @return The protection, if any.
   */
  Optional<Protection> findProtection(Block block, SearchMode searchMode);

  /**
   * Gets whether this block can be protected by a sign. This can either be because it is itself a
   * block that can be locked (like a chest) or because it supports a block that is protectable
   * (like the block below a door). Note that this method returns false for signs, since you cannot
   * protect a sign with another sign. See {@link #isSignNearbyProtectable(Block)} for that case.
   *
   * @param block The block to check.
   * @return True if the block is protectable, false otherwise.
   */
  boolean isProtectable(Block block);

  /**
   * Gets whether the given sign is near a block that can be protected. In other words, this method
   * returns true if the sign is attached to a chest, furnace etc. or placed above a door.
   *
   * <p>This method doesn't care whether the block that can be protected is actually protected.
   *
   * @param signBlock The sign block.
   * @return True if the sign is placed near a block that can be protected.
   */
  boolean isSignNearbyProtectable(Block signBlock);

  /**
   * Creates a new protection sign, ignoring the content already on the sign.
   *
   * <p>To inspect the contents of existing signs, use {@link #findProtection(Block)}.
   *
   * @param sign The sign.
   * @param signType Type to set the sign to.
   * @param onFirstLine Person to place on the first line.
   * @return The new protection sign.
   */
  ProtectionSign newProtectionSign(Sign sign, SignType signType, Profile onFirstLine);
}
