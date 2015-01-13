package nl.rutgerkok.blocklocker.impl;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import nl.rutgerkok.blocklocker.ProtectionType;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

final class Config {
    private final static class Key {
        private final static String
                LANGUAGE_FILE = "languageFile",
                PROTECTABLE_CONTAINERS = "protectableContainers",
                PROTECTABLE_DOORS = "protectableDoors",
                PROTECTABLE_TRAP_DOORS = "protectableTrapDoors",
                DEFAULT_DOOR_OPEN_SECONDS = "defaultDoorOpenSeconds";
    }

    static final String DEFAULT_TRANSLATIONS_FILE = "translations-en.yml";

    private final int defaultDoorOpenSeconds;
    private final String languageFile;
    private final Logger logger;

    private final Map<ProtectionType, Set<Material>> protectableMaterialsMap;
    /**
     * Combination of the sets of all individual protection types.
     */
    private final Set<Material> protectableMaterialsSet;

    Config(Logger logger, FileConfiguration config) {
        this.logger = logger;

        languageFile = config.getString(Key.LANGUAGE_FILE,
                DEFAULT_TRANSLATIONS_FILE);
        defaultDoorOpenSeconds = config.getInt(Key.DEFAULT_DOOR_OPEN_SECONDS, 0);

        protectableMaterialsMap = new EnumMap<ProtectionType, Set<Material>>(ProtectionType.class);
        protectableMaterialsMap.put(ProtectionType.CONTAINER,
                toMaterialSet(config.getStringList(Key.PROTECTABLE_CONTAINERS)));
        protectableMaterialsMap.put(ProtectionType.DOOR,
                toMaterialSet(config.getStringList(Key.PROTECTABLE_DOORS)));
        protectableMaterialsMap.put(ProtectionType.TRAP_DOOR,
                toMaterialSet(config.getStringList(Key.PROTECTABLE_TRAP_DOORS)));

        // Create combined set
        protectableMaterialsSet = EnumSet.noneOf(Material.class);
        for (Set<Material> protectableByType : protectableMaterialsMap.values()) {
            protectableMaterialsSet.addAll(protectableByType);
        }
    }

    /**
     * Gets the default amount of ticks a door stays open before automatically
     * closing. 0 or negative values will make the door never close
     * automatically.
     * 
     * @return The default amount of ticks.
     */
    int getDefaultDoorOpenSeconds() {
        return defaultDoorOpenSeconds;
    }

    /**
     * Gets the file name of the selected language.
     *
     * @return The language.
     */
    String getLanguageFileName() {
        return languageFile;
    }

    /**
     * Gets whether the material can be protected by the given type.
     * @param type
     *            Protection type that must be checked for being able to protect
     *            this type.
     * @param material
     *            Material to check.
     * 
     * @return True if the material can be protected by the given type, false
     *         otherwise.
     */
    boolean canProtect(ProtectionType type, Material material) {
        Set<Material> materials = this.protectableMaterialsMap.get(type);
        if (materials == null) {
            return false;
        }
        return materials.contains(material);
    }

    /**
     * Gets whether the material can be protected by any type.
     *
     * @param material
     *            Material to check.
     * @return True if the material can be protected by the given type, false
     *         otherwise.
     */
    boolean canProtect(Material material) {
        return protectableMaterialsSet.contains(material);
    }

    /**
     * Transforms the string collection into a material set, by parsing each
     * string using {@link Material#matchMaterial(String)}. The resulting set
     * will be mutable. All strings that cannot be parsed are logged and then
     * ignored.
     *
     * @param strings
     *            The string collection.
     * @return The material set.
     */
    private Set<Material> toMaterialSet(Collection<String> strings) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        for (String string : strings) {
            Material material = Material.matchMaterial(string);
            if (material == null) {
                logger.warning("Cannot recognize material " + string + ", ignoring it");
                continue;
            }
            materials.add(material);
        }
        return materials;
    }
}
