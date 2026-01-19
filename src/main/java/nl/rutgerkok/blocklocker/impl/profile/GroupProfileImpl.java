package nl.rutgerkok.blocklocker.impl.profile;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import nl.rutgerkok.blocklocker.SecretSignEntry;
import nl.rutgerkok.blocklocker.group.GroupSystem;
import nl.rutgerkok.blocklocker.profile.GroupProfile;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Implementation of {@link GroupProfile}. Players are considered part of a group when the name of
 * their scoreboard team matches this group, or when they have the permission node for this group.
 */
class GroupProfileImpl implements GroupProfile {

  static final String GROUP_KEY = "g";
  private String groupName;
  private final GroupSystem groupSystem;

  GroupProfileImpl(GroupSystem groupSystem, String groupName) {
    this.groupSystem = Preconditions.checkNotNull(groupSystem);
    this.groupName = Preconditions.checkNotNull(groupName);
  }

  /**
   * We compare uuids or names. Objects are equal if the uuids are present in both objects and are
   * equal, or if the uuids are present in neither objects and are not equal.
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

  @Override
  public void getSaveObject(SecretSignEntry entry) {
    entry.setString(GROUP_KEY, groupName);
  }

  @Override
  public int hashCode() {
    return groupName.toLowerCase(Locale.ROOT).hashCode();
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

    return groupSystem.isInGroup(player, groupName);
  }

  @Override
  public boolean isExpired(Date cutoffDate) {
    // Group profiles never expire
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[name=" + groupName + "]";
  }
}
