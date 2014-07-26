package nl.rutgerkok.chestsignprotect.impl;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private final static class Key {
        private final static String LANGUAGE_FILE = "languageFile";
    }

    private final String languageFile;

    public Config(FileConfiguration config) {
        languageFile = config.getString(Key.LANGUAGE_FILE,
                "translations-en.yml");
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
