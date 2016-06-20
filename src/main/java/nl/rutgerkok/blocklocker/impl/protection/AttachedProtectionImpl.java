package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;

import nl.rutgerkok.blocklocker.BlockData;
import nl.rutgerkok.blocklocker.OpenBlockSound;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.protection.AttachedProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

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

    private final BlockFinder blockFinder;
    private final Block protecionBlock;

    private AttachedProtectionImpl(Collection<ProtectionSign> signs, BlockFinder blockFinder, Block trapDoor) {
        super(signs);
        this.protecionBlock = trapDoor;
        this.blockFinder = blockFinder;
    }

    private AttachedProtectionImpl(ProtectionSign sign, BlockFinder blockFinder, Block trapDoor) {
        super(sign);
        this.protecionBlock = trapDoor;
        this.blockFinder = blockFinder;
    }

    @Override
    public boolean canBeOpened() {
        return Openable.class.isAssignableFrom(protecionBlock.getType().getData());
    }

    @Override
    protected Collection<ProtectionSign> fetchSigns() {
        Block supportingBlock = blockFinder.findSupportingBlock(protecionBlock);
        return blockFinder.findAttachedSigns(supportingBlock);
    }

    @Override
    public boolean isOpen() {
        MaterialData materialData = BlockData.get(protecionBlock);
        if (materialData instanceof Openable) {
            return ((Openable) materialData).isOpen();
        }
        return false;
    }

    @Override
    public void setOpen(boolean open) {
        setOpen(open, SoundCondition.NEVER);
    }

    @Override
    public void setOpen(boolean open, SoundCondition playSound) {
        MaterialData materialData = BlockData.get(protecionBlock);
        if (!(materialData instanceof Openable)) {
            return;
        }

        Openable openable = (Openable) materialData;

        if (openable.isOpen() == open) {
            return;
        }

        openable.setOpen(open);
        BlockData.set(protecionBlock, materialData);

        if (playSound == SoundCondition.ALWAYS) {
            Sound sound = OpenBlockSound.get(materialData.getItemType(), open);
            protecionBlock.getWorld().playSound(protecionBlock.getLocation(), sound, 1f, 0.7f);
        }
    }

}
