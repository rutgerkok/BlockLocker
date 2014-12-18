package nl.rutgerkok.chestsignprotect;

import nl.rutgerkok.chestsignprotect.profile.PlayerProfile;

import org.bukkit.entity.Player;

public interface ProfileFactory {

    /**
     * Creates a new profile from the given name and id.
     *
     * @param nameAndId
     *            The name and id.
     * @return The profile.
     */
    PlayerProfile fromNameAndUniqueId(NameAndId nameAndId);

    /**
     * Creates a new profile representing the online player.
     *
     * @param player
     *            The online player.
     * @return The profile.
     */
    PlayerProfile fromPlayer(Player player);
}
