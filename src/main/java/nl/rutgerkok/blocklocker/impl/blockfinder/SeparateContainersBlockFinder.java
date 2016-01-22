package nl.rutgerkok.blocklocker.impl.blockfinder;

import java.util.Collections;
import java.util.List;

import nl.rutgerkok.blocklocker.BlockData;
import nl.rutgerkok.blocklocker.SignParser;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Chest;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import com.google.common.collect.ImmutableList;

public final class SeparateContainersBlockFinder extends BlockFinder {
    SeparateContainersBlockFinder(SignParser parser) {
        super(parser);
    }

    @Override
    public List<Block> findContainerNeighbors(Block block) {
        // Currently only chests share an inventory
        // Minecraft connects two chests next to each other that have the same
        // direction. We simply check for that condition, taking both normal
        // and trapped chests into account
        if (!(BlockData.get(block) instanceof Chest)) {
            return Collections.singletonList(block);
        }

        Material chestMaterial = block.getType(); // CHEST or TRAPPED_CHEST
        BlockFace chestFacing = ((Directional) BlockData.get(block)).getFacing();

        for (BlockFace face : CARDINAL_FACES) {
            Block atPosition = block.getRelative(face);
            if (atPosition.getType() != chestMaterial) {
                continue;
            }

            MaterialData materialData = BlockData.get(atPosition);
            if (!(materialData instanceof Directional)) {
                continue;
            }

            BlockFace facing = ((Directional) materialData).getFacing();
            if (!facing.equals(chestFacing)) {
                if (!facing.equals(chestFacing.getOppositeFace())) {
                    // ^ If the chest was carried over from older Minecraft
                    // versions, block data can be a bit weird. So opposite
                    // face is allowed too for chest connections
                    continue;
                }
            }

            return ImmutableList.of(block, atPosition);
        }

        return Collections.singletonList(block);
    }
}
