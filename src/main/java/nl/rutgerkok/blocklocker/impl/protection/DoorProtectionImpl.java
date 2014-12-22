package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.impl.BlockFinder;
import nl.rutgerkok.blocklocker.impl.Door;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.profile.TimerProfile;
import nl.rutgerkok.blocklocker.protection.DoorProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

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
    public static Protection fromDoorWithSign(ProtectionSign sign, BlockFinder blockFinder, Door door) {
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
    public static Protection fromDoorWithSigns(Collection<ProtectionSign> signs, BlockFinder blockFinder, Door door) {
        return new DoorProtectionImpl(signs, blockFinder, door);
    }

    private final BlockFinder blockFinder;
    private final Door door;

    private DoorProtectionImpl(Collection<ProtectionSign> signs, BlockFinder blockFinder, Door door) {
        super(signs);
        this.door = door;
        this.blockFinder = blockFinder;
    }

    private DoorProtectionImpl(ProtectionSign sign, BlockFinder blockFinder, Door door) {
        super(sign);
        this.door = door;
        this.blockFinder = blockFinder;
    }

    @Override
    protected Collection<ProtectionSign> fetchSigns() {
        return blockFinder.findAttachedSigns(door.getBlocksForSigns());
    }

    @Override
    public void setOpen(boolean open) {
        door.setOpen(open);
    }

    @Override
    public void toggleOpen() {
        door.toggleOpen();
    }

    @Override
    public boolean isOpen() {
        return door.isOpen();
    }

    @Override
    public int getOpenTicks() {
        for (Profile profile : getAllowed()) {
            if (profile instanceof TimerProfile) {
                return ((TimerProfile) profile).getOpenTicks();
            }
        }
        return -1;
    }

}
