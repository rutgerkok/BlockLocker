package nl.rutgerkok.blocklocker.impl.group;

import nl.rutgerkok.blocklocker.group.GroupSystem;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Throwables;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

/**
 * Group system hooking into the Factions plugin by MassiveCraft.
 *
 */
public final class TownyGroupSystem extends GroupSystem {

    /**
     * Tests if the Towny plugin is installed.
     * 
     * @return True if the factions plugin is installed, false otherwise.
     */
    public static boolean isAvailable() {
        try {
            JavaPlugin.getProvidingPlugin(Towny.class);
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public boolean isInGroup(Player player, String groupName) {
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
            Town town = resident.getTown();
            if (town.getName().equalsIgnoreCase(groupName)) {
                return true;
            }

            Nation nation = town.getNation();
            if (nation.getName().equalsIgnoreCase(groupName)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            // Cannot use catch (NotRegisteredException e) because the class
            // cannot be loaded then when Towny isn't present
            if (e instanceof NotRegisteredException) {
                return false;
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean isGroupLeader(Player player, String groupName) {
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
            Town town = resident.getTown();
            if (town.getName().equalsIgnoreCase(groupName)) {
                if (town.isMayor(resident) || town.hasAssistant(resident)) {
                    return true;
                }
            }

            Nation nation = town.getNation();
            if (nation.getName().equalsIgnoreCase(groupName)) {
                if (nation.isKing(resident) || nation.hasAssistant(resident)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            // Cannot use catch (NotRegisteredException e) because the class
            // cannot be loaded then when Towny isn't present
            if (e instanceof NotRegisteredException) {
                return false;
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean keepOnReload() {
        // BlockLocker will re-add the group system
        return false;
    }

}
