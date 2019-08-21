package nl.rutgerkok.blocklocker.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import nl.rutgerkok.blocklocker.Translator;

/**
 * Provides translations from a configuration file.
 *
 */
class ConfigTranslator extends Translator {

    private boolean needsSave = false;
    private final Map<Translation, TranslationValue> translations;

    /**
     * Little class to hold the different representations of the translated
     * values. The original ({@code &1foo}), the colored ({@code §1foo}) and
     * uncolored ({@code foo}).
     *
     */
    private static class TranslationValue {
        private final String original;
        private final String uncolored;
        private final String colored;

        private TranslationValue(String original) {
            this.original = original.trim();
            this.colored = ChatColor.translateAlternateColorCodes('&', original);
            this.uncolored = ChatColor.stripColor(colored);
        }
        
        public List<TranslationValue> getAll() {
            return Arrays.asList(this);
        }
    }
    
    /**
     * Little class that contains multiple translated values.
     * This class extends a translationValue which is the primary value,
     * it contains a list with all the other possible values.
     *
     */
    private static class MultiTranslationValue extends TranslationValue {
    	private final List<TranslationValue> aliases;
    	
    	private MultiTranslationValue(List<String> values) {
    		super(values.get(0));
    		this.aliases = new ArrayList<TranslationValue>();
    		
    		for (String alias : values.subList(1, values.size())) {
    			this.aliases.add(new TranslationValue(alias));
    		}
    	}
    	
    	@Override
    	public List<TranslationValue> getAll() {
    	    List<TranslationValue> all = new ArrayList<ConfigTranslator.TranslationValue>();
    	    
    	    all.add(this);
    	    
    	    this.aliases.forEach(value->all.add(value));
    	    
    	    return all;
    	}
    }

    ConfigTranslator(ConfigurationSection config) {
        translations = new EnumMap<Translation, TranslationValue>(Translation.class);
        for (Translation translation : Translation.values()) {
            String key = translation.toString();

            if (!hasSpecifiedValue(config, key)) {
                // Not overridden, so no value in config file
                needsSave = true;
            }

            // Check if there is a list as value
            List<String> values = config.getStringList(key);
            
            // No list, add string to list
            if (values.size() <= 0) {
                values.add(config.getString(key));
            }
            
        	// Check the list for null values
        	for (int i = 0; i < values.size(); i++) {
        		if (values.get(i) == null) {
        			// No default value was specified, strange
                    values.set(i, "~~TODO translate " + key + "~~");
        		}
        	}
        	
        	// When there is only one value, add a translationValue, otherwise add a multiTranslationValue
        	translations.put(translation, (values.size() == 1) ? new TranslationValue(values.get(0)) : new MultiTranslationValue(values));
        }
    }

    /**
     * Checks if the user has specified a value for the given key. If a default
     * value was specified, this method still returns false, unlike the method
     * on {@link ConfigurationSection}.
     * 
     * @param config
     *            The configuration section.
     * @param key
     *            The key.
     * @return True if the user has specified a value for the given key, false
     *         otherwise.
     */
    private boolean hasSpecifiedValue(ConfigurationSection config, String key) {
        return !config.getString(key, "foo").equals("foo");
    }

    @Override
    public String get(Translation key) {
        // Note that the contract specifies that the key must be returned on
        // value, and from this implementation it looks like null is returned.
        // This is not the case, as *all* possible Translation keys have an
        // entry in the map: if a value is missing in the config file, the
        // constructor assigns the default value
        return translations.get(key).colored;
    }

    @Override
    public String getWithoutColor(Translation key) {
        return translations.get(key).uncolored;
    }
    
	@Override
	public List<String> getAll(Translation key) {
		List<String> all = new ArrayList<String>();
		
		translations.get(key).getAll().forEach(value->all.add(value.colored));
		
		return all;
	}

	@Override
	public List<String> getAllWithoutColor(Translation key) {
		List<String> all = new ArrayList<String>();
		
		translations.get(key).getAll().forEach(value->all.add(value.uncolored));
		
		return all;
	}
	
    boolean needsSave() {
        return needsSave;
    }

    /**
     * Saves the translations to a file.
     *
     * @param file
     *            The file to save to.
     * @throws IOException
     *             If an IO error occurs.
     */
    void save(File file) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        for (Entry<Translation, TranslationValue> translationEntry : translations.entrySet()) {
            config.set(translationEntry.getKey().toString(), translationEntry.getValue().original);
        }
        config.save(file);
        needsSave = false;
    }

    @Override
    public void sendMessage(CommandSender player, Translation translation) {
        player.sendMessage(get(translation));
    }

}
