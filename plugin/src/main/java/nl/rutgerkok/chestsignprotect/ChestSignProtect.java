package nl.rutgerkok.chestsignprotect;

import nl.rutgerkok.chestsignprotect.protection.Protection;

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
     * Logs a message with severe importance, along with a stack trace.
     *
     * @param string
     *            The message to log.
     * @param t
     *            The exception that provides a stacktrace.
     */
    void severe(String string, Throwable t);
}
