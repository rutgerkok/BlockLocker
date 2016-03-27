package nl.rutgerkok.blocklocker;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import com.google.common.base.Optional;

/**
 * Parses a single sign. It essentially converts between {@link Sign} and
 * {@link ProtectionSign}.
 *
 */
public interface SignParser {

    /**
     * Gets the type of the sign.
     *
     * @param sign
     *            The sign.
     * @return The type of the sign.
     */
    Optional<SignType> getSignType(Sign sign);

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
     * Parses the given sign for all names on it.
     *
     * @param sign
     *            The sign to parse.
     * @return The parsed sign.
     * @deprecated Use {@link #parseSign(Block)} instead.
     */
    @Deprecated
    Optional<ProtectionSign> parseSign(Sign sign);

    /**
     * Saves the contents of the given sign to the world.
     *
     * @param sign
     *            The sign to save.
     */
    void saveSign(ProtectionSign sign);

}
