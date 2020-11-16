package nl.rutgerkok.blocklocker.impl.event;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.location.IllegalLocationException;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.protection.Protection;

public final class BlockPlaceListener extends EventListener {

    public BlockPlaceListener(BlockLockerPlugin plugin) {
        super(plugin);
    }

    private boolean isOwner(Player player, Block block) {
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block);
        if (!protection.isPresent()) {
            return true;
        }
        PlayerProfile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        return !protection.get().isOwner(playerProfile);
    }

    /**
     * Sends a message that the player can protect a chest.
     *
     * @param event
     *            The block place event.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (willInterfereWith(player, event.getBlockPlaced())) {
            // Not allowed to place a block here, would interfere with an existing
            // protection
            if (!event.getPlayer().hasPermission(Permissions.CAN_BYPASS)) {
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_NO_ACCESS);
                event.setCancelled(true);
                return;
            } else {
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_BYPASSED);
            }
        }

        // Rest of the method: check if sign placing hint must be shown
        if (event.getBlockPlaced().getType() != Material.CHEST) {
            return;
        }

        if (!player.hasPermission(Permissions.CAN_PROTECT)) {
            return;
        }

        try {
            plugin.getLocationCheckers().checkLocationAndPermission(player, event.getBlockPlaced());
        } catch (IllegalLocationException e) {
            return; // Cannot place protection here, so don't show hint
        }

        String message = plugin.getTranslator().get(Translation.PROTECTION_CHEST_HINT);
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }

    private boolean willInterfereWith(Player player, Block block) {
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            // Single left chest may interfere with right chest that locked by others, and
            // vice versa
            if (player.isSneaking()) {
                return false; // If a player is sneaking, the chest will not connect, so it's safe
            }
            for (BlockFace blockFace : BlockFinder.CARDINAL_FACES) {
                Block nearBlock = block.getRelative(blockFace);
                if (block.getType().equals(nearBlock.getType()) &&
                        !(((InventoryHolder) nearBlock.getState()).getInventory().getHolder() instanceof DoubleChest) &&
                        ((Directional) nearBlock.getBlockData()).getFacing()
                                .equals(((Directional) block.getBlockData()).getFacing())
                        &&
                        isProtected(nearBlock) && isOwner(player, nearBlock)) {
                    return true;
                }
            }
        } else if (Tag.DOORS.isTagged(block.getType())) {
            // Left door may interfere with right door that locked by others, and vice
            // versa
            for (BlockFace blockFace : BlockFinder.CARDINAL_FACES) {
                Block nearBlock = block.getRelative(blockFace);
                if (block.getType().equals(nearBlock.getType()) && isProtected(nearBlock)
                        && isOwner(player, nearBlock)) {
                    return true;
                }
            }
        }
        return false;
    }

}
