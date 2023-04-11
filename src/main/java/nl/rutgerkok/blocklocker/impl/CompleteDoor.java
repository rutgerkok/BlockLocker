package nl.rutgerkok.blocklocker.impl;

import java.util.Collection;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;

import com.google.common.collect.ImmutableList;

import nl.rutgerkok.blocklocker.OpenBlockSound;
import nl.rutgerkok.blocklocker.protection.Protection.SoundCondition;

/**
 * Represents a single or double door. Used to find all blocks of a door, as
 * well to open or close a door.
 *
 */
public final class CompleteDoor {

    private enum Hinge {
        LEFT,
        RIGHT,
        UNKNOWN
    }

    private static Door asDoorMaterialOrNull(Block nullableBlock) {
        if (nullableBlock == null) {
            return null;
        }
        BlockData materialData = nullableBlock.getBlockData();
        if (materialData instanceof Door) {
            return (Door) materialData;
        }
        return null;
    }

    private static BlockFace getFaceOrNull(Block doorBlock) {
        Door door = asDoorMaterialOrNull(doorBlock);
        if (door == null) {
            return null;
        }
        return door.getFacing();
    }

    @Nullable
    private static BlockFace getFaceToLeftDoorOrNull(Block bottomHalfDoorBlock) {
        BlockFace rightDoor = getFaceToRightDoorOrNull(bottomHalfDoorBlock);
        if (rightDoor == null) {
            return null;
        }
        return rightDoor.getOppositeFace();
    }

    @Nullable
    private static BlockFace getFaceToRightDoorOrNull(Block bottomHalfDoorBlock) {
        Door door = asDoorMaterialOrNull(bottomHalfDoorBlock);
        if (door == null) {
            // Not a valid door, lower half is missing
            return null;
        }

        switch (door.getFacing()) {
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            case NORTH:
                return BlockFace.EAST;
            default:
                throw new RuntimeException("Invalid facing for door: " + door);
        }
    }

    private static Hinge getHinge(Block topHalfDoorBlock) {
        Door door = asDoorMaterialOrNull(topHalfDoorBlock);
        if (door != null) {
            return door.getHinge() == Door.Hinge.LEFT ? Hinge.LEFT : Hinge.RIGHT;
        }
        return Hinge.UNKNOWN;
    }

    @Nullable
    private static Block getRelativeOrNull(@Nullable Block block, @Nullable  BlockFace face) {
        if (block == null || face == null) {
            return null;
        }
        return block.getRelative(face);
    }

    private static boolean isTopHalf(Block doorBlock) {
        Door door = asDoorMaterialOrNull(doorBlock);
        if (door != null) {
            return door.getHalf() == Half.TOP;
        }
        return false;
    }

    private final Material doorMaterial;
    /** Top left block, may be null. */
    private final Block topLeftBlock;
    /** Top right block, may be null. */
    private final Block topRightBlock;

    /** Bottom left block, may be null. */
    private final Block bottomLeftBlock;

    /** Bottom right block, may be null. */
    private final Block bottomRightBlock;

    /**
     * Creates a new door. The given block must be part of the door.
     *
     * @param doorBlock
     *            A block that is part of the door.
     */
    public CompleteDoor(Block doorBlock) {
        doorMaterial = doorBlock.getType();
        BlockFace doorFace = getFaceOrNull(doorBlock);

        Block topBlock;
        Block bottomBlock;

        if (isTopHalf(doorBlock)) {
            // Top half
            topBlock = doorBlock;
            bottomBlock = doorBlock.getRelative(BlockFace.DOWN);
        } else {
            // Bottom half
            bottomBlock = doorBlock;
            topBlock = doorBlock.getRelative(BlockFace.UP);
        }

        Hinge hinge = getHinge(topBlock);
        if (hinge == Hinge.LEFT) {
            // Hinge on the left, there may be another door on the right
            topLeftBlock = asDoorBlockOrNull(topBlock);
            bottomLeftBlock = asDoorBlockOrNull(bottomBlock);

            BlockFace faceToRightDoor = getFaceToRightDoorOrNull(bottomBlock);
            topRightBlock = asDoorBlockOrNull(getRelativeOrNull(topBlock, faceToRightDoor), Hinge.RIGHT, doorFace);
            bottomRightBlock = asDoorBlockOrNull(getRelativeOrNull(bottomBlock, faceToRightDoor), Hinge.RIGHT, doorFace);
        } else if (hinge == Hinge.RIGHT) {
            // Hinge on the right, there may be another door on the left
            topRightBlock = asDoorBlockOrNull(topBlock);
            bottomRightBlock = asDoorBlockOrNull(bottomBlock);

            BlockFace faceToLeftDoor = getFaceToLeftDoorOrNull(bottomBlock);
            topLeftBlock = asDoorBlockOrNull(getRelativeOrNull(topBlock, faceToLeftDoor), Hinge.LEFT, doorFace);
            bottomLeftBlock = asDoorBlockOrNull(getRelativeOrNull(bottomBlock, faceToLeftDoor), Hinge.LEFT, doorFace);
        } else {
            topLeftBlock = null;
            topRightBlock = null;
            bottomLeftBlock = null;
            bottomRightBlock = null;
        }
    }

    private Block asDoorBlockOrNull(Block nullableBlock) {
        if (nullableBlock != null && nullableBlock.getType() == doorMaterial) {
            return nullableBlock;
        }
        return null;
    }

