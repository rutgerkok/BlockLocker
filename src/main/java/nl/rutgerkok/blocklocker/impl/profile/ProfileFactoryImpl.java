package nl.rutgerkok.blocklocker.impl.profile;

import java.util.UUID;

import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class ProfileFactoryImpl implements ProfileFactory {
    private final String everyoneTagString;
    private final String redstoneTagString;
    private final String timerTagStart;
    private final Translator translator;
    private final Profile everyoneProfile;
    private final Profile redstoneProfile;

    public ProfileFactoryImpl(Translator translator) {
        Validate.notNull(translator);
        this.translator = translator;
        this.everyoneTagString = "[" + translator.getWithoutColor(Translation.TAG_EVERYONE) + "]";
        this.redstoneTagString = "[" + translator.getWithoutColor(Translation.TAG_REDSTONE) + "]";
        this.timerTagStart = "[" + translator.getWithoutColor(Translation.TAG_TIMER) + ":";
        this.everyoneProfile = new EveryoneProfileImpl(translator.get(Translation.TAG_EVERYONE));
        this.redstoneProfile = new RedstoneProfileImpl(translator.get(Translation.TAG_REDSTONE));
    }

    /**
     * Parses a profile from the text displayed on a sign. Used for newly
     * created signs and for signs created by Lockette/Deadbolt.
     *
     * @param text
     *            The text on a single line.
     * @return The profile.
     */
    public Profile fromDisplayText(String text) {
        text = ChatColor.stripColor(text.trim());

        // [Everyone]
        if (text.equalsIgnoreCase(everyoneTagString)) {
            return this.everyoneProfile;
        }

        // [Redstone]
        if (text.equalsIgnoreCase(redstoneTagString)) {
            return this.redstoneProfile;
        }

        // [Timer:X]
        if (text.startsWith(timerTagStart) && text.endsWith("]")) {
            int seconds = readDigit(text.charAt(timerTagStart.length()));
            return new TimerProfileImpl(translator.getWithoutColor(Translation.TAG_TIMER), seconds);
        }

        // [GroupName]
        if (text.startsWith("[") && text.endsWith("]") && text.length() >= 3) {
            return new GroupProfileImpl(text.substring(1, text.length() - 1));
        }

        return new PlayerProfileImpl(text, Optional.<UUID> absent());
    }

    private int readDigit(char digit) {
        try {
            return Integer.parseInt(String.valueOf(digit));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public PlayerProfile fromNameAndUniqueId(String name, Optional<UUID> uuid) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(uuid, "uuid");
        return new PlayerProfileImpl(name, uuid);
    }

    @Override
    public PlayerProfile fromPlayer(Player player) {
        Validate.notNull(player);
        Optional<UUID> uuid = Optional.of(player.getUniqueId());
        return new PlayerProfileImpl(player.getName(), uuid);
    }

    /**
     * Converts the given profile from a saved JSON object.
     *
     * @param json
     *            The object to convert from.
     * @return The profile, if any.
     */
    public Optional<Profile> fromSavedObject(JSONObject json) {
        // Player
        Optional<String> name = getValue(json, PlayerProfileImpl.NAME_KEY, String.class);
        if (name.isPresent()) {
            Optional<UUID> uuid = getUniqueId(json, PlayerProfileImpl.UUID_KEY);
            Profile profile = new PlayerProfileImpl(name.get(), uuid);
            return Optional.of(profile);
        }

        // [Everyone]
        Optional<Boolean> isEveryone = getValue(json, EveryoneProfileImpl.EVERYONE_KEY, Boolean.class);
        if (isEveryone.isPresent()) {
            return Optional.of(this.everyoneProfile);
        }

        // [Redstone]
        Optional<Boolean> isRedstone = getValue(json, RedstoneProfileImpl.REDSTONE_KEY, Boolean.class);
        if (isRedstone.isPresent()) {
            return Optional.of(this.redstoneProfile);
        }

        // Timer
        Optional<Number> secondsOpen = getValue(json, TimerProfileImpl.TIME_KEY, Number.class);
        if (secondsOpen.isPresent()) {
            Profile profile = new TimerProfileImpl(translator.getWithoutColor(Translation.TAG_TIMER), secondsOpen.get().intValue());
            return Optional.of(profile);
        }

        // Groups
        Optional<String> groupName = getValue(json, GroupProfileImpl.GROUP_KEY, String.class);
        if (groupName.isPresent()) {
            Profile profile = new GroupProfileImpl(groupName.get());
            return Optional.of(profile);
        }

        return Optional.absent();
    }

    private <T> Optional<T> getValue(JSONObject object, String key, Class<T> type) {
        Object value = object.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.absent();
    }

    private Optional<UUID> getUniqueId(JSONObject object, String key) {
        Object uuidObject = object.get(key);
        if (!(uuidObject instanceof String)) {
            return Optional.absent();
        }
        try {
            UUID uuid = UUID.fromString((String) uuidObject);
            return Optional.of(uuid);
        } catch (IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    @Override
    public Profile fromRedstone() {
        return this.redstoneProfile;
    }

}
