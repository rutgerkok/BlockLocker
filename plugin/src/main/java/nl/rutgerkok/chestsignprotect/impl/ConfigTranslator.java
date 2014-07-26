package nl.rutgerkok.chestsignprotect.impl;

import java.util.EnumMap;
import java.util.Map;

import nl.rutgerkok.chestsignprotect.Translator;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

class ConfigTranslator implements Translator {

    private final Map<Translation, String> translations;

    ConfigTranslator(ConfigurationSection config) {
        translations = new EnumMap<Translation, String>(Translation.class);
        for (Translation translation : Translation.values()) {
            String value = config.getString(translation.toString(),
                    translation.toString());
            value = ChatColor.translateAlternateColorCodes('&', value.trim());
            translations.put(translation, value);
        }
    }

    @Override
    public String get(Translation key) {
        // Note that the contract specifies that the key must be returned on
        // value, and from this implementation it looks like null is returned.
        // This is not the case, as *all* possible Translation keys have an
        // entry in the map: if a value is missing in the config file, the
        // constructor assigns the default value
        return translations.get(key);
    }

    @Override
    public String getWithoutColor(Translation key) {
        return ChatColor.stripColor(get(key));
    }

}
