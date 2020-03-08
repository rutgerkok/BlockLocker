package nl.rutgerkok.blocklocker.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import nl.rutgerkok.blocklocker.Translator;

/**
 * Provides translations from a configuration file.
 *
 */
class ConfigTranslator extends Translator {

    /**
     * Little class that contains multiple translated values. This class extends a
     * translationValue which is the primary value, it contains a list with all the
     * other possible values.
     *
     */
    private static class MultiTranslationValue extends TranslationValue {
        private final List<TranslationValue> aliases;

        private MultiTranslationValue(List<String> values) {
            super(values.get(0));
            this.aliases = new ArrayList<>();

            for (String alias : values.subList(1, values.size())) {
                this.aliases.add(new TranslationValue(alias));
            }
        }

        @Override
        public List<TranslationValue> getAll() {
            List<TranslationValue> all = new ArrayList<>();

            all.add(this);

            this.aliases.forEach(value -> all.add(value));

            return all;
        }
    }

    /**
     * Little class to hold the different representations of the translated values.
     * The original ({@code &1foo}), the colored ({@code §1foo}) and uncolored
     * ({@code foo}).
     *
     */
    private static class TranslationValue {
        private final String original;
        private final String uncolored;
        private final String colored;

        private TranslationValue(String original) {
            this.original = original.trim();
            this.colored = ChatColor.translateAlternateColorCodes('&', original);
            this.uncolored = ChatColor.stripColor(colored);
        }

        public List<TranslationValue> getAll() {
            return Arrays.asList(this);
        }
    }

    private boolean needsSave = false;

    private final Map<Translation, TranslationValue> translations;

    ConfigTranslator(ConfigurationSection config) {
        translations = new EnumMap<>(Translation.class);
        for (Translation translation : Translation.values()) {
            String key = translation.toString();

            if (!config.contains(key, true)) {
                // Not overridden, so no value in config file
                needsSave = true;
            }

            // Get and store the value
            Object value = config.get(key);
            if (value instanceof String) {
                translations.put(translation, new TranslationValue((String) value));
            } else if (isStringList(value)) {
                @SuppressWarnings("unchecked") // Checked by line above
                List<String> stringList = (List<String>) value;
                translations.put(translation, new MultiTranslationValue(stringList));
            } else {
                // Make sure there are no null entries
                translations.put(translation, new TranslationValue("~~TODO translate " + key + "~~"));
            }
        }
    }

    @Override
    public String get(Translation key) {
        // Note that the contract specifies that the key must be returned on
        // value, and from this implementation it looks like null is returned.
        // This is not the case, as *all* possible Translation keys have an
        // entry in the map: if a value is missing in the config file, the
        // constructor assigns the default value
        return translations.get(key).colored;
    }

    @Override
    public List<String> getAll(Translation key) {
        List<String> all = new ArrayList<>();

        translations.get(key).getAll().forEach(value -> all.add(value.colored));

        return all;
    }

    @Override
    public List<String> getAllWithoutColor(Translation key) {
        List<String> all = new ArrayList<>();

        translations.get(key).getAll().forEach(value -> all.add(value.uncolored));

        return all;
    }

    @Override
    public String getWithoutColor(Translation key) {
        return translations.get(key).uncolored;
    }

    /**
     * Checks if all values in the list are strings. Returns false if not a list or
     * if the list contains something else.
     *
     * @param value
     *            The list.
     * @return True if value is a list and contains only strings.
     */
    private boolean isStringList(Object value) {
        if (!(value instanceof List<?>)) {
            return false;
        }
        List<?> list = (List<?>) value;
        for (Object element : list) {
            if (!(element instanceof String)) {
                return false;
            }
        }
        return true;
    }

    boolean needsSave() {
        return needsSave;
    }

    /**
     * Saves the translations to a file.
     *
     * @param file
     *            The file to save to.
     * @throws IOException
     *             If an IO error occurs.
     */
    void save(File file) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        for (Entry<Translation, TranslationValue> translationEntry : translations.entrySet()) {
            String key = translationEntry.getKey().toString();
            List<TranslationValue> values = translationEntry.getValue().getAll();
            if (values.size() == 1) {
                config.set(key, translationEntry.getValue().original);
            } else {
                List<String> stringValues = values.stream()
                        .map(value -> value.original)
                        .collect(Collectors.toList());
                config.set(key, stringValues);
            }
        }
        config.save(file);
        needsSave = false;
    }

    @Override
    public void sendMessage(CommandSender player, Translation translation) {
        player.sendMessage(get(translation));
    }

}
