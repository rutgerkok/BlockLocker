package nl.rutgerkok.blocklocker;

import org.bukkit.Material;

import com.google.common.base.Optional;

public interface ChestSettings {

    /**
     * The different types of protections.
     *
     */
    public enum ProtectionType {
        CONTAINER,
        DOOR;
    }

    /**
     * Checks if the given block can be protected by the given type.
     *
     * @param type
     *            The protection type.
     * @param material
     *            The material to check.
     * @return True if the protection can protect the given material, false
     *         otherwise.
     */
    boolean canProtect(ProtectionType type, Material material);

    /**
     * Gets the default amount of ticks a door stays open before closing
     * automatically. When set to less than 1, the door is never closed
     * automatically. Players can override this value for a door.
     * 
     * @return The amount.
     */
    int getDefaultDoorOpenSeconds();

    /**
     * Gets the localized header for the given sign type, includes brackets and
     * colors.
     *
     * @param signType
     *            The type of the sign.
     * @return The header.
     */
    String getFancyLocalizedHeader(SignType signType);

    /**
     * Gets the type of the protection.
     *
     * @param material
     *            Material of the protection.
     * @return Type of the protection.
     */
    Optional<ProtectionType> getProtectionType(Material material);

    /**
     * Gets the localized header for the given sign type, without colors.
     * 
     * @param signType
     *            The type of the sign.
     * @return The header.
     */
    String getSimpleLocalizedHeader(SignType signType);

}
