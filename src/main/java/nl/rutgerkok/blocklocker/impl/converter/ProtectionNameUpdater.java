package nl.rutgerkok.blocklocker.impl.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

/**
 * Updates outdated names on protections.
 *
 */
final class ProtectionNameUpdater {
    private final ProfileFactory profileFactory;
    private final SignParser signParser;

    ProtectionNameUpdater(SignParser signParser, ProfileFactory profileFactory) {
        this.signParser = Preconditions.checkNotNull(signParser);
        this.profileFactory = Preconditions.checkNotNull(profileFactory);
    }

    /**
     * Updates any outdated names on the protection. Does nothing if the
     * protection doesn't have outdated names.
     *
     * @param protection
     *            The protection to update.
     */
    void updateNames(Protection protection) {
        for (ProtectionSign sign : protection.getSigns()) {
            // newProfileCollection is lazily initialized
            List<Profile> newProfileCollection = new ArrayList<Profile>();
            List<Profile> oldProfileCollection = sign.getProfiles();
            boolean changed = false;

            for (Profile profile : oldProfileCollection) {
                Profile newProfile = withUpdatedNameOrNull(profile);
                if (newProfile != null) {
                    newProfileCollection.add(newProfile);
                    changed = true;
                } else {
                    newProfileCollection.add(profile);
                }
            }

            if (changed) {
                signParser.saveSign(sign.withProfiles(newProfileCollection));
            }
        }
    }

    private Profile withUpdatedNameOrNull(Profile profile) {
        if (!(profile instanceof PlayerProfile)) {
            return null;
        }
        PlayerProfile playerProfile = (PlayerProfile) profile;
        Optional<UUID> uuid = playerProfile.getUniqueId();
        if (!uuid.isPresent()) {
            return null;
        }

        Player player = Bukkit.getPlayer(uuid.get());
        if (player != null) {
            if (!player.getName().equals(playerProfile.getDisplayName())) {
                // Player has new name, update
                return profileFactory.fromPlayer(player);
            }
        }

        return null;
    }
}
