package nl.rutgerkok.chestsignprotect;

import java.util.UUID;

import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.bukkit.entity.Player;

public interface ProfileFactory {

    /**
     * Creates a new profile from the given name and id.
     *
     * @param name
     *            The name of the player.
     * @param uuid
     *            The unique id of the player.
     * @return The profile.
     */
    Profile fromNameAndUniqueId(String name, UUID uuid);

    /**
     * Creates a new profile representing the online player.
     * 
     * @param player
     *            The online player.
     * @return The profile.
     */
    Profile fromPlayer(Player player);
}
