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
     * Gets whether this sign needs an update, for example because UUIDs are
     * missing, the extra data is not yet stored or the color scheme of the text
     * doesn't match the current one.
     *
     * @param uuidsEnabled
     *            True if the server is using unique ids, false if not (offline
     *            mode server).
     * @return True if this sign needs to be updated, false otherwise.
     */
    boolean needsUpdate(boolean uuidsEnabled);

    /**
     * Checks if the given profile is the owner of this chest.
     *
     * @param profile
     *            The profile to check.
     * @return True if it is the owner, false otherwise.
     * @see #getOwner()
     */
    boolean isOwner(Profile profile);

    /**
     * Gets the display name of the owner of the protection. If
     * {@link #getOwner()} is absent, "?" is returned.
     *
     * @return The display name.
     */
    String getOwnerDisplayName();

}
