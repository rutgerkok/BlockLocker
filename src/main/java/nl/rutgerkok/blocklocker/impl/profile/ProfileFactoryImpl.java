package nl.rutgerkok.blocklocker.impl.profile;

import java.util.UUID;

import nl.rutgerkok.blocklocker.NameAndId;
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

public class ProfileFactoryImpl implements ProfileFactory {
    private final String everyoneTagWithoutColor;
    private final Translator translator;

    public ProfileFactoryImpl(Translator translator) {
        Validate.notNull(translator);
        this.translator = translator;
        everyoneTagWithoutColor = translator
                .getWithoutColor(Translation.TAG_EVERYONE);
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

        if (text.equalsIgnoreCase(everyoneTagWithoutColor)) {
            return new EveryoneProfile(translator.get(Translation.TAG_EVERYONE));
        }
        if (text.startsWith("[") && text.endsWith("]") && text.length() >= 3) {
            return new GroupProfileImpl(text.substring(1, text.length() - 1));
        }

        return new PlayerProfileImpl(text, Optional.<UUID> absent());
    }

    @Override
    public PlayerProfile fromNameAndUniqueId(NameAndId nameAndId) {
        Validate.notNull(nameAndId);
        Optional<UUID> uuid = Optional.of(nameAndId.getUniqueId());
        return new PlayerProfileImpl(nameAndId.getName(), uuid);
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
        Optional<String> name = getString(json, PlayerProfileImpl.NAME_KEY);
        if (name.isPresent()) {
            Optional<UUID> uuid = getUniqueId(json, PlayerProfileImpl.UUID_KEY);
            Profile profile = new PlayerProfileImpl(name.get(), uuid);
            return Optional.of(profile);
        }

        // [Everyone]
        Optional<String> value = getString(json, EveryoneProfile.EVERYONE_KEY);
        if (value.isPresent()) {
            Profile profile = new EveryoneProfile(value.get());
            return Optional.of(profile);
        }

        // Groups
        Optional<String> groupName = getString(json, GroupProfileImpl.GROUP_KEY);
        if (groupName.isPresent()) {
            Profile profile = new GroupProfileImpl(groupName.get());
            return Optional.of(profile);
        }

        return Optional.absent();
    }

    private Optional<String> getString(JSONObject object, String key) {
        Object stringObject = object.get(key);
        if (stringObject instanceof String) {
            return Optional.of((String) stringObject);
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

}
