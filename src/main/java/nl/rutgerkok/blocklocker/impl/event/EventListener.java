package nl.rutgerkok.blocklocker.impl.event;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import nl.rutgerkok.blocklocker.ProtectionCache;
import nl.rutgerkok.blocklocker.SearchMode;
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

abstract class EventListener implements Listener {

    final BlockLockerPluginImpl plugin;

    EventListener(BlockLockerPluginImpl plugin) {
        Validate.notNull(plugin);
        this.plugin = plugin;
    }

    boolean anyProtected(Collection<Block> blocks) {
        for (Block block : blocks) {
            if (isProtected(block)) {
                return true;
            }
        }
        return false;
    }

    boolean isExpired(Protection protection) {
        Optional<Date> cutoffDate = plugin.getChestSettings().getChestExpireDate();
        if (cutoffDate.isPresent()) {
            return protection.isExpired(cutoffDate.get());
        }
        return false;
    }

    boolean isProtected(Block block) {
        return plugin.getProtectionFinder().findProtection(block).isPresent();
    }

    boolean isRedstoneDenied(Block block) {
        ProtectionCache.CacheFlag flag = plugin.getProtectionCache().getAllowed(block, ProtectionCache.CacheType.REDSTONE);
        if (flag != ProtectionCache.CacheFlag.MISS_CACHE) {
            return flag == ProtectionCache.CacheFlag.NOT_ALLOWED;
        } else {
            Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block, SearchMode.MAIN_BLOCKS_ONLY);
            if (protection.isEmpty()) {
                plugin.getProtectionCache().setAllowed(block, ProtectionCache.CacheType.REDSTONE,true);
                return false;
            }
            Profile redstone = plugin.getProfileFactory().fromRedstone();
            boolean allowed = protection.get().isAllowed(redstone);
            plugin.getProtectionCache().setAllowed(block, ProtectionCache.CacheType.REDSTONE, allowed);
            return !allowed;
        }
    }
}
