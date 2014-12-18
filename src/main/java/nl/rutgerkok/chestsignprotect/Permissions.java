package nl.rutgerkok.chestsignprotect;

import org.bukkit.permissions.Permission;

public final class Permissions {
    private Permissions() {
        // No instances!
    }

    public static final Permission CAN_PROTECT = new Permission("chestsignprotect.protect");
}
