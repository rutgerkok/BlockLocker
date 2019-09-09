package nl.rutgerkok.blocklocker;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

public interface ProfileFactory {

    /**
     * Gets the profile represented by [Everyone] on a sign. Allows everyone
     * else to access the sign.
     * 
     * @return The profile.
     */
    Profile fromEveryone();

    /**
     * Creates a new profile from the given name and id.
     *
     * @param name
     *            The name.
     * @param uuid
     *            The uuid. When absent, comparisons between other
     *            {@link PlayerProfile} the plugin may try to fetch this UUID at
     *            any time based on the name. {@code new UUID(0,0)}
     * @return The profile.
     */
    PlayerProfile fromNameAndUniqueId(String name, Optional<UUID> uuid);

    /**
     * Creates a new profile representing the online player.
     *
     * @param player
     *            The online player.
     * @return The profile.
     */
    PlayerProfile fromPlayer(Player player);

    /**
     * Gets the profile for redstone.
     * 
     * @return The profile.
     */
    Profile fromRedstone();
}
