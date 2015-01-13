package nl.rutgerkok.blocklocker;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Chest;
import org.bukkit.material.MaterialData;

/**
 * Two methods that Bukkit should really add to their {@link Block) interface.
 *
 */
public final class BlockData {

    private BlockData() {
        
    }
    
    /**
     * Fast alternative for the slow {@code block.getState().getData()} call.
     * This method skips the part where unnecessary BlockStates are created. For
     * chests this is quite slow, as all items need to be copied.
     *
     * <p>
     * Some materials, like Trapped Chest, don't return the appropriate material
     * data in Bukkit. This method fixes those bugs. For example,
     * {@link Material#TRAPPED_CHEST} returns a {@link MaterialData} of
     * {@link Chest}.
     * 
     * @param block
     *            The block.
     * @return The material data of the block.
     */
    @SuppressWarnings("deprecation")
    public static MaterialData get(Block block) {
        Material material = block.getType();
        byte data = block.getData();

        // Special-case trapped chest, it should have a Chest MaterialData, but
        // hasn't for some reason
        if (material == Material.TRAPPED_CHEST) {
            return new Chest(Material.TRAPPED_CHEST, data);
        }

        return material.getNewData(data);
    }

    /**
     * Sets the block to the given material data.
     * 
     * @param block
     *            The block.
     * @param materialData
     *            The material data.
     * @return Whether the block was changed.
     */
    @SuppressWarnings("deprecation")
    public static boolean set(Block block, MaterialData materialData) {
        return block.setTypeIdAndData(materialData.getItemTypeId(), materialData.getData(), true);
    }
}
