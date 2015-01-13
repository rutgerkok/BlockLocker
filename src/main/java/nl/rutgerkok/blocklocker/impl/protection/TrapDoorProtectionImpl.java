package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;

import nl.rutgerkok.blocklocker.BlockData;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.impl.BlockFinder;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.profile.TimerProfile;
import nl.rutgerkok.blocklocker.protection.Protection;
import nl.rutgerkok.blocklocker.protection.TrapDoorProtection;

import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

/**
 * Implementation of {@link TrapDoorProtection}.
 *
 */
public final class TrapDoorProtectionImpl extends AbstractProtection implements TrapDoorProtection {

    /**
     * Gets a door protection from a door, with only a single sign looked up.
     *
     * @param sign
     *            A hint. If it is a main sign, the owner can easily be looked
     *            up, speeding up {@link #getOwner()}.
     * @param blockFinder
     *            The block finder.
     * @param trapDoor
     *            The door.
     * 
     * @return The door protection object.
     */
    public static Protection fromDoorWithSign(ProtectionSign sign, BlockFinder blockFinder, Block trapDoor) {
        return new TrapDoorProtectionImpl(sign, blockFinder, trapDoor);
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
    public static Protection fromDoorWithSigns(Collection<ProtectionSign> signs, BlockFinder blockFinder, Block trapDoor) {
        return new TrapDoorProtectionImpl(signs, blockFinder, trapDoor);
    }

    private final BlockFinder blockFinder;
    private final Block trapDoor;

    private TrapDoorProtectionImpl(Collection<ProtectionSign> signs, BlockFinder blockFinder, Block trapDoor) {
        super(signs);
        this.trapDoor = trapDoor;
        this.blockFinder = blockFinder;
    }

    private TrapDoorProtectionImpl(ProtectionSign sign, BlockFinder blockFinder, Block trapDoor) {
        super(sign);
        this.trapDoor = trapDoor;
        this.blockFinder = blockFinder;
    }

    @Override
    protected Collection<ProtectionSign> fetchSigns() {
        Block supportingBlock = blockFinder.findSupportingBlock(trapDoor);
        return blockFinder.findAttachedSigns(supportingBlock);
    }

    @Override
    public int getOpenSeconds() {
        for (Profile profile : getAllowed()) {
            if (profile instanceof TimerProfile) {
                return ((TimerProfile) profile).getOpenSeconds();
            }
        }
        return -1;
    }

    @Override
    public boolean isOpen() {
        MaterialData materialData = BlockData.get(trapDoor);
        if (materialData instanceof Openable) {
            return ((Openable) materialData).isOpen();
        }
        return false;
    }

    @Override
    public void setOpen(boolean open) {
        MaterialData materialData = BlockData.get(trapDoor);
        if (materialData instanceof Openable) {
            ((Openable) materialData).setOpen(open);
            BlockData.set(trapDoor, materialData);
        }
    }

}
