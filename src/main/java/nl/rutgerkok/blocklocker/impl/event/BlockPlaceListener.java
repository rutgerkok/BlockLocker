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

    /**
     * Returns true if there's a protection, but the player is not the owner.
     *
     * @param player
     *            The player.
     * @param block
     *            A block that's part of the protection.
     * @return True if there is a protection, but the player is not the owner. False
     *         otherwise. Still returns false if the player is allowed, but not the
     *         owner.
     */
    private Optional<Protection> getProtectionBySomeoneElse(Player player, Block block) {
        return plugin.getProtectionFinder().findProtection(block).filter(protection -> {
            PlayerProfile playerProfile = plugin.getProfileFactory().fromPlayer(player);
            return !protection.isOwner(playerProfile);
        });
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

        Optional<Protection> interferingProtection = willInterfereWith(player, event.getBlockPlaced());
        if (interferingProtection.isPresent()) {
            // Not allowed to place a block here, would interfere with an existing
            // protection
            if (!event.getPlayer().hasPermission(Permissions.CAN_BYPASS)) {
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_NO_ACCESS, interferingProtection.get()
                        .getOwnerDisplayName());
                event.setCancelled(true);
                return;
            } else {
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_BYPASSED, interferingProtection.get()
                        .getOwnerDisplayName());
                return;
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

        plugin.getTranslator().sendMessage(player, Translation.PROTECTION_CHEST_HINT);
    }

    private Optional<Protection> willInterfereWith(Player player, Block block) {
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            // Single left chest may interfere with right chest that locked by others, and
            // vice versa
            if (player.isSneaking()) {
                return Optional.empty(); // If a player is sneaking, the chest will not connect, so it's safe
            }
            for (BlockFace searchFace : BlockFinder.CARDINAL_FACES) {
                Block nearBlock = block.getRelative(searchFace);

                if (block.getType() != nearBlock.getType()) {
                    continue;
                }
                BlockFace faceOfNearBlock = ((Directional) nearBlock.getBlockData()).getFacing();
                BlockFace faceOfBlock = ((Directional) block.getBlockData()).getFacing();
                boolean alreadyADoubleChest = (((InventoryHolder) nearBlock
                        .getState()).getInventory().getHolder() instanceof DoubleChest);
                if (alreadyADoubleChest) {
                    continue;
                }

                // You are allowed to place two NORTH facing chests in adjacent blocks, provided
                // they are behind each other
                boolean areSiblings = (faceOfNearBlock == faceOfBlock) && faceOfBlock != searchFace
                        && faceOfBlock != searchFace.getOppositeFace();

                if (!areSiblings) {
                    continue;
                }

                Optional<Protection> protectionBySomeoneElse = getProtectionBySomeoneElse(player, nearBlock);
                if (protectionBySomeoneElse.isPresent()) {
                    return protectionBySomeoneElse;
                }
            }
        } else if (Tag.DOORS.isTagged(block.getType())) {
            // Left door may interfere with right door that locked by others, and vice
            // versa
            for (BlockFace searchFace : BlockFinder.CARDINAL_FACES) {
                Block nearBlock = block.getRelative(searchFace);
                if (block.getType() != nearBlock.getType()) {
                    continue;
                }
                Optional<Protection> protectionBySomeoneElse = getProtectionBySomeoneElse(player, nearBlock);
                if (protectionBySomeoneElse.isPresent()) {
                    return protectionBySomeoneElse;
                }
            }
        }
        return Optional.empty();
    }

}
