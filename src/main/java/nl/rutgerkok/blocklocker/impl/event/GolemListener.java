package nl.rutgerkok.blocklocker.impl.event;

import io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent;
import nl.rutgerkok.blocklocker.AttackType;
import nl.rutgerkok.blocklocker.ProtectionCache;
import nl.rutgerkok.blocklocker.SearchMode;
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;

import java.util.Optional;

/**
 * Listener for just golems. Separated to allow the plugin to run on older Minecraft versions. In the future, when
 * backwards compatibility is no longer necessary, the hopper event could be handled here too, then it would be an
 * ExtractListener or something.
 */
public final class GolemListener extends EventListener {

    public GolemListener(BlockLockerPluginImpl plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityContainerTarget(ItemTransportingEntityValidateTargetEvent event) {
        // Golem handling
        if (!event.isAllowed() || plugin.getChestSettings().allowDestroyBy(AttackType.GOLEM)) {
            return;
        }

        Block block = event.getBlock();
        ProtectionCache cache = this.plugin.getProtectionCache();
        ProtectionCache.CacheFlag cacheFlag = cache.getAllowed(block, ProtectionCache.CacheType.GOLEM);
        if (cacheFlag == ProtectionCache.CacheFlag.ALLOWED) {
            return; // Don't do anything
        }
        if (cacheFlag == ProtectionCache.CacheFlag.NOT_ALLOWED) {
            event.setAllowed(false); // Prevent targeting
            return;
        }

        // If we're here, cache miss
        // Retrieve the value, and store it in the cache
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block, SearchMode.MAIN_BLOCKS_ONLY);
        if (protection.isEmpty()) {
            cache.setAllowed(block, ProtectionCache.CacheType.GOLEM, false);
            return;
        }
        Profile golemProfile = plugin.getProfileFactory().fromGolem();
        boolean allowed = protection.get().isAllowed(golemProfile);
        cache.setAllowed(block, ProtectionCache.CacheType.GOLEM, allowed);
        if (!allowed) {
            event.setAllowed(false);
        }
    }
}
