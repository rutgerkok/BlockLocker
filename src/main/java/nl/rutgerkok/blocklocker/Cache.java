package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class Cache {
    private BlockLockerPluginImpl plugin;
    private long expireTime = 1000;
    private String prefix;

    public Cache(BlockLockerPluginImpl plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;
    }

    public boolean hasValidCache(Block block) {
        if (!block.hasMetadata(prefix + "time")) {
            return false;
        }
        if ((System.currentTimeMillis() - block.getMetadata(prefix + "time").get(0).asLong()) > expireTime) {
            this.resetCache(block);
            return false;
        }
        return true;
    }

    public boolean getLocked(Block block) {
        List<MetadataValue> metadatas = block.getMetadata(prefix + "locked");
        return metadatas.get(0).asBoolean();
    }

    public void setCache(Block block, boolean locked) {
        block.removeMetadata(prefix + "locked", plugin);
        block.removeMetadata(prefix + "time", plugin);
        block.setMetadata(prefix + "locked", new FixedMetadataValue(plugin, locked));
        block.setMetadata(prefix + "time", new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    public void resetCache(Block block) {
        block.removeMetadata(prefix + "expires", plugin);
        block.removeMetadata(prefix + "time", plugin);
        for (BlockFace blockface : BlockFace.values()) {
            Block relative = block.getRelative(blockface);
            if (relative.getType() == block.getType()) {
                relative.removeMetadata(prefix + "expires", plugin);
                relative.removeMetadata(prefix + "time", plugin);
            }
        }
    }

}
