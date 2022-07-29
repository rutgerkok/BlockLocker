package nl.rutgerkok.blocklocker.impl.protection;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;

import com.google.common.collect.Lists;

import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.profile.TimerProfile;
import nl.rutgerkok.blocklocker.protection.Protection;

/**
 * Base class for protection implementations.
 *
 */
abstract class AbstractProtection implements Protection {

    /**
     * Checks if the instance is of {@link Openable} that will open correctly when
     * you call {@link Openable#setOpen(boolean)}. For barrels, opening them using
     * {@link Openable#setOpen(boolean)} won't show the inventory, so those
     * shouldn't be considered openable.
     *
     * @param blockData
     *            The block data.
     * @return True if the block data is openable in a way that is functional.
     */
    protected static boolean isFunctionalOpenable(BlockData blockData) {
        if (!(blockData instanceof Openable)) {
            return false;
        }
        if (blockData.getMaterial() == Material.BARREL) {
            return false; // Barrels are openable in Bukkit since 1.19, but then the inventory won't show up
        }
        return true;
    }
    private Optional<Collection<Profile>> allAllowed = Optional.empty();
    private Optional<Profile> owner = Optional.empty();

    private Optional<Collection<ProtectionSign>> allSigns = Optional.empty();

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
                    return Optional.empty();
                }
                return Optional.of(profiles.get(0));
            }
        }
        return Optional.empty();
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
    public final int getOpenSeconds() {
        for (Profile profile : getAllowed()) {
            if (profile instanceof TimerProfile) {
                return ((TimerProfile) profile).getOpenSeconds();
            }
        }
        return -1;
    }

    @Override
    public final Optional<Profile> getOwner() {
        if (!owner.isPresent()) {
            owner = fetchOwner();
        }

        return owner;
    }

    @Override
    public final String getOwnerDisplayName() {
        Optional<Profile> owner = getOwner();
        if (owner.isPresent()) {
            return owner.get().getDisplayName();
        }
        return "?";
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
        if (!getOwner().isPresent()) {
            // [Private] sign is missing, only [More Users]
            // signs are remaining. So allow everyone.
            return true;
        }
        return false;
    }

    @Override
    public boolean isExpired(Date cutoffDate) {
        Optional<Profile> owner = getOwner();

        if (owner.isPresent()) {
            return owner.get().isExpired(cutoffDate);
        }

        // Protections without an owner are invalid, but not expired
        return false;
    }

    @Override
    public final boolean isOwner(Profile profile) {
        Validate.notNull(profile);

        Optional<Profile> owner = getOwner();

        if (!owner.isPresent()) {
            return true; // No owner - so owned by everyone
            // Only half-broken protections can be in this state;
            // protections no [Private] sign, but with [More Users] signs
        }

        return owner.get().includes(profile);
    }
}
