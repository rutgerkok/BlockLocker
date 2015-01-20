package nl.rutgerkok.blocklocker.impl.group;

import nl.rutgerkok.blocklocker.group.GroupSystem;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

/**
 * Group system hooking into the Factions plugin by MassiveCraft.
 *
 */
public final class FactionsGroupSystem extends GroupSystem {

    /**
     * Tests if the Factions plugin is installed.
     * 
     * @return True if the factions plugin is installed, false otherwise.
     */
    public static boolean isAvailable() {
        try {
            JavaPlugin.getProvidingPlugin(MPlayer.class);
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public boolean isInGroup(Player player, String groupName) {
        MPlayer mPlayer = MPlayer.get(player);
        Faction faction = mPlayer.getFaction();
        return (faction != null && faction.getName().equalsIgnoreCase(groupName));
    }

    @Override
    public boolean keepOnReload() {
        // BlockLocker will re-add the group system
        return false;
    }

}
