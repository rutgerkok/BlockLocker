package nl.rutgerkok.blocklocker.impl.event;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.SearchMode;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;
import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

abstract class EventListener implements Listener {

    final BlockLockerPlugin plugin;

    EventListener(BlockLockerPlugin plugin) {
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

    boolean isProtected(Block block) {

        if (plugin.getProtectCache().hasValidCache(block)) {
            return plugin.getProtectCache().getLocked(block);
        } else {
            boolean protecting = plugin.getProtectionFinder().findProtection(block).isPresent();
            plugin.getProtectCache().setCache(block, protecting);
            return protecting;
        }
    }

    boolean isProtectedForRedstone(Block block) {
        if (plugin.getRedstoneProtectCache().hasValidCache(block)) {
            return plugin.getRedstoneProtectCache().getLocked(block);
        } else {
            Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block, SearchMode.NO_SUPPORTING_BLOCKS);
            if (!protection.isPresent()) {
                plugin.getRedstoneProtectCache().setCache(block, false);
                return false;
            }
            Profile redstone = plugin.getProfileFactory().fromRedstone();
            boolean protecting = !protection.get().isAllowed(redstone);
            plugin.getRedstoneProtectCache().setCache(block, protecting);
            return protecting;
        }
    }

    boolean isExpired(Protection protection) {
        Optional<Date> cutoffDate = plugin.getChestSettings().getChestExpireDate();
        if (cutoffDate.isPresent()) {
            return protection.isExpired(cutoffDate.get());
        }
        return false;
    }
}