    private @Nullable Block asDoorBlockOrNull(@Nullable Block nullableBlock, @Nullable Hinge expectedHinge,
            @Nullable BlockFace expectedFace) {
        if (nullableBlock != null && nullableBlock.getType() == doorMaterial) {
            Door door = asDoorMaterialOrNull(nullableBlock);
            if (door.getFacing() != expectedFace) {
                return null;
            }

            switch (door.getHinge()) {
                // Working with a different enum, so we need to translate the value here
                case LEFT:
                    if (expectedHinge == Hinge.LEFT) {
                        return nullableBlock;
                    }
                    break;
                case RIGHT:
                    if (expectedHinge == Hinge.RIGHT) {
                        return nullableBlock;
                    }
                    break;
            }
        }
        return null;
    }

    /**
     * Gets a collection of all blocks where attached protection signs are used
     * for this door. This includes the blocks above and below the door, and the
     * blocks on the left and right of the door.
     *
     * @return All blocks that can have protection signs attached.
     */
    public Collection<Block> getBlocksForSigns() {
        BlockFace toLeft = getFaceToLeftDoor();
        BlockFace toRight = toLeft.getOppositeFace();

        ImmutableList.Builder<Block> blocks = ImmutableList.builder();
        if (bottomLeftBlock != null) {
            blocks.add(bottomLeftBlock);
            blocks.add(bottomLeftBlock.getRelative(BlockFace.DOWN));
            blocks.add(bottomLeftBlock.getRelative(toLeft));
            if (bottomRightBlock == null) {
                // Single door, add block to the right too
                blocks.add(bottomLeftBlock.getRelative(toRight));
            }
        }
        if (bottomRightBlock != null) {
            blocks.add(bottomRightBlock);
            blocks.add(bottomRightBlock.getRelative(BlockFace.DOWN));
            blocks.add(bottomRightBlock.getRelative(toRight));
            if (bottomLeftBlock == null) {
                // Single door, add block to the left too
                blocks.add(bottomRightBlock.getRelative(toLeft));
            }
        }
        if (topLeftBlock != null) {
            blocks.add(topLeftBlock);
            blocks.add(topLeftBlock.getRelative(BlockFace.UP));
            blocks.add(topLeftBlock.getRelative(toLeft));
            if (topRightBlock == null) {
                // Single door, add block to the right too
                blocks.add(topLeftBlock.getRelative(toRight));
            }
        }
        if (topRightBlock != null) {
            blocks.add(topRightBlock);
            blocks.add(topRightBlock.getRelative(BlockFace.UP));
            blocks.add(topRightBlock.getRelative(toRight));
            if (topLeftBlock == null) {
                // Single door, add block to the left too
                blocks.add(topRightBlock.getRelative(toLeft));
            }
        }
        return blocks.build();
    }

    private BlockFace getFaceToLeftDoor() {
        Block bottomBlock = asDoorBlockOrNull(this.bottomLeftBlock);
        if (bottomBlock == null) {
            bottomBlock = asDoorBlockOrNull(this.bottomRightBlock);
            if (bottomBlock == null) {
                return BlockFace.SELF;
            }
        }

        return getFaceToLeftDoorOrNull(bottomBlock);
    }

    /**
     * Returns one of the two (or four) door blocks.
     *
     * @return One of the two (or four) blocks.
     */
    public Block getSomeDoorBlock() {
        if (this.bottomLeftBlock != null) {
            return this.bottomLeftBlock;
        }
        if (this.bottomRightBlock != null) {
            return this.bottomRightBlock;
        }
        if (this.topLeftBlock != null) {
            return this.topLeftBlock;
        }
        if (this.topRightBlock != null) {
            return this.topRightBlock;
        }
        throw new IllegalStateException("All four door blocks where null, this should not be possible");
    }

    /**
     * Gets whether the door is currently open. The result is undefined if the
     * door is half-open, half-closed.
     *
     * @return True if the door is currently open, false otherwise.
     */
    public boolean isOpen() {
        BlockData materialData = null;
        if (bottomRightBlock != null) {
            materialData = bottomRightBlock.getBlockData();
        }
        if (bottomLeftBlock != null) {
            materialData = bottomLeftBlock.getBlockData();
        }
        return (materialData instanceof Door) && ((Door) materialData).isOpen();
    }

    private void playSound(Block bottomBlock, boolean open, SoundCondition condition) {
        if (condition == SoundCondition.NEVER) {
            return;
        }
        if (open == isOpen()) {
            return;
        }
        boolean ironDoor = bottomBlock.getType() == Material.IRON_DOOR;
        if (condition == SoundCondition.AUTOMATIC && !ironDoor) {
            return;
        }

        Sound sound = OpenBlockSound.get(bottomBlock.getType(), open);

        bottomBlock.getWorld().playSound(bottomBlock.getLocation(), sound, 1f, 0.7f);
    }

    /**
     * Opens or closes the door. If the door has been destroyed after creating
     *
     * @param open
     *            Whether the door must be opened (true) or closed (false).
     * @param soundAction
     *            Whether a sound must be played.
     */
    public void setOpen(boolean open, SoundCondition soundAction) {
        Door leftDoor = asDoorMaterialOrNull(bottomLeftBlock);
        if (leftDoor != null) {
            // Sound effect
            playSound(bottomLeftBlock, open, soundAction);

            // Don't play sound for other half
            soundAction = SoundCondition.NEVER;

            // Door toggle
            leftDoor.setOpen(open);
            bottomLeftBlock.setBlockData(leftDoor);
        }

        Door rightDoor = asDoorMaterialOrNull(bottomRightBlock);
        if (rightDoor != null) {
            // Sound effect
            playSound(bottomRightBlock, open, soundAction);

            // Door toggle
            rightDoor.setOpen(open);
            bottomRightBlock.setBlockData(rightDoor);
        }
    }

}
