package nl.rutgerkok.blocklocker.impl;

import java.util.List;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.profile.Profile;

import org.bukkit.Location;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

final class ProtectionSignImpl implements ProtectionSign {

    private static final int MAX_PROFILES = 3;
    private final SignType signType;
    private final List<Profile> profiles;
    private final Location location;

    ProtectionSignImpl(Location location, SignType signType, List<Profile> profiles) {
        this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.signType = Preconditions.checkNotNull(signType);
        this.profiles = ImmutableList.copyOf(profiles);
        if (profiles.isEmpty() || profiles.size() > MAX_PROFILES) {
            throw new IllegalArgumentException("Invalid size for profiles collection: " + profiles);
        }
        if (profiles.indexOf(null) != -1) {
            throw new IllegalArgumentException("Profiles list contains null profile: " + profiles);
        }
    }

    @Override
    public SignType getType() {
        return signType;
    }

    @Override
    public List<Profile> getProfiles() {
        return profiles;
    }

    @Override
    public ProtectionSign withProfiles(List<Profile> profiles) {
        return new ProtectionSignImpl(location, signType, profiles);
    }

    @Override
    public Location getLocation() {
        // Location is mutable, so always return a clone
        return location.clone();
    }

}
