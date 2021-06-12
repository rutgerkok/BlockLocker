package nl.rutgerkok.blocklocker;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Used to store secret extra data on a sign. This class represents a single
 * entry, so it can for example store one player.
 *
 */
public interface SecretSignEntry {

    /**
     * Gets a boolean with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @return The value, if present.
     */
    Optional<Boolean> getBoolean(String key);

    /**
     * Gets an integer with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @return The value, if present.
     */
    OptionalInt getInteger(String key);

    /**
     * Gets a string with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @return The value, if present.
     */
    Optional<String> getString(String key);

    /**
     * Gets a unique id with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @return The value, if present.
     */
    Optional<UUID> getUniqueId(String key);

    /**
     * Sets a boolean with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @param value
     *            The value.
     */
    void setBoolean(String key, boolean value);

    /**
     * Sets an integer with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @param value
     *            The value.
     */
    void setInteger(String key, int value);

    /**
     * Sets a string with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @param value
     *            The value, may not be null.
     */
    void setString(String key, String value);

    /**
     * Sets a unique id with the given name.
     *
     * @param key
     *            The key, must only contain [a-z_].
     * @param value
     *            The value, may not be null.
     */
    void setUniqueId(String key, UUID value);
}
