package nl.rutgerkok.chestsignprotect.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.chestsignprotect.SignParser;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.material.Attachable;
import org.bukkit.material.Chest;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

class SignFinder {
    private static BlockFace[] MAIN_FACES = { BlockFace.NORTH, BlockFace.EAST,
            BlockFace.SOUTH, BlockFace.WEST };

    private SignParser parser;

    SignFinder(SignParser parser) {
        this.parser = parser;
    }

    /**
     * Finds all valid attached signs to a block.
     *
     * @param block
     *            The block to check attached signs on.
     * @return The signs.
     */
    private Collection<Sign> findAttachedSigns(Block block) {
        Collection<Sign> signs = new ArrayList<Sign>();
        for (BlockFace face : MAIN_FACES) {
            Block atPosition = block.getRelative(face);
            Material material = atPosition.getType();
            if (material != Material.WALL_SIGN) {
                continue;
            }
            Sign sign = (Sign) atPosition.getState();
            if (!isValidSign(sign, atPosition, block)) {
                continue;
            }
            signs.add(sign);
        }
        return signs;
    }

    Collection<Sign> findAttachedSigns(List<Block> blocks) {
        if (blocks.size() == 1) {
            // Avoid creating a builder, iterator and extra set
            return findAttachedSigns(blocks.get(0));
        }

        ImmutableSet.Builder<Sign> signs = ImmutableSet.builder();
        for (Block block : blocks) {
            signs.addAll(findAttachedSigns(block));
        }
        return signs.build();
    }

    /**
     * Searches for containers of the same type attached to this container.
     *
     * @param block
     *            The container.
     * @return List of attached containers, including the given container.
     */
    List<Block> findContainerNeighbors(Block block) {
        // Currently only chests share an inventory
        // Minecraft connects two chests next to each other that have the same
        // direction. We simply check for that condition, taking both normal
        // and trapped chests into account
        if (!(getData(block) instanceof Chest)) {
            return Collections.singletonList(block);
        }

        Material chestMaterial = block.getType(); // CHEST or TRAPPED_CHEST
        BlockFace chestFacing = ((Directional) getData(block)).getFacing();

        for (BlockFace face : MAIN_FACES) {
            Block atPosition = block.getRelative(face);
            if (atPosition.getType() != chestMaterial) {
                continue;
            }

            MaterialData materialData = getData(atPosition);
            if (!(materialData instanceof Directional)) {
                continue;
            }

            BlockFace facing = ((Directional) materialData).getFacing();
            if (!facing.equals(chestFacing)) {
                continue;
            }

            return ImmutableList.of(block, atPosition);
        }

        return Collections.singletonList(block);
    }

    /**
     * Fast alternative for the slow {@code block.getState().getData()} call.
     * This method skips the part where unnecessary BlockStates are created. For
     * chests this is quite slow, as all items need to be copied.
     *
     * @param block
     *            The block.
     * @return The material data of the block.
     */
    @SuppressWarnings("deprecation")
    private MaterialData getData(Block block) {
        return block.getType().getNewData(block.getData());
    }

    /**
     * Gets the parser for signs.
     *
     * @return The parser.
     */
    SignParser getSignParser() {
        return parser;
    }

    /**
     * Checks if the sign at the given position is a valid sign for the
     * container.
     *
     * @param sign
     *            The sign to check.
     * @param signBlock
     *            The block the sign is on ({@link Block#getState()}
     *            {@code .equals(sign)} must return true)
     * @param attachedTo
     *            The block the sign must be attached to. If this is not the
     *            case, the method returns false.
     * @return True if the direction and header of the sign are valid, false
     *         otherwise.
     */
    private boolean isValidSign(Sign sign, Block signBlock, Block attachedTo) {
        BlockFace requiredFace = signBlock.getFace(attachedTo);
        BlockFace actualFace = ((Attachable) sign.getData()).getAttachedFace();
        if (actualFace != requiredFace) {
            return false;
        }
        return parser.hasValidHeader(sign);
    }
}
