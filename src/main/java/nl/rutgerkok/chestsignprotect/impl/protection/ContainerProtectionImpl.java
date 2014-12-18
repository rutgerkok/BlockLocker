package nl.rutgerkok.chestsignprotect.impl.protection;

import java.util.Collection;
import java.util.List;

import nl.rutgerkok.chestsignprotect.ProtectionSign;
import nl.rutgerkok.chestsignprotect.SignType;
import nl.rutgerkok.chestsignprotect.impl.BlockFinder;
import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.ContainerProtection;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public final class ContainerProtectionImpl implements ContainerProtection {

    /**
     * Creates a new protection for the protected block.
     *
     * @param blocks
     *            The blocks that are protected. (Usually one block, multiple
     *            for double chests.)
     * @param blockFinder
     *            The sign finder.
     * @return The protection.
     */
    public static Protection fromBlocks(List<Block> blocks, BlockFinder blockFinder) {
        return new ContainerProtectionImpl(blocks, blockFinder);
    }

    /**
     * Creates a new protection for the protection block. Calling this method
     * will make for a faster {@link #getOwner()}, as it can simply read the
     * first entry on the main sign.
     *
     * @param blocks
     *            The block that are protected. (Usually one block, multiple for
     *            double chests.)
     * @param blockFinder
     *            The sign finder.
     * @param mainSign
     *            The main sign, used for {@link #getOwner()}.
     * @return The protection.
     */
    public static Protection fromBlocksWithMainSign(List<Block> blocks,
            BlockFinder blockFinder, ProtectionSign mainSign) {
        ContainerProtectionImpl protection = new ContainerProtectionImpl(
                blocks, blockFinder);
        protection.mainSign = Optional.of(mainSign);
        return protection;
    }

    /**
     * Creates a new protection for the protection block. Calling this method
     * will make for a faster {@link #getAllowed()} and {@link #getOwner()}
     *
     * @param blocks
     *            The blocks that are protected. (Usually one block, multiple
     *            for double chests.)
     * @param blockFinder
     *            The sign finder.
     * @param signs
     *            All signs in the protection.
     * @return The protection.
     */
    public static Protection fromBlocksWithSigns(List<Block> blocks,
            BlockFinder blockFinder, Collection<ProtectionSign> signs) {
        ContainerProtectionImpl protection = new ContainerProtectionImpl(
                blocks, blockFinder);
        protection.fetchMainSignAndAllowed(signs);
        return protection;
    }

    private Optional<Collection<Profile>> allAllowed = Optional.absent();
    private final BlockFinder blockFinder;

    private final List<Block> blocks;
    private Optional<ProtectionSign> mainSign = Optional.absent();
    private Optional<Profile> owner = Optional.absent();

    private ContainerProtectionImpl(List<Block> blocks, BlockFinder blockFinder) {
        Validate.notEmpty(blocks);
        this.blocks = blocks;
        this.blockFinder = blockFinder;
    }

    private void fetchMainSignAndAllowed(Collection<ProtectionSign> signs) {
        Collection<Profile> allAllowed = Lists.newArrayList();
        for (ProtectionSign sign : signs) {
            // Check for main sign
            SignType type = sign.getType();
            if (type.isMainSign()) {
                mainSign = Optional.of(sign);
            }

            // Parse it
            allAllowed.addAll(sign.getProfiles());
        }
        this.allAllowed = Optional.of(allAllowed);
    }

    private void fetchOwner() {
        if (!mainSign.isPresent()) {
            // Need information about the main sign
            fetchMainSignAndAllowed(fetchSigns());
        }

        if (!mainSign.isPresent()) {
            // Still no main sign, it must be missing
            return;
        }

        // We have a hint, grab the first name on it
        ProtectionSign mainSign = this.mainSign.get();
        List<Profile> profiles = mainSign.getProfiles();
        owner = Optional.of(profiles.get(0));
    }

    private Collection<ProtectionSign> fetchSigns() {
        return blockFinder.findAttachedSigns(blocks);
    }

    @Override
    public Collection<Profile> getAllowed() {
        if (allAllowed.isPresent()) {
            return allAllowed.get();
        }
        fetchMainSignAndAllowed(fetchSigns());
        return allAllowed.get();
    }

    @Override
    public Optional<Profile> getOwner() {
        if (owner.isPresent()) {
            return owner;
        }

        fetchOwner();
        return owner;
    }

    @Override
    public Collection<ProtectionSign> getSigns() {
        // Sign objects cannot be cached
        return fetchSigns();
    }

    @Override
    public boolean isAllowed(Profile profile) {
        for (Profile allowed : getAllowed()) {
            if (allowed.includes(profile)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMissingUniqueIds() {
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
    public boolean isOwner(Profile profile) {
        Validate.notNull(profile);

        Optional<Profile> owner = getOwner();

        if (!owner.isPresent()) {
            return false;
        }

        return owner.get().includes(profile);
    }

}
