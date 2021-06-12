package nl.rutgerkok.blocklocker.impl.blockfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.WallSign;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignParser;

/**
 * Finds blocks that logically belong together, like the other half of a chest,
 * the attached signs, etc.
 *
 */
public abstract class BlockFinder {

    public static final BlockFace[] CARDINAL_FACES = { BlockFace.NORTH, BlockFace.EAST,
                BlockFace.SOUTH, BlockFace.WEST };
    private static final BlockFace[] SIGN_ATTACHMENT_FACES = { BlockFace.NORTH, BlockFace.EAST,
                BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP };
    public static final BlockFace[] VERTICAL_FACES = { BlockFace.UP, BlockFace.DOWN };
    public static final BlockFace[] NORTH_EAST_SOUTH_WEST_UP_DOWN = { BlockFace.NORTH, BlockFace.EAST,
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };

    /**
     * Creates a new block finder.
     *
     * @param parser
     *            The parser of signs.
     * @param connectContainers
     *            Whether containers must be connected.
     * @return The block finder.
     */
    public static BlockFinder create(SignParser parser, boolean connectContainers) {
        if (connectContainers) {
            return new ConnectedContainersBlockFinder(parser);
        } else {
            return new SeparateContainersBlockFinder(parser);
        }
    }

    protected final SignParser parser;

    BlockFinder(SignParser parser) {
        this.parser = Preconditions.checkNotNull(parser);
    }

    /**
     * Finds all attached signs to a block, that are valid protection signs.
     *
     * @param block
     *            The block to check attached signs on.
     * @return The signs.
     */
    public Collection<ProtectionSign> findAttachedSigns(Block block) {
        Collection<ProtectionSign> signs = new ArrayList<>();
        for (BlockFace face : SIGN_ATTACHMENT_FACES) {
            Block atPosition = block.getRelative(face);
            Material material = atPosition.getType();
            if (!Tag.WALL_SIGNS.isTagged(material) && !Tag.STANDING_SIGNS.isTagged(material)) {
                continue;
            }
            if (!isAttachedSign(atPosition, block)) {
                continue;
            }
            Optional<ProtectionSign> parsedSign = parser.parseSign(atPosition);
            if (parsedSign.isPresent()) {
                signs.add(parsedSign.get());
            }
        }
        return signs;
    }

    /**
     * Finds all attached signs to a block, that are valid protection signs.
     *
     * @param blocks
     *            The blocks to check attached signs on.
     * @return The signs.
     */
    public Collection<ProtectionSign> findAttachedSigns(Collection<Block> blocks) {
        if (blocks.size() == 1) {
            // Avoid creating a builder, iterator and extra set
            return findAttachedSigns(blocks.iterator().next());
        }

        ImmutableSet.Builder<ProtectionSign> signs = ImmutableSet.builder();
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
    public abstract List<Block> findContainerNeighbors(Block block);

    /**
     * Gets the block that supports the given block. If the returned block is
     * destroyed, the given block is destroyed too.
     *
     * For blocks that are self-supporting (most blocks in Minecraft), the
     * method returns the block itself.
     *
     * @param block
     *            The block.
     * @return The block the given block is attached on.
     */
    public Block findSupportingBlock(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Gate) {
            return block.getRelative(BlockFace.DOWN);
        }
        if (data instanceof Directional) {
            return block.getRelative(((Directional) data).getFacing().getOppositeFace());
        }
        return block.getRelative(BlockFace.DOWN);
    }

    protected BlockFace getChestNeighborFaceOrNull(Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Chest)) {
            return null;
        }

        Chest chest = (Chest) blockData;
        if (chest.getType() == Type.SINGLE) {
            return null;
        }
        BlockFace towardsLeft = this.turn90Degrees(chest.getFacing());
        return chest.getType() == Type.LEFT ? towardsLeft : towardsLeft.getOppositeFace();
    }

    /**
     * Gets the parser for signs.
     *
     * @return The parser.
     */
    public SignParser getSignParser() {
        return parser;
    }

    /**
     * Checks if the sign at the given position is attached to the container.
     * Doens't check the text on the sign.
     *
     * @param signBlock
     *            The block that is a sign.
     * @param attachedTo
     *            The block the sign must be attached to. If this is not the
     *            case, the method returns false.
     * @return True if the direction and header of the sign are valid, false
     *         otherwise.
     */
    private boolean isAttachedSign(Block signBlock, Block attachedTo) {
        BlockFace requiredFace = signBlock.getFace(attachedTo);
        BlockData materialData = signBlock.getBlockData();
        BlockFace actualFace = BlockFace.DOWN;
        if (materialData instanceof WallSign) {
            actualFace = ((WallSign) materialData).getFacing().getOppositeFace();
        }
        return (actualFace == requiredFace);
    }

    protected BlockFace turn90Degrees(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                throw new IllegalArgumentException("Cannot handle " + face);
        }
    }

}