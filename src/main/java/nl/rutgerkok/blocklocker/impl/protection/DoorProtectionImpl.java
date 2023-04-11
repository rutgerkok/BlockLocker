package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;

import org.bukkit.block.Block;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.impl.CompleteDoor;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.protection.DoorProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

/**
 * Implementation of {@link DoorProtection}.
 *
 */
public final class DoorProtectionImpl extends AbstractProtection implements DoorProtection {

    /**
     * Gets a door protection from a door, with only a single sign looked up.
     *
     * @param sign
     *            A hint. If it is a main sign, the owner can easily be looked
     *            up, speeding up {@link #getOwner()}.
     * @param blockFinder
     *            The block finder.
     * @param door
     *            The door.
     *
     * @return The door protection object.
     */
    public static Protection fromDoorWithSign(ProtectionSign sign, BlockFinder blockFinder, CompleteDoor door) {
        return new DoorProtectionImpl(sign, blockFinder, door);
    }

    /**
     * Creates a new protection for a door, with all signs looked up.
     *
     * @param signs
     *            All signs of the protection. Collection may not be empty.
     * @param blockFinder
     *            The block finder.
     * @param door
     *            The door that is protected.
     * @return The protection.
     */
    public static Protection fromDoorWithSigns(Collection<ProtectionSign> signs, BlockFinder blockFinder, CompleteDoor door) {
        return new DoorProtectionImpl(signs, blockFinder, door);
    }

    private final BlockFinder blockFinder;
    private final CompleteDoor door;

    private DoorProtectionImpl(Collection<ProtectionSign> signs, BlockFinder blockFinder, CompleteDoor door) {
        super(signs);
        this.door = door;
        this.blockFinder = blockFinder;
    }

    private DoorProtectionImpl(ProtectionSign sign, BlockFinder blockFinder, CompleteDoor door) {
        super(sign);
        this.door = door;
        this.blockFinder = blockFinder;
    }

    @Override
    public boolean canBeOpened() {
        return true;
    }

    @Override
    protected Collection<ProtectionSign> fetchSigns() {
        return blockFinder.findAttachedSigns(door.getBlocksForSigns());
    }

    @Override
    public Block getSomeProtectedBlock() {
        return this.door.getSomeDoorBlock();
    }

    @Override
    public boolean isOpen() {
        return door.isOpen();
    }

    @Override
    public void setOpen(boolean open, SoundCondition playSound) {
        door.setOpen(open, playSound);
    }

}
