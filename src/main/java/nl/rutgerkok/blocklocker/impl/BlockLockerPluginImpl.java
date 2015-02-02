package nl.rutgerkok.blocklocker.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.logging.Level;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.ProtectionFinder;
import nl.rutgerkok.blocklocker.ProtectionUpdater;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.SignSelector;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.group.CombinedGroupSystem;
import nl.rutgerkok.blocklocker.group.GroupSystem;
import nl.rutgerkok.blocklocker.impl.converter.ProtectionUpdaterImpl;
import nl.rutgerkok.blocklocker.impl.event.BlockDestroyListener;
import nl.rutgerkok.blocklocker.impl.event.BlockLockerCommand;
import nl.rutgerkok.blocklocker.impl.event.BlockPlaceListener;
import nl.rutgerkok.blocklocker.impl.event.InteractListener;
import nl.rutgerkok.blocklocker.impl.event.SignChangeListener;
import nl.rutgerkok.blocklocker.impl.group.FactionsGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.PermissionsGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.ScoreboardGroupSystem;
import nl.rutgerkok.blocklocker.impl.nms.NMSAccessor;
import nl.rutgerkok.blocklocker.impl.profile.ProfileFactoryImpl;
import nl.rutgerkok.blocklocker.impl.updater.Updater;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class BlockLockerPluginImpl extends JavaPlugin implements
        BlockLockerPlugin {
    private ChestSettings chestSettings;
    private CombinedGroupSystem combinedGroupSystem;
    private Config config;
    private NMSAccessor nms;
    private ProfileFactoryImpl profileFactory;
    private ProtectionFinderImpl protectionFinder;
    private ProtectionUpdater protectionUpdater;
    private SignParser signParser;
    private SignSelector signSelector;
    private Translator translator;

    @Override
    public ChestSettings getChestSettings() {
        return chestSettings;
    }

    @Override
    public CombinedGroupSystem getGroupSystems() {
        Preconditions.checkState(combinedGroupSystem != null);
        return combinedGroupSystem;
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
            getLogger().log(Level.SEVERE, "Failed to close stream", e);
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
    public ProtectionUpdater getProtectionUpdater() {
        return protectionUpdater;
    }

    @Override
    public SignParser getSignParser() {
        return signParser;
    }

    @Override
    public SignSelector getSignSelector() {
        return signSelector;
    }

    @Override
    public Translator getTranslator() {
        return translator;
    }

    private void loadGroupSystems() {
        this.combinedGroupSystem = new CombinedGroupSystem();
        this.combinedGroupSystem.addSystem(new PermissionsGroupSystem());
        this.combinedGroupSystem.addSystem(new ScoreboardGroupSystem());

        if (FactionsGroupSystem.isAvailable()) {
            this.combinedGroupSystem.addSystem(new FactionsGroupSystem());
        }
    }

    private void loadServices() {
        // Configuration
        saveDefaultConfig();
        config = new Config(getLogger(), getConfig());

        // Group systems
        loadGroupSystems();

        // Translation
        translator = loadTranslations(config.getLanguageFileName());

        // Parsers and finders
        profileFactory = new ProfileFactoryImpl(combinedGroupSystem, translator);
        chestSettings = new ChestSettingsImpl(translator, config);
        signParser = new SignParserImpl(chestSettings, nms, profileFactory);
        BlockFinder blockFinder = new BlockFinder(signParser);
        protectionFinder = new ProtectionFinderImpl(blockFinder, chestSettings);
        protectionUpdater = new ProtectionUpdaterImpl(this);
        signSelector = new SignSelectorImpl(this);
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
                getLogger().log(Level.SEVERE, "Failed to save translation file", e);
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
            getLogger().log(Level.SEVERE, "This Minecraft version is not supported. Find another version of the plugin, if available.", t);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadServices();

        // Events
        registerEvents();

        // Updater
        new Updater(config.getUpdatePreference(), translator, this).startUpdater();
    }

    /**
     * Registers all events of this plugin.
     */
    private void registerEvents() {
        PluginManager plugins = Bukkit.getPluginManager();
        plugins.registerEvents(new BlockDestroyListener(this), this);
        plugins.registerEvents(new BlockPlaceListener(this), this);
        plugins.registerEvents(new InteractListener(this), this);
        plugins.registerEvents(new SignChangeListener(this), this);
        getCommand(getName().toLowerCase()).setExecutor(new BlockLockerCommand(this));
    }

    @Override
    public void reload() {
        Collection<GroupSystem> reloadSurvivors = this.combinedGroupSystem.getReloadSurvivors();

        reloadConfig();
        loadServices();

        // Add back group systems from before the reload
        this.combinedGroupSystem.addSystems(reloadSurvivors);
    }

    @Override
    public void runLater(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    @Override
    public void runLater(Runnable runnable, int ticks) {
        getServer().getScheduler().runTaskLater(this, runnable, ticks);
    }

}
