package nl.rutgerkok.blocklocker.impl.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.SecretSignEntry;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.group.GroupSystem;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

public final class ProfileFactoryImpl implements ProfileFactory {

    private static final Profile EMPTY_PROFILE = new PlayerProfileImpl("", Optional.empty());

    private final Profile everyoneProfile;
    private final List<String> everyoneTagList;
    private final GroupSystem groupSystem;
    private final Profile redstoneProfile;
    private final List<String> redstoneTagList;
    private final List<String> timerTagStart;
    private final Translator translator;

    public ProfileFactoryImpl(GroupSystem groupSystem, Translator translator) {
        this.groupSystem = Preconditions.checkNotNull(groupSystem);
        this.translator = Preconditions.checkNotNull(translator);

        this.everyoneTagList = new ArrayList<>();
        this.redstoneTagList = new ArrayList<>();
        this.timerTagStart = new ArrayList<>();

        translator.getAllWithoutColor(Translation.TAG_EVERYONE).forEach(value -> this.everyoneTagList.add("[" + value + "]"));
        translator.getAllWithoutColor(Translation.TAG_REDSTONE).forEach(value -> this.redstoneTagList.add("[" + value + "]"));
        translator.getAllWithoutColor(Translation.TAG_TIMER).forEach(value -> this.timerTagStart.add("[" + value + ":"));

        this.everyoneProfile = new EveryoneProfileImpl(translator.get(Translation.TAG_EVERYONE));
        this.redstoneProfile = new RedstoneProfileImpl(translator.get(Translation.TAG_REDSTONE));
    }

    /**
     * Gets an empty profile, which is equal to what
     * {@link #fromDisplayText(String)} returns for an empty string.
     *
     * @return The empty profile.
     */
    public Profile emptyProfile() {
        return EMPTY_PROFILE;
    }

    /**
     * Parses a profile from the text displayed on a sign. Used for newly created
     * signs and for signs created by Lockette/Deadbolt.
     *
     * @param text
     *            The text on a single line.
     * @return The profile.
     */
    public Profile fromDisplayText(String text) {
        final String stripped = ChatColor.stripColor(text.trim());

        if (stripped.length() > 2) {
            // [Everyone]
            if (everyoneTagList.stream().anyMatch(s -> s.equalsIgnoreCase(stripped))) {
                return new EveryoneProfileImpl(stripped.substring(1, stripped.length() - 1));
            }

            // [Redstone]
            if (redstoneTagList.stream().anyMatch(s -> s.equalsIgnoreCase(stripped))) {
                return new RedstoneProfileImpl(stripped.substring(1, stripped.length() - 1));
            }

            // [Timer:X]
            if (timerTagStart.stream().anyMatch(s -> StringUtil.startsWithIgnoreCase(stripped, s) && stripped.endsWith("]"))) {
                return readTimerProfile(stripped);
            }

            // [GroupName]
            if (stripped.startsWith("[") && stripped.endsWith("]")) {
                return new GroupProfileImpl(groupSystem, stripped.substring(1, stripped.length() - 1));
            }

            // +GroupName+
            if (stripped.startsWith("+") && stripped.endsWith("+")) {
                return new GroupLeaderProfileImpl(groupSystem, stripped.substring(1, stripped.length() - 1));
            }

            // DisplayName#UUID (format of LockettePro)
            int hashCharIndex = stripped.indexOf('#');
            if (hashCharIndex > 0) {
                String name = stripped.substring(0, hashCharIndex);
                try {
                    UUID uuid = UUID.fromString(stripped.substring(hashCharIndex + 1));
                    return new PlayerProfileImpl(name, Optional.of(uuid));
                } catch (IllegalArgumentException e) {
                    // Ignore, not in name#uuid format. Someone probably added
                    // a # for some other reason
                }
            }
        }

        return new PlayerProfileImpl(stripped, Optional.empty());
    }

    @Override
    public Profile fromEveryone() {
        return this.everyoneProfile;
    }

    @Override
    public PlayerProfile fromNameAndUniqueId(String name, Optional<UUID> uuid) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(uuid, "uuid");
        return new PlayerProfileImpl(name, uuid);
    }

    @Override
    public PlayerProfile fromPlayer(Player player) {
        Preconditions.checkNotNull(player);
        Optional<UUID> uuid = Optional.of(player.getUniqueId());
        return new PlayerProfileImpl(player.getName(), uuid);
    }

    @Override
    public Profile fromRedstone() {
        return this.redstoneProfile;
    }



    /**
     * Converts the given profile from a saved JSON object.
     *
     * @param object
     *            The object to convert from.
     * @return The profile, if any.
     */
    public Optional<Profile> fromSavedObject(SecretSignEntry object) {
        // Player
        Optional<String> name = object.getString(PlayerProfileImpl.NAME_KEY);
        if (name.isPresent()) {
            Optional<UUID> uuid = object.getUniqueId(PlayerProfileImpl.UUID_KEY);
            Profile profile = new PlayerProfileImpl(name.get(), uuid);
            return Optional.of(profile);
        }

        // [Everyone]
        Optional<Boolean> isEveryone = object.getBoolean(EveryoneProfileImpl.EVERYONE_KEY);
        if (isEveryone.isPresent()) {
            return Optional.of(this.everyoneProfile);
        }

        // [Redstone]
        Optional<Boolean> isRedstone = object.getBoolean(RedstoneProfileImpl.REDSTONE_KEY);
        if (isRedstone.isPresent()) {
            return Optional.of(this.redstoneProfile);
        }

        // Timer
        OptionalInt secondsOpen = object.getInteger(TimerProfileImpl.TIME_KEY);
        if (secondsOpen.isPresent()) {
            Profile profile = new TimerProfileImpl(translator.getWithoutColor(Translation.TAG_TIMER),
                    secondsOpen.getAsInt());
            return Optional.of(profile);
        }

        // Groups
        Optional<String> groupName = object.getString(GroupProfileImpl.GROUP_KEY);
        if (groupName.isPresent()) {
            Profile profile = new GroupProfileImpl(groupSystem, groupName.get());
            return Optional.of(profile);
        }

        // Group leaders
        groupName = object.getString(GroupLeaderProfileImpl.GROUP_LEADER_KEY);
        if (groupName.isPresent()) {
            Profile profile = new GroupLeaderProfileImpl(groupSystem, groupName.get());
            return Optional.of(profile);
        }

        return Optional.empty();
    }

    private int readDigit(char digit) {
        try {
            return Integer.parseInt(String.valueOf(digit));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Profile readTimerProfile(String text) {
        // First decide which one to use
        String tagStart = timerTagStart.stream()
                .filter(tag -> StringUtil.startsWithIgnoreCase(text, tag) && text.endsWith("]")).findFirst()
                .orElse(null);

        char digit = text.charAt(tagStart.length());
        if (digit == ' ') {
            // In format [Timer: X]
            digit = text.charAt(tagStart.length() + 1);
        }
        int seconds = readDigit(digit);
        return new TimerProfileImpl(tagStart.substring(1, tagStart.length() - 1), seconds);
    }

}
