package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.block.Block;
import java.util.Map;
import java.util.WeakHashMap;

public class Cache {
    private BlockLockerPluginImpl plugin;
    private long expireTime = 1000;
    private Map<Block, CacheContainer> accessCaching = new WeakHashMap<>(500);

    public Cache(BlockLockerPluginImpl plugin) {
        this.plugin = plugin;
    }

    public boolean hasValidCache(Block block) {
       CacheContainer container = accessCaching.get(block);
       if(container == null){
           return false;
       }
       return System.currentTimeMillis() - container.getTime() > expireTime;
    }

    public boolean getLocked(Block block) {
        return accessCaching.get(block).isLocked();
    }

    public void setCache(Block block, boolean locked) {
        accessCaching.put(block, new CacheContainer(locked, System.currentTimeMillis()));
    }

    public void resetCache(Block block) {
        accessCaching.remove(block);
    }

}
class CacheContainer{
    private boolean locked;
    private long time;
    public CacheContainer(boolean locked, long time){
        this.locked = locked;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setTime(long time) {
        this.time = time;
    }
}