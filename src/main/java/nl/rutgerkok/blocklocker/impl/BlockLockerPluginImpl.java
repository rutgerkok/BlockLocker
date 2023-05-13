package nl.rutgerkok.blocklocker.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.HopperCache;
import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.ProtectableBlocksSettings;
import nl.rutgerkok.blocklocker.ProtectionFinder;
import nl.rutgerkok.blocklocker.ProtectionUpdater;
import nl.rutgerkok.blocklocker.SignParser;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.group.CombinedGroupSystem;
import nl.rutgerkok.blocklocker.group.GroupSystem;
import nl.rutgerkok.blocklocker.impl.blockfinder.BlockFinder;
import nl.rutgerkok.blocklocker.impl.event.BlockDestroyListener;
import nl.rutgerkok.blocklocker.impl.event.BlockLockerCommand;
import nl.rutgerkok.blocklocker.impl.event.BlockPlaceListener;
import nl.rutgerkok.blocklocker.impl.event.InteractListener;
import nl.rutgerkok.blocklocker.impl.event.SignChangeListener;
import nl.rutgerkok.blocklocker.impl.group.FactionsGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.GuildsGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.PermissionsGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.ScoreboardGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.SimpleClansGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.TownyGroupSystem;
import nl.rutgerkok.blocklocker.impl.group.mcMMOGroupSystem;
import nl.rutgerkok.blocklocker.impl.location.TownyLocationChecker;
import nl.rutgerkok.blocklocker.impl.profile.ProfileFactoryImpl;
import nl.rutgerkok.blocklocker.impl.updater.Updater;
import nl.rutgerkok.blocklocker.location.CombinedLocationChecker;
import nl.rutgerkok.blocklocker.location.LocationChecker;

public class BlockLockerPluginImpl extends JavaPlugin implements BlockLockerPlugin {

    private ChestSettings chestSettings;
    private CombinedGroupSystem combinedGroupSystem;
    private Config config;
    private ProfileFactoryImpl profileFactory;
    private ProtectionFinderImpl protectionFinder;
    private ProtectionUpdater protectionUpdater;
    private SignParser signParser;
    private Translator translator;
    private CombinedLocationChecker combinedLocationChecker;
    private SchedulerSupport schedulerSupport;
    private HopperCache redstoneProtectCache;

    @Override
    public <E extends Event> E callEvent(E event) {
        this.getServer().getPluginManager().callEvent(event);
        return event;
    }

    @Override
    public ChestSettings getChestSettings() {
        return chestSettings;
    }

    @Override
    public CombinedGroupSystem getGroupSystems() {
        Preconditions.checkState(combinedGroupSystem != null);
        return combinedGroupSystem;
    }

    @Override
    public HopperCache getHopperCache() {
        return redstoneProtectCache;
    }

    /**
     * Gets a configuration file from the jar file. You can only use single file
     * names as paths; slashes are not allowed. The file name must end with .yml
     * (case insensitive).
     *
     * @param path
     *            Path in the jar file.
     * @return The configuration file.
     */
    private Optional<Configuration> getJarConfig(String path) {
        if (path.contains("/") || path.contains("\\")) {
            // Disallow searching through the JAR file.
            return Optional.empty();
        }
        if (!path.toLowerCase(Locale.ROOT).endsWith(".yml")) {
            return Optional.empty();
        }

        InputStream resource = getResource(path);
        if (resource == null) {
            // Not found
            return Optional.empty();
        }
        Reader reader = new InputStreamReader(resource, Charsets.UTF_8);
        Configuration config = YamlConfiguration.loadConfiguration(reader);
        try {
            resource.close();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to close stream", e);
        }
        return Optional.of(config);
    }

