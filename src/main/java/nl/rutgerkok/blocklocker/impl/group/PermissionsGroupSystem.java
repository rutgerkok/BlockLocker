package nl.rutgerkok.blocklocker.impl.group;

import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.group.GroupSystem;
import org.bukkit.entity.Player;

/** Considers players with the right group permission node part of a group. */
public final class PermissionsGroupSystem extends GroupSystem {

  @Override
  public boolean isInGroup(Player player, String groupName) {
    return player.hasPermission(Permissions.getGroupNode(groupName));
  }

  @Override
  public boolean keepOnReload() {
    // BlockLocker will re-add the group system
    return false;
  }
}
