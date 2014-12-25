package nl.rutgerkok.blocklocker;

import java.util.UUID;

import nl.rutgerkok.blocklocker.profile.PlayerProfile;

import org.bukkit.entity.Player;

import com.google.common.base.Optional;

public interface ProfileFactory {

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
}
