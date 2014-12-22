package nl.rutgerkok.blocklocker.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

import nl.rutgerkok.blocklocker.ChestSettings.ProtectionType;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

final class Config {
    private final static class Key {
        private final static String
                LANGUAGE_FILE = "languageFile",
                PROTECTABLE_CONTAINERS = "protectableContainers",
                PROTECTABLE_DOORS = "protectableDoors";
    }

    static final String DEFAULT_TRANSLATIONS_FILE = "translations-en.yml";

    private final String languageFile;
    private final Logger logger;

    private final Set<Material> protectableContainers;
    private final Set<Material> protectableDoors;

    Config(Logger logger, FileConfiguration config) {
        this.logger = logger;

        languageFile = config.getString(Key.LANGUAGE_FILE,
                DEFAULT_TRANSLATIONS_FILE);

        protectableContainers = toMaterialSet(config.getStringList(Key.PROTECTABLE_CONTAINERS));
        protectableDoors = toMaterialSet(config.getStringList(Key.PROTECTABLE_DOORS));
    }

    /**
     * Gets the file name of the selected language.
     *
     * @return The language.
     */
    public String getLanguageFileName() {
        return languageFile;
    }

    /**
     * Gets an immutable set of all materials that can be protected for the
     * given type.
     *
     * @return The set.
     */
    public Set<Material> getProtectables(ProtectionType type) {
        switch (type) {
            case CONTAINER:
                return protectableContainers;
            case DOOR:
                return protectableDoors;
            default:
                return Collections.emptySet();
        }
    }

    /**
     * Transforms the string collection into a material set, by parsing each
     * string using {@link Material#matchMaterial(String)}. The resulting set
     * will be immutable. All strings that cannot be parsed are logged and then
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
        return Collections.unmodifiableSet(materials);
    }
}
