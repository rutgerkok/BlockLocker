package nl.rutgerkok.blocklocker.impl;

import java.util.List;
import java.util.UUID;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

final class ProtectionSignImpl implements ProtectionSign {

    private static final int MAX_PROFILES = 3;
    private final SignType signType;
    private final List<Profile> profiles;
    private final Location location;
    private final boolean needsUpdateOverride;

    ProtectionSignImpl(Location location, SignType signType,
            List<Profile> profiles, boolean needsUpdateOverride) {
        this.location = location;
        this.signType = Preconditions.checkNotNull(signType);
        this.profiles = ImmutableList.copyOf(profiles);
        this.needsUpdateOverride = needsUpdateOverride;
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
        return new ProtectionSignImpl(location, signType, profiles, needsUpdateOverride);
    }

    @Override
    public Location getLocation() {
        // Location is mutable, so always return a clone
        return location.clone();
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
    public boolean needsUpdate(boolean useUniqueIds) {
        if (needsUpdateOverride) {
            return true;
        }
        if (!useUniqueIds) {
            return false;
        }
        for (Profile profile : profiles) {
            if (profile instanceof PlayerProfile) {
                PlayerProfile playerProfile = (PlayerProfile) profile;
                if (needsUpdateInOnlineMode(playerProfile)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean needsUpdateInOnlineMode(PlayerProfile playerProfile) {
        Optional<UUID> uuid = playerProfile.getUniqueId();
        if (uuid.isPresent()) {
            // Check if name is up-to-date
            Player player = Bukkit.getPlayer(uuid.get());
            if (player != null && player.getName().equals(playerProfile.getDisplayName())) {
                return true;
            }
        } else {
            // Missing UUID
            return true;
        }
        return false;
    }

}
