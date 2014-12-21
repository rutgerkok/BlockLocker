package nl.rutgerkok.blocklocker.impl;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private final static class Key {
        private final static String LANGUAGE_FILE = "languageFile";
    }

    static final String DEFAULT_TRANSLATIONS_FILE = "translations-en.yml";

    private final String languageFile;

    public Config(FileConfiguration config) {
        languageFile = config.getString(Key.LANGUAGE_FILE,
                DEFAULT_TRANSLATIONS_FILE);
    }

    /**
     * Gets the file name of the selected language.
     *
     * @return The language.
     */
    public String getLanguageFileName() {
        return languageFile;
    }
}
