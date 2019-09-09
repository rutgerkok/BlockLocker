package nl.rutgerkok.blocklocker.impl;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;

import nl.rutgerkok.blocklocker.SignSelector;

/**
 * Selected signs are stored as metadata on a player. The values won't get
 * cleared automatically, so we cannot store a {@link World} object in a player:
 * that will cause memory leaks when a world is unloaded. We cannot store a
 * {@link Location} too: that object holds a {@link World} object. Instead we
 * store a {@link BlockVector} and a {@link String} that represents the world
 * name.
 *
 */
final class SignSelectorImpl implements SignSelector {

    private static final int EXPIRATION_MINUTES = 4;

    private static final String SIGN_TIME = SignSelectorImpl.class + ".signTime";
    private static final String SIGN_VECTOR = SignSelectorImpl.class + ".signBlock";
    private static final String SIGN_WORLD = SignSelectorImpl.class + ".signWorld";

    private final Plugin plugin;

    public SignSelectorImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    private void clearValues(Player player) {
        player.removeMetadata(SIGN_VECTOR, plugin);
        player.removeMetadata(SIGN_WORLD, plugin);
        player.removeMetadata(SIGN_TIME, plugin);
    }

    @Override
    public Optional<Sign> getSelectedSign(Player player) {
        List<MetadataValue> signVector = player.getMetadata(SIGN_VECTOR);
        List<MetadataValue> signWorld = player.getMetadata(SIGN_WORLD);
        List<MetadataValue> signTime = player.getMetadata(SIGN_TIME);

        if (signVector.isEmpty() || signWorld.isEmpty() || signTime.isEmpty()) {
            return Optional.empty();
        }

        BlockVector vector = (BlockVector) signVector.get(0).value();
        World world = Bukkit.getWorld(signWorld.get(0).asString());
        if (world == null) {
            return Optional.empty();
        }

        // Check for expiration
        long createdMillis = signTime.get(0).asLong();
        if (isExpired(createdMillis)) {
            clearValues(player);
            return Optional.empty();
        }

        Location signLocation = vector.toLocation(world);
        Block signBlock = signLocation.getBlock();
        BlockState signState = signBlock.getState();
        if (signState instanceof Sign) {
            return Optional.of((Sign) signState);
        }

        return Optional.empty();
    }

    private boolean isExpired(long time) {
        long now = System.currentTimeMillis();
        if ((now - time) > (1000 * 60 * EXPIRATION_MINUTES)) {
            return true;
        }
        return false;
    }

    @Override
    public void setSelectedSign(Player player, Sign sign) {
        BlockVector blockVector = new BlockVector(sign.getX(), sign.getY(), sign.getZ());

        player.setMetadata(SIGN_VECTOR, new FixedMetadataValue(plugin, blockVector));
        player.setMetadata(SIGN_WORLD, new FixedMetadataValue(plugin, sign.getWorld().getName()));
        player.setMetadata(SIGN_TIME, new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

}
