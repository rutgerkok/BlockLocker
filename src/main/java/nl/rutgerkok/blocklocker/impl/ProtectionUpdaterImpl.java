package nl.rutgerkok.blocklocker.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.ProtectionUpdater;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

public class ProtectionUpdaterImpl implements ProtectionUpdater {

    private final Server server;
    private final SignParser signParser;
    private final ProfileFactory profileFactory;

    public ProtectionUpdaterImpl(Server server, SignParser signParser, ProfileFactory profileFactory) {
        this.server = Objects.requireNonNull(server, "server");
        this.signParser = Objects.requireNonNull(signParser, "signParser");
        this.profileFactory = Objects.requireNonNull(profileFactory, "profileFactory");
    }

    @Nullable
    private PlayerProfile getUpdatedProfile(PlayerProfile profile) {
        if (profile.getUniqueId().isPresent()) {
           Player player = server.getPlayer(profile.getUniqueId().get());
           if (player != null && !player.getName().equals(profile.getDisplayName())) {
               // Found a changed name
               return profileFactory.fromPlayer(player);
           }
           return null;
        } else {
            // Found a missing unique id
            String name = profile.getDisplayName();
            if (name.isEmpty()) {
                return null; // Empty line, ignore
            }
            Player player = server.getPlayerExact(name);
            if (player == null) {
                return null; // No player online with that name, lookup failed
            }
            return profileFactory.fromPlayer(player);
        }
    }

    @Override
    public void update(Protection protection, boolean newProtection) {
        for (ProtectionSign protectionSign : protection.getSigns()) {
            updateProtectionSign(protectionSign);
        }
    }

    @Nullable
    private List<Profile> updateProfiles(ProtectionSign protectionSign) {
        List<Profile> updatedProfiles = null;

        int i = 0;
        for (Profile profile : protectionSign.getProfiles()) {
            if (!(profile instanceof PlayerProfile)) {
                continue;
            }
            PlayerProfile updatedProfile = getUpdatedProfile((PlayerProfile) profile);
            if (updatedProfile == null) {
                continue; // Nothing to update
            }

            if (updatedProfiles == null) {
                // Need to initialize list
                updatedProfiles = new ArrayList<>(protectionSign.getProfiles());
            }
            updatedProfiles.set(i, updatedProfile);
            i++;
        }
        return updatedProfiles;
    }

    private void updateProtectionSign(ProtectionSign protectionSign) {
        List<Profile> updatedProfiles = updateProfiles(protectionSign);

        if (updatedProfiles != null) {
            protectionSign = protectionSign.withProfiles(updatedProfiles);
        }

        if (updatedProfiles != null || protectionSign.requiresResave()) {
            signParser.saveSign(protectionSign);
        }
    }
}
