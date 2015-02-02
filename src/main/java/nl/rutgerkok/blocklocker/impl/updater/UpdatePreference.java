package nl.rutgerkok.blocklocker.impl.updater;

import com.google.common.base.Optional;

/**
 * The preference the user set for auto updating.
 *
 */
public enum UpdatePreference {
    /**
     * Automatically check for and install updates.
     */
    AUTO_INSTALL,
    /**
     * Don't check for updates, don't install updates.
     */
    DISABLED,
    /**
     * Check for updates, do nothing else
     */
    JUST_NOTIFY;

    /**
     * Parses the update preference from the string. The string is
     * {@link String#trim() trimmed}, uppercased and spaces are replaced with
     * underscores. Then {@link #valueOf(String)} is called, and the result is
     * returned as an {@link Optional}.
     * 
     * @param string
     *            String to parse.
     * @return The update preference, or absent if parsing failed.
     */
    public static Optional<UpdatePreference> parse(String string) {
        string = string.trim().toUpperCase().replace(' ', '_');
        try {
            return Optional.of(valueOf(string));
        } catch (IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    /**
     * Gets whether the plugin should check for new updates.
     *
     * @return True if the plugin should check for updates, false otherwise.
     */
    public boolean checkForUpdates() {
        return this == AUTO_INSTALL || this == JUST_NOTIFY;
    }

    /**
     * Gets whether updates should be downloaded and installed automatically.
     *
     * @return True if updates should be downloaded and installed automatically,
     *         false otherwise.
     */
    public boolean installUpdates() {
        return this == AUTO_INSTALL;
    }
}
