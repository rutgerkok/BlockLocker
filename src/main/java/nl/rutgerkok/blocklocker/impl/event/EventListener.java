package nl.rutgerkok.blocklocker.impl.event;

import java.util.Collection;
import java.util.Date;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import com.google.common.base.Optional;

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
        return plugin.getProtectionFinder().findProtection(block).isPresent();
    }

    boolean isProtectedForRedstone(Block block) {
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block);
        if (!protection.isPresent()) {
            return false;
        }
        Profile redstone = plugin.getProfileFactory().fromRedstone();
        return !protection.get().isAllowed(redstone);
    }

    boolean isExpired(Protection protection) {
        Optional<Date> cutoffDate = plugin.getChestSettings().getChestExpireDate();
        if (cutoffDate.isPresent()) {
            return protection.isExpired(cutoffDate.get());
        }
        return false;
    }
}
