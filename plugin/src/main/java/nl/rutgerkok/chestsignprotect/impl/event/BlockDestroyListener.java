package nl.rutgerkok.chestsignprotect.impl.event;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.google.common.base.Optional;

public class BlockDestroyListener implements Listener {

    private final ChestSignProtect plugin;

    public BlockDestroyListener(ChestSignProtect plugin) {
        Validate.notNull(plugin);
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<Protection> protection = plugin.getProtectionFinder()
                .findProtection(event.getBlock());
        if (!protection.isPresent()) {
            return;
        }

        Player player = event.getPlayer();
        Profile profile = plugin.getProfileFactory().fromPlayer(player);
        if (!protection.get().isOwner(profile)) {
            event.setCancelled(true);
        }
    }

}
