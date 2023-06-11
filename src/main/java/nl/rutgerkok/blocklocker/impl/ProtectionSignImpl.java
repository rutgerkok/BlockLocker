package nl.rutgerkok.blocklocker.impl;

import java.util.List;

import org.bukkit.Location;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.profile.Profile;

final class ProtectionSignImpl implements ProtectionSign {

    private static final int MAX_PROFILES = 6;
    private final SignType signType;
    private final List<Profile> profiles;
    private final Location location;
    private final boolean requiresResave;

    ProtectionSignImpl(Location location, SignType signType,
            List<Profile> profiles, boolean requiresResave) {
        this.location = location;
        this.signType = Preconditions.checkNotNull(signType);
        this.profiles = ImmutableList.copyOf(profiles);
        this.requiresResave = requiresResave;
        if (profiles.isEmpty() || profiles.size() > MAX_PROFILES) {
            throw new IllegalArgumentException("Invalid size for profiles collection: " + profiles);
        }
        if (profiles.indexOf(null) != -1) {
            throw new IllegalArgumentException("Profiles list contains null profile: " + profiles);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ProtectionSignImpl)) {
            return false;
        }
        ProtectionSignImpl other = (ProtectionSignImpl) obj;
        if (!Objects.equal(location.getWorld(), other.location.getWorld())) {
            return false;
        }
        if (location.getBlockX() != other.location.getBlockX()) {
            return false;
        }
        if (location.getBlockY() != other.location.getBlockY()) {
            return false;
        }
        if (location.getBlockZ() != other.location.getBlockZ()) {
            return false;
        }
        return true;
    }

    @Override
    public Location getLocation() {
        // Location is mutable, so always return a clone
        return location.clone();
    }

    @Override
    public List<Profile> getProfiles() {
        return profiles;
    }

    @Override
    public SignType getType() {
        return signType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + location.getBlockX();
        result = prime * result + location.getBlockY();
        result = prime * result + location.getBlockZ();
        return result;
    }

    @Override
    public boolean requiresResave() {
        return this.requiresResave;
    }

    @Override
    public ProtectionSign withProfiles(List<Profile> profiles) {
        return new ProtectionSignImpl(location, signType, profiles, requiresResave);
    }

    @Override
    public ProtectionSign withRequiringResave() {
        return new ProtectionSignImpl(location, signType, profiles, true);
    }

}
