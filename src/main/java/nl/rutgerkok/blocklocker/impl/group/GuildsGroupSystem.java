package nl.rutgerkok.blocklocker.impl.group;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import nl.rutgerkok.blocklocker.group.GroupSystem;

/**
 * Group system hooking into the Guilds plugin.
 *
 */
public final class GuildsGroupSystem extends GroupSystem {

    /**
     * Tests if the Guilds plugin is installed.
     * 
     * @return True if the Guilds plugin is installed, false otherwise.
     */
    public static boolean isAvailable() {
        try {
            JavaPlugin.getProvidingPlugin(GuildsAPI.class);
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public boolean isGroupLeader(Player player, String groupName) {
        Guild guild = Guilds.getApi().getGuild(player);
        if (guild == null) {
            return false;
        }
        GuildMember master = guild.getGuildMaster();
        if (master == null) {
            // Group has no master, so player is not the group master
            return false;
        }
        if (!master.getUuid().equals(player.getUniqueId())) {
            // Player is not het group master
            return false;
        }
        // Player is the group master, but also check name
        return guild.getName().equalsIgnoreCase(groupName);
    }

    @Override
    public boolean isInGroup(Player player, String groupName) {
        Guild guild = Guilds.getApi().getGuild(player);
        if (guild == null) {
            return false;
        }
        return guild.getName().equalsIgnoreCase(groupName);
    }

    @Override
    public boolean keepOnReload() {
        // BlockLocker will re-add the group system
        return false;
    }

}
