package nl.rutgerkok.chestsignprotect.impl.profile;

import nl.rutgerkok.chestsignprotect.Translator;

import org.bukkit.command.CommandSender;

/**
 * Simply returns the key.
 *
 */
public class NullTranslator implements Translator {

    @Override
    public String get(Translation key) {
        return key.toString();
    }

    @Override
    public String getWithoutColor(Translation key) {
        return key.toString();
    }

    @Override
    public void sendMessage(CommandSender player, Translation translation) {
        player.sendMessage(get(translation));
    }

}
