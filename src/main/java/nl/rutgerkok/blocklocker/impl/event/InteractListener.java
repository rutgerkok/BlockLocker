package nl.rutgerkok.blocklocker.impl.event;

import java.util.Optional;
import java.util.Set;

import nl.rutgerkok.blocklocker.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.ImmutableSet;

import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.event.PlayerProtectionCreateEvent;
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import nl.rutgerkok.blocklocker.location.IllegalLocationException;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;
import nl.rutgerkok.blocklocker.protection.Protection.SoundCondition;

public final class InteractListener extends EventListener {

    private static final Set<BlockFace> AUTOPLACE_BLOCK_FACES = ImmutableSet.of(BlockFace.NORTH, BlockFace.EAST,
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP);

    public InteractListener(BlockLockerPluginImpl plugin) {
        super(plugin);
    }

    private boolean allowedByBlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst,
            EquipmentSlot placedUsingHand, Player player) {
        Material originalMaterial = placedBlock.getType();

        ItemStack itemInHand = placedUsingHand == EquipmentSlot.OFF_HAND ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
        BlockPlaceEvent placeEvent = new BlockPlaceEvent(placedBlock, replacedBlockState, placedAgainst,
                itemInHand, player, true, placedUsingHand);
        Bukkit.getPluginManager().callEvent(placeEvent);

        Material placedMaterial = placeEvent.getBlockPlaced().getType();
        if (placeEvent.isCancelled() || !placedMaterial.equals(originalMaterial)) {
            // We consider the event cancelled too when the placed block was
            // changed
            return false;
        }
        return true;
    }

    /**
     * Gets whether players are allowed to build in the given game mode.
     *
     * @param gameMode
     *            The game mode, may be null.
     * @return True for survival and creative, false for the other modes.
     */
    private boolean canBuildInMode(GameMode gameMode) {
        if (gameMode == null) {
            return false;
        }
        return switch (gameMode) {
            case ADVENTURE, SPECTATOR -> false;
            case CREATIVE, SURVIVAL -> true;
        };
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
     * Gets the block the inventory is currently positioned at. Note: even for
     * inventories in entities, this will return a block.
     *
     * @param inventory
     *            The inventory.
     * @return The block, or null if the inventory doesn't have a location (only
     *         custom inventories as far as I know).
     */
    private Block getInventoryBlockOrNull(Inventory inventory) {
        Location location = inventory.getLocation();
        if (location == null) {
            return null;
        }
        return location.getBlock();
    }

    private org.bukkit.block.data.type.Sign getRotatedSignPost(Player player, Material signMaterial) {
        float rotation = player.getLocation().getYaw();
        if (rotation < 0) {
            rotation += 360.0f;
        }
        org.bukkit.block.data.type.Sign materialData = (org.bukkit.block.data.type.Sign) signMaterial
                .createBlockData();
        materialData.setRotation(rotationToBlockFace(rotation));
        return materialData;
    }

    private Waterlogged getSignBlockData(BlockFace blockFace, Player player, Material signMaterial) {
        if (blockFace == BlockFace.UP) {
            // Place standing sign in direction of player
            return getRotatedSignPost(player, signMaterial);
        } else {
            // Place attached sign
            WallSign wallSignData = (WallSign) toWallSign(signMaterial).createBlockData();
            wallSignData.setFacing(blockFace);
            return wallSignData;
        }
    }

    private Optional<Material> getSignInHand(Player player, EquipmentSlot hand) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = hand == EquipmentSlot.OFF_HAND ? inventory.getItemInOffHand() : inventory.getItemInMainHand();
        if (isOfType(item, Tag.SIGNS)) {
            return Optional.of(item.getType());
        }
        return Optional.empty();
    }

    private void handleAllowed(PlayerInteractEvent event, Protection protection, boolean clickedSign,
                               boolean usedOffHand) {
        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        PlayerProfile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        boolean isOwner = protection.isOwner(playerProfile);

        // Select signs
        if (clickedSign) {
            if ((!isOwner && !player.hasPermission(Permissions.CAN_ADMIN)) || usedOffHand || player.isSneaking()) {
                // Don't open sign editor in these cases
                event.setCancelled(true);
            }
            return;
        }

        // Add [More Users] sign
        if (isOwner && tryPlaceSign(player, clickedBlock, event.getBlockFace(), event.getHand(), SignType.MORE_USERS)) {
            event.setCancelled(true);
            return;
        }

        // Open (double/trap/fence) doors manually
        boolean clickedMainBlock = plugin.getProtectionFinder().findProtection(clickedBlock, SearchMode.NO_SUPPORTING_BLOCKS)
                .filter(p -> p.equals(protection)).isPresent();
        if (protection.canBeOpened() && !isSneakPlacing(player) && clickedMainBlock) {
            event.setCancelled(true);

            if (!usedOffHand) {
                if (protection.isOpen()) {
                    protection.setOpen(false, SoundCondition.AUTOMATIC);
                } else {
                    protection.setOpen(true, SoundCondition.AUTOMATIC);
                }

                // Schedule automatic close
                scheduleClose(protection);
            }
        }
    }

    private void handleDisallowed(PlayerInteractEvent event, Protection protection, boolean clickedSign,
            boolean usedOffHand) {
        event.setCancelled(true);

        if (usedOffHand) {
            // Don't send messages
            return;
        }

        Player player = event.getPlayer();
        if (clickedSign) {
            plugin.getTranslator()
                    .sendMessage(player, Translation.PROTECTION_IS_CLAIMED_BY, protection.getOwnerDisplayName());
        } else {
            plugin.getTranslator()
                    .sendMessage(player, Translation.PROTECTION_NO_ACCESS, protection.getOwnerDisplayName());
        }
    }

    private boolean isNullOrAir(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0;
    }

    private boolean isOfType(ItemStack stackOrNull, Tag<Material> tag) {
        if (stackOrNull == null) {
            return false;
        }
        return tag.isTagged(stackOrNull.getType());
    }

    private boolean isSneakPlacing(Player player) {
        if (!player.isSneaking()) {
            return false;
        }
        if (isNullOrAir(player.getInventory().getItemInMainHand())) {
            return false;
        }
        if (isNullOrAir(player.getInventory().getItemInOffHand())) {
            return false;
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        // Prevents villagers from opening doors
        if (!(event.getEntity() instanceof Villager)) {
            return;
        }
        if (plugin.getChestSettings().allowDestroyBy(AttackType.VILLAGER)) {
           return;
        }
        if (isProtected(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        Block from = getInventoryBlockOrNull(event.getSource());
        if (from != null) {
            if (isRedstoneDenied(from)) {
                event.setCancelled(true);
                return;
            }
        }
        Block to = getInventoryBlockOrNull(event.getDestination());
        if (to != null) {
            if (isRedstoneDenied(to)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Prevents access to containers.
     *
     * @param event The event object.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Material material = block.getType();
        boolean clickedSign = Tag.STANDING_SIGNS.isTagged(material) || Tag.WALL_SIGNS.isTagged(material);
        // When using the offhand check, access checks must still be performed,
        // but no messages must be sent
        boolean usedOffHand = event.getHand() == EquipmentSlot.OFF_HAND;
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block);

        if (protection.isEmpty()) {
            if (tryPlaceSign(event.getPlayer(), block, event.getBlockFace(), event.getHand(), SignType.PRIVATE)) {
                event.setCancelled(true);
            }
            return;
        }

        // Check if protection needs update
        plugin.getProtectionUpdater().update(protection.get(), false);

        // Check if player is allowed, open door
        if (checkAllowed(player, protection.get(), clickedSign)) {
            handleAllowed(event, protection.get(), clickedSign, usedOffHand);
        } else {
            handleDisallowed(event, protection.get(), clickedSign, usedOffHand);
        }
    }

    private ItemStack removeOneItem(ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            return item;
        } else {
            return null;
        }
    }

    private void removeSingleSignFromHand(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Keep order (main, then off hand) the same as in getSignInHand - otherwise you
        // might remove the wrong sign
        PlayerInventory inventory = player.getInventory();
        if (isOfType(inventory.getItemInMainHand(), Tag.SIGNS)) {
            inventory.setItemInMainHand(removeOneItem(inventory.getItemInMainHand()));
        } else if (isOfType(inventory.getItemInOffHand(), Tag.SIGNS)) {
            inventory.setItemInOffHand(removeOneItem(inventory.getItemInOffHand()));
        }
    }

    private BlockFace rotationToBlockFace(float rotation) {
        int intRotation = Math.round((rotation / 360.0f) * 16.0f);
        byte dataValue = (byte) ((intRotation + 8) % 16);
        switch (dataValue) {
            case 0x0:
                return BlockFace.SOUTH;
            case 0x1:
                return BlockFace.SOUTH_SOUTH_WEST;
            case 0x2:
                return BlockFace.SOUTH_WEST;
            case 0x3:
                return BlockFace.WEST_SOUTH_WEST;
            case 0x4:
                return BlockFace.WEST;
            case 0x5:
                return BlockFace.WEST_NORTH_WEST;
            case 0x6:
                return BlockFace.NORTH_WEST;
            case 0x7:
                return BlockFace.NORTH_NORTH_WEST;
            case 0x8:
                return BlockFace.NORTH;
            case 0x9:
                return BlockFace.NORTH_NORTH_EAST;
            case 0xA:
                return BlockFace.NORTH_EAST;
            case 0xB:
                return BlockFace.EAST_NORTH_EAST;
            case 0xC:
                return BlockFace.EAST;
            case 0xD:
                return BlockFace.EAST_SOUTH_EAST;
            case 0xE:
                return BlockFace.SOUTH_EAST;
            case 0xF:
                return BlockFace.SOUTH_SOUTH_EAST;
        }
        throw new RuntimeException("Couldn't handle rotation " + rotation);
    }

    private void scheduleClose(final Protection protection) {
        if (!protection.isOpen()) {
            return;
        }
        int openSeconds = protection.getOpenSeconds();
        if (openSeconds == -1) {
            // Not specified, use default
            openSeconds = plugin.getChestSettings().getDefaultDoorOpenSeconds();
        }
        if (openSeconds <= 0) {
            return;
        }
        plugin.runLater(protection
                .getSomeProtectedBlock(), () -> protection.setOpen(false, SoundCondition.ALWAYS), openSeconds * 20);
    }

    private Material toWallSign(Material signMaterial) {
        return Material.valueOf(signMaterial.name().replace("_SIGN", "_WALL_SIGN"));
    }

    private boolean tryPlaceSign(Player player, Block block, BlockFace clickedSide, EquipmentSlot hand,
            SignType signType) {
        if (player.isSneaking() || !canBuildInMode(player.getGameMode())) {
            return false;
        }
        Optional<Material> optionalSignMaterial = getSignInHand(player, hand);
        if (!optionalSignMaterial.isPresent()) {
            return false;
        }
        Material signMaterial = optionalSignMaterial.get();

        if (!plugin.getProtectionFinder().isProtectable(block)) {
            return false;
        }
        if (!player.hasPermission(Permissions.CAN_PROTECT)) {
            return false;
        }
        if (!AUTOPLACE_BLOCK_FACES.contains(clickedSide)) {
            return false;
        }
        try {
            plugin.getLocationCheckers().checkLocationAndPermission(player, block);
        } catch (IllegalLocationException e) {
            return false;
        }

        Block signBlock = block.getRelative(clickedSide);
        boolean waterlogged = false;
        if (!signBlock.getType().isAir()) {
            if (signBlock.getType() != Material.WATER) {
                return false;
            }
            // So block is a water block - check its water level
            waterlogged = ((Levelled) signBlock.getBlockData()).getLevel() == 0;
        }

        // Fire our PlayerProtectionCreateEvent
        if (this.plugin.callEvent(new PlayerProtectionCreateEvent(player, signBlock)).isCancelled()) {
            return false;
        }

        // Create sign and fire Bukkit's BlockPlaceEvent for the sign to be placed
        BlockState oldState = signBlock.getState();
        Waterlogged newBlockData = getSignBlockData(clickedSide, player, signMaterial);
        newBlockData.setWaterlogged(waterlogged);
        signBlock.setBlockData(newBlockData);
        if (!allowedByBlockPlaceEvent(signBlock, oldState, block, hand, player)) {
            // Revert to old state
            oldState.update(true);
            return false;
        }

        // Get state again now that block has been changed, so that it can
        // be casted to Sign
        Sign sign = (Sign) signBlock.getState();

        // Decide what the text on the sign is going to be
        Profile profile = signType.isMainSign() ? plugin.getProfileFactory().fromPlayer(player)
                : plugin.getProfileFactory().fromRedstone();
        ProtectionSign protectionSign = plugin.getProtectionFinder().newProtectionSign(sign, signType, profile);
        String[] newLines = plugin.getSignParser().getDisplayLines(protectionSign);

        // Test if we can place it
        SignChangeEvent signChangeEvent = new SignChangeEvent(sign.getBlock(), player, newLines, Side.FRONT);
        Bukkit.getPluginManager().callEvent(signChangeEvent);

        if (sign.getBlock().getType() != sign.getType()) {
            // The plugin listening to the event removed/replaced the sign
            removeSingleSignFromHand(player); // We're forced to consume the sign, to avoid item duplication
            return false; // Report as failed
        }

        if (signChangeEvent.isCancelled()) {
            // Event failed, revert to old state
            oldState.update(true); // Remove the entire sign, to avoid leaving an empty sign
            return false; // And report as failed (player will keep the sign)
        }

        // Actually write the text
        SignSide frontSide = sign.getSide(Side.FRONT);
        for (int i = 0; i < newLines.length; i++) {
            frontSide.setLine(i, newLines[i]);
        }
        sign.update();

        // Remove the sign from the player's hand
        removeSingleSignFromHand(player);
        return true;
    }

}
