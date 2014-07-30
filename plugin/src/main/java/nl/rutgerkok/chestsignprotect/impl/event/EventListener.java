package nl.rutgerkok.chestsignprotect.impl.event;

import java.util.Collection;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

abstract class EventListener implements Listener {

    final ChestSignProtect plugin;

    EventListener(ChestSignProtect plugin) {
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
}
