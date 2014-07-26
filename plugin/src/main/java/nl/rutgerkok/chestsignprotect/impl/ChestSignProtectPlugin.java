package nl.rutgerkok.chestsignprotect.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;
import nl.rutgerkok.chestsignprotect.ProfileFactory;
import nl.rutgerkok.chestsignprotect.ProtectionFinder;
import nl.rutgerkok.chestsignprotect.SignParser;
import nl.rutgerkok.chestsignprotect.impl.event.BlockDestroyListener;
import nl.rutgerkok.chestsignprotect.impl.event.PlayerInteractListener;
import nl.rutgerkok.chestsignprotect.impl.profile.ProfileFactoryImpl;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteStreams;

public class ChestSignProtectPlugin extends JavaPlugin implements
        ChestSignProtect {
    private NMSAccessor nms;
    private ProfileFactoryImpl profileFactory;
    private ProtectionFinderImpl protectionFinder;

    /**
     * Gets the file with the given name in the data folder of this plugin. If
     * the file does not exist yet, but the JAR file contains it, it is exported
     * first.
     * 
     * @param name
     *            Name of the file.
     * @return The file.
     */
    private File getOrExportFile(String name) {
        try {
            return getOrExportFile0(name);
        } catch (IOException e) {
            severe("Failed to copy file from jar to data folder", e);
            return new File(getDataFolder(), name);
        }
    }

    private File getOrExportFile0(String name) throws IOException {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            InputStream resource = null;
            OutputStream output = null;
            try {
                resource = getResource(name);
                if (resource == null) {
                    return file;
                }
                output = new FileOutputStream(file);
                ByteStreams.copy(resource, output);
            } finally {
                if (resource != null) {
                    resource.close();
                }
                if (output != null) {
                    output.close();
                }
            }
        }
        return file;
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
    public void onEnable() {
        // NMS checks
        try {
            nms = new NMSAccessor();
        } catch (NMSException e) {
            severe("Failed to load support for this Minecraft version."
                    + " Disabling this plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Configuration
        saveDefaultConfig();
        Config config = new Config(getConfig());
        File translationFile = getOrExportFile(config.getLanguageFileName());
        ConfigTranslator translator = new ConfigTranslator(
                YamlConfiguration.loadConfiguration(translationFile));

        // Parsers and finders
        profileFactory = new ProfileFactoryImpl(translator);
        ChestSettingsImpl chestSettings = new ChestSettingsImpl();
        SignParser signParser = new SignParserImpl(chestSettings, nms,
                profileFactory);
        SignFinder signFinder = new SignFinder(signParser);
        protectionFinder = new ProtectionFinderImpl(signFinder, chestSettings);

        // Events
        registerEvents();
    }

    /**
     * Registers all events of this plugin.
     */
    private void registerEvents() {
        PluginManager plugins = Bukkit.getPluginManager();
        plugins.registerEvents(new BlockDestroyListener(this), this);
        plugins.registerEvents(new PlayerInteractListener(this), this);
    }

    /**
     * Logs a message with severe importance, along with a stack trace.
     *
     * @param message
     *            The message to log.
     * @param t
     *            The exception that provides the stack trace.
     */
    private void severe(String message, Throwable t) {
        getLogger().log(Level.SEVERE, message, t);
    }

}
