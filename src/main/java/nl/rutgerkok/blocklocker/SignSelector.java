package nl.rutgerkok.blocklocker;

import java.util.Optional;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

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
     * Sets the sign that the player has selected. This will grant the right to
     * use a command as many times as the player wants to edit this specific
     * sign. In other words: check if the player is actually allowed to edit
     * this sign before calling this method.
     *
     * @param player
     *            The player.
     * @param sign
     *            The sign that the player has selected.
     */
    void setSelectedSign(Player player, Sign sign);
}
