package nl.rutgerkok.chestsignprotect.impl.profile;

import java.util.UUID;

import nl.rutgerkok.chestsignprotect.ProfileFactory;
import nl.rutgerkok.chestsignprotect.Translator;
import nl.rutgerkok.chestsignprotect.Translator.Translation;
import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

    public Profile fromDisplayText(String text) {
        text = ChatColor.stripColor(text.trim());

        if (text.equalsIgnoreCase(everyoneTagWithoutColor)) {
            return new EveryoneProfile(translator.get(Translation.TAG_EVERYONE));
        }
        // Group support will come later

        return fromPlayerName(text);
    }

    @Override
    public Profile fromNameAndUniqueId(String name, UUID uuid) {
        Validate.notNull(name);
        Validate.notNull(uuid);
        return new PlayerProfile(name, Optional.of(uuid));
    }

    @Override
    public Profile fromPlayer(Player player) {
        return fromNameAndUniqueId(player.getName(), player.getUniqueId());
    }

    private Profile fromPlayerName(String name) {
        Validate.notNull(name);
        return new PlayerProfile(name, Optional.<UUID> absent());
    }

    public Optional<Profile> fromSavedText(String text) {
        // Parse UUID|Name syntax
        int pipeIndex = text.indexOf('|');
        if (pipeIndex != -1) {
            try {
                UUID uuid = UUID.fromString(text.substring(0, pipeIndex));
                String name = text.substring(pipeIndex + 1);
                return Optional.of(fromNameAndUniqueId(name, uuid));
            } catch (IllegalArgumentException e) {
                return Optional.absent();
            }
        }

        // Parse other syntaxes
        return Optional.of(fromDisplayText(text));
    }

}
