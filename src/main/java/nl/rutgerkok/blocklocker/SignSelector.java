package nl.rutgerkok.blocklocker;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.google.common.base.Optional;

/**
 * Holds the signs the players have selected.
 *
 */
public interface SignSelector {

    /**
     * Gets the sign the player has selected. The player will have permission to
     * edit this sign.
     *
     * @param player
     *            The player.
     * @return The sign the player has selected, if any.
     */
    Optional<Sign> getSelectedSign(Player player);

    /**
     * Sets the sign that the player has selected. The player must have
     * permission to edit this sign; in other words: the player must be the
     * owner of the protection the sign belongs to.
     *
     * @param player
     *            The player.
     * @param sign
     *            The sign that the player has selected.
     */
    void setSelectedSign(Player player, Sign sign);
}
