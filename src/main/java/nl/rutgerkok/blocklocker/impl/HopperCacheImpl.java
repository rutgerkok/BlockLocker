package nl.rutgerkok.blocklocker.impl;

import java.util.concurrent.TimeUnit;

import org.bukkit.block.Block;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import nl.rutgerkok.blocklocker.ProtectionCache;

final class HopperCacheImpl implements ProtectionCache {

    private static final long EXPIRE_TIME_SECONDS = 10;
    private final Cache<Block, Boolean> redstoneCache;
    private final Cache<Block, Boolean> golemCache;

    HopperCacheImpl() {
        redstoneCache = CacheBuilder.newBuilder().initialCapacity(1000)
                .maximumSize(5000)
                .expireAfterWrite(EXPIRE_TIME_SECONDS, TimeUnit.SECONDS)
                .build();
        golemCache = CacheBuilder.newBuilder().initialCapacity(1000)
                .maximumSize(5000)
                .expireAfterWrite(EXPIRE_TIME_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    private Cache<Block, Boolean> getCache(CacheType cacheType) {
        return switch (cacheType) {
            case REDSTONE -> this.redstoneCache;
            case GOLEM -> this.golemCache;
        };
    }

    @Override
    public CacheFlag getAllowed(Block block, CacheType cacheType) {
        Boolean isAllowed = getCache(cacheType).getIfPresent(block);

        if (isAllowed == null) {
            return CacheFlag.MISS_CACHE;
        }
        if (isAllowed) {
            return CacheFlag.ALLOWED;
        } else {
            return CacheFlag.NOT_ALLOWED;
        }
    }

    @Override
    public void setAllowed(Block block, CacheType cacheType, boolean allowed) {
        getCache(cacheType).put(block, allowed);
    }
}
