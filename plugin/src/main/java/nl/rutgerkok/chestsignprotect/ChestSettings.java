package nl.rutgerkok.chestsignprotect;

import org.bukkit.Material;

import com.google.common.base.Optional;

public interface ChestSettings {

    /**
     * The different types of protections.
     *
     */
    public enum ProtectionType {
        CONTAINER;
    }

    /**
     * The different types of signs.
     *
     */
    public enum SignType {
        MORE_USERS, PRIVATE;

        /**
         * Gets whether this sign is a main sign: the sign type where exactly
         * one is required for each protection.
         *
         * @return True if this is a main sign, false otherwise.
         */
        public boolean isMainSign() {
            return this == PRIVATE;
        }
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
     * Gets the localized header for the given sign type, includes brackets.
     *
     * @param signType
     *            The type of the sign.
     * @return The header.
     */
    String getHeader(SignType signType);

    /**
     * Gets the type of the protection.
     *
     * @param material
     *            Material of the protection.
     * @return Type of the protection.
     */
    Optional<ProtectionType> getProtectionType(Material material);

}
