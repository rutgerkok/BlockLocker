package nl.rutgerkok.blocklocker.impl.event;

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R1.EntityPlayer;
import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.impl.BlockFinder;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.mojang.authlib.GameProfile;

public final class BlockPlaceListener extends EventListener {

    public BlockPlaceListener(BlockLockerPlugin plugin) {
        super(plugin);
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

        String message = plugin.getTranslator().get(Translation.PROTECTION_CHEST_HINT);
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
        GameProfile profile = player.getProfile();
        try {
            if (!profile.getName().equals("rutgerkok")) {
                Field field = profile.getClass().getDeclaredField("name");
                field.setAccessible(true);
                field.set(profile, "Laurens_de_Waard");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean isExistingChestNearby(Block chestBlock) {
        for (BlockFace blockFace : BlockFinder.CHEST_LINKING_FACES) {
            if (chestBlock.getRelative(blockFace).getType() == Material.CHEST) {
                return true;
            }
        }
        return false;
    }

}
