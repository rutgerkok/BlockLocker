package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;
import java.util.List;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.apache.commons.lang.Validate;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * Base class for protection implementations.
 *
 */
abstract class AbstractProtection implements Protection {

    private Optional<Collection<Profile>> allAllowed = Optional.absent();
    private Optional<Profile> owner = Optional.absent();
    private Optional<Collection<ProtectionSign>> allSigns = Optional.absent();

    /**
     * Constructor for creating the protection with just a single sign looked
     * up. Not all signs are found yet. If it turns out that all signs are
     * needed, {@link #fetchSigns()} will be called.
     * 
     * @param sign
     *            A sign attached to the protection.
     */
    AbstractProtection(ProtectionSign sign) {
        if (sign.getType().isMainSign()) {
            List<Profile> profiles = sign.getProfiles();
            if (!profiles.isEmpty()) {
                this.owner = Optional.of(profiles.get(0));
            }
        }
    }

    /**
     * Constructor for creating the protection with all signs already looked up.
     * Collection may not be empty.
     * 
     * @param signs
     *            All signs.
     */
    AbstractProtection(Collection<ProtectionSign> signs) {
        Validate.notEmpty(signs);
        this.allSigns = Optional.of(signs);
    }

    private Collection<Profile> fetchAllowed(Collection<ProtectionSign> signs) {
        Collection<Profile> allAllowed = Lists.newArrayList();
        for (ProtectionSign sign : signs) {
            // Parse it
            allAllowed.addAll(sign.getProfiles());
        }
        return allAllowed;
    }

    private Optional<Profile> fetchOwner() {
        for (ProtectionSign sign : getSigns()) {
            if (sign.getType().isMainSign()) {
                List<Profile> profiles = sign.getProfiles();
                if (profiles.isEmpty()) {
                    return Optional.absent();
                }
                return Optional.of(profiles.get(0));
            }
        }
        return Optional.absent();
    }

    /**
     * Finds all signs attached to this protection. This method must always do a
     * lookup, results may not be cached: {@link AbstractProtection} already
     * does that.
     *
     * @return All signs.
     */
    protected abstract Collection<ProtectionSign> fetchSigns();

    @Override
    public final Collection<Profile> getAllowed() {
        if (!allAllowed.isPresent()) {
            allAllowed = Optional.of(fetchAllowed(getSigns()));
        }
        return allAllowed.get();
    }

    @Override
    public final Optional<Profile> getOwner() {
        if (!owner.isPresent()) {
            owner = fetchOwner();
        }

        return owner;
    }

    @Override
    public final Collection<ProtectionSign> getSigns() {
        if (!this.allSigns.isPresent()) {
            this.allSigns = Optional.of(fetchSigns());
        }
        return this.allSigns.get();
    }

    @Override
    public final boolean isAllowed(Profile profile) {
        for (Profile allowed : getAllowed()) {
            if (allowed.includes(profile)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean isMissingUniqueIds() {
        for (Profile profile : getAllowed()) {
            if (!(profile instanceof PlayerProfile)) {
                continue;
            }
            if (((PlayerProfile) profile).getUniqueId().isPresent()) {
                continue;
            }

            // Found missing id
            return true;
        }
        return false;
    }

    @Override
    public final boolean isOwner(Profile profile) {
        Validate.notNull(profile);

        Optional<Profile> owner = getOwner();

        if (!owner.isPresent()) {
            return false;
        }

        return owner.get().includes(profile);
    }
}
