package nl.rutgerkok.blocklocker.impl.blockfinder;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;

import nl.rutgerkok.blocklocker.SignParser;

final class SeparateContainersBlockFinder extends BlockFinder {
    SeparateContainersBlockFinder(SignParser parser) {
        super(parser);
    }

    @Override
    public List<Block> findContainerNeighbors(Block block) {
        // Currently only chests share an inventory
        // Minecraft connects two chests next to each other that have the same
        // direction. We simply check for that condition, taking both normal
        // and trapped chests into account
        if (!(block.getBlockData() instanceof Chest)) {
            return Collections.singletonList(block);
        }

        Material chestMaterial = block.getType(); // CHEST or TRAPPED_CHEST
        BlockFace chestFacing = ((Directional) block.getBlockData()).getFacing().getOppositeFace();

        for (BlockFace face : CARDINAL_FACES) {
            Block atPosition = block.getRelative(face);
            if (atPosition.getType() != chestMaterial) {
                continue;
            }

            BlockData materialData = atPosition.getBlockData();
            if (!(materialData instanceof Directional)) {
                continue;
            }

            BlockFace facing = ((Directional) materialData).getFacing().getOppositeFace();
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
