package nl.rutgerkok.chestsignprotect.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.chestsignprotect.ChestSettings;
import nl.rutgerkok.chestsignprotect.ChestSettings.ProtectionType;
import nl.rutgerkok.chestsignprotect.ProtectionFinder;
import nl.rutgerkok.chestsignprotect.ProtectionSign;
import nl.rutgerkok.chestsignprotect.SignType;
import nl.rutgerkok.chestsignprotect.impl.protection.ContainerProtectionImpl;
import nl.rutgerkok.chestsignprotect.impl.protection.DoorProtectionImpl;
import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class ProtectionFinderImpl implements ProtectionFinder {
    private final BlockFinder blockFinder;
    private final ChestSettings settings;

    ProtectionFinderImpl(BlockFinder lookup, ChestSettings settings) {
        blockFinder = lookup;
        this.settings = settings;
    }

    private Optional<Protection> findExistingProtectionForBlock(Block block) {
        Material blockMaterial = block.getType();
        Optional<ProtectionType> protectionType = settings.getProtectionType(blockMaterial);
        if (!protectionType.isPresent()) {
            return Optional.absent();
        }

        // We don't know yet if signs are attached, so we have to check for that
        // in each case of the switch statement

        switch (protectionType.get()) {
            case CONTAINER:
                List<Block> blocks = blockFinder.findContainerNeighbors(block);
                Collection<ProtectionSign> signs = blockFinder.findAttachedSigns(blocks);
                if (signs.isEmpty()) {
                    return Optional.absent();
                }
                return Optional.of(ContainerProtectionImpl.fromBlocksWithSigns(
                        signs, blocks, blockFinder));
            case DOOR:
                Door door = new Door(block);
                Collection<ProtectionSign> doorSigns = blockFinder.findAttachedSigns(door.getBlocksForSigns());
                if (doorSigns.isEmpty()) {
                    return Optional.absent();
                }
                return Optional.of(DoorProtectionImpl.fromDoorWithSigns(
                        doorSigns, blockFinder, door));
            default:
                throw new UnsupportedOperationException("Don't know how to handle protection type " + protectionType.get());
        }

    }

    private Optional<Protection> findExistingProtectionForExistingSign(Sign sign) {
        // Get type of sign
        Optional<ProtectionSign> parsed = blockFinder.getSignParser().parseSign(sign);
        if (!parsed.isPresent()) {
            // Not actually a protection sign, so no protection
            return Optional.absent();
        }

        Optional<Block> protectionBlock = getProtectionBlockForSign(sign);
        if (protectionBlock.isPresent()) {
            return findExistingProtectionForBlock(protectionBlock.get(), parsed.get());
        }

        return Optional.absent();
    }

    @Override
    public Optional<Protection> findExistingProtectionForNewSign(Block signBlock) {
        BlockState blockState = signBlock.getState();
        if (!(blockState instanceof Sign)) {
            return Optional.absent();
        }

        Sign sign = (Sign) blockState;
        Optional<Block> attachedTo = this.getProtectionBlockForSign(sign);
        if (!attachedTo.isPresent()) {
            return Optional.absent();
        }

        return findExistingProtectionForBlock(attachedTo.get());
    }

    @Override
    public Optional<Protection> findProtection(Block block) {
        Preconditions.checkNotNull(block);
        Material blockMaterial = block.getType();

        // Check for sign
        if (blockMaterial == Material.WALL_SIGN || blockMaterial == Material.SIGN_POST) {
            Sign sign = (Sign) block.getState();
            return findExistingProtectionForExistingSign(sign);
        }

        // Check for other blocks
        return findExistingProtectionForBlock(block);
    }

    /**
     * Gets the block the sign protects. The block will be of a type that can be
     * protected. If the sign is a valid protection sign, this means that the
     * blocks is protected. If the sign is not yet a valid protection sign, keep
     * in mind that the block may still be protected by another sign.
     *
     * @param sign
     *            The sign, whether properly formatted or not.
     * @return A block that can be protected, and may or may not be protected
     *         currently.
     */
    private Optional<Block> getProtectionBlockForSign(Sign sign) {
        Block attachedBlock = blockFinder.findAttachedTo(sign);
        if (settings.canProtect(ProtectionType.CONTAINER, attachedBlock.getType())) {
            return Optional.of(attachedBlock);
        }

        // Search above and below that block for doors
        for (Block maybeDoor : blockFinder.getAboveAndBelow(attachedBlock)) {
            if (settings.canProtect(ProtectionType.DOOR, maybeDoor.getType())) {
                return Optional.of(maybeDoor);
            }
        }

        return Optional.absent();
    }

    @Override
    public boolean isSignNearbyProtectionBlock(Block signBlock) {
        BlockState blockState = signBlock.getState();
        if (blockState instanceof Sign) {
            return getProtectionBlockForSign((Sign) blockState).isPresent();
        }
        // Not a sign, so definitely not a sign nearby a protection block
        return false;
    }

    /**
     * Gets the {@link Protection} for the given block, which is already part of
     * a protection and is not a sign. A sign is given as a hint to this method,
     * so that the {@link Protection#getOwner()} method executes a little
     * faster.
     *
     * @param containerBlock
     *            The block that represents the protection (is a door or
     *            container block).
     * @param sign
     *            The sign used for finding the block.
     * @return The created protection.
     */
    private Optional<Protection> findExistingProtectionForBlock(Block protectionBlock, ProtectionSign sign) {
        Optional<ProtectionType> protectionType = settings.getProtectionType(protectionBlock.getType());
        if (!protectionType.isPresent()) {
            return Optional.absent();
        }

        switch (protectionType.get()) {
            case CONTAINER:
                Collection<Block> blocks = blockFinder.findContainerNeighbors(protectionBlock);
                return Optional.of(ContainerProtectionImpl.fromBlocksWithSign(
                        sign, blocks, blockFinder));
            case DOOR:
                Door door = new Door(protectionBlock);
                return Optional.of(DoorProtectionImpl.fromDoorWithSign(sign, blockFinder, door));
            default:
                throw new UnsupportedOperationException("Don't know how to handle protection type " + protectionType.get());
        }
    }

    @Override
    public ProtectionSign newProtectionSign(Sign sign, SignType signType, PlayerProfile owner) {
        return new ProtectionSignImpl(sign.getLocation(), signType, Collections.<Profile> singletonList(owner));
    }

}
