package nl.rutgerkok.chestsignprotect.impl.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;
import nl.rutgerkok.chestsignprotect.NameAndId;
import nl.rutgerkok.chestsignprotect.ProtectionSign;
import nl.rutgerkok.chestsignprotect.SignParser;
import nl.rutgerkok.chestsignprotect.Translator.Translation;
import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Converts signs without UUIDs to signs with UUIDs.
 *
 */
public class SignConverter {
    private final Queue<ProtectionMissingIds> missingUniqueIds;
    private final ChestSignProtect plugin;
    private final SignParser signParser;

    public SignConverter(ChestSignProtect plugin, SignParser signParser) {
        this.plugin = plugin;
        this.signParser = signParser;
        missingUniqueIds = new ConcurrentLinkedQueue<ProtectionMissingIds>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(
                JavaPlugin.getProvidingPlugin(getClass()), new Runnable() {
                    @Override
                    public void run() {
                        processMissingUniqueIdsQueue();
                    }
                }, 60, 60);
    }

    private void finishFix(Collection<Protection> protections,
            Map<String, NameAndId> nameCache) {
        System.out.println(protections.size() + " protections to fix");
        for (Protection protection : protections) {
            finishFix(protection, nameCache);
        }
    }

    private void finishFix(Protection protection,
            Map<String, NameAndId> nameCache) {

        for (ProtectionSign sign : protection.getSigns()) {
            System.out.println("Fixing a sign at " + sign.getLocation());
            Collection<Profile> oldProfileCollection = sign.getProfiles();
            Collection<Profile> newProfileCollection = new ArrayList<Profile>(3);
            for (Profile profile : oldProfileCollection) {
                profile = replaceProfile(profile, nameCache);
                newProfileCollection.add(profile);
            }
            signParser.saveSign(sign);
        }
    }

    /**
     * Marks this protection as needing fixes for invalid UUIDs.
     *
     * @param protection
     *            The protection to mark.
     */
    public void fixMissingUniqueIds(Protection protection) {
        Validate.notNull(protection);
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
        final Map<String, NameAndId> nameCache;
        try {
            nameCache = new UUIDFetcher(names).call();
            plugin.runLater(new Runnable() {
                        @Override
                        public void run() {
                            // Notify the protections
                            finishFix(protectionsToFix, nameCache);
                        }
                    });
        } catch (Exception e) {
            plugin.severe("Failed to fetch UUIDs", e);
        }
    }

    private Profile replaceProfile(Profile oldProfile,
            Map<String, NameAndId> nameCache) {
        if (!(oldProfile instanceof PlayerProfile)) {
            return oldProfile;
        }
        PlayerProfile playerProfile = (PlayerProfile) oldProfile;
        if (playerProfile.getUniqueId().isPresent()) {
            return oldProfile;
        }
        String name = playerProfile.getDisplayName().toLowerCase();
        NameAndId nameAndId = nameCache.get(name);
        if (nameAndId == null) {
            // Invalid profile
            String line = plugin.getTranslator().get(
                    Translation.TAG_PLAYER_NOT_FOUND);
            NameAndId invalid = NameAndId.of(line, new UUID(0, 0));
            return plugin.getProfileFactory().fromNameAndUniqueId(invalid);
        } else {
            // Valid profile, replace
            System.out.println("Replaced " + oldProfile + " with " + nameAndId);
            return plugin.getProfileFactory().fromNameAndUniqueId(nameAndId);
        }
    }
}
