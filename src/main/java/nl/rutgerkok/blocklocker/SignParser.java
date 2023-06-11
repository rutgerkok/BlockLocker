package nl.rutgerkok.blocklocker;

import java.util.Optional;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Parses a single sign. It essentially converts between {@link Sign} and
 * {@link ProtectionSign}.
 *
 */
public interface SignParser {

    /**
     * Gets the text-only display lines that should be displayed on the sign at the
     * front.
     *
     * @param sign
     *            The sign.
     * @return The display text.
     */
    String[] getDisplayLines(ProtectionSign sign);

    /**
     * Gets the type of the sign.
     *
     * @param sign
     *            The sign.
     * @param side
     *            The side of the sign to read.
     * @return The type of the sign.
     */
    Optional<SignType> getSignType(Sign sign, Side side);

    /**
     * Gets the type of the sign from the {@link SignChangeEvent}. The event
     * fires before the sign in the world is updated, so trying to read from the
     * world will result in outdated information.
     *
     * @param event
     *            The sign change event.
     * @return The type of the sign.
     */
    Optional<SignType> getSignType(SignChangeEvent event);

    /**
     * Gets whether the sign header is valid. Calling this method is the same as
     * calling {@code getSignType(sign).isPresent()}.
     *
     * @param sign
     *            The sign to check.
     * @return True if the header is valid, false otherwise.
     */
    boolean hasValidHeader(Sign sign);

    /**
     * Parses the given sign for all names on it.
     *
     * @param signBlock
     *            The sign to parse.
     * @return The parsed sign.
     */
    Optional<ProtectionSign> parseSign(Block signBlock);

    /**
     * Saves the contents of the given sign to the world.
     *
     * @param sign
     *            The sign to save.
     */
    void saveSign(ProtectionSign sign);

}
