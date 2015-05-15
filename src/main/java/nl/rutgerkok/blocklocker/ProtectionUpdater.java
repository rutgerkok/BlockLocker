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
     * Does generic cleaning for the signs on the protection at some point in
     * the near future. Fixes the missing UUIDs in the given protection, by
     * looking them up online.
     *
     * @param protection
     *            The protection to fix. If the protection was already queued
     *            for an update, nothing happens.
     * @param newProtection
     *            True if this protection is new (lookups of past names are not
     *            necessary then), false otherwise.
     */
    void update(Protection protection, boolean newProtection);

}