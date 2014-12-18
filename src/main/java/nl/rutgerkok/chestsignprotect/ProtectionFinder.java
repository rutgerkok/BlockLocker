package nl.rutgerkok.chestsignprotect;

import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.google.common.base.Optional;

/**
 * Finds a {@link Protection} in the world.
 *
 */
public interface ProtectionFinder {

    /**
     * Returns the protection at the given block, if any.
     *
     * @param block
     *            The block to search at.
     * @return The protection, if any.
     */
    Optional<Protection> findProtection(Block block);

    /**
     * Returns the protection at the given block, if any.
     *
     * @param world
     *            The world the protection is in.
     * @param x
     *            X coordinate of the block.
     * @param y
     *            (Vertical) Y coordinate of the block.
     * @param z
     *            Z coordinate of the block.
     * @return The protection, if any.
     *
     */
    Optional<Protection> findProtection(World world, int x, int y, int z);

    /**
     * Creates a new protection sign, ignoring the content already on the sign.
     * 
     * <p>
     * To inspect the contents of existing signs, use
     * {@link #findProtection(Block)}.
     * 
     * @param sign
     *            The sign.
     * @param signType
     *            Type to set the sign to.
     * @param owner
     *            Owner for the sign.
     * @return The new protection sign.
     */
    ProtectionSign newProtectionSign(Sign sign, SignType signType, PlayerProfile owner);
}
