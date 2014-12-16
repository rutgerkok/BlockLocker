package nl.rutgerkok.chestsignprotect.impl;

import java.util.Collection;
import java.util.List;

import nl.rutgerkok.chestsignprotect.ChestSettings;
import nl.rutgerkok.chestsignprotect.ChestSettings.SignType;
import nl.rutgerkok.chestsignprotect.SignParser;
import nl.rutgerkok.chestsignprotect.impl.nms.NMSAccessor;
import nl.rutgerkok.chestsignprotect.impl.profile.ProfileFactoryImpl;
import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;

/**
 * Reads a single sign for profiles. Doesn't verify the sign header, the first
 * line is simply skipped for reading.
 *
 */
class SignParserImpl implements SignParser {

    private final ChestSettings chestSettings;
    private final NMSAccessor nms;
    private final ProfileFactoryImpl profileFactory;

    SignParserImpl(ChestSettings chestSettings, NMSAccessor nms,
            ProfileFactoryImpl profileFactory) {
        this.nms = nms;
        this.profileFactory = profileFactory;
        this.chestSettings = chestSettings;
    }

    @Override
    public Optional<SignType> getSignType(Sign sign) {
        String header = sign.getLine(0);
        header = ChatColor.stripColor(header).trim();
        for (SignType type : SignType.values()) {
            if (header.equalsIgnoreCase(chestSettings.getLocalizedHeader(type))) {
                return Optional.of(type);
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean hasValidHeader(Sign sign) {
        return getSignType(sign).isPresent();
    }

    /**
     * Used for signs where the hidden text was found.
     *
     * @param jsonArray
     *            The hidden text.
     * @param addTo
     *            The profile collection to add all profiles to.
     */
    private void parseAdvancedSign(List<JSONObject> list,
            Collection<Profile> addTo) {
        for (JSONObject object : list) {
            Optional<Profile> profile = profileFactory.fromSavedObject(object);
            if (profile.isPresent()) {
                addTo.add(profile.get());
            }
        }
    }

    /**
     * Parses the given sign for all names on it.
     *
     * @param sign
     *            The sign to parse.
     * @param addTo
     *            The collection to add the profiles to.
     */
    @Override
    public void parseSign(Sign sign, Collection<Profile> addTo) {
        Optional<List<JSONObject>> foundTextData = nms.getJsonData(sign);
        if (foundTextData.isPresent()) {
            System.out.println("Found extra data");
            parseAdvancedSign(foundTextData.get(), addTo);
        } else {
            System.out.println("Found simple sign");
            parseSimpleSign(sign.getLines(), addTo);
        }
    }

    /**
     * Used for signs where the hidden information was never written or was
     * lost.
     *
     * @param lines
     *            The lines on the sign.
     * @param addTo
     *            The profile collection to add all profiles to.
     */
    private void parseSimpleSign(String[] lines, Collection<Profile> addTo) {
        // First line is not parsed, as it contains the header
        for (int i = 1; i < lines.length; i++) {
            String name = lines[i].trim();
            if (!name.isEmpty()) {
                addTo.add(profileFactory.fromDisplayText(name));
            }
        }
    }
}
