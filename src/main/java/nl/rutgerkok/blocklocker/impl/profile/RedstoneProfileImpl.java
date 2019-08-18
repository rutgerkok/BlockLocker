package nl.rutgerkok.blocklocker.impl.profile;

import java.util.Date;

import org.json.simple.JSONObject;

import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.profile.Profile;

class RedstoneProfileImpl implements Profile {

    static final String REDSTONE_KEY = "r";

    private final String tag;

    /**
     * Creates a new [Redstone]-profile.
     *
     * @param translation
     *            Usually "Redstone", may be localized.
     */
    RedstoneProfileImpl(String translation) {
        this.tag = translation;
    }

    /**
     * All instances of this object are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getClass() == other.getClass();
    }

    @Override
    public String getDisplayName() {
        return '[' + tag + ']';
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getSaveObject() {
        JSONObject object = new JSONObject();
        object.put(REDSTONE_KEY, true);
        return object;
    }

    /**
     * All instances of this object are equal.
     */
    @Override
    public int hashCode() {
        return 4;
    }

    @Override
    public boolean includes(Profile other) {
        Preconditions.checkNotNull(other);
        return other instanceof RedstoneProfileImpl;
    }

    @Override
    public boolean isExpired(Date cutoffDate) {
        // These never expire
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
