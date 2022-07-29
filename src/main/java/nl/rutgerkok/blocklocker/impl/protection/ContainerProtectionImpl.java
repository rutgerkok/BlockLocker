package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;

import nl.rutgerkok.blocklocker.OpenBlockSound;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.protection.ContainerProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

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
     * @param blocks
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

    private ContainerProtectionImpl(Collection<ProtectionSign> allSigns, Collection<Block> blocks, BlockFinder blockFinder) {
        super(allSigns);
        this.blocks = blocks;
        this.blockFinder = blockFinder;
    }

    private ContainerProtectionImpl(ProtectionSign mainSign, Collection<Block> blocks, BlockFinder blockFinder) {
        super(mainSign);
        this.blocks = blocks;
        this.blockFinder = blockFinder;
    }

    @Override
    public boolean canBeOpened() {
        for (Block block : blocks) {
            if (isFunctionalOpenable(block.getBlockData())) {
                return true;
            }
            // Only try first block, as all blocks should be of the same type
            return false;
        }
        return false;
    }

    @Override
    protected Collection<ProtectionSign> fetchSigns() {
        return blockFinder.findAttachedSigns(blocks);
    }

    @Override
    public boolean isOpen() {
        for (Block block : blocks) {
            BlockData materialData = block.getBlockData();
            if (isFunctionalOpenable(materialData)) {
                return ((Openable) materialData).isOpen();
            }
        }
        return false;
    }


    private boolean setBlockOpen(Block block, boolean open) {
        BlockData materialData = block.getBlockData();
        if (!isFunctionalOpenable(materialData)) {
            return false;
        }

        Openable openable = (Openable) materialData;
        if (openable.isOpen() == open) {
            return false;
        }

        // Change the state
        openable.setOpen(open);
        block.setBlockData(materialData);
        return true;
    }

    @Override
    public void setOpen(boolean open, SoundCondition playSound) {
        boolean changed = false;
        Block aBlock = null;
        for (Block block : blocks) {
            changed |= setBlockOpen(block, open);
            aBlock = block;
        }
        if (aBlock == null) {
            return;
        }

        if (changed && playSound == SoundCondition.ALWAYS) {
            Sound sound = OpenBlockSound.get(aBlock.getType(), open);
            aBlock.getWorld().playSound(aBlock.getLocation(), sound, 1, 0.7f);
        }
    }

}
