package nl.rutgerkok.blocklocker.protection;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.bukkit.block.Block;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.profile.Profile;

/**
 * Represents a generic protection container with its attached signs.
 *
 */
public interface Protection {

    /**
     * When to play a sound for opening a door.
     *
     */
    enum SoundCondition {
        /**
         * Never play a sound.
         */
        NEVER,
        /**
         * Only play a sound for doors that the Minecraft client doesn't
         * automatically play a sound for a player opens it.
         */
        AUTOMATIC,
        /**
         * Always play a sound.
         */
        ALWAYS
    }

    /**
     * Gets whether the protection can be opened as a door/gate/trapdoor.
     * @return True if the protection can be opened, false otherwise.
     */
    boolean canBeOpened();

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
     * Gets the location of the protection. Returns one of the blocks in the
     * protection (if there are multiple blocks), and not one of the signs.
     *
     * @return The location.
     */
    Block getSomeProtectedBlock();

    /**
     * Gets the amount of seconds the door should stay open, before closing
     * automatically. If no amount of seconds was specified on the door, -1 is
     * returned.
     *
     * @return The amount of ticks, or -1 if unspecified.
     */
    int getOpenSeconds();

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
     * Gets the display name of the owner of the protection. If
     * {@link #getOwner()} is absent, "?" is returned.
     *
     * @return The display name.
     */
    String getOwnerDisplayName();

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
     * Gets whether this protection is expired. As protection is expired when
     * the {@link #getOwner() owner profile} is considered
     * {@link Profile#isExpired(Date) expired}.
     *
     * @param cutoffDate
     *            The cutoff date.
     * @return True if the profile is expired, false otherwise.
     */
    boolean isExpired(Date cutoffDate);

    /**
     * Gets whether the door is currently opened. If only parts of the door are
     * open, the result of this method is undefined.
     *
     * @return Whether the door is currently opened.
     */
    boolean isOpen();

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
     * Opens or closes the door, as specified. Plays no sound.
     *
     * @param open
     *            True to open the door, false otherwise.
     * @see #setOpen(boolean, SoundCondition) Control over sound playing.
     */
    default void setOpen(boolean open) {
        setOpen(open, SoundCondition.NEVER);
    }

    /**
     * Opens or closes the door, as specified.
     *
     * @param open
     *            True to open the door, false otherwise.
     * @param playSound
     *            Whether to play a sound.
     */
    void setOpen(boolean open, SoundCondition playSound);

}
