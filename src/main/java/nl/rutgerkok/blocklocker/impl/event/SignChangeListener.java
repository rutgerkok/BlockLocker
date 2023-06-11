package nl.rutgerkok.blocklocker.impl.event;


import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.event.PlayerProtectionCreateEvent;
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import nl.rutgerkok.blocklocker.location.IllegalLocationException;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;


public class SignChangeListener extends EventListener {

    public SignChangeListener(BlockLockerPluginImpl plugin) {
        super(plugin);
    }


    private Optional<SignType> getExistingSignType(Block block, Side side) {
        BlockState blockState = block.getState();
        if (blockState instanceof Sign) {
            return plugin.getSignParser().getSignType((Sign) blockState, side);
        }
        return Optional.empty();
    }

    private void handleSignNearbyProtection(SignChangeEvent event, Protection protection) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Profile playerProfile = plugin.getProfileFactory().fromPlayer(player);
        Optional<SignType> newSignType = plugin.getSignParser().getSignType(event);
        Optional<SignType> oldSignType = this.getExistingSignType(event.getBlock(), event.getSide());

        // Only protection signs should be handled
        if (!newSignType.isPresent() && !oldSignType.isPresent()) {
            return;
        }

        // If a sign type was already specified, don't allow changing it
        if (oldSignType.isPresent() && !oldSignType.equals(newSignType)) {
            event.setLine(0, ChatColor.stripColor(plugin.getChestSettings()
                    .getFancyLocalizedHeader(oldSignType.get(), event.getLine(0))));
            newSignType = oldSignType;
        }

        // Only the owner may add (or edit) signs nearby a protection
        if (!protection.isOwner(playerProfile) && !player.hasPermission(Permissions.CAN_ADMIN)) {
            plugin.getTranslator().sendMessage(player, Translation.PROTECTION_CANNOT_CHANGE_SIGN);
            event.setCancelled(true);
            return;
        }

        if (newSignType.filter(SignType::isMainSign).isPresent()) {
            // Edited a main sign (for an existing protection)
            if (oldSignType.isPresent()) {
                // Make sure the owner name on the sign stays the same
                // (except for players with the correct permission)
                if (!player.hasPermission(Permissions.CAN_ADMIN)) {
                    Optional<Profile> owner = protection.getOwner();
                    if (owner.isPresent() && !event.getLine(1).equals(owner.get().getDisplayName())) {
                        if (!event.getLine(1).strip().equals(owner.get().getDisplayName().strip())) {
                            // Only notify for visible name changes
                            plugin.getTranslator().sendMessage(player, Translation.COMMAND_CANNOT_EDIT_OWNER);
                        }
                        event.setLine(1, owner.get().getDisplayName());
                    }
                }
            } else {
                // Second main sign is not allowed
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_ADD_MORE_USERS_SIGN_INSTEAD);
                block.breakNaturally(); // Not ideal if other side is the main sign
                event.setCancelled(true);
                return;
            }
        }

        // Mark protection for UUID update
        // Note that we can't use the Protection instance for this, it will
        // still use the outdated signs the next tick
        updateBlockForUniqueIdsSoon(block);
    }

    private void handleSignNotNearbyProtection(SignChangeEvent event) {
        Optional<SignType> parsedSign = plugin.getSignParser().getSignType(event);
        if (!parsedSign.isPresent()) {
            // Not trying to claim a container
            return;
        }

        SignType signType = parsedSign.get();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!player.hasPermission(Permissions.CAN_PROTECT)) {
            plugin.getTranslator().sendMessage(player, Translation.PROTECTION_NO_PERMISSION_FOR_CLAIM);
            block.breakNaturally();
            event.setCancelled(true);
            return;
        }

        // Don't allow placing signs in the wilderness
        try {
            plugin.getLocationCheckers().checkLocationAndPermission(player, block);
        } catch (IllegalLocationException e) {
            player.sendMessage(e.getTranslatedMessage(plugin.getTranslator()));
            block.breakNaturally();
            event.setCancelled(true);
            return;
        }

        // Only the main sign can be used to create new protections
        if (!signType.isMainSign()) {
            plugin.getTranslator().sendMessage(player, Translation.PROTECTION_NOT_NEARBY);
            block.breakNaturally();
            event.setCancelled(true);
            return;
        }

        // Sign must be attached to container
        if (!plugin.getProtectionFinder().isSignNearbyProtectable(block)) {
            plugin.getTranslator().sendMessage(player, Translation.PROTECTION_NOT_NEARBY);
            block.breakNaturally();
            event.setCancelled(true);
            return;
        }

        // Check event
        if (this.plugin.callEvent(new PlayerProtectionCreateEvent(event.getPlayer(), block)).isCancelled()) {
            block.breakNaturally();
            event.setCancelled(true);
            return;
        }

        // Make sure the owner name is on the second line
        event.setLine(1, event.getPlayer().getName());

        // We need to fetch UUIDs soon
        updateBlockForUniqueIdsSoon(block);

        // Send confirmation
        plugin.getTranslator().sendMessage(player, Translation.PROTECTION_CLAIMED_MANUALLY);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Optional<Protection> protection = plugin.getProtectionFinder().findExistingProtectionForNewSign(event.getBlock());

        if (protection.isPresent()) {
            // Changing a sign near a container
            handleSignNearbyProtection(event, protection.get());
        } else {
            // Changing a sign not nearby a container
            handleSignNotNearbyProtection(event);
        }
    }

    /**
     * Call this method for newly created protections. The next tick the
     * protection will be found and updated for uuids.
     *
     * @param block
     *            The block that will be part of the protection.
     */
    private void updateBlockForUniqueIdsSoon(final Block block) {
        plugin.runLater(block, new Runnable() {
            @Override
            public void run() {
                Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block);
                if (protection.isPresent()) {
                    plugin.getProtectionUpdater().update(protection.get(), true);
                }
            }
        });
    }
}
