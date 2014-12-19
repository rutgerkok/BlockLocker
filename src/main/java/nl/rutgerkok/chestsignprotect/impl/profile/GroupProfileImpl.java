package nl.rutgerkok.chestsignprotect.impl.profile;

import java.util.UUID;

import nl.rutgerkok.chestsignprotect.Permissions;
import nl.rutgerkok.chestsignprotect.profile.GroupProfile;
import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;
import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;

/**
 * Implementation of {@link GroupProfile}. Players are considered part of a
 * group when the name of their scoreboard team matches this group, or when they
 * have the permission node for this group.
 *
 */
class GroupProfileImpl implements GroupProfile {

    static final String GROUP_KEY = "g";
    private final String groupName;

    GroupProfileImpl(String groupName) {
        this.groupName = groupName;
    }

    /**
     * We compare uuids or names. Objects are equal if the uuids are present in
     * both objects and are equal, or if the uuids are present in neither
     * objects and are not equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        GroupProfileImpl otherProfile = (GroupProfileImpl) other;
        return groupName.equalsIgnoreCase(otherProfile.groupName);
    }

    @Override
    public String getDisplayName() {
        return "[" + groupName + "]";
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getSaveObject() {
        JSONObject object = new JSONObject();
        object.put(GROUP_KEY, groupName);
        return object;
    }

    @Override
    public int hashCode() {
        return groupName.toLowerCase().hashCode();
    }

    @Override
    public boolean includes(Profile other) {
        if (!(other instanceof PlayerProfile)) {
            return false;
        }

        PlayerProfile playerProfile = (PlayerProfile) other;
        Optional<UUID> uuid = playerProfile.getUniqueId();
        if (!uuid.isPresent()) {
            return false;
        }

        // Check for relevant permission node
        Player onlinePlayer = Bukkit.getPlayer(uuid.get());
        if (onlinePlayer != null && onlinePlayer.hasPermission(Permissions.getGroupNode(groupName))) {
            return true;
        }

        // Check for team
        OfflinePlayer player = (onlinePlayer == null) ? Bukkit.getOfflinePlayer(uuid.get()) : onlinePlayer;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team playerTeam = scoreboard.getPlayerTeam(player);
        if (playerTeam == null) {
            return false;
        }

        return playerTeam.getName().equalsIgnoreCase(this.groupName);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + groupName + "]";
    }

}
