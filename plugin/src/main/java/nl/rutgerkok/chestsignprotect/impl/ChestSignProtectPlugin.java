package nl.rutgerkok.chestsignprotect.impl;

import java.util.logging.Level;

import nl.rutgerkok.chestsignprotect.ChestSignProtect;
import nl.rutgerkok.chestsignprotect.ProfileFactory;
import nl.rutgerkok.chestsignprotect.ProtectionFinder;
import nl.rutgerkok.chestsignprotect.SignParser;
import nl.rutgerkok.chestsignprotect.impl.event.BlockDestroyListener;
import nl.rutgerkok.chestsignprotect.impl.profile.ProfileFactoryImpl;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSignProtectPlugin extends JavaPlugin implements
        ChestSignProtect {
    private NMSAccessor nms;
    private ProfileFactoryImpl profileFactory;
    private ProtectionFinderImpl protectionFinder;

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
        try {
            nms = new NMSAccessor();
        } catch (NMSException e) {
            severe("Failed to load support for this Minecraft version."
                    + " Disabling this plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        profileFactory = new ProfileFactoryImpl();
        ChestSettingsImpl chestSettings = new ChestSettingsImpl();
        SignParser signParser = new SignParserImpl(chestSettings, nms,
                profileFactory);
        SignFinder signFinder = new SignFinder(signParser);
        protectionFinder = new ProtectionFinderImpl(signFinder, chestSettings);

        PluginManager plugins = Bukkit.getPluginManager();
        plugins.registerEvents(new BlockDestroyListener(this), this);
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
