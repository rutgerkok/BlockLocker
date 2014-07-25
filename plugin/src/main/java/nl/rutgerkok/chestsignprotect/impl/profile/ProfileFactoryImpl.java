package nl.rutgerkok.chestsignprotect.impl.profile;

import java.util.UUID;

import nl.rutgerkok.chestsignprotect.ProfileFactory;
import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import com.google.common.base.Optional;

public class ProfileFactoryImpl implements ProfileFactory {

    public Profile fromDisplayText(String text) {
        return fromName(text);
        // Group support will come later
    }

    private Profile fromName(String name) {
        Validate.notNull(name);
        return new PlayerProfile(name, Optional.<UUID> absent());
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
