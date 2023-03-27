package nl.rutgerkok.blocklocker;

import java.util.List;

import org.bukkit.Location;

import nl.rutgerkok.blocklocker.profile.Profile;

/**
 * Represents the information on a protection sign in the world. Instances of
 * this interface must be immutable.
 *
 * <p>
 * Two protection signs are considered equal when they are placed on the same
 * block in the same world and are of the same implementation.
 */
public interface ProtectionSign {

    /**
     * Gets the location of this sign.
     *
     * @return The location.
     */
    Location getLocation();

    /**
     * Gets all profiles currently on the sign. The list will have one, two or
     * three profiles. In other words: it is always save to call
     * {@code getProfiles().get(0)}.
     *
     * @return All profiles.
     */
    List<Profile> getProfiles();

    /**
     * Gets the type of this sign. The type of the sign depends on the header of
     * the sign.
     *
     * @return The type.
     */
    SignType getType();

    /**
     * Returns true if this sign is saved in an outdated way (plain text, missing
     * header color/wrong casing) and would require a resave.
     *
     * @return Whether the sign requires a resaves.
     */
    boolean requiresResave();

    /**
     * Creates a new protection sign object with the given profiles. Existing
     * profiles are erased. If this sign is not of the type
     * {@link SignType#MORE_USERS} the first entry of the list will become the
     * owner.
     *
     * <p>
     * This object is immutable and will not be modified. The actual sign in the
     * world will not be modified too. You must save the returned
     * {@link ProtectionSign} using {@link SignParser#saveSign(ProtectionSign)}.
     *
     *
     * @param profiles
     *            The profiles for the protection sign.
     * @return The new object. * @throws NullPointerException If any entry in the
     *         list is 0.
     * @throws IllegalArgumentException
     *             If the list is empty, or if the list has a size larger than 3.
     */
    ProtectionSign withProfiles(List<Profile> profiles);

    /**
     * Returns a instance that requires a resave.
     *
     * @see #requiresResave()
     * @return An instance.
     */
    ProtectionSign withRequiringResave();

}
