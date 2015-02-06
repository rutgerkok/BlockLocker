package nl.rutgerkok.blocklocker.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

/**
 * Class that combines several group systems into one. The
 * {@link #isInGroup(Player, String)} method will return true if any of the
 * group systems returns true.
 *
 */
public final class CombinedGroupSystem extends GroupSystem {

    private List<GroupSystem> systems;

    /**
     * Constructs an initially empty combined group system.
     *
     * @see #addSystem(GroupSystem)
     */
    public CombinedGroupSystem() {
        this.systems = new ArrayList<GroupSystem>();
    }

    @Override
    public boolean isInGroup(Player player, String groupName) {
        for (GroupSystem system : systems) {
            if (system.isInGroup(player, groupName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isGroupLeader(Player player, String groupName) {
        for (GroupSystem system : systems) {
            if (system.isGroupLeader(player, groupName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new group system. After calling this method, the
     * {@link #isInGroup(Player, String)} method on this object is guaranteed to
     * return true if the {@link #isInGroup(Player, String)} on the given group
     * system returns true too. too.
     *
     * @param system
     *            The group system, may not be null.
     */
    public void addSystem(GroupSystem system) {
        this.systems.add(Preconditions.checkNotNull(system));
    }

    /**
     * Adds all group systems in the given collection. The collection may not
     * contain null elements.
     * 
     * @param systems
     *            The systems to add.
     */
    public void addSystems(Iterable<GroupSystem> systems) {
        for (GroupSystem system : systems) {
            addSystem(system);
        }
    }

    /**
     * Gets all groups that must be kept on reload.
     * 
     * @return All groups that must be kept.
     */
    public Collection<GroupSystem> getReloadSurvivors() {
        Collection<GroupSystem> reloadSurvivors = new ArrayList<GroupSystem>();
        for (GroupSystem system : this.systems) {
            if (system.keepOnReload()) {
                reloadSurvivors.add(system);
            }
        }
        return reloadSurvivors;
    }

    @Override
    public boolean keepOnReload() {
        // BlockLocker will re-add the group system
        return false;
    }
}
