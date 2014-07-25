package nl.rutgerkok.chestsignprotect.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import nl.rutgerkok.chestsignprotect.ChestSettings.SignType;
import nl.rutgerkok.chestsignprotect.profile.Profile;
import nl.rutgerkok.chestsignprotect.protection.ContainerProtection;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

class ContainerProtectionImpl implements ContainerProtection {

    /**
     * Creates a new protection for the protected block.
     *
     * @param block
     *            The block that is protected.
     * @param signFinder
     *            The sign finder.
     * @return The protection.
     */
    static Protection fromBlock(Block block, SignFinder signFinder) {
        return new ContainerProtectionImpl(block, signFinder);
    }

    /**
     * Creates a new protection for the protection block. Calling this method
     * will make for a faster {@link #getOwner()}, as it can simply read the
     * first entry on the main sign.
     *
     * @param block
     *            The block that is protected.
     * @param signFinder
     *            The sign finder.
     * @param mainSign
     *            The main sign, used for {@link #getOwner()}.
     * @return The protection.
     */
    static Protection fromBlockWithMainSign(Block block, SignFinder signFinder,
            Sign mainSign) {
        ContainerProtectionImpl protection = new ContainerProtectionImpl(block,
                signFinder);
        protection.mainSign = Optional.of(mainSign);
        return protection;
    }

    /**
     * Creates a new protection for the protection block. Calling this method
     * will make for a faster {@link #getAllowed()} and {@link #getOwner()}
     *
     * @param block
     * @param signFinder
     * @param signs
     * @return
     */
    static Protection fromBlockWithSigns(Block block, SignFinder signFinder,
            Collection<Sign> signs) {
        ContainerProtectionImpl protection = new ContainerProtectionImpl(block,
                signFinder);
        protection.fetchOwnerAndAllowed(signs);
        return protection;
    }

    private Optional<Collection<Profile>> allAllowed = Optional.absent();
    private final Block block;

    private Optional<Sign> mainSign = Optional.absent();
    private Optional<Profile> owner = Optional.absent();
    private final SignFinder signFinder;

    private ContainerProtectionImpl(Block block, SignFinder signFinder) {
        this.block = block;
        this.signFinder = signFinder;
    }

    private void fetchOwner() {
        if (!mainSign.isPresent()) {
            // Need information about the main sign
            fetchOwnerAndAllowed(fetchSigns());
        }

        // We have a hint, grab the first name on it
        Sign mainSign = this.mainSign.get();
        Collection<Profile> profiles = new ArrayList<Profile>(4);
        parseSign(mainSign, profiles);

        Iterator<Profile> iterator = profiles.iterator();
        if (iterator.hasNext()) {
            owner = Optional.of(iterator.next());
        }

    }

    private void fetchOwnerAndAllowed(Collection<Sign> signs) {
        Collection<Profile> allAllowed = Lists.newArrayList();
        for (Sign sign : signs) {
            parseSign(sign, allAllowed);
        }
        this.allAllowed = Optional.of(allAllowed);
    }

    private Collection<Sign> fetchSigns() {
        return signFinder.findAttachedSigns(block);
    }

    @Override
    public Collection<Profile> getAllowed() {
        if (allAllowed.isPresent()) {
            return allAllowed.get();
        }
        fetchOwnerAndAllowed(fetchSigns());
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

    private void parseSign(Sign sign, Collection<Profile> addTo) {
        Optional<SignType> type = signFinder.getSignParser().getSignType(
                sign.getLine(0));
        if (type.isPresent() && type.get().isMainSign()) {
            mainSign = Optional.of(sign);
        }
        signFinder.getSignParser().parseSign(sign, addTo);
    }

}
