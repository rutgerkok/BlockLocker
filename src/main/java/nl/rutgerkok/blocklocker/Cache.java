package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cache {
    private BlockLockerPluginImpl plugin;
    private long expireTime = 10000;
    private Map<Block, CacheContainer> accessCaching = Collections.synchronizedMap(new HashMap<>(1000));

    public Cache(BlockLockerPluginImpl plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::cleanCache,10000*20,10000*20);
    }

    public boolean hasValidCache(Block block) {
        CacheContainer container = accessCaching.get(block);
        if (container == null) {
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

    public void cleanCache() {
        final List<Block> pendingRemoval = new CopyOnWriteArrayList<>();
        accessCaching.keySet().parallelStream().forEach(b -> { //Faster when there have a lot of caches.
            if (!hasValidCache(b)) {
                pendingRemoval.add(b);
            }
        });
        pendingRemoval.forEach(b -> accessCaching.remove(b));
    }

}

class CacheContainer {
    private boolean locked;
    private long time;

    public CacheContainer(boolean locked, long time) {
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