package nl.rutgerkok.blocklocker.impl.group;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.api.PartyAPI;

import nl.rutgerkok.blocklocker.group.GroupSystem;

/**
 * Group system hooking into the mcMMO plugin.
 * 
 */

public final class mcMMOGroupSystem extends GroupSystem {

	/**
	 * Tests if the mcMMO plugin is installed.
	 * 
	 * @return True if the mcMMO plugin is installed, false otherwise.
	 */
	public static boolean isAvailable() {
		try {
			JavaPlugin.getProvidingPlugin(mcMMO.class);
			return true;
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}
	
	@Override
	public boolean isGroupLeader(Player player, String groupName) {
		if (!isInGroup(player, groupName)) {
			return false;
		}
		
		String leader = PartyAPI.getPartyLeader(groupName);
		
		return Objects.equals(player.getName(), leader) ? true : false;
	}
	
	@Override
	public boolean isInGroup(Player player, String groupName) {
		if (!PartyAPI.inParty(player)) {
			// Player is not in a party
			return false;
		}
		
		String partyName = PartyAPI.getPartyName(player);

		// Ignore case, mcMMO is not case sensitive
	    return partyName.equalsIgnoreCase(groupName);
	}
	
	@Override
    public boolean keepOnReload() {
        // BlockLocker will re-add the group system
        return false;
    }

}