package nl.rutgerkok.blocklocker.impl;

import java.util.ArrayList;
import java.util.List;

import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.impl.nms.ServerSpecific;
import nl.rutgerkok.blocklocker.impl.nms.ServerSpecific.JsonSign;
import nl.rutgerkok.blocklocker.impl.profile.ProfileFactoryImpl;
import nl.rutgerkok.blocklocker.profile.Profile;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;

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

    @Override
    public Optional<SignType> getSignType(Sign sign) {
        String header = sign.getLine(0);
        return Optional.fromNullable(getSignTypeOrNull(header));
    }

    @Override
    public Optional<SignType> getSignType(SignChangeEvent event) {
        return Optional.fromNullable(getSignTypeOrNull(event.getLine(0)));
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
     * @param header
     *            The header on the sign.
     * @param list
     *            The profile collection to add all profiles to.
     * @return The parsed sign, if the sign is actually a protection sign.
     */
    private Optional<ProtectionSign> parseAdvancedSign(Location location, String header, Iterable<JSONObject> list) {
        SignType signType = getSignTypeOrNull(header);
        if (signType == null) {
            return Optional.absent();
        }

        List<Profile> profiles = new ArrayList<Profile>();
        for (JSONObject object : list) {
            Optional<Profile> profile = profileFactory.fromSavedObject(object);
            if (profile.isPresent()) {
                profiles.add(profile.get());
            }
        }

        return Optional.<ProtectionSign> of(new ProtectionSignImpl(location, signType, profiles));
    }

    @Override
    public Optional<ProtectionSign> parseSign(Block sign) {
        JsonSign foundTextData = nms.getJsonData(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        if (foundTextData.hasData()) {
            return parseAdvancedSign(sign.getLocation(), foundTextData.getFirstLine(), foundTextData);
        } else {
            return parseSimpleSign(sign.getLocation(), ((Sign)sign.getState()).getLines());
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
            return Optional.absent();
        }

        List<Profile> profiles = new ArrayList<Profile>();
        for (int i = 1; i < lines.length; i++) {
            String name = lines[i].trim();
            profiles.add(profileFactory.fromDisplayText(name));
        }

        // Last parameter true -> simple signs always need to be updated to be
        // advanced signs
        return Optional.<ProtectionSign> of(new ProtectionSignImpl(location, signType, profiles));
    }

    @SuppressWarnings("unchecked")
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

        JSONArray jsonArray = new JSONArray();
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
