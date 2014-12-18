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
import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.Attachable;

import com.google.common.base.Optional;

class ProtectionFinderImpl implements ProtectionFinder {
    private final BlockFinder blockFinder;
    private final ChestSettings settings;

    ProtectionFinderImpl(BlockFinder lookup, ChestSettings settings) {
        blockFinder = lookup;
        this.settings = settings;
    }

    private Optional<Protection> findForContainer(Block block) {
        Material blockMaterial = block.getType();
        if (!settings.canProtect(ProtectionType.CONTAINER, blockMaterial)) {
            return Optional.absent();
        }
        List<Block> blocks = blockFinder.findContainerNeighbors(block);
        Collection<ProtectionSign> signs = blockFinder.findAttachedSigns(blocks);
        if (signs.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(ContainerProtectionImpl.fromBlocksWithSigns(blocks,
                blockFinder, signs));
    }

    private Optional<Protection> findForSign(Sign sign) {
        // Get type of sign
        Optional<ProtectionSign> parsed = blockFinder.getSignParser().parseSign(sign);
        if (!parsed.isPresent()) {
            return Optional.absent();
        }

        Attachable attachable = (Attachable) sign.getData();
        Block attachedBlock = sign.getBlock().getRelative(
                attachable.getAttachedFace());

        // Check if attached block is a container
        if (settings.canProtect(ProtectionType.CONTAINER,
                attachedBlock.getType())) {
            return Optional.of(newContainerProtection(attachedBlock, parsed.get()));
        }

        // Check if block above or below is a door
        // TODO

        return Optional.absent();
    }

    @Override
    public Optional<Protection> findProtection(Block block) {
        Validate.notNull(block);
        Material blockMaterial = block.getType();
        if (blockMaterial == Material.WALL_SIGN) {
            Sign sign = (Sign) block.getState();
            return findForSign(sign);
        }
        if (settings.canProtect(ProtectionType.CONTAINER, blockMaterial)) {
            return findForContainer(block);
        }
        return Optional.absent();
    }

    @Override
    public Optional<Protection> findProtection(World world, int x, int y, int z) {
        return findProtection(world.getBlockAt(x, y, z));
    }

    /**
     * Direct method to call the constructor of {@link ContainerProtectionImpl}.
     *
     * @param containerBlock
     *            The block that represents the container.
     * @param sign
     *            The sign used for finding the block.
     * @param isMainSign
     *            Type of the sign: true if [Private] signs, false for [More
     *            Users] signs.
     * @return The created protection.
     */
    private Protection newContainerProtection(Block containerBlock, ProtectionSign sign) {
        List<Block> blocks = blockFinder.findContainerNeighbors(containerBlock);
        if (sign.getType().isMainSign()) {
            return ContainerProtectionImpl.fromBlocksWithMainSign(blocks,
                    blockFinder, sign);
        } else {
            return ContainerProtectionImpl.fromBlocks(blocks, blockFinder);
        }
    }

    @Override
    public ProtectionSign newProtectionSign(Sign sign, SignType signType, PlayerProfile owner) {
        return new ProtectionSignImpl(sign.getLocation(), signType, Collections.<Profile> singletonList(owner));
    }

}
