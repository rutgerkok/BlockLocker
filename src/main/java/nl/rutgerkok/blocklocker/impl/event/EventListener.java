package nl.rutgerkok.blocklocker.impl.event;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import nl.rutgerkok.blocklocker.HopperCache;
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
        HopperCache.CacheFlag flag = plugin.getHopperCache().getIsRedstoneAllowed(block);
        if (flag != HopperCache.CacheFlag.MISS_CACHE) {
            return flag == HopperCache.CacheFlag.PROTECTED;
        } else {
            Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block, SearchMode.NO_SUPPORTING_BLOCKS);
            if (!protection.isPresent()) {
                plugin.getHopperCache().setIsRedstoneAllowed(block, false);
                return false;
            }
            Profile redstone = plugin.getProfileFactory().fromRedstone();
            boolean denied = !protection.get().isAllowed(redstone);
            plugin.getHopperCache().setIsRedstoneAllowed(block, denied);
            return denied;
        }
    }
}
