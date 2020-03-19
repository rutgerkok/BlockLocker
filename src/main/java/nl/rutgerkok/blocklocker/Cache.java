package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class Cache {
    private BlockLockerPluginImpl plugin;
    private long expireTime = 1000;
    private Map<Block, Boolean> accessCaching = new WeakHashMap<>(50);
    private Map<Block, Long> cachingExpire = new WeakHashMap<>(50);

    public Cache(BlockLockerPluginImpl plugin) {
        this.plugin = plugin;
    }

    public boolean hasValidCache(Block block) {
        Long time = cachingExpire.get(block);
        if(time == null){
            return false;
        }
        return time <= expireTime;
    }

    public boolean getLocked(Block block) {
        return accessCaching.getOrDefault(block,false);
    }

    public void setCache(Block block, boolean locked) {
        accessCaching.put(block,locked);
        cachingExpire.put(block,System.currentTimeMillis());
    }

    public void resetCache(Block block) {
        accessCaching.remove(block);
        cachingExpire.remove(block);
    }

}
