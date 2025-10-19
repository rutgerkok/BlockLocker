package nl.rutgerkok.blocklocker.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import nl.rutgerkok.blocklocker.AttackType;
import nl.rutgerkok.blocklocker.ProtectionType;
import nl.rutgerkok.blocklocker.impl.updater.UpdatePreference;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

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
                ALLOW_DESTROY_BY = "allowDestroyBy",
                CONFIG_VERSION = "configVersion";
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

    Config(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        logger = plugin.getLogger();

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
        protectableMaterialsSet = new HashSet<>();
        for (Set<Material> protectableByType : protectableMaterialsMap.values()) {
            protectableMaterialsSet.addAll(protectableByType);
        }

        // Config upgrades
        int version = config.getInt(Key.CONFIG_VERSION, 1);
        if (version < 2) {
            logger.info("Upgrading configuration...");
            // We load the default configuration, apply our settings, and then save it
            try (InputStream configStream = Objects.requireNonNull(plugin.getResource("config.yml"))) {
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream, StandardCharsets.UTF_8));
                writeToConfig(config);
                config.save(new File(plugin.getDataFolder(), "config.yml"));
                plugin.reloadConfig();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to read default config", e);
            }
        }
    }

    /**
     * Writes out all the current settings to a configuration.
     * @param config The config to write to.
     */
    private void writeToConfig(FileConfiguration config) {
        config.set(Key.LANGUAGE_FILE, this.languageFile);
        config.set(Key.DEFAULT_DOOR_OPEN_SECONDS, this.defaultDoorOpenSeconds);
        config.set(Key.UPDATER, this.updatePreference.toString());
        config.set(Key.CONNECT_CONTAINERS, this.connectContainers);
        config.set(Key.AUTO_EXPIRE_DAYS, this.autoExpireDays);
        config.set(Key.ALLOW_DESTROY_BY, this.allowDestroyBy.stream().map(AttackType::toString).toList());
        config.set(Key.PROTECTABLE_CONTAINERS, writeMaterialSet(protectableMaterialsMap.get(ProtectionType.CONTAINER)));
        config.set(Key.PROTECTABLE_DOORS, writeMaterialSet(protectableMaterialsMap.get(ProtectionType.DOOR)));
        config.set(Key.PROTECTABLE_ATTACHABLES, writeMaterialSet(protectableMaterialsMap.get(ProtectionType.ATTACHABLE)));
    }

    /**
     * Writes a material set to a string list, suitable for the configuration.
     * @param materials The materials.
     * @return The material list. Will be empty if {@code materials} is null.
     */
    private List<String> writeMaterialSet(@Nullable Set<Material> materials) {
        if (materials == null) {
            return Collections.emptyList();
        }
        return materials.stream().map(mat -> mat.getKey().toString()).toList();
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
     * @param block
     *            Block to check.
     * @return True if the material can be protected by the given type, false
     *         otherwise.
     */
    boolean canProtect(Block block) {
        return protectableMaterialsSet.contains(block.getType());
    }

    /**
     * Gets whether the material can be protected by the given type.
     *
     * @param type
     *            Protection type that must be checked for being able to protect
     *            this type.
     * @param block
     *            Block to check.
     *
     * @return True if the material can be protected by the given type, false
     *         otherwise.
     */
    boolean canProtect(ProtectionType type, Block block) {
        Set<Material> materials = this.protectableMaterialsMap.get(type);
        if (materials == null) {
            return false;
        }
        return materials.contains(block.getType());
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
                materials.add(AttackType.valueOf(string.toUpperCase(Locale.ROOT)));
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
        Set<Material> materials = new HashSet<>();
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
