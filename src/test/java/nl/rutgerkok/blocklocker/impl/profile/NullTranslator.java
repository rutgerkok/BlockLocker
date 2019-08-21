package nl.rutgerkok.blocklocker.impl.profile;

import nl.rutgerkok.blocklocker.Translator;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Simply returns the key.
 *
 */
public class NullTranslator extends Translator {

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

	@Override
	public List<String> getAll(Translation key) {
		return Arrays.asList(key.toString());
	}

	@Override
	public List<String> getAllWithoutColor(Translation key) {
		return Arrays.asList(key.toString());
	}

}
