package nl.rutgerkok.blocklocker.profile;

import java.util.Date;

import org.json.simple.JSONObject;

/**
 * Represents a profile. A profile is usually a player, but it may also be a whole group or something abstract like
 * "Redstone".
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
     * Gets the object as it should be saved.
     *
     * @return The object as it should be saved.
     */
    JSONObject getSaveObject();

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

    /**
     * Gets whether this profile is expired.
     * 
     * @param cutoffDate
     *            The cutoff date: there must be activity for this profile after
     *            the given date, or else it's considered expired.
     * @return True if the profile is expired, false otherwise.
     */
    boolean isExpired(Date cutoffDate);
    
}
