package nl.rutgerkok.blocklocker.impl.profile;

import java.util.UUID;

import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

import org.json.simple.JSONObject;

import com.google.common.base.Optional;

class PlayerProfileImpl implements PlayerProfile {

    static final String NAME_KEY = "n";
    static final String UUID_KEY = "u";
    private final String displayName;
    private final Optional<UUID> uuid;

    PlayerProfileImpl(String displayName, Optional<UUID> uuid) {
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

        PlayerProfileImpl otherProfile = (PlayerProfileImpl) other;
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

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getSaveObject() {
        JSONObject object = new JSONObject();
        object.put(NAME_KEY, displayName);
        if (uuid.isPresent()) {
            object.put(UUID_KEY, uuid.get().toString());
        }
        return object;
    }

    @Override
    public Optional<UUID> getUniqueId() {
        return uuid;
    }

    @Override
    public int hashCode() {
        if (uuid.isPresent()) {
            return uuid.hashCode();
        }
        return displayName.toLowerCase().hashCode();
    }

    @Override
    public boolean includes(Profile other) {
        if (!(other instanceof PlayerProfile)) {
            return false;
        }

        PlayerProfile otherProfile = (PlayerProfile) other;
        if (uuid.isPresent()) {
            return uuid.equals(otherProfile.getUniqueId());
        }
        return displayName.equalsIgnoreCase(otherProfile.getDisplayName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[uuid=" + uuid.orNull() + ",name=" + displayName + "]";
    }

}
