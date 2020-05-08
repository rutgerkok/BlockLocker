package nl.rutgerkok.blocklocker.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.rutgerkok.blocklocker.HopperCache;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

final class HopperCacheImpl implements HopperCache {

    private static class CacheContainer {

        final long creationTimeMillis;
        final boolean isLocked;

        public CacheContainer(boolean locked, long time) {
            this.isLocked = locked;
            this.creationTimeMillis = time;
        }
    }

    private static final long EXPIRE_TIME_SECONDS = 10;
    private Cache<Block, CacheContainer> accessCaching;

    HopperCacheImpl(Plugin plugin) {
        accessCaching = CacheBuilder.newBuilder().initialCapacity(1000)
                .maximumSize(5000)
                .expireAfterWrite(EXPIRE_TIME_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public CacheFlag getIsRedstoneAllowed(Block block) {
        CacheContainer container;
        container = accessCaching.getIfPresent(block);

        if (container == null) {
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
