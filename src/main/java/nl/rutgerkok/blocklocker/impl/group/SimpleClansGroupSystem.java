package nl.rutgerkok.blocklocker.impl.group;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import nl.rutgerkok.blocklocker.group.GroupSystem;

/**
 * Group system hooking into the SimpleClans plugin.
 *
 */
public final class SimpleClansGroupSystem extends GroupSystem {

    /**
     * Tests if the SimpleClans plugin is installed.
     *
     * @return True if the SimpleClans plugin is installed, false otherwise.
     */
    public static boolean isAvailable() {
        try {
            JavaPlugin.getProvidingPlugin(ClanPlayer.class);
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public boolean isGroupLeader(Player player, String groupName) {
        SimpleClans simpleClans = JavaPlugin.getPlugin(SimpleClans.class);
        ClanPlayer clanPlayer = simpleClans.getClanManager().getClanPlayer(player.getUniqueId());
        if (!clanPlayer.isLeader()) {
            return false;
        }
        Clan clan = clanPlayer.getClan();
        if (clan == null) {
            return false;
        }
        return clan.getName().equalsIgnoreCase(groupName);
    }

    @Override
    public boolean isInGroup(Player player, String groupName) {
        SimpleClans simpleClans = JavaPlugin.getPlugin(SimpleClans.class);
        ClanPlayer clanPlayer = simpleClans.getClanManager().getClanPlayer(player.getUniqueId());
        if (clanPlayer == null) {
            return false;
        }
        return clanPlayer.getClan().getName().equalsIgnoreCase(groupName);
    }

    @Override
    public boolean keepOnReload() {
        // BlockLocker will re-add the group system
        return false;
    }

}
