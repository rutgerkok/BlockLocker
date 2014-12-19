package nl.rutgerkok.chestsignprotect;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class Permissions {
    public static final Permission CAN_PROTECT = new Permission("chestsignprotect.protect");

    private static final String GROUP_PREFIX = "chestsignprotect.group.";

    /**
     * Gets the permission node that a player needs to have to be considered
     * part of a group.
     * 
     * @param groupName
     *            The name of the group.
     * @return The permission node.
     */
    public static Permission getGroupNode(String groupName) {
        return new Permission(GROUP_PREFIX + groupName.toLowerCase(), PermissionDefault.FALSE);
    }

    private Permissions() {
        // No instances!
    }
}
