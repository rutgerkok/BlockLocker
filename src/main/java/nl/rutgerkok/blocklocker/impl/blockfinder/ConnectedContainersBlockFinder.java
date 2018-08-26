package nl.rutgerkok.blocklocker.impl.blockfinder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import nl.rutgerkok.blocklocker.SignParser;

final class ConnectedContainersBlockFinder extends BlockFinder {
    private static final int MAX_SEARCH_DISTANCE = 10;

    ConnectedContainersBlockFinder(SignParser parser) {
        super(parser);
    }

    @Override
    public List<Block> findContainerNeighbors(Block block) {
        Material containerMaterial = block.getType();
        List<Block> blocks = new ArrayList<>();
        blocks.add(block);

        // Search above and below on the starting block
        searchVertical(containerMaterial, block, blocks);

        // Just searches in the four cardinal faces, until it hits a block
        // of another type. Blocks above and below of the same block type are
        // also searched
        for (BlockFace face : CARDINAL_FACES) {
            int distance = 0;
            Block atPosition = block.getRelative(face);
            while (distance < MAX_SEARCH_DISTANCE && atPosition.getType() == containerMaterial) {
                blocks.add(atPosition);
                searchVertical(containerMaterial, atPosition, blocks);

                atPosition = atPosition.getRelative(face);
                distance++;
            }
        }

        BlockFace chestNeighborFace = this.getChestNeighborFaceOrNull(block);
        if (chestNeighborFace != null) {
            // Double chest, also perform search for other chest block
            Block chestNeighbor = block.getRelative(chestNeighborFace);
            BlockFace[] searchDirections = { this.turn90Degrees(chestNeighborFace),
                    this.turn90Degrees(chestNeighborFace).getOppositeFace() };
            for (BlockFace face : searchDirections) {
                int distance = 0;
                Block atPosition = chestNeighbor.getRelative(face);
                while (distance < MAX_SEARCH_DISTANCE && atPosition.getType() == containerMaterial) {
                    blocks.add(atPosition);
                    searchVertical(containerMaterial, atPosition, blocks);

                    atPosition = atPosition.getRelative(face);
                    distance++;
                }
            }
        }

        return blocks;
    }

    /**
     * Searches for blocks of the same type above and below the starting block.
     *
     * @param containerMaterial
     *            The material {@link Block#getType() startingBlock.getType()}
     *            returns.
     * @param startingBlock
     *            The starting block.
     * @param blocks
     *            All connected blocks above and below (so not the starting block
     *            itself) of the same type will be added to this list.
     */
    private void searchVertical(Material containerMaterial, Block startingBlock, List<Block> blocks) {
        for (BlockFace face : VERTICAL_FACES) {
            int distance = 0;
            Block atPosition = startingBlock.getRelative(face);
            while (distance < MAX_SEARCH_DISTANCE && atPosition.getType() == containerMaterial) {
                blocks.add(atPosition);

                atPosition = atPosition.getRelative(face);
                distance++;
            }
        }
    }
}
