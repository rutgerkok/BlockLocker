package nl.rutgerkok.blocklocker.location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Location checker returns false if any of the given location checkers returns
 * false.
 *
 */
public final class CombinedLocationChecker implements LocationChecker {

    private final List<LocationChecker> checkers = new ArrayList<>();

    /**
     * Adds a location checker.
     *
     * @param checker
     *            The location checker.
     */
    public void addChecker(LocationChecker checker) {
        this.checkers.add(Objects.requireNonNull(checker, "checker"));
    }

    @Override
    public void checkLocation(Player player, Block block) throws IllegalLocationException {
        for (LocationChecker checker : this.checkers) {
            checker.checkLocation(player, block);
        }
    }

    /**
     * Gets all groups that must be kept on reload.
     *
     * @return All groups that must be kept.
     */
    public Collection<LocationChecker> getReloadSurvivors() {
        Collection<LocationChecker> reloadSurvivors = new ArrayList<>();
        for (LocationChecker checker : this.checkers) {
            if (checker.keepOnReload()) {
                reloadSurvivors.add(checker);
            }
        }
        return reloadSurvivors;
    }

}
