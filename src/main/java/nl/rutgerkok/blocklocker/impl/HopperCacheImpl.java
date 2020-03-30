package nl.rutgerkok.blocklocker.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import nl.rutgerkok.blocklocker.HopperCache;

final class HopperCacheImpl implements HopperCache {

    private static class CacheContainer {

        final long creationTimeMillis;
        final boolean isLocked;

        public CacheContainer(boolean locked, long time) {
            this.isLocked = locked;
            this.creationTimeMillis = time;
        }

        boolean isExpired(long currentTimeMillis) {
            return currentTimeMillis - this.creationTimeMillis > EXPIRE_TIME_SECONDS * 1000;
        }
    }

    private static final long EXPIRE_TIME_SECONDS = 10;
    private Map<Block, CacheContainer> accessCaching = new HashMap<>(1000);

    HopperCacheImpl(Plugin plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanCache, EXPIRE_TIME_SECONDS * 20,
                EXPIRE_TIME_SECONDS * 20);
    }

    /**
     * Removes all expired entries.
     */
    private void cleanCache() {
        long currentTime = System.currentTimeMillis();
        Predicate<CacheContainer> isExpired = cacheEntry -> cacheEntry.isExpired(currentTime);
        accessCaching.values().removeIf(isExpired);
    }

    @Override
    public CacheFlag getIsRedstoneAllowed(Block block) {
        CacheContainer container;
        container = accessCaching.get(block);

        if (container == null) {
            return CacheFlag.MISS_CACHE;
        }
        if (container.isExpired(System.currentTimeMillis())) {
            return CacheFlag.MISS_CACHE;
        }
        if (container.isLocked) {
            return CacheFlag.PROTECTED;
        } else {
            return CacheFlag.NOT_PROTECTED;
        }
    }

    @Override
    public void setIsRedstoneAllowed(Block block, boolean locked) {
        accessCaching.put(block, new CacheContainer(locked, System.currentTimeMillis()));
    }
}