    @Override
    public CombinedLocationChecker getLocationCheckers() {
        Preconditions.checkState(this.combinedLocationChecker != null);
        return this.combinedLocationChecker;
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
        if (TownyGroupSystem.isAvailable()) {
            this.combinedGroupSystem.addSystem(new TownyGroupSystem());
        }
        if (mcMMOGroupSystem.isAvailable()) {
            this.combinedGroupSystem.addSystem(new mcMMOGroupSystem());
        }
        if (GuildsGroupSystem.isAvailable()) {
            this.combinedGroupSystem.addSystem(new GuildsGroupSystem());
        }
        if (SimpleClansGroupSystem.isAvailable()) {
            this.combinedGroupSystem.addSystem(new SimpleClansGroupSystem());
        }
    }

    private void loadLocationCheckers() {
        this.combinedLocationChecker = new CombinedLocationChecker();
        if (TownyLocationChecker.isAvailable()) {
            this.combinedLocationChecker.addChecker(new TownyLocationChecker());
        }
    }

    private void loadServices() {
        // Scheduler
        schedulerSupport = new SchedulerSupport(this);

        // Configuration
        saveDefaultConfig();
        config = new Config(getLogger(), getConfig());

        // Connections with external systems
        loadGroupSystems();
        loadLocationCheckers();

        // Translation
        translator = loadTranslations(config.getLanguageFileName());

        // Parsers and finders
        profileFactory = new ProfileFactoryImpl(combinedGroupSystem, translator);
        chestSettings = new ChestSettingsImpl(translator, config);
        signParser = new SignParserImpl(chestSettings, profileFactory);
        BlockFinder blockFinder = BlockFinder.create(signParser, config.getConnectContainers());
        protectionFinder = new ProtectionFinderImpl(blockFinder, chestSettings);
        protectionUpdater = new ProtectionUpdaterImpl(getServer(), signParser, profileFactory);
        redstoneProtectCache = new HopperCacheImpl(this);
    }

    private Translator loadTranslations(String fileName) {
        File file = new File(getDataFolder(), fileName);
        Configuration config = YamlConfiguration.loadConfiguration(file);
        Optional<Configuration> defaultsForLanguage = getJarConfig(fileName);
        if (defaultsForLanguage.isPresent()) {
            config.addDefaults(defaultsForLanguage.get());
        } else {
            Configuration defaultsForEnglish = getJarConfig(Config.DEFAULT_TRANSLATIONS_FILE)
                    .orElseGet(YamlConfiguration::new);
            config.addDefaults(defaultsForEnglish);
        }

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
        getCommand(getName().toLowerCase(Locale.ROOT)).setExecutor(new BlockLockerCommand(this));
    }

    @Override
    public void reload() {
        Collection<GroupSystem> keepGroupSystems = this.combinedGroupSystem.getReloadSurvivors();
        Collection<LocationChecker> keepLocationCheckers = this.combinedLocationChecker.getReloadSurvivors();
        Collection<ProtectableBlocksSettings> keepProtectables = this.chestSettings.getExtraProtectables();

        reloadConfig();
        loadServices();

        // Add back external systems from before the reload
        keepGroupSystems.forEach(this.combinedGroupSystem::addSystem);
        keepLocationCheckers.forEach(this.combinedLocationChecker::addChecker);
        this.chestSettings.getExtraProtectables().addAll(keepProtectables);
    }

    @Override
    public void runLater(Block block, Runnable runnable) {
        schedulerSupport.runLater(block, runnable);
    }

    @Override
    public void runLater(Block block, Runnable runnable, int ticks) {
        schedulerSupport.runLater(block, runnable, ticks);
    }

    @Override
    public void runLaterGlobally(Runnable runnable, int ticks) {
        schedulerSupport.runLaterGlobally(runnable, ticks);
    }

    /**
     * Folia-compatible alternative for running a timer task asynchronously.
     *
     * @param task
     *            The task.
     * @param checkInterval
     *            The check interval in ticks.
     */
    public void runTimerAsync(Consumer<BukkitTask> task, long checkInterval) {
        schedulerSupport.runTimerAsync(task, checkInterval);
    }

}
