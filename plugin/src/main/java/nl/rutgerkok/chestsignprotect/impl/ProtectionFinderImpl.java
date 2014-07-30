package nl.rutgerkok.chestsignprotect.impl;

import java.util.Collection;
import java.util.List;

import nl.rutgerkok.chestsignprotect.ChestSettings;
import nl.rutgerkok.chestsignprotect.ChestSettings.ProtectionType;
import nl.rutgerkok.chestsignprotect.ChestSettings.SignType;
import nl.rutgerkok.chestsignprotect.ProtectionFinder;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.Attachable;

import com.google.common.base.Optional;

class ProtectionFinderImpl implements ProtectionFinder {
    private final ChestSettings settings;
    private final BlockFinder blockFinder;

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
        Collection<Sign> signs = blockFinder.findAttachedSigns(blocks);
        if (signs.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(ContainerProtectionImpl.fromBlocksWithSigns(blocks,
                blockFinder, signs));
    }

    private Optional<Protection> findForSign(Sign sign) {
        // Get type of sign
        Optional<SignType> signType = blockFinder.getSignParser().getSignType(
                sign.getLine(0));
        if (!signType.isPresent()) {
            return Optional.absent();
        }
        boolean isMainSign = signType.get().isMainSign();

        Attachable attachable = (Attachable) sign.getData();
        Block attachedBlock = sign.getBlock().getRelative(
                attachable.getAttachedFace());

        // Check if attached block is a container
        if (settings.canProtect(ProtectionType.CONTAINER,
                attachedBlock.getType())) {
            return Optional.of(newContainerProtection(attachedBlock, sign,
                    isMainSign));
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
    private Protection newContainerProtection(Block containerBlock, Sign sign,
            boolean isMainSign) {
        List<Block> blocks = blockFinder.findContainerNeighbors(containerBlock);
        if (isMainSign) {
            return ContainerProtectionImpl.fromBlocksWithMainSign(blocks,
                    blockFinder, sign);
        } else {
            return ContainerProtectionImpl.fromBlocks(blocks, blockFinder);
        }
    }

}
