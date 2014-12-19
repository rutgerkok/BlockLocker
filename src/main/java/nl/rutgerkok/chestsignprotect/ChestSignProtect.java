package nl.rutgerkok.chestsignprotect;

import nl.rutgerkok.chestsignprotect.protection.Protection;

/**
 * Main entry point of the plugin.
 *
 */
public interface ChestSignProtect {

    /**
     * Fixes the missing uuids in the given protection, by looking them up
     * online.
     *
     * @param protection
     *            The protection to fix.
     */
    void fixMissingUniqueIds(Protection protection);

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
     * Gets the translator, used to translate messages.
     *
     * @return The translator.
     */
    Translator getTranslator();

    /**
     * Gets the {@link ChestSettings} object, containing all customizable
     * aspects of the plugin.
     * 
     * @return The settings object.
     */
    ChestSettings getChestSettings();

    /**
     * Gets the {@link SignParser} object.
     * 
     * @return The sign parser.
     */
    SignParser getSignParser();

    /**
     * Logs a message with severe importance, along with a stack trace.
     *
     * @param string
     *            The message to log.
     * @param t
     *            The exception that provides a stacktrace.
     */
    void severe(String string, Throwable t);

    /**
     * Runs a task the next tick on the server thread.
     *
     * @param runnable
     *            The task.
     */
    void runLater(Runnable runnable);
}
