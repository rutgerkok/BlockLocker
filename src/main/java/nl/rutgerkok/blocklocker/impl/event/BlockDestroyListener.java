package nl.rutgerkok.blocklocker.impl.event;

import java.util.Iterator;
import java.util.List;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.google.common.base.Optional;

public class BlockDestroyListener extends EventListener {

    public BlockDestroyListener(BlockLockerPlugin plugin) {
        super(plugin);
    }

    private Optional<ProtectionSign> asMainSign(Block block) {
        if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
            return Optional.absent();
        }

        Sign sign = (Sign) block.getState();
        Optional<ProtectionSign> protectionSign = plugin.getSignParser().parseSign(sign);
        if (!protectionSign.isPresent()) {
            return Optional.absent();
        }
        if (!protectionSign.get().getType().isMainSign()) {
            return Optional.absent();
        }
        return protectionSign;
    }

    private void destroyOtherSigns(ProtectionSign protectionSign, Protection protection) {
        for (ProtectionSign foundSign : protection.getSigns()) {
            if (!foundSign.equals(protectionSign)) {
                foundSign.getLocation().getBlock().breakNaturally();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<Protection> protection = plugin.getProtectionFinder()
                .findProtection(block);
        if (!protection.isPresent()) {
            return;
        }

        Player player = event.getPlayer();
        Profile profile = plugin.getProfileFactory().fromPlayer(player);
        if (!protection.get().isOwner(profile)) {
            if (player.hasPermission(Permissions.CAN_BYPASS)) {
                String ownerName = protection.get().getOwnerDisplayName();
                plugin.getTranslator().sendMessage(player, Translation.PROTECTION_BYPASSED, ownerName);
            } else {
                event.setCancelled(true);
                return;
            }
        }

        Optional<ProtectionSign> mainSign = asMainSign(block);
        if (mainSign.isPresent()) {
            destroyOtherSigns(mainSign.get(), protection.get());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurnEvent(BlockBurnEvent event) {
        if (isProtected(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFadeEvent(BlockFadeEvent event) {
        if (isProtected(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (anyProtected(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isSticky() && isProtected(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isProtected(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        for (Iterator<Block> it = event.blockList().iterator(); it.hasNext();) {
            Block block = it.next();
            if (isProtected(block)) {
                it.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        // Check deleted blocks
        List<BlockState> blocks = event.getBlocks();
        for (Iterator<BlockState> it = blocks.iterator(); it.hasNext();) {
            BlockState blockState = it.next();

            if (blockState.getType() == Material.AIR) {
                // Almost all replaced blocks are air, so this is a cheap,
                // zero-allocation way out
                continue;
            }

            if (isProtected(blockState.getBlock())) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        if (event.getNewCurrent() == event.getOldCurrent()) {
            return;
        }

        if (isProtectedForRedstone(event.getBlock())) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }
}
