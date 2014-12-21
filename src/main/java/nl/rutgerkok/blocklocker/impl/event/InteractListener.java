package nl.rutgerkok.blocklocker.impl.event;

import java.util.Set;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.ChestSettings.ProtectionType;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.DoorProtection;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class InteractListener extends EventListener {

    private static Set<BlockFace> AUTOPLACE_BLOCK_FACES = ImmutableSet.of(
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP);

    public InteractListener(BlockLockerPlugin plugin) {
        super(plugin);
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

    private void handleAllowed(PlayerInteractEvent event, Protection protection) {
        if (protection instanceof DoorProtection) {
            // Open (double) doors manually
            event.setCancelled(true);
            ((DoorProtection) protection).toggleOpen();
        }
    }

    private void handleDisallowed(Player player) {
        plugin.getTranslator().sendMessage(player, Translation.PROTECTION_NO_ACCESS);
    }

    private boolean hasSignInHand(Player player) {
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null || itemInHand.getAmount() == 0 || itemInHand.getType() != Material.SIGN) {
            return false;
        }
        return true;
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
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block);

        if (!protection.isPresent()) {
            if (tryPlaceSign(event.getPlayer(), block, event.getBlockFace())) {
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_CLAIMED_CONTAINER);
                event.setCancelled(true);
            }
            return;
        }

        // Check if protection needs update
        if (protection.get().isMissingUniqueIds()) {
            plugin.fixMissingUniqueIds(protection.get());
        }

        // Check if player is allowed
        Profile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        if (protection.get().isAllowed(playerProfile)) {
            handleAllowed(event, protection.get());
        } else {
            handleDisallowed(player);
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

    @SuppressWarnings("deprecation")
    private void setBlockMaterialData(Block block, MaterialData materialData) {
        block.setTypeIdAndData(materialData.getItemTypeId(), materialData.getData(), false);
    }

    private boolean tryPlaceSign(Player player, Block block, BlockFace clickedSide) {
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

        // Create empty sign

        // Set base material so that .getState() will be of the correct type
        setBlockMaterialData(signBlock, getSignMaterial(clickedSide, player));
        Sign sign = (Sign) signBlock.getState();

        // Place text on it
        PlayerProfile profile = plugin.getProfileFactory().fromPlayer(player);
        ProtectionSign protectionSign = plugin.getProtectionFinder().newProtectionSign(sign, SignType.PRIVATE, profile);
        plugin.getSignParser().saveSign(protectionSign);

        // Remove the sign from the player's hand
        removeSingleItemFromHand(player);
        return true;
    }

}
