package nl.rutgerkok.chestsignprotect.impl.event;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import com.google.common.base.Optional;

public class InteractListener extends EventListener {

    public InteractListener(ChestSignProtect plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        InventoryHolder from = event.getInitiator().getHolder();
        InventoryHolder to = event.getDestination().getHolder();
        if (from instanceof BlockState) {
            if (isProtected(((BlockState) from).getBlock())) {
                event.setCancelled(true);
                return;
            }
        }
        if (to instanceof BlockState) {
            if (isProtected(((BlockState) to).getBlock())) {
                event.setCancelled(true);
                return;
            }
        }
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
