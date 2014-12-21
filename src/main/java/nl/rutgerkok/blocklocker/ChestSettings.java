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
     * Gets the localized header for the given sign type, includes brackets and
     * colors.
     *
     * @param signType
     *            The type of the sign.
     * @return The header.
     */
    String getFancyLocalizedHeader(SignType signType);

    /**
     * Gets the localized header for the given sign type, without colors.
     * 
     * @param signType
     *            The type of the sign.
     * @return The header.
     */
    String getSimpleLocalizedHeader(SignType signType);

    /**
     * Gets the type of the protection.
     *
     * @param material
     *            Material of the protection.
     * @return Type of the protection.
     */
    Optional<ProtectionType> getProtectionType(Material material);

}
