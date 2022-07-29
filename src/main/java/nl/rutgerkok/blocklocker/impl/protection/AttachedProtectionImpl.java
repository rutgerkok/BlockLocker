package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;

import nl.rutgerkok.blocklocker.OpenBlockSound;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.protection.AttachedProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

/**
 * Implementation of {@link AttachedProtection}.
 *
 */
public final class AttachedProtectionImpl extends AbstractProtection implements AttachedProtection {

    /**
     * Gets a door protection from a door, with only a single sign looked up.
     *
     * @param sign
     *            A hint. If it is a main sign, the owner can easily be looked
     *            up, speeding up {@link #getOwner()}.
     * @param blockFinder
     *            The block finder.
     * @param protectionBlock
     *            The door.
     *
     * @return The door protection object.
     */
    public static Protection fromBlockWithSign(ProtectionSign sign, BlockFinder blockFinder, Block protectionBlock) {
        return new AttachedProtectionImpl(sign, blockFinder, protectionBlock);
    }

    /**
     * Creates a new protection for a door, with all signs looked up.
     *
     * @param signs
     *            All signs of the protection. Collection may not be empty.
     * @param blockFinder
     *            The block finder.
     * @param trapDoor
     *            The door that is protected.
     * @return The protection.
     */
    public static Protection fromBlockWithSigns(Collection<ProtectionSign> signs, BlockFinder blockFinder, Block trapDoor) {
        return new AttachedProtectionImpl(signs, blockFinder, trapDoor);
    }

    private static void setBlockOpen(Block block, boolean open) {
        BlockData blockData = block.getBlockData();
        if (!isFunctionalOpenable(blockData)) {
            return;
        }
        Openable openable = (Openable) blockData;

        if (openable.isOpen() == open) {
            return;
        }

        openable.setOpen(open);
        block.setBlockData(blockData);
    }
    private final BlockFinder blockFinder;

    private final Block protectionBlock;

    private AttachedProtectionImpl(Collection<ProtectionSign> signs, BlockFinder blockFinder, Block trapDoor) {
        super(signs);
        this.protectionBlock = trapDoor;
        this.blockFinder = blockFinder;
    }

    private AttachedProtectionImpl(ProtectionSign sign, BlockFinder blockFinder, Block trapDoor) {
        super(sign);
        this.protectionBlock = trapDoor;
        this.blockFinder = blockFinder;
    }

    @Override
    public boolean canBeOpened() {
        return isFunctionalOpenable(protectionBlock.getBlockData());
    }

    @Override
    protected Collection<ProtectionSign> fetchSigns() {
        Block supportingBlock = blockFinder.findSupportingBlock(protectionBlock);
        return blockFinder.findAttachedSigns(Arrays.asList(protectionBlock, supportingBlock));
    }

    @Override
    public boolean isOpen() {
        BlockData materialData = protectionBlock.getBlockData();
        if (isFunctionalOpenable(materialData)) {
            return ((Openable) materialData).isOpen();
        }
        return false;
    }

    @Override
    public void setOpen(boolean open, SoundCondition playSound) {
        setBlockOpen(protectionBlock, open);
        Block supportingBlock = blockFinder.findSupportingBlock(protectionBlock);
        setBlockOpen(supportingBlock, open);

        if (playSound == SoundCondition.ALWAYS && isOpen() != open) {
            Sound sound = OpenBlockSound.get(protectionBlock.getType(), open);
            protectionBlock.getWorld().playSound(protectionBlock.getLocation(), sound, 1f, 0.7f);
        }
    }

}
