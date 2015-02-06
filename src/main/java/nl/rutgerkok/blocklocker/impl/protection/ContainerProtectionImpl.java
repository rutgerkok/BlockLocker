package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;
import java.util.List;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.protection.ContainerProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.block.Block;

public final class ContainerProtectionImpl extends AbstractProtection implements ContainerProtection {

    /**
     * Creates a new protection for the protection block. Calling this method
     * might make for a faster {@link #getOwner()}, as it can read the owner
     * from the sign if it is a main sign.
     * 
     * @param sign
     *            The sign. If it is a main sign it is used for
     *            {@link #getOwner()}.
     * @param blockFinder
     *            The sign finder.
     * @param blocks2
     *            The block that are protected. (Usually one block, multiple for
     *            double chests.)
     *
     * @return The protection.
     */
    public static Protection fromBlocksWithSign(ProtectionSign sign,
            Collection<Block> blocks, BlockFinder blockFinder) {
        return new ContainerProtectionImpl(sign, blocks, blockFinder);
    }

    /**
     * Creates a new protection for the protection block. Calling this method
     * will make for a faster {@link #getAllowed()} and {@link #getOwner()}
     * 
     * @param signs
     *            All signs in the protection. Collection may not be empty.
     * @param blocks
     *            The blocks that are protected. (Usually one block, multiple
     *            for double chests.)
     * @param blockFinder
     *            The sign finder.
     *
     * @return The protection.
     */
    public static Protection fromBlocksWithSigns(Collection<ProtectionSign> signs,
            List<Block> blocks, BlockFinder blockFinder) {
        return new ContainerProtectionImpl(signs, blocks, blockFinder);
    }

    private final BlockFinder blockFinder;
    private final Collection<Block> blocks;

    private ContainerProtectionImpl(ProtectionSign mainSign, Collection<Block> blocks, BlockFinder blockFinder) {
        super(mainSign);
        this.blocks = blocks;
        this.blockFinder = blockFinder;
    }

    private ContainerProtectionImpl(Collection<ProtectionSign> allSigns, Collection<Block> blocks, BlockFinder blockFinder) {
        super(allSigns);
        this.blocks = blocks;
        this.blockFinder = blockFinder;
    }

    protected Collection<ProtectionSign> fetchSigns() {
        return blockFinder.findAttachedSigns(blocks);
    }

}
