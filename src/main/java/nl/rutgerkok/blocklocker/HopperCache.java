package nl.rutgerkok.blocklocker;

import org.bukkit.block.Block;

/**
 * A simple cache that stores whether a block is protected. Entries expire after
 * a few seconds. Used for hoppers, which do <em>a lot</em> of successive
 * checks.
 */
public interface HopperCache {

    public enum CacheFlag {
        /**
         * Block is certainly protected.
         */
        PROTECTED,
        /**
         * Block is not protected.
         */
        NOT_PROTECTED,
        /**
         * Not cached; we don't know whether the block is protected or not.
         */
        MISS_CACHE
    }

    /**
     * Gets whether the given block is locked.
     * 
     * @param block
     *            The block.
     * @return Whether the given block is locked.
     */
    CacheFlag getIsRedstoneAllowed(Block block);

    /**
     * Sets whether the given block is locked for redstone.
     * 
     * @param block
     *            The block.
     * @param locked
     *            Whether the block is locked.
     */
    void setIsRedstoneAllowed(Block block, boolean locked);

}