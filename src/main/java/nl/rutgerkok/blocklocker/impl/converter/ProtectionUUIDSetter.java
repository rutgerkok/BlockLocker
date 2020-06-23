package nl.rutgerkok.blocklocker.impl.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.impl.converter.UUIDHandler.Result;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

class ProtectionUUIDSetter extends UUIDHandler.ResultConsumer {

    private final BlockLockerPlugin plugin;
    private final Collection<Protection> protections;

    ProtectionUUIDSetter(BlockLockerPlugin plugin, Collection<Protection> protections) {
        super(true);
        this.plugin = plugin;
        this.protections = protections;
    }

    @Override
    public void accept(Map<String, Result> results) {
        for (Protection protection : protections) {
            finishFix(protection, results);
        }
    }

    private void finishFix(Protection protection, Map<String, Result> results) {

        SignParser signParser = plugin.getSignParser();
        for (ProtectionSign sign : protection.getSigns()) {
            List<Profile> oldProfileCollection = sign.getProfiles();
            List<Profile> newProfileCollection = new ArrayList<>(3);
            for (Profile profile : oldProfileCollection) {
                profile = replaceProfile(profile, results);
                newProfileCollection.add(profile);
            }
            signParser.saveSign(sign.withProfiles(newProfileCollection));
        }
    }

    private Profile replaceProfile(Profile oldProfile, Map<String, Result> results) {
        if (!(oldProfile instanceof PlayerProfile)) {
            return oldProfile;
        }
        PlayerProfile playerProfile = (PlayerProfile) oldProfile;
        if (playerProfile.getUniqueId().isPresent()) {
            return oldProfile;
        }
        String name = playerProfile.getDisplayName().toLowerCase(Locale.ROOT);
        Result result = results.get(name);
        if (result == null) {
            // No lookup :(
            return plugin.getProfileFactory().fromNameAndUniqueId(name, Optional.empty());
        } else {
            // Valid profile, replace
            return plugin.getProfileFactory().fromNameAndUniqueId(result.getName(), result.getUniqueId());
        }
    }

}