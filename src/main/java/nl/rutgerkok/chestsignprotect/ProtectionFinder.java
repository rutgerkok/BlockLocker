package nl.rutgerkok.chestsignprotect;

import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.bukkit.World;
import org.bukkit.block.Block;

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
}
