package nl.rutgerkok.blocklocker;

import java.util.logging.Logger;

import org.bukkit.event.Event;

import nl.rutgerkok.blocklocker.group.CombinedGroupSystem;
import nl.rutgerkok.blocklocker.group.GroupSystem;
import nl.rutgerkok.blocklocker.location.CombinedLocationChecker;

/**
 * Main entry point of the plugin.
 *
 */
public interface BlockLockerPlugin {

	/**
	 * Calls the given event, allowing other plugins to react to this event.
	 *
	 * @param <E> The type of the event.
	 * @param event The event, may not be null.
	 * @return The same event, for chaining.
	 */
	<E extends Event> E callEvent(E event);

    /**
     * Gets the {@link ChestSettings} object, containing all customizable
     * aspects of the plugin.
     *
     * @return The settings object.
     */
    ChestSettings getChestSettings();

    /**
     * Gets the combined group system of the plugin, which can be used to add
     * other group systems.
     *
     * @return The combined group system.
     * @see CombinedGroupSystem#addSystem(GroupSystem)
     */
    CombinedGroupSystem getGroupSystems();

    /**
     * Gets the location checkers, which are used to prevent players from placing chests in the wilderness.
     * @return A location checker.
     */
    CombinedLocationChecker getLocationCheckers();

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
     * Gets the protection updater, used to mark protections as needing an
     * update, for example for fixing missing UUIDs.
     *
     * @return The protection updater.
     */
    ProtectionUpdater getProtectionUpdater();

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
     * Gets the caches, used to caching protection query result (for redstone).
     * @return The cache.
     */
    Cache getRedstoneProtectCache();
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
     * Runs a task in the given amount of ticks on the server thread.
     *
     * @param runnable
     *            The task.
     * @param ticks
     *            In how many ticks the method needs to run.
     */
    void runLater(Runnable runnable, int ticks);
}
