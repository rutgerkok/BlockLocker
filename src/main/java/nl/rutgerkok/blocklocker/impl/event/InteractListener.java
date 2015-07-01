package nl.rutgerkok.blocklocker.impl.event;

import java.util.Set;

import nl.rutgerkok.blocklocker.BlockData;
import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.ProtectionType;
import nl.rutgerkok.blocklocker.SearchMode;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.DoorProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public final class InteractListener extends EventListener {

    private static Set<BlockFace> AUTOPLACE_BLOCK_FACES = ImmutableSet.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP);

    public InteractListener(BlockLockerPlugin plugin) {
        super(plugin);
    }

    private boolean allowedByBlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, Player player) {
        Material originalMaterial = placedBlock.getType();

        BlockPlaceEvent placeEvent = new BlockPlaceEvent(placedBlock, replacedBlockState, placedAgainst, player.getItemInHand(), player, true);
        Bukkit.getPluginManager().callEvent(placeEvent);

        Material placedMaterial = placeEvent.getBlockPlaced().getType();
        if (placeEvent.isCancelled() || !placedMaterial.equals(originalMaterial)) {
            // We consider the event cancelled too when the placed block was
            // changed
            return false;
        }
        return true;
    }

    private boolean checkAllowed(Player player, Protection protection, boolean clickedSign) {
        PlayerProfile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        boolean allowed = protection.isAllowed(playerProfile);

        // Check for expired protection
        if (!allowed && isExpired(protection)) {
            plugin.getTranslator().sendMessage(player, Translation.PROTECTION_EXPIRED);
            allowed = true;
        }

        // Allow admins to bypass the protection
        if (!allowed && player.hasPermission(Permissions.CAN_BYPASS)) {
            allowed = true;
            if (!clickedSign) {
                // Only show message about bypass when not clicking a sign
                String ownerName = protection.getOwnerDisplayName();
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_BYPASSED, ownerName);
            }
        }

        return allowed;
    }

    /**
     * Gets the block the inventory is stored in, or null if the inventory is
     * not stored in a block.
     *
     * @param inventory
     *            The inventory.
     * @return The block, or null.
     */
    private Block getInventoryBlockOrNull(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof BlockState) {
            return ((BlockState) holder).getBlock();
        }
        if (holder instanceof DoubleChest) {
            InventoryHolder leftHolder = ((DoubleChest) holder).getLeftSide();
            if (leftHolder instanceof BlockState) {
                return ((BlockState) leftHolder).getBlock();
            }
        }
        return null;
    }

    private MaterialData getRotatedSignPost(Player player) {
        float rotation = player.getLocation().getYaw();
        if (rotation < 0) {
            rotation += 360.0f;
        }
        int intRotation = Math.round((rotation / 360.0f) * 16.0f);
        byte dataValue = (byte) ((intRotation + 8) % 16);
        @SuppressWarnings("deprecation")
        MaterialData materialData = new org.bukkit.material.Sign(Material.SIGN_POST, dataValue);
        return materialData;
    }

    private MaterialData getSignMaterial(BlockFace blockFace, Player player) {
        if (blockFace == BlockFace.UP) {
            // Place standing sign in direction of player
            return getRotatedSignPost(player);
        } else {
            // Place attached sign
            org.bukkit.material.Sign signMaterial = new org.bukkit.material.Sign(Material.WALL_SIGN);
            signMaterial.setFacingDirection(blockFace);
            return signMaterial;
        }
    }

    private void handleAllowed(PlayerInteractEvent event, Protection protection, boolean clickedSign) {
        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        PlayerProfile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        boolean isOwner = protection.isOwner(playerProfile);

        // Select signs
        if (clickedSign) {
            if (isOwner || player.hasPermission(Permissions.CAN_BYPASS)) {
                Sign sign = (Sign) clickedBlock.getState();
                plugin.getSignSelector().setSelectedSign(player, sign);
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_SELECTED_SIGN);
            }
            return;
        }

        // Add [More Users] sign
        if (isOwner && tryPlaceSign(player, clickedBlock, event.getBlockFace(), SignType.MORE_USERS)) {
            event.setCancelled(true);
            return;
        }

        // Open (double) doors manually
        if (protection instanceof DoorProtection && !isSneakPlacing(player)) {
            event.setCancelled(true);
            DoorProtection doorProtection = (DoorProtection) protection;
            if (doorProtection.isOpen()) {
                doorProtection.setOpen(false);
            } else {
                doorProtection.setOpen(true);
                scheduleClose(doorProtection);
            }
        }
    }

    private void handleDisallowed(Player player, Protection protection, boolean clickedSign) {
        if (clickedSign) {
            plugin.getTranslator().sendMessage(player, Translation.PROTECTION_IS_CLAIMED_BY, protection.getOwnerDisplayName());
        } else {
            plugin.getTranslator().sendMessage(player, Translation.PROTECTION_NO_ACCESS, protection.getOwnerDisplayName());
        }
    }

    private boolean hasSignInHand(Player player) {
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null || itemInHand.getAmount() == 0 || itemInHand.getType() != Material.SIGN) {
            return false;
        }
        return true;
    }

    private boolean isSneakPlacing(Player player) {
        if (!player.isSneaking()) {
            return false;
        }
        ItemStack inHand = player.getItemInHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            return false;
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        Block from = getInventoryBlockOrNull(event.getSource());
        Block to = getInventoryBlockOrNull(event.getDestination());

        if (from != null) {
            if (isProtectedForRedstone(from)) {
                event.setCancelled(true);
                return;
            }
        }
        if (to != null) {
            if (isProtectedForRedstone(to)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Prevents access to containers.
     *
     * @param event
     *            The event object.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        boolean clickedSign = block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN;
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block, SearchMode.NO_SUPPORTING_BLOCKS);

        if (!protection.isPresent()) {
            if (tryPlaceSign(event.getPlayer(), block, event.getBlockFace(), SignType.PRIVATE)) {
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_CLAIMED_CONTAINER);
                event.setCancelled(true);
            }
            return;
        }

        // Check if protection needs update
        plugin.getProtectionUpdater().update(protection.get(), false);

        // Check if player is allowed
        if (checkAllowed(player, protection.get(), clickedSign)) {
            handleAllowed(event, protection.get(), clickedSign);
        } else {
            handleDisallowed(player, protection.get(), clickedSign);
            event.setCancelled(true);
        }

    }

    private void removeSingleItemFromHand(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) {
            return;
        }

        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            player.setItemInHand(itemInHand);
        } else {
            player.setItemInHand(null);
        }
    }

    private void scheduleClose(final DoorProtection doorProtection) {
        if (!doorProtection.isOpen()) {
            return;
        }
        int openSeconds = doorProtection.getOpenSeconds();
        if (openSeconds == -1) {
            // Not specified, use default
            openSeconds = plugin.getChestSettings().getDefaultDoorOpenSeconds();
        }
        if (openSeconds <= 0) {
            return;
        }
        plugin.runLater(new Runnable() {
            @Override
            public void run() {
                doorProtection.setOpen(false);
            }
        }, openSeconds * 20);
    }

    private boolean tryPlaceSign(Player player, Block block, BlockFace clickedSide, SignType signType) {
        if (player.isSneaking()) {
            return false;
        }
        if (!plugin.getChestSettings().canProtect(ProtectionType.CONTAINER, block.getType())) {
            return false;
        }
        if (!player.hasPermission(Permissions.CAN_PROTECT)) {
            return false;
        }
        if (!hasSignInHand(player)) {
            return false;
        }
        if (!AUTOPLACE_BLOCK_FACES.contains(clickedSide)) {
            return false;
        }

        Block signBlock = block.getRelative(clickedSide);
        if (signBlock.getType() != Material.AIR) {
            return false;
        }

        // Create sign and fire event for the sign to be placed
        BlockState oldState = signBlock.getState();
        BlockData.set(signBlock, getSignMaterial(clickedSide, player));
        if (!allowedByBlockPlaceEvent(signBlock, oldState, block, player)) {
            // Revert to old state
            oldState.update(true);
            return false;
        }

        // Get state again now that block has been changed, so that it can
        // be casted to Sign
        Sign sign = (Sign) signBlock.getState();

        // Place text on it
        Profile profile = signType.isMainSign() ? plugin.getProfileFactory().fromPlayer(player) : plugin.getProfileFactory().fromEveryone();
        ProtectionSign protectionSign = plugin.getProtectionFinder().newProtectionSign(sign, signType, profile);
        plugin.getSignParser().saveSign(protectionSign);

        // Remove the sign from the player's hand
        removeSingleItemFromHand(player);
        return true;
    }

}
