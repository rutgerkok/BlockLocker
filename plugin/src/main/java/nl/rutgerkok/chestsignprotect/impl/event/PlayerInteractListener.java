package nl.rutgerkok.chestsignprotect.impl.event;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.base.Optional;

public class PlayerInteractListener implements Listener {

    private final ChestSignProtect plugin;

    public PlayerInteractListener(ChestSignProtect plugin) {
        Validate.notNull(plugin);
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Optional<Protection> protection = plugin.getProtectionFinder()
                .findProtection(event.getClickedBlock());
        if (!protection.isPresent()) {
            return;
        }

        // Check if protection needs update
        if (protection.get().isMissingUniqueIds()) {
            plugin.fixMissingUniqueIds(protection.get());
        }

        // Check if player is allowed
        Player player = event.getPlayer();
        Profile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        if (protection.get().isAllowed(playerProfile)) {
            return;
        }

        event.setCancelled(true);
    }
}
