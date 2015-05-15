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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Converts signs without UUIDs to signs with UUIDs.
 *
 */
public class ProtectionUpdaterImpl implements ProtectionUpdater {
    private final Queue<ProtectionMissingIds> missingUniqueIds;
    private final ProtectionNameUpdater nameUpdater;
    private final BlockLockerPlugin plugin;
    private final UUIDHandler uuidHandler;

    public ProtectionUpdaterImpl(BlockLockerPlugin plugin) {
        this.plugin = plugin;
        this.uuidHandler = new UUIDHandler(plugin.getLogger());
        missingUniqueIds = new ConcurrentLinkedQueue<ProtectionMissingIds>();
        nameUpdater = new ProtectionNameUpdater(plugin.getSignParser(), plugin.getProfileFactory());

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
        boolean lookupPastNames = false;
        while ((protection = missingUniqueIds.poll()) != null) {
            protectionsToFix.add(protection.getProtection());
            if (protection.mustLookupPastNames()) {
                lookupPastNames = true;
            }
            names.addAll(protection.getNamesMissingUniqueIds());
        }

        // There was nothing in the queue
        if (protectionsToFix.isEmpty()) {
            return;
        }

        // Fetch them
        uuidHandler.fetchUniqueIds(names, new ProtectionUUIDSetter(plugin, protectionsToFix), lookupPastNames);
    }

    @Override
    public void update(Protection protection, boolean newProtection) {
        Preconditions.checkNotNull(protection, "protection");

        boolean lookupPastNames = !newProtection;

        if (uuidHandler.isOnlineMode()) {
            nameUpdater.updateNames(protection);
            updateForMissingIds(protection, lookupPastNames);
        }
    }

    private void updateForMissingIds(Protection protection, boolean lookupPastNames) {
        Optional<ProtectionMissingIds> missingIds = ProtectionMissingIds.of(protection, lookupPastNames);

        // Check if there is something that needs to be converted
        if (!missingIds.isPresent()) {
            return;
        }

        // Add it to queue
        if (!missingUniqueIds.contains(missingIds.get())) {
            missingUniqueIds.add(missingIds.get());
        }
    }


}
