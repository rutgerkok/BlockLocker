package nl.rutgerkok.chestsignprotect.profile;

import nl.rutgerkok.chestsignprotect.protection.Protection;

/**
 * Represents a profile. A profile is something that owns or is allowed access
 * to a {@link Protection}.
 *
 */
public interface Profile {

    /**
     * Gets the name of the profile as it should be displayed on signs.
     *
     * @return The name of the profile.
     */
    String getDisplayName();

    /**
     * Gets the name as it should be saved. May not contain spaces.
     *
     * @return The name as it should be saved.
     */
    String getSaveName();

    /**
     * Gets whether this profile includes another profile. For example,
     * "[Everyone]" includes all other profiles, and "jeb_" includes everyone
     * with the name jeb_.
     *
     * @param other
     *            The other profile.
     * @return True if this profile includes the other profile, false otherwise.
     */
    boolean includes(Profile other);
}
