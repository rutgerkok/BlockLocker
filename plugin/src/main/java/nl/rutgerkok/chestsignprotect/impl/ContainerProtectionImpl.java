package nl.rutgerkok.chestsignprotect.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.rutgerkok.chestsignprotect.ChestSettings.SignType;
import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.ContainerProtection;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

class ContainerProtectionImpl implements ContainerProtection {

    /**
     * Creates a new protection for the protected block.
     *
     * @param blocks
     *            The blocks that are protected. (Usually one block, multiple
     *            for double chests.)
     * @param signFinder
     *            The sign finder.
     * @return The protection.
     */
    static Protection fromBlocks(List<Block> blocks, SignFinder signFinder) {
        return new ContainerProtectionImpl(blocks, signFinder);
    }

    /**
     * Creates a new protection for the protection block. Calling this method
     * will make for a faster {@link #getOwner()}, as it can simply read the
     * first entry on the main sign.
     *
     * @param blocks
     *            The block that are protected. (Usually one block, multiple for
     *            double chests.)
     * @param signFinder
     *            The sign finder.
     * @param mainSign
     *            The main sign, used for {@link #getOwner()}.
     * @return The protection.
     */
    static Protection fromBlocksWithMainSign(List<Block> blocks,
            SignFinder signFinder, Sign mainSign) {
        ContainerProtectionImpl protection = new ContainerProtectionImpl(
                blocks, signFinder);
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
     * @param signFinder
     *            The sign finder.
     * @param signs
     *            All signs in the protection.
     * @return The protection.
     */
    static Protection fromBlocksWithSigns(List<Block> blocks,
            SignFinder signFinder, Collection<Sign> signs) {
        ContainerProtectionImpl protection = new ContainerProtectionImpl(
                blocks, signFinder);
        protection.fetchMainSignAndAllowed(signs);
        return protection;
    }

    private Optional<Collection<Profile>> allAllowed = Optional.absent();
    private final List<Block> blocks;

    private Optional<Sign> mainSign = Optional.absent();
    private Optional<Profile> owner = Optional.absent();
    private final SignFinder signFinder;

    private ContainerProtectionImpl(List<Block> blocks, SignFinder signFinder) {
        Validate.notEmpty(blocks);
        this.blocks = blocks;
        this.signFinder = signFinder;
    }

    private void fetchMainSignAndAllowed(Collection<Sign> signs) {
        Collection<Profile> allAllowed = Lists.newArrayList();
        for (Sign sign : signs) {
            // Check for main sign
            Optional<SignType> type = signFinder.getSignParser().getSignType(
                    sign.getLine(0));
            if (type.isPresent() && type.get().isMainSign()) {
                mainSign = Optional.of(sign);
            }

            // Parse it
            signFinder.getSignParser().parseSign(sign, allAllowed);
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
        Sign mainSign = this.mainSign.get();
        Collection<Profile> profiles = new ArrayList<Profile>(4);
        signFinder.getSignParser().parseSign(mainSign, profiles);

        Iterator<Profile> iterator = profiles.iterator();
        if (iterator.hasNext()) {
            owner = Optional.of(iterator.next());
        }

    }

    private Collection<Sign> fetchSigns() {
        return signFinder.findAttachedSigns(blocks);
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
    public Collection<Sign> getSigns() {
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
