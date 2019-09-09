package nl.rutgerkok.blocklocker.impl;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import nl.rutgerkok.blocklocker.AttackType;
import nl.rutgerkok.blocklocker.ProtectionType;
import nl.rutgerkok.blocklocker.impl.updater.UpdatePreference;

final class Config {
	
    private final static class Key {
        private final static String LANGUAGE_FILE = "languageFile",
                PROTECTABLE_CONTAINERS = "protectableContainers",
                PROTECTABLE_DOORS = "protectableDoors",
                PROTECTABLE_TRAP_DOORS = "protectableTrapDoors",
                PROTECTABLE_ATTACHABLES = "protectableAttachables",
                DEFAULT_DOOR_OPEN_SECONDS = "defaultDoorOpenSeconds",
                UPDATER = "updater",
                CONNECT_CONTAINERS = "connectContainers",
                AUTO_EXPIRE_DAYS = "autoExpireDays",
                ALLOW_DESTROY_BY = "allowDestroyBy";
    }

    static final String DEFAULT_TRANSLATIONS_FILE = "translations-en.yml";

    private final Set<AttackType> allowDestroyBy;
    private final int autoExpireDays;
    private final boolean connectContainers;
    private final int defaultDoorOpenSeconds;
    private final String languageFile;
    private final Logger logger;
    private final Map<ProtectionType, Set<Material>> protectableMaterialsMap;
    /**
     * Combination of the sets of all individual protection types.
     */
    private final Set<Material> protectableMaterialsSet;
    private final UpdatePreference updatePreference;

    Config(Logger logger, FileConfiguration config) {
        this.logger = logger;

        languageFile = config.getString(Key.LANGUAGE_FILE, DEFAULT_TRANSLATIONS_FILE);
        defaultDoorOpenSeconds = config.getInt(Key.DEFAULT_DOOR_OPEN_SECONDS, 0);
        updatePreference = readUpdatePreference(config.getString(Key.UPDATER));
        connectContainers = config.getBoolean(Key.CONNECT_CONTAINERS);
        autoExpireDays = config.getInt(Key.AUTO_EXPIRE_DAYS);
        allowDestroyBy = readAttackTypeSet(config.getStringList(Key.ALLOW_DESTROY_BY));

        // Materials
        protectableMaterialsMap = new EnumMap<>(ProtectionType.class);
        protectableMaterialsMap.put(ProtectionType.CONTAINER, readMaterialSet(config.getStringList(Key.PROTECTABLE_CONTAINERS)));
        protectableMaterialsMap.put(ProtectionType.DOOR, readMaterialSet(config.getStringList(Key.PROTECTABLE_DOORS)));
        if (config.contains(Key.PROTECTABLE_TRAP_DOORS)) {
            // Still support old name:
            protectableMaterialsMap.put(ProtectionType.ATTACHABLE,
                    readMaterialSet(config.getStringList(Key.PROTECTABLE_TRAP_DOORS)));
        } else {
            protectableMaterialsMap.put(ProtectionType.ATTACHABLE,
                    readMaterialSet(config.getStringList(Key.PROTECTABLE_ATTACHABLES)));
        }

        // Create combined set
        protectableMaterialsSet = EnumSet.noneOf(Material.class);
        for (Set<Material> protectableByType : protectableMaterialsMap.values()) {
            protectableMaterialsSet.addAll(protectableByType);
        }
    }

    /**
     * Gets whether the given attack type can destroy protections.
     *
     * @param attackType
     *            The attack type.
     * @return True if the attack type can destroy protections, false otherwise.
     */
    boolean allowDestroyBy(AttackType attackType) {
        return allowDestroyBy.contains(attackType);
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
     * Gets whether the material can be protected by the given type.
     *
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
     * Gets the amount of days that a chest owner has to be offline for a chest
     * to expire. 0 or negative means that chests never expire.
     *
     * @return The amount of days.
     */
    int getAutoExpireDays() {
        return autoExpireDays;
    }

    /**
     * Gets whether containers should be connected.
     *
     * @return True if containers should be connected, false otherwise.
     */
    boolean getConnectContainers() {
        return connectContainers;
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
     * Gets the update preference.
     *
     * @return The update preference.
     */
    UpdatePreference getUpdatePreference() {
        return updatePreference;
    }

    private Set<AttackType> readAttackTypeSet(List<String> strings) {
        Set<AttackType> materials = EnumSet.noneOf(AttackType.class);
        for (String string : strings) {
            try {
                materials.add(AttackType.valueOf(string.toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warning("Cannot recognize attack type " + string + ", ignoring it");
                continue;
            }
        }
        return materials;
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
    private Set<Material> readMaterialSet(Collection<String> strings) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        for (String string : strings) {
            Material material = Material.matchMaterial(string);
            if (material == null) {
                material = Material.matchMaterial(string, true);
            }
            if (material == null) {
                logger.warning("Cannot recognize material " + string + ", ignoring it");
                continue;
            }
            materials.add(material);
        }
        return materials;
    }

    private UpdatePreference readUpdatePreference(String string) {
        Optional<UpdatePreference> updatePreference = UpdatePreference.parse(string);
        if (updatePreference.isPresent()) {
            return updatePreference.get();
        } else {
            logger.warning("Unknown update setting: " + string + ". Disabling automatic updater.");
            return UpdatePreference.DISABLED;
        }
    }
}
