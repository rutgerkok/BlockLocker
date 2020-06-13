package nl.rutgerkok.blocklocker.impl;

import java.util.concurrent.TimeUnit;

import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import nl.rutgerkok.blocklocker.HopperCache;

final class HopperCacheImpl implements HopperCache {

    private static final long EXPIRE_TIME_SECONDS = 10;
    private Cache<Block, Boolean> accessCaching;

    HopperCacheImpl(Plugin plugin) {
        accessCaching = CacheBuilder.newBuilder().initialCapacity(1000)
                .maximumSize(5000)
                .expireAfterWrite(EXPIRE_TIME_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public CacheFlag getIsRedstoneAllowed(Block block) {
        Boolean isLocked = accessCaching.getIfPresent(block);

        if (isLocked == null) {
            return CacheFlag.MISS_CACHE;
        }
        if (isLocked == true) {
            return CacheFlag.PROTECTED;
        } else {
            return CacheFlag.NOT_PROTECTED;
        }
    }

    @Override
    public void setIsRedstoneAllowed(Block block, boolean locked) {
        accessCaching.put(block, locked);
    }
}
