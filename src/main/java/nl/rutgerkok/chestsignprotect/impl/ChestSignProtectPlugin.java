package nl.rutgerkok.chestsignprotect.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;
import nl.rutgerkok.chestsignprotect.ProfileFactory;
import nl.rutgerkok.chestsignprotect.ProtectionFinder;
import nl.rutgerkok.chestsignprotect.SignParser;
import nl.rutgerkok.chestsignprotect.Translator;
import nl.rutgerkok.chestsignprotect.impl.converter.SignConverter;
import nl.rutgerkok.chestsignprotect.impl.event.BlockDestroyListener;
import nl.rutgerkok.chestsignprotect.impl.event.InteractListener;
import nl.rutgerkok.chestsignprotect.impl.nms.NMSAccessor;
import nl.rutgerkok.chestsignprotect.impl.profile.ProfileFactoryImpl;
import nl.rutgerkok.chestsignprotect.protection.Protection;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Charsets;

public class ChestSignProtectPlugin extends JavaPlugin implements
        ChestSignProtect {
    private NMSAccessor nms;
    private ProfileFactoryImpl profileFactory;
    private ProtectionFinderImpl protectionFinder;
    private SignConverter signConverter;
    private Translator translator;

    @Override
    public void fixMissingUniqueIds(Protection protection) {
        signConverter.fixMissingUniqueIds(protection);
    }

    /**
     * Gets a configuration file from the jar file. Unlike
     * {@link #getFileConfig(String)}, the configuration file is not exported to
     * the data folder.
     *
     * @param path
     *            Path in the jar file.
     * @return The configuration file.
     */
    private Configuration getJarConfig(String path) {
        InputStream resource = getResource(path);
        if (resource == null) {
            // Not found
            return new YamlConfiguration();
        }
        Reader reader = new InputStreamReader(resource, Charsets.UTF_8);
        Configuration config = YamlConfiguration.loadConfiguration(reader);
        try {
            resource.close();
        } catch (IOException e) {
            severe("Failed to close stream", e);
        }
        return config;
    }

    @Override
    public ProfileFactory getProfileFactory() {
        return profileFactory;
    }

    @Override
    public ProtectionFinder getProtectionFinder() {
        return protectionFinder;
    }

    @Override
    public Translator getTranslator() {
        return translator;
    }

    private Translator loadTranslations(String fileName) {
        File file = new File(getDataFolder(), fileName);
        Configuration config = YamlConfiguration.loadConfiguration(file);
        config.addDefaults(getJarConfig(Config.DEFAULT_TRANSLATIONS_FILE));

        ConfigTranslator translator = new ConfigTranslator(config);
        if (translator.needsSave()) {
            getLogger().info("Saving translations");
            try {
                translator.save(file);
            } catch (IOException e) {
                severe("Failed to save translation file", e);
            }
        }
        return translator;
    }

    @Override
    public void onEnable() {
        // NMS checks
        try {
            nms = new NMSAccessor();
        } catch (Throwable t) {
            severe("This Minecraft version is not supported. Find another version of the plugin, if available.", t);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Configuration
        saveDefaultConfig();
        Config config = new Config(getConfig());

        // Translation
        translator = loadTranslations(config.getLanguageFileName());

        // Parsers and finders
        profileFactory = new ProfileFactoryImpl(translator);
        ChestSettingsImpl chestSettings = new ChestSettingsImpl(translator);
        SignParser signParser = new SignParserImpl(chestSettings, nms,
                profileFactory);
        BlockFinder blockFinder = new BlockFinder(signParser);
        protectionFinder = new ProtectionFinderImpl(blockFinder, chestSettings);
        signConverter = new SignConverter(this, signParser, nms);

        // Events
        registerEvents();
    }

    /**
     * Registers all events of this plugin.
     */
    private void registerEvents() {
        PluginManager plugins = Bukkit.getPluginManager();
        plugins.registerEvents(new BlockDestroyListener(this), this);
        plugins.registerEvents(new InteractListener(this), this);
    }

    @Override
    public void severe(String message, Throwable t) {
        getLogger().log(Level.SEVERE, message, t);
    }

}
