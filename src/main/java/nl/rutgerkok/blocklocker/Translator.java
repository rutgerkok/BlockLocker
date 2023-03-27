package nl.rutgerkok.blocklocker;

import java.util.List;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Collection of translations.
 *
 */
public abstract class Translator {

    public enum Translation {
        COMMAND_CANNOT_BE_USED_BY_CONSOLE,
        COMMAND_CANNOT_EDIT_OWNER,
        COMMAND_LINE_NUMBER_OUT_OF_BOUNDS,
        COMMAND_NO_PERMISSION,
        COMMAND_NO_SIGN_SELECTED,
        COMMAND_PLAYER_NAME_TOO_LONG,
        COMMAND_PLUGIN_RELOADED,
        COMMAND_SIGN_NO_LONGER_PART_OF_PROTECTION,
        COMMAND_UPDATED_SIGN,
        PROTECTION_ADD_MORE_USERS_SIGN_INSTEAD,
        PROTECTION_BYPASSED,
        PROTECTION_CANNOT_CHANGE_SIGN,
        PROTECTION_CHEST_HINT,
        PROTECTION_CLAIMED_CONTAINER,
        PROTECTION_CLAIMED_MANUALLY,
        PROTECTION_EXPIRED,
        PROTECTION_IN_WILDERNESS,
        PROTECTION_IS_CLAIMED_BY,
        PROTECTION_NO_ACCESS,
        PROTECTION_NO_PERMISSION_FOR_CLAIM,
        PROTECTION_NOT_NEARBY,
        TAG_EVERYONE,
        TAG_MORE_USERS,
        TAG_PRIVATE,
        TAG_REDSTONE,
        TAG_TIMER,
        UPDATER_MORE_INFORMATION,
        UPDATER_UNSUPPORTED_SERVER,
        UPDATER_UPDATE_AVAILABLE;

        /**
         * Gets the key used in configuration files.
         */
        @Override
        public String toString() {
            return name().replaceFirst("_", ".").toLowerCase(Locale.ROOT);
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
    public abstract String get(Translation key);

    /**
     * Returns a list of all possible translations.
     *
     * @param key
     * 			The key of the translation.
     * @return A list of all possible translations, or the key (in a list) if not found.
     */
    public abstract List<String> getAll(Translation key);

    /**
     * Same as {@link #getAll(Translation)}, but with
     * {@link ChatColor#stripColor(String)} applied.
     *
     * @param key
     * 			The key of the translation.
     * @return A list of all possible translations, or the key (in a list) if not found.
     */
    public abstract List<String> getAllWithoutColor(Translation key);

    /**
     * Same as {@link #get(Translation)}, but with
     * {@link ChatColor#stripColor(String)} applied.
     *
     * @param key
     *            The key of the translation.
     * @return The translation, or the key if not found.
     */
    public abstract String getWithoutColor(Translation key);

    /**
     * Sends the specified message translated to the given player.
     *
     * @param player
     *            The player (or console) to the send the message to.
     * @param translation
     *            The message to send.
     */
    public abstract void sendMessage(CommandSender player, Translation translation);

    /**
     * Sends the specified message translated to the given player.
     *
     * @param player
     *            The player (or console) to the send the message to.
     * @param translation
     *            The message to send.
     * @param parameters
     *            Replacements for the message. {0} will be replaced by the
     *            first parameter, etc.
     */
    public final void sendMessage(CommandSender player, Translation translation, String... parameters) {
        String translated = get(translation);
        for (int i = 0; i < parameters.length; i++) {
            translated = translated.replace("{" + i + "}", parameters[i]);
        }
        player.sendMessage(translated);
    }
}
