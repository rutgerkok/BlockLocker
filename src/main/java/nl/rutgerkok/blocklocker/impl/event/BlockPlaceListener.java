package nl.rutgerkok.blocklocker.impl.event;

import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.protection.Protection;
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

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.location.IllegalLocationException;
import org.bukkit.inventory.InventoryHolder;

import java.util.Optional;

public final class BlockPlaceListener extends EventListener {

    public BlockPlaceListener(BlockLockerPlugin plugin) {
        super(plugin);
    }

    private boolean isExistingChestNearby(Block chestBlock) {
        for (BlockFace blockFace : BlockFinder.CARDINAL_FACES) {
            if (chestBlock.getRelative(blockFace).getType() == Material.CHEST) {
                return true;
            }
        }
        return false;
    }

    private boolean isOwner(Player player, Block block) {
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block);
        if (!protection.isPresent()) {
            return true;
        }
        PlayerProfile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        return !protection.get().isOwner(playerProfile);
    }

    private boolean mayInterfereWith(Player player, Block block) {
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            //Single left chest may interfere with right chest that locked by others, and vice versa.
            for (BlockFace blockFace : BlockFinder.CARDINAL_FACES) {
                Block nearBlock = block.getRelative(blockFace);
                if (block.getType().equals(nearBlock.getType()) &&
                        !(((InventoryHolder) nearBlock.getState()).getInventory().getHolder() instanceof DoubleChest) &&
                        ((Directional) nearBlock.getBlockData()).getFacing().equals(((Directional) block.getBlockData()).getFacing()) &&
                        isProtected(nearBlock) && isOwner(player, nearBlock)) {
                    return true;
                }
            }
        } else if (Tag.DOORS.isTagged(block.getType())) {
            //Left door may interfere with right door that locked by others, and vice versa.
            for (BlockFace blockFace : BlockFinder.CARDINAL_FACES) {
                Block nearBlock = block.getRelative(blockFace);
                if (block.getType().equals(nearBlock.getType()) && isProtected(nearBlock) && isOwner(player, nearBlock)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Prevent placed block may interfere with protected block.
     *
     * @param event
     *            The block place event.
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceMayInterfereWith(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (player.hasPermission(Permissions.CAN_INTERFERE)) {
            return;
        }

        if (mayInterfereWith(player, block)) {
            String message = plugin.getTranslator().get(Translation.PROTECTION_NO_PERMISSION_FOR_INTERFERE);
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
            event.setCancelled(true);
        }
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

        if (event.getBlockPlaced().getType() != Material.CHEST) {
            return;
        }

        if (!player.hasPermission(Permissions.CAN_PROTECT)) {
            return;
        }

        if (isExistingChestNearby(event.getBlockPlaced())) {
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

}
