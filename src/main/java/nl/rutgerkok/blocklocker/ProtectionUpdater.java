package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.protection.Protection;

/**
 * Queues protections for updates of the attached signs. In offline mode, just
 * the colors have to be updated, but in online mode the UUID may be looked up,
 * or the player name may be updated.
 *
 */
public interface ProtectionUpdater {

    /**
     * How to handle protections that currently return true from the
     * {@link Protection#needsUpdate(boolean)} method.
     *
     */
    enum UpdateMode {
        /**
         * Always tries to update the protection.
         */
        FORCED,
        /**
         * Only updates the protection if
         * {@link Protection#needsUpdate(boolean)} returns true.
         */
        IF_NEEDED
    }

    /**
     * Does generic cleaning for the signs on the protection at some point in
     * the near future. Fixes the missing UUIDs in the given protection, by
     * looking them up online.
     *
     * @param protection
     *            The protection to fix. If the protection was already queued
     *            for an update, nothing happens.
     * @param updateMode
     *            The update mode. Keep in mind that if the protection is
     *            currently being edited, the changes may not yet have written
     *            through, so the protection may not get an update even if it
     *            would need one the next tick. So avoid the mode
     *            {@link UpdateMode#IF_NEEDED} if the protection is currently
     *            being edited.
     */
    void update(Protection protection, UpdateMode updateMode);

}