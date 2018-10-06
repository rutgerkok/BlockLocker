package nl.rutgerkok.blocklocker.impl.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.ProtectionUpdater;
import nl.rutgerkok.blocklocker.protection.Protection;

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
        missingUniqueIds = new ConcurrentLinkedQueue<>();
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
        Set<String> names = new HashSet<>();
        final Collection<Protection> protectionsToFix = new ArrayList<>();

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
    public void update(Protection protection, boolean newProtection) {
        Preconditions.checkNotNull(protection, "protection");

        if (uuidHandler.isOnlineMode()) {
            nameUpdater.updateNames(protection);
            updateForMissingIds(protection);
        }
    }

    private void updateForMissingIds(Protection protection) {
        Optional<ProtectionMissingIds> missingIds = ProtectionMissingIds.of(protection);

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
