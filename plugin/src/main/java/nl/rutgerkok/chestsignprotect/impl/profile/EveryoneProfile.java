package nl.rutgerkok.chestsignprotect.impl.profile;

import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.apache.commons.lang.Validate;

class EveryoneProfile implements Profile {

    private final String tag;

    /**
     * Creates a new [Everyone]-profile.
     *
     * @param tag
     *            Usually "[Everyone]", may be localized.
     */
    EveryoneProfile(String tag) {
        this.tag = tag;
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
        return tag;
    }

    @Override
    public String getSaveName() {
        return tag;
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
    public String toString() {
        return "Everyone:" + tag;
    }

}
