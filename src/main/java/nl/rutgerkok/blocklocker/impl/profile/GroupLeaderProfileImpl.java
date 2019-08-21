package nl.rutgerkok.blocklocker.impl.profile;

import java.util.Date;
import java.util.UUID;

import nl.rutgerkok.blocklocker.group.GroupSystem;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Implementation of {@link Profile}. Players are considered part of a group
 * when the {@link GroupSystem#isGroupLeader(Player, String)} method returns
 * true.
 *
 */
class GroupLeaderProfileImpl implements Profile {

    static final String GROUP_LEADER_KEY = "l";
    private String groupName;
    private final GroupSystem groupSystem;

    GroupLeaderProfileImpl(GroupSystem groupSystem, String groupName) {
        this.groupSystem = Preconditions.checkNotNull(groupSystem);
        this.groupName = Preconditions.checkNotNull(groupName);
    }

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

        GroupLeaderProfileImpl otherProfile = (GroupLeaderProfileImpl) other;
        return groupName.equalsIgnoreCase(otherProfile.groupName);
    }

    @Override
    public String getDisplayName() {
        return "+" + groupName + "+";
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getSaveObject() {
        JSONObject object = new JSONObject();
        object.put(GROUP_LEADER_KEY, groupName);
        return object;
    }

    @Override
    public int hashCode() {
        // Bits are inverted to avoid hash code collision with {@link
        // GroupSystem}.
        return ~groupName.toLowerCase().hashCode();
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

        Player player = Bukkit.getPlayer(uuid.get());
        if (player == null) {
            return false;
        }

        return groupSystem.isGroupLeader(player, groupName);
    }

    @Override
    public boolean isExpired(Date cutoffDate) {
        // Group leader profiles never expire
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + groupName + "]";
    }

}
