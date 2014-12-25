package nl.rutgerkok.blocklocker.impl.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.ProtectionUpdater;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Preconditions;

/**
 * Converts signs without UUIDs to signs with UUIDs.
 *
 */
public class ProtectionUpdaterImpl implements ProtectionUpdater {
    private final Queue<ProtectionMissingIds> missingUniqueIds;
    private final BlockLockerPlugin plugin;
    private final UUIDHandler uuidHandler;

    public ProtectionUpdaterImpl(BlockLockerPlugin plugin) {
        this.plugin = plugin;
        this.uuidHandler = new UUIDHandler(plugin.getLogger());
        missingUniqueIds = new ConcurrentLinkedQueue<ProtectionMissingIds>();

        Bukkit.getScheduler().runTaskTimer(
                JavaPlugin.getProvidingPlugin(getClass()), new Runnable() {
                    @Override
                    public void run() {
                        processMissingUniqueIdsQueue();
                    }
                }, 60, 60);
    }

    private void processMissingUniqueIdsQueue() {
        // Collect the names
        Set<String> names = new HashSet<String>();
        final Collection<Protection> protectionsToFix = new ArrayList<Protection>();

        ProtectionMissingIds protection;
        while ((protection = missingUniqueIds.poll()) != null) {
            protectionsToFix.add(protection.getProtection());
            names.addAll(protection.getNamesMissingUniqueIds());
        }

        // There was nothing in the queue
        if (protectionsToFix.isEmpty()) {
            return;
        }

        // Fetch them
        uuidHandler.fetchUniqueIds(names, new ProtectionUUIDSetter(plugin, protectionsToFix));
    }

    @Override
    public void update(Protection protection, UpdateMode updateMode) {
        Preconditions.checkNotNull(protection, "protection");
        Preconditions.checkNotNull(updateMode, "updateMode");

        if (updateMode == UpdateMode.IF_NEEDED) {
            if (!protection.needsUpdate(uuidHandler.isOnlineMode())) {
                return;
            }
        }

        ProtectionMissingIds missingIds = new ProtectionMissingIds(protection);

        // Check if there is something that needs to be converted
        if (missingIds.getNamesMissingUniqueIds().isEmpty()) {
            return;
        }

        // Add it to queue
        if (!missingUniqueIds.contains(missingIds)) {
            missingUniqueIds.add(missingIds);
        }
    }

}
