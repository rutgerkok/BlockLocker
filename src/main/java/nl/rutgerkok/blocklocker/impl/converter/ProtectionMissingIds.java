package nl.rutgerkok.blocklocker.impl.converter;

import java.util.Set;
import java.util.UUID;

import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * Represents a {@link Protection} missing one or more {@link UUID}s.
 *
 */
final class ProtectionMissingIds {

    /**
     * Creates a {@link ProtectionMissingIds} for the given protection.
     *
     * @param protection
     *            The protection.
     * @param lookupPastNames
     *            Whether past names need to be looked up. False for new signs.
     * @return The {@link ProtectionMissingIds}, or absent if the protection is
     *         not missing ids.
     */
    static Optional<ProtectionMissingIds> of(Protection protection, boolean lookupPastNames) {
        ImmutableSet.Builder<String> namesMissingUniqueIds = ImmutableSet.builder();
        boolean missingIds = false;

        for (Profile profile : protection.getAllowed()) {
            if (!(profile instanceof PlayerProfile)) {
                continue;
            }

            PlayerProfile playerProfile = (PlayerProfile) profile;
            Optional<UUID> uuid = playerProfile.getUniqueId();
            if (!uuid.isPresent()) {
                namesMissingUniqueIds.add(playerProfile.getDisplayName());
                missingIds = true;
            }
        }

        if (missingIds) {
            return Optional.of(new ProtectionMissingIds(protection, namesMissingUniqueIds, lookupPastNames));
        } else {
            return Optional.absent();
        }
    }

    private final Set<String> namesMissingUniqueIds;
    private final Protection protection;
    private final boolean lookupPastNames;

    /**
     * Builds the list of UUIDs to fetch. Must be called on the server thread.
     *
     * @param protection
     *            The protection to fetch the UUIDs for.
     * @param namesMissingUniqueIds
     *            The names that need to be looked up.
     * @param lookupPastNames
     *            Whether the names may be outdated.
     */
    private ProtectionMissingIds(Protection protection, ImmutableSet.Builder<String> namesMissingUniqueIds, boolean lookupPastNames) {
        this.protection = protection;
        this.namesMissingUniqueIds = namesMissingUniqueIds.build();
        this.lookupPastNames = lookupPastNames;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof ProtectionMissingIds)) {
            return false;
        }
        ProtectionMissingIds otherProtection = (ProtectionMissingIds) other;
        return otherProtection.protection.equals(protection);
    }

    /**
     * Gets the names that are missing unique ids. Can be called from any
     * thread.
     *
     * @return The names.
     */
    Set<String> getNamesMissingUniqueIds() {
        return namesMissingUniqueIds;
    }

    /**
     * Gets the protection that was missing unique ids. Can be called from any
     * thread, but keep in mind that methods on {@link Protection} aren't thread
     * safe.
     *
     * @return The protection.
     */
    Protection getProtection() {
        return protection;
    }

    @Override
    public int hashCode() {
        return protection.hashCode();
    }

    /**
     * Gets whether past names from before name changes were allowed must be
     * looked up. This value should be false for new protections.
     * 
     * @return True if past names must be looked up, false otherwise.
     */
    public boolean mustLookupPastNames() {
        return lookupPastNames;
    }
}
