package nl.rutgerkok.blocklocker.impl.blockfinder;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import nl.rutgerkok.blocklocker.SignParser;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

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
    BlockFace chestNeighborFace = this.getChestNeighborFaceOrNull(block);
    if (chestNeighborFace == null) {
      return Collections.singletonList(block);
    }
    return ImmutableList.of(block, block.getRelative(chestNeighborFace));
  }
}
