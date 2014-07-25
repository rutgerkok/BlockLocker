package nl.rutgerkok.chestsignprotect;

import java.util.UUID;

import nl.rutgerkok.chestsignprotect.profile.Profile;

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
}
