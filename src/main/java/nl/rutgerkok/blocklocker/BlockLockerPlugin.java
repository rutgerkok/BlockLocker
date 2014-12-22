package nl.rutgerkok.blocklocker;

import java.util.logging.Logger;

import nl.rutgerkok.blocklocker.protection.Protection;

/**
 * Main entry point of the plugin.
 *
 */
public interface BlockLockerPlugin {

    /**
     * Fixes the missing uuids in the given protection, by looking them up
     * online.
     *
     * @param protection
     *            The protection to fix.
     */
    void fixMissingUniqueIds(Protection protection);

    /**
     * Gets the {@link ChestSettings} object, containing all customizable
     * aspects of the plugin.
     * 
     * @return The settings object.
     */
    ChestSettings getChestSettings();

    /**
     * Gets the logger of the plugin.
     * 
     * @return The logger.
     */
    Logger getLogger();

    /**
     * Gets the profile factory, used to create profiles.
     *
     * @return The profile factory.
     */
    ProfileFactory getProfileFactory();

    /**
     * Gets the protection finder, used to find protections in the world.
     *
     * @return The protection finder.
     */
    ProtectionFinder getProtectionFinder();

    /**
     * Gets the {@link SignParser} object.
     * 
     * @return The sign parser.
     */
    SignParser getSignParser();

    /**
     * Gets the sign selector, that holds the currently selected signs of all
     * players.
     * 
     * @return The sign selector.
     */
    SignSelector getSignSelector();

    /**
     * Gets the translator, used to translate messages.
     *
     * @return The translator.
     */
    Translator getTranslator();

    /**
     * Reloads the configuration files of the plugin.
     */
    void reload();

    /**
     * Runs a task the next tick on the server thread.
     *
     * @param runnable
     *            The task.
     */
    void runLater(Runnable runnable);

    /**
     * Logs a message with severe importance, along with a stack trace.
     *
     * @param string
     *            The message to log.
     * @param t
     *            The exception that provides a stacktrace.
     */
    void severe(String string, Throwable t);
}
