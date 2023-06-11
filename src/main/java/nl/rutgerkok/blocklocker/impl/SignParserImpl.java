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
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.ProtectionSign;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.SignType;
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
            NbtSecretSignEntry.key("profile_2"), NbtSecretSignEntry.key("profile_3"),
            NbtSecretSignEntry.key("profile_4"), NbtSecretSignEntry.key("profile_5"),
            NbtSecretSignEntry.key("profile_6") };

    private final ChestSettings chestSettings;
    private final ProfileFactoryImpl profileFactory;

    SignParserImpl(ChestSettings chestSettings, ProfileFactoryImpl profileFactory) {
        this.profileFactory = profileFactory;
        this.chestSettings = chestSettings;
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
    public Optional<SignType> getSignType(Sign sign, Side side) {
        String header = sign.getSide(side).getLine(0);
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
        return getSignType(sign, Side.FRONT).isPresent();
    }

    private Optional<ProtectionSign> parseAdvancedSign(Sign sign) {
     // Get sign type
        PersistentDataContainer data = sign.getPersistentDataContainer();
        if (!data.has(HEADER_KEY, PersistentDataType.STRING)) {
            return Optional.empty();
        }

        // Check whether back side is in use
        boolean useBackside = getSignTypeOrNull(sign.getSide(Side.BACK).getLines()[0]) != null;

        // Collected displayed names of front (and back, if in use)
        String[] displayedNames = new String[useBackside ? 6 : 3];
        System.arraycopy(sign.getSide(Side.FRONT).getLines(), 1, displayedNames, 0, 3);
        if (useBackside) {
            System.arraycopy(sign.getSide(Side.BACK).getLines(), 1, displayedNames, 3, 3);
        }


        String signTypeString = data.get(HEADER_KEY, PersistentDataType.STRING);
        SignType type;
        try {
            type = SignType.valueOf(signTypeString);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        // Check header
        String header = sign.getSide(Side.FRONT).getLine(0);
        boolean headerMismatch = !chestSettings.getFancyLocalizedHeader(type, header).equals(header);
        if (useBackside) {
            String backHeader = sign.getSide(Side.BACK).getLine(0);
            if (!chestSettings.getFancyLocalizedHeader(SignType.MORE_USERS, header).equals(backHeader)) {
                headerMismatch = true;
            }
        }

        // Get profiles
        List<Profile> profiles = new ArrayList<>();
        int i = 0;
        boolean signHadDataMismatch = false; // Set to true when the display text doesn't match the stored data
        for (NamespacedKey profileKey : PROFILE_KEYS) {
            NbtSecretSignEntry entry = data.get(profileKey, NbtSecretSignEntry.TAG_TYPE);
            if (i >= 3 && !useBackside) {
                if (entry != null) {
                    // Found more data, but backside of sign is not in use
                    signHadDataMismatch = true;
                }
                break;
            }
            Profile profile = entry == null ? profileFactory.emptyProfile()
                    : profileFactory.fromSavedObject(entry).orElseGet(profileFactory::emptyProfile);

            if (profile.getDisplayName().equals(displayedNames[i])) {
                // Text on sign matches, use stored profile
                profiles.add(profile);
            } else {
                // Text on sign doesn't match, so use the text
                Profile newProfile = profileFactory.fromDisplayText(displayedNames[i]);
                profiles.add(newProfile);
                signHadDataMismatch = true;
            }

            i++;
        }

        return Optional
                .of(new ProtectionSignImpl(sign.getLocation(), type, profiles, signHadDataMismatch || headerMismatch));
    }

    @Override
    public Optional<ProtectionSign> parseSign(Block sign) {
        Sign signState = (Sign) sign.getState();

        // Try modern method
        Optional<ProtectionSign> parsedSign = parseAdvancedSign(signState);
        if (parsedSign.isPresent()) {
            return parsedSign;
        }

        // Try plain sign, written by the user
        String[] frontLines = signState.getSide(Side.FRONT).getLines();
        String[] backLines = signState.getSide(Side.BACK).getLines();
        return parseSimpleSign(sign.getLocation(), frontLines, backLines);
    }

    /**
     * Used for signs where the hidden information was never written or was lost.
     *
     * @param location
     *            The location on the sign.
     * @param frontLines
     *            The lines on the front of the sign.
     * @param backLines
     *            The lines on the back of the sign.
     * @return The protection sign, if the header format is correct.
     */
    private Optional<ProtectionSign> parseSimpleSign(Location location, String[] frontLines, String[] backLines) {
        SignType signType = getSignTypeOrNull(frontLines[0]);
        if (signType == null) {
            return Optional.empty();
        }

        List<Profile> profiles = new ArrayList<>();
        for (int i = 1; i < frontLines.length; i++) {
            String name = frontLines[i].trim();
            profiles.add(profileFactory.fromDisplayText(name));
        }

        // Try to read backside too
        if (getSignTypeOrNull(backLines[0]) != null) {
            for (int i = 1; i < backLines.length; i++) {
                String name = backLines[i].trim();
                profiles.add(profileFactory.fromDisplayText(name));
            }
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
        List<Profile> profiles = sign.getProfiles();
        Sign signState = (Sign) blockState;
        SignSide frontSide = signState.getSide(Side.FRONT);
        SignSide backSide = signState.getSide(Side.BACK);

        // Set headers correctly
        frontSide.setLine(0, chestSettings.getFancyLocalizedHeader(sign.getType(), frontSide.getLine(0)));
        if (profiles.size() > 3) {
            backSide.setLine(0, chestSettings.getFancyLocalizedHeader(SignType.MORE_USERS, backSide.getLine(0)));
        }

        PersistentDataContainer data = signState.getPersistentDataContainer();
        data.set(HEADER_KEY, PersistentDataType.STRING, sign.getType().toString());
        int i = 0;
        for (Profile profile : profiles) {
            if (i < 3) {
                frontSide.setLine(i + 1, profile.getDisplayName());
            } else {
                backSide.setLine(i - 2, profile.getDisplayName());
            }
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
