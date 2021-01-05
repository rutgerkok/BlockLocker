package nl.rutgerkok.blocklocker.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.impl.nms.ServerSpecific;
import nl.rutgerkok.blocklocker.impl.nms.ServerSpecific.JsonSign;
import nl.rutgerkok.blocklocker.impl.profile.ProfileFactoryImpl;
import nl.rutgerkok.blocklocker.profile.Profile;

/**
 * Reads a single sign for profiles. Doesn't verify the sign header, the first
 * line is simply skipped for reading.
 *
 */
class SignParserImpl implements SignParser {

    private final ChestSettings chestSettings;
    private final ServerSpecific nms;
    private final ProfileFactoryImpl profileFactory;

    SignParserImpl(ChestSettings chestSettings, ServerSpecific nms,
            ProfileFactoryImpl profileFactory) {
        this.nms = nms;
        this.profileFactory = profileFactory;
        this.chestSettings = chestSettings;
    }

    private int countProfileLines(String[] linesOnSign) {
        int count = 0;
        for (int i = 1; i < linesOnSign.length; i++) {
            if (linesOnSign[i].length() > 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Optional<SignType> getSignType(Sign sign) {
        String header = sign.getLine(0);
        return Optional.ofNullable(getSignTypeOrNull(header));
    }

    @Override
    public Optional<SignType> getSignType(SignChangeEvent event) {
        return Optional.ofNullable(getSignTypeOrNull(event.getLine(0)));
    }

    private SignType getSignTypeOrNull(String header) {
        final String trimmed = ChatColor.stripColor(header).trim();
        for (SignType type : SignType.values()) {
            if (chestSettings.getSimpleLocalizedHeaders(type).stream().anyMatch(localizedHeader -> trimmed.equalsIgnoreCase(localizedHeader))) {
                return type;
            }
        }
        return null;
    }

    @Override
    public boolean hasValidHeader(Sign sign) {
        return getSignType(sign).isPresent();
    }

    /**
     * Used for signs where the hidden text was found.
     *
     * @param location
     *            The location of the sign.
     * @param jsonSign
     *            The hidden JSON stored on the sign.
     * @param linesOnSign
     *            The displayed lines stored on the sign.
     * @return The parsed sign, if the sign is actually a protection sign.
     */
    private Optional<ProtectionSign> parseAdvancedSign(Location location, JsonSign jsonSign, String[] linesOnSign) {
        SignType signType = getSignTypeOrNull(jsonSign.getFirstLine());
        if (signType == null) {
            return Optional.empty();
        }

        List<Profile> profiles = new ArrayList<>();
        int lineNumber = 1; // Starts as one, as line 0 contains the sign header`
        for (JsonObject object : jsonSign) {
            Optional<Profile> oProfile = profileFactory.fromSavedObject(object);
            if (oProfile.isPresent()) {
                Profile profile = oProfile.get();

                String lineOnSign = linesOnSign[lineNumber];
                if (!profile.getDisplayName().equals(lineOnSign)) {
                    // JSON data doesn't match sign contents, so it must be corrupt or outdated
                    // Therefore, ignore the data
                    return parseSimpleSign(location, linesOnSign);
                }

                profiles.add(profile);
            }
            lineNumber++;
        }

        if (countProfileLines(linesOnSign) > profiles.size()) {
            // JSON data is incomplete, therefore corrupt or outdated
            // Therefore, ignore the data
            return parseSimpleSign(location, linesOnSign);
        }

        return Optional.<ProtectionSign>of(new ProtectionSignImpl(location, signType, profiles));
    }

    @Override
    public Optional<ProtectionSign> parseSign(Block sign) {
        JsonSign foundTextData = nms.getJsonData(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        String[] lines = ((Sign) sign.getState()).getLines();
        if (foundTextData.hasData()) {
            return parseAdvancedSign(sign.getLocation(), foundTextData, lines);
        } else {
            return parseSimpleSign(sign.getLocation(), lines);
        }
    }

    @Override
    @Deprecated
    public Optional<ProtectionSign> parseSign(Sign sign) {
        return parseSign(sign.getBlock());
    }

    /**
     * Used for signs where the hidden information was never written or was
     * lost.
     *
     * @param location
     *            The location on the sign.
     * @param lines
     *            The lines on the sign.
     * @return The protection sign, if the header format is correct.
     */
    private Optional<ProtectionSign> parseSimpleSign(Location location, String[] lines) {
        SignType signType = getSignTypeOrNull(lines[0]);
        if (signType == null) {
            return Optional.empty();
        }

        List<Profile> profiles = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String name = lines[i].trim();
            profiles.add(profileFactory.fromDisplayText(name));
        }

        // Last parameter true -> simple signs always need to be updated to be
        // advanced signs
        return Optional.<ProtectionSign> of(new ProtectionSignImpl(location, signType, profiles));
    }

    @Override
    public void saveSign(ProtectionSign sign) {
        // Find sign
        Location signLocation = sign.getLocation();
        Block block = signLocation.getWorld().getBlockAt(signLocation);
        BlockState blockState = block.getState();
        if (!(blockState instanceof Sign)) {
            return;
        }

        // Update sign, both visual and using raw JSON
        Sign signState = (Sign) blockState;

        signState.setLine(0, chestSettings.getFancyLocalizedHeader(sign.getType(), signState.getLine(0)));

        JsonArray jsonArray = new JsonArray();
        int i = 1; // Start at 1 to avoid overwriting the header
        for (Profile profile : sign.getProfiles()) {
            signState.setLine(i, profile.getDisplayName());
            jsonArray.add(profile.getSaveObject());
            i++;
        }

        // Save the text and JSON
        // (JSON after text, to avoid text overwriting the JSON)
        signState.update();
        nms.setJsonData(signState, jsonArray);
    }

}
