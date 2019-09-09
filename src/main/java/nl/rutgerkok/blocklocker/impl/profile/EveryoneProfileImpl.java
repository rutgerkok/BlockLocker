package nl.rutgerkok.blocklocker.impl.profile;

import java.util.Date;

import org.apache.commons.lang.Validate;

import com.google.gson.JsonObject;

import nl.rutgerkok.blocklocker.profile.Profile;

class EveryoneProfileImpl implements Profile {

    static final String EVERYONE_KEY = "e";

    private final String tag;

    /**
     * Creates a new [Everyone]-profile.
     *
     * @param translation
     *            Usually "Everyone", may be localized.
     */
    EveryoneProfileImpl(String translation) {
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
    
    @Override
    public JsonObject getSaveObject() {
    	JsonObject object = new JsonObject();
        object.addProperty(EVERYONE_KEY, true);
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
        Validate.notNull(other);
        return true;
    }

    @Override
    public boolean isExpired(Date cutoffDate) {
        // The [Everyone] profile never expires
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
