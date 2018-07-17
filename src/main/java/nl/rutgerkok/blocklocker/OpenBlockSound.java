package nl.rutgerkok.blocklocker;

import org.bukkit.Material;
import org.bukkit.Sound;

/**
 * Sounds for when a block is opened/closed.
 *
 */
public final class OpenBlockSound {

    /**
     * Gets the sound for opening/closing the given material. For unknown
     * materials, a generic sound is returned.
     *
     * @param material
     *            The material.
     * @param open
     *            Whether ther material is opened.
     * @return The sound.
     */
    public static Sound get(Material material, boolean open) {
        if (open) {
            switch (material) {
                case ACACIA_FENCE_GATE:
                case BIRCH_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case SPRUCE_FENCE_GATE:
                case OAK_FENCE_GATE:
                    return Sound.BLOCK_FENCE_GATE_OPEN;
                case OAK_DOOR:
                case SPRUCE_DOOR:
                case BIRCH_DOOR:
                case JUNGLE_DOOR:
                case ACACIA_DOOR:
                case DARK_OAK_DOOR:
                    return Sound.BLOCK_WOODEN_DOOR_OPEN;
                case IRON_DOOR:
                    return Sound.BLOCK_IRON_DOOR_OPEN;
                case OAK_TRAPDOOR:
                case SPRUCE_TRAPDOOR:
                case BIRCH_TRAPDOOR:
                case JUNGLE_TRAPDOOR:
                case ACACIA_TRAPDOOR:
                case DARK_OAK_TRAPDOOR:
                    return Sound.BLOCK_WOODEN_TRAPDOOR_OPEN;
                case IRON_TRAPDOOR:
                    return Sound.BLOCK_IRON_TRAPDOOR_OPEN;
                default:
                    return Sound.BLOCK_WOODEN_TRAPDOOR_OPEN;
            }
        } else {
            switch (material) {
                case ACACIA_FENCE_GATE:
                case BIRCH_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case SPRUCE_FENCE_GATE:
                case OAK_FENCE_GATE:
                    return Sound.BLOCK_FENCE_GATE_CLOSE;
                case OAK_DOOR:
                case SPRUCE_DOOR:
                case BIRCH_DOOR:
                case JUNGLE_DOOR:
                case ACACIA_DOOR:
                case DARK_OAK_DOOR:
                    return Sound.BLOCK_WOODEN_DOOR_CLOSE;
                case IRON_DOOR:
                    return Sound.BLOCK_IRON_DOOR_CLOSE;
                case OAK_TRAPDOOR:
                case SPRUCE_TRAPDOOR:
                case BIRCH_TRAPDOOR:
                case JUNGLE_TRAPDOOR:
                case ACACIA_TRAPDOOR:
                case DARK_OAK_TRAPDOOR:
                    return Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE;
                case IRON_TRAPDOOR:
                    return Sound.BLOCK_IRON_TRAPDOOR_CLOSE;
                default:
                    return Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE;
            }
        }
    }
}
