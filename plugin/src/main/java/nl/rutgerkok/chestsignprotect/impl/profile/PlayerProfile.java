package nl.rutgerkok.chestsignprotect.impl.profile;

import java.util.UUID;

import nl.rutgerkok.chestsignprotect.profile.Profile;

import com.google.common.base.Optional;

class PlayerProfile implements Profile {

    private final String displayName;
    private final Optional<UUID> uuid;

    PlayerProfile(String displayName, Optional<UUID> uuid) {
        this.displayName = displayName;
        this.uuid = uuid;
    }

    /**
     * We compare uuids or names. Objects are equal if the uuids are present in
     * both objects and are equal, or if the uuids are present in neither
     * objects and are not equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        PlayerProfile otherProfile = (PlayerProfile) other;
        if (uuid.isPresent() != otherProfile.uuid.isPresent()) {
            return false;
        }

        if (uuid.isPresent()) {
            return uuid.equals(otherProfile.uuid);
        } else {
            return displayName.equals(otherProfile.displayName);
        }
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSaveName() {
        if (uuid.isPresent()) {
            return uuid.get() + "|" + displayName;
        }
        return displayName;
    }

    @Override
    public int hashCode() {
        if (uuid.isPresent()) {
            return uuid.hashCode();
        }
        return displayName.hashCode();
    }

}
