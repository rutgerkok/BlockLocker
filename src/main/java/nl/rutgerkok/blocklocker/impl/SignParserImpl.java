package nl.rutgerkok.blocklocker.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SecretSignEntry;
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

    private static final NamespacedKey HEADER_KEY = NbtSecretSignEntry.key("header");
    private static final NamespacedKey[] PROFILE_KEYS = { NbtSecretSignEntry.key("profile_1"),
            NbtSecretSignEntry.key("profile_2"), NbtSecretSignEntry.key("profile_3") };

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
    public String[] getDisplayLines(ProtectionSign sign) {
        List<Profile> profiles = sign.getProfiles();
        return new String[] {
                chestSettings.getFancyLocalizedHeader(sign.getType(), "?"),
                profiles.size() > 0 ? profiles.get(0).getDisplayName() : "",
                profiles.size() > 1 ? profiles.get(1).getDisplayName() : "",
                profiles.size() > 2 ? profiles.get(2).getDisplayName() : "",
        };
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

    private Optional<ProtectionSign> parseAdvancedSign(Sign sign) {
        String[] displayedText = sign.getLines();
        PersistentDataContainer data = sign.getPersistentDataContainer();

        // Get sign type
        if (!data.has(HEADER_KEY, PersistentDataType.STRING)) {
            return Optional.empty();
        }
        String signTypeString = data.get(HEADER_KEY, PersistentDataType.STRING);
        SignType type;
        try {
            type = SignType.valueOf(signTypeString);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        // Get profiles
        List<Profile> profiles = new ArrayList<>();
        int lineNumber = 1;
        boolean signHadDataMismatch = false; // Set to true when the display text doesn't match the stored data
        for (NamespacedKey profileKey : PROFILE_KEYS) {
            NbtSecretSignEntry entry = data.get(profileKey, NbtSecretSignEntry.TAG_TYPE);
            Profile profile = entry == null ? profileFactory.emptyProfile()
                    : profileFactory.fromSavedObject(entry).orElseGet(profileFactory::emptyProfile);

            if (profile.getDisplayName().equals(displayedText[lineNumber])) {
                // Text on sign matches, use stored profile
                profiles.add(profile);
            } else {
                // Text on sign doesn't match, so use the text
                Profile newProfile = profileFactory.fromDisplayText(displayedText[lineNumber]);
                profiles.add(newProfile);
                signHadDataMismatch = true;
            }

            lineNumber++;
        }

        return Optional.of(new ProtectionSignImpl(sign.getLocation(), type, profiles, signHadDataMismatch));
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
    private Optional<ProtectionSign> parseOldAdvancedSign(Location location, JsonSign jsonSign, String[] linesOnSign) {
        SignType signType = getSignTypeOrNull(jsonSign.getFirstLine());
        if (signType == null) {
            return Optional.empty();
        }

        List<Profile> profiles = new ArrayList<>();
        int lineNumber = 1; // Starts as one, as line 0 contains the sign header
        for (SecretSignEntry object : jsonSign) {
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

        // Last argument == true: we want to save this sign in our new format
        ProtectionSignImpl protectionSignImpl = new ProtectionSignImpl(location, signType, profiles, true);
        return Optional.<ProtectionSign>of(protectionSignImpl);
    }

    @Override
    public Optional<ProtectionSign> parseSign(Block sign) {
        Sign signState = (Sign) sign.getState();

        // Try modern method
        Optional<ProtectionSign> parsedSign = parseAdvancedSign(signState);
        if (parsedSign.isPresent()) {
            return parsedSign;
        }

        // Try old, hacky method
        JsonSign foundTextData = nms.getJsonData(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        String[] lines = signState.getLines();
        if (foundTextData.hasData()) {
            return parseOldAdvancedSign(sign.getLocation(), foundTextData, lines);
        }

        // Try plain sign, written by the user
        return parseSimpleSign(sign.getLocation(), lines);
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

        // Last argument == true: we want to save this sign in our own format
        ProtectionSignImpl protectionSignImpl = new ProtectionSignImpl(location, signType, profiles, true);
        return Optional.<ProtectionSign>of(protectionSignImpl);
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

        PersistentDataContainer data = signState.getPersistentDataContainer();
        data.set(HEADER_KEY, PersistentDataType.STRING, sign.getType().toString());
        int i = 0;
        for (Profile profile : sign.getProfiles()) {
            signState.setLine(i + 1, profile.getDisplayName());
            NbtSecretSignEntry signEntry = new NbtSecretSignEntry(
                    data.getAdapterContext().newPersistentDataContainer());
            profile.getSaveObject(signEntry);
            data.set(PROFILE_KEYS[i], NbtSecretSignEntry.TAG_TYPE, signEntry);
            i++;
        }

        // Save the text and secret data
        signState.update();
    }

}
