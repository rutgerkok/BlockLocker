package nl.rutgerkok.blocklocker;

import com.google.common.collect.MapMaker;
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.TimerTask;

public class Cache extends TimerTask {
    private BlockLockerPluginImpl plugin;
    private long expireTime = 10000;
    private Map<Block, CacheContainer> accessCaching = new MapMaker().initialCapacity(1000).makeMap();
    private boolean cachingFlushing = false;

    public Cache(BlockLockerPluginImpl plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanCache();
            }
        }.runTaskTimerAsynchronously(plugin, 0, expireTime * 20);
    }

    public CacheFlag getLocked(Block block) {
        CacheContainer container = accessCaching.get(block);
        if (container == null) {
            return CacheFlag.MISS_CACHE;
        }
        if (isExpired(container)) {
            return CacheFlag.MISS_CACHE;
        }
        if (container.isLocked()) {
            return CacheFlag.PROTECTED;
        } else {
            return CacheFlag.NOT_PROTECTED;
        }
    }

    public boolean isExpired(CacheContainer container) {
        return System.currentTimeMillis() - container.getTime() > expireTime;
    }

    public void setCache(Block block, boolean locked) {
        accessCaching.put(block, new CacheContainer(locked, System.currentTimeMillis()));
    }

    public void resetCache(Block block) {
        accessCaching.remove(block);
    }

    public void cleanCache() {
        if (cachingFlushing) {
            return; //Make sure there only one thread to clean the caches.
        }
        cachingFlushing = true;
        accessCaching.keySet().removeIf(e -> isExpired(accessCaching.get(e)));
        cachingFlushing = false;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        cleanCache();
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

