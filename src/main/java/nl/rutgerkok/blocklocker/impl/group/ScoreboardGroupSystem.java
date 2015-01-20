package nl.rutgerkok.blocklocker.impl.group;

import nl.rutgerkok.blocklocker.group.GroupSystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Looks at the teams on the main scoreboard of the server.
 *
 */
public final class ScoreboardGroupSystem extends GroupSystem {

    @Override
    public boolean isInGroup(Player player, String groupName) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = mainScoreboard.getPlayerTeam(player);
        if (team == null) {
            return false;
        }
        return team.getName().equalsIgnoreCase(groupName);
    }

    @Override
    public boolean keepOnReload() {
        // BlockLocker will re-add the group system
        return false;
    }

}
