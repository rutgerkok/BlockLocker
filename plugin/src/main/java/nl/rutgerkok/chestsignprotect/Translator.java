package nl.rutgerkok.chestsignprotect;

import org.bukkit.ChatColor;

/**
 * Collection of translations.
 *
 */
public interface Translator {

    public enum Translation {
        TAG_EVERYONE, TAG_MORE_USERS, TAG_PLAYER_NOT_FOUND, TAG_PRIVATE;

        /**
         * Gets the key used in configuration files.
         */
        @Override
        public String toString() {
            return name().replaceFirst("_", ".").toLowerCase();
        }
    }

    /**
     * Returns the translation with the given key. If no such translation
     * exists, the key is returned.
     *
     * @param key
     *            The key of the translation.
     * @return The translation, or the key if not found.
     */
    String get(Translation key);

    /**
     * Same as {@link #get(Translation)}, but with
     * {@link ChatColor#stripColor(String)} applied.
     *
     * @param key
     *            The key of the translation.
     * @return The translation, or the key if not found.
     */
    String getWithoutColor(Translation key);
}
