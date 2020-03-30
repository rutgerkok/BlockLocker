package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class Cache extends TimerTask {
    private BlockLockerPluginImpl plugin;
    private long expireTime = 10;
    private Map<Block, CacheContainer> accessCaching = new HashMap<>(1000);
    private final Object lock = new Object();

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
        CacheContainer container;
        synchronized (lock) {
            container = accessCaching.get(block);
        }
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
        synchronized (lock) {
            accessCaching.put(block, new CacheContainer(locked, System.currentTimeMillis()));
        }
    }

    public void cleanCache() {
        synchronized (lock) {
            accessCaching.keySet().removeIf(e -> isExpired(accessCaching.get(e)));
        }
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

