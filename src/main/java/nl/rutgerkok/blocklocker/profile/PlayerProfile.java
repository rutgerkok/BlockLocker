package nl.rutgerkok.blocklocker.profile;

import java.util.UUID;

import com.google.common.base.Optional;

public interface PlayerProfile extends Profile {

    /**
     * Gets the unique id of this player, if any.
     *
     * @return The unique id.
     */
    Optional<UUID> getUniqueId();

}
