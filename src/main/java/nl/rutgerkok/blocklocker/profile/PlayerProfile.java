package nl.rutgerkok.blocklocker.profile;

import java.util.Optional;
import java.util.UUID;

public interface PlayerProfile extends Profile {

  /**
   * Gets the unique id of this player, if any.
   *
   * @return The unique id.
   */
  Optional<UUID> getUniqueId();
}
