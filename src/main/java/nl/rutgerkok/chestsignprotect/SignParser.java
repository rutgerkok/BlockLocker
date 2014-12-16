package nl.rutgerkok.chestsignprotect;

import java.util.Collection;

import nl.rutgerkok.chestsignprotect.ChestSettings.SignType;
import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.bukkit.block.Sign;

import com.google.common.base.Optional;

/**
 * Parses a single sign. It can find the type of the header and get a collection
 * of profiles stored on the sign.
 *
 */
public interface SignParser {

    /**
     * Gets the type of the sign.
     *
     * @param header
     *            First line of the sign, may contain extra spaces and color
     *            codes.
     * @return The type of the sign.
     */
    Optional<SignType> getSignType(Sign sign);

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
     * @param sign
     *            The sign to parse.
     * @param addTo
     *            The collection to add the profiles to.
     */
    void parseSign(Sign sign, Collection<Profile> addTo);

}
