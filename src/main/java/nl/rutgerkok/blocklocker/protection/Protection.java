package nl.rutgerkok.blocklocker.protection;

import java.util.Collection;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.profile.Profile;

import com.google.common.base.Optional;

/**
 * Represents a generic protection container with its attached signs.
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
     * Gets all signs used in this protection. Low-level method,
     * {@link #getAllowed()} is preferred. Example usage is changing the
     * contents on the sign.
     *
     * @return All signs used by this protection.
     */
    Collection<ProtectionSign> getSigns();

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
     * Gets whether this sign is missing unique ids, and has to rely on player
     * names.
     *
     * @return True if this sign is missing unique ids, false otherwise.
     */
    boolean isMissingUniqueIds();

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
