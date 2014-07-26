package nl.rutgerkok.chestsignprotect.protection;

import java.util.Collection;

import nl.rutgerkok.chestsignprotect.profile.Profile;

import com.google.common.base.Optional;

/**
 * Represents a generic protection container.
 *
 */
public interface Protection {

    /**
     * Gets the allowed profiles in this protection. Allowed profiles can
     * place/take items and open doors, but cannot break the protection. This
     * list includes the {@link #getOwner() owner}.
     *
     * @return The allowed profiles.
     * @see #isAllowed(Profile)
     */
    Collection<Profile> getAllowed();

    /**
     * Gets the owner of this protection. The owner is the only one allowed to
     * destroy this protection. All protections have an owner, unless they have
     * been corrupted by for example a world editor.
     *
     * @return The owner.
     * @see #isOwner(Profile)
     */
    Optional<Profile> getOwner();

    /**
     * Checks if the given profile has been allowed to this protection.
     *
     * @param profile
     *            The profile to check.
     * @return True if the profile is allowed, false otherwise.
     * @see #getAllowed()
     */
    boolean isAllowed(Profile profile);

    /**
     * Checks if the given profile is the owner of this chest.
     *
     * @param profile
     *            The profile to check.
     * @return True if it is the owner, false otherwise.
     * @see #getOwner()
     */
    boolean isOwner(Profile profile);

}
