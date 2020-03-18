package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class Cache {
    private BlockLockerPluginImpl plugin;
    private long expireTime = 1000;
    public Cache (BlockLockerPluginImpl plugin){
        this.plugin = plugin;
    }

    public boolean hasValidCache(Block block){
        if(block == null){
            return false;
        }
        if(!block.hasMetadata("time")){
            return false;
        }
        if((System.currentTimeMillis() - block.getMetadata("time").get(0).asLong()) > expireTime){
            this.resetCache(block);
            return false;
        }
        return true;
    }

    public boolean getLocked(Block block){
        List<MetadataValue> metadatas = block.getMetadata("locked");
        return metadatas.get(0).asBoolean();
    }

    public void setCache(Block block, boolean locked){
        block.removeMetadata("locked", plugin);
        block.removeMetadata("time",plugin);
        block.setMetadata("locked", new FixedMetadataValue(plugin, locked));
        block.setMetadata("time", new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    public void resetCache(Block block){
        block.removeMetadata("expires", plugin);
        block.removeMetadata("time",plugin);
        for (BlockFace blockface : BlockFace.values()){
            Block relative = block.getRelative(blockface);
            if (relative.getType() == block.getType()){
                relative.removeMetadata("expires", plugin);
                block.removeMetadata("time",plugin);
            }
        }
    }

}
