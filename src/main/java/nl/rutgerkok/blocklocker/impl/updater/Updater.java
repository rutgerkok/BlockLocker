package nl.rutgerkok.blocklocker.impl.updater;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.impl.updater.UpdateResult.Status;

/**
 * Handles the complete update procedure.
 *
 */
public final class Updater {

    /**
     * Every twelve hours.
     */
    private static final long CHECK_INTERVAL = 20 * 60 * 60 * 12;

    private final File installDestination;
    private final Plugin plugin;
    private volatile UpdatePreference preference;
    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            if (preference.checkForUpdates()) {
                updateSync();
            } else {
                this.cancel();
            }
        }
    };
    private final Translator translator;

    public Updater(UpdatePreference preference, Translator translator, Plugin plugin) {
        this.preference = Preconditions.checkNotNull(preference);
        this.translator = Preconditions.checkNotNull(translator);
        this.plugin = Preconditions.checkNotNull(plugin);

        this.installDestination = new File(plugin.getServer().getUpdateFolderFile(), getJarFileName());
    }

    /**
     * Gets the name of the JAR file we're in, like BlockLocker-1.2.4.jar.
     * @return The name of the JAR file.
     */
    private String getJarFileName() {
        return new File(Updater.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                        .getName();
    }

    private Optional<String> getMinecraftVersion() {
        String serverVersion = plugin.getServer().getVersion();
        String regex = "MC\\: *([A-Za-z0-9\\._\\-]+)";
        Matcher matcher = Pattern.compile(regex).matcher(serverVersion);
        if (matcher.find() && matcher.groupCount() == 1) {
            return Optional.of(matcher.group(1));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Notifies admins of the server of an updated version of the plugin. Can be
     * called from any thread.
     *
     * @param result
     *            The update result.
     */
    private void notifyServer(final UpdateResult result) {
        if (!result.hasNotification()) {
            return;
        }

        // Disable further update checks
        preference = UpdatePreference.DISABLED;

        // Notify admins of existing result
        if (plugin.getServer().isPrimaryThread()) {
            notifyServerFromServerThread(result);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    notifyServerFromServerThread(result);
                }
            });
        }
    }

    private void notifyServerFromServerThread(UpdateResult result) {
        // Must be called from notifyServer
        // Result must have a notification

        UpdateNotifier notifier = new UpdateNotifier(translator, result);

        // Notify players
        plugin.getServer().getPluginManager().registerEvents(notifier, plugin);

        // Notify console
        notifier.sendNotification(plugin.getServer().getConsoleSender());
    }

    /**
     * Starts the update process. Does nothing if updates have been disabled.
     *
     * @throws IllegalStateException
     *             If this method was called earlier.
     */
    public void startUpdater() {
        if (!preference.checkForUpdates()) {
            return;
        }
        task.runTaskTimerAsynchronously(plugin, 1, CHECK_INTERVAL);
    }

    private void updateInstallSync(UpdateCheckResult result) throws IOException {
        if (preference.installUpdates() && result.getDownloadUrl().isPresent()) {
            Optional<String> minecraftVersion = getMinecraftVersion();
            
            if (minecraftVersion.isPresent() && result.getMinecraftVersions().containsAll(ImmutableSet.of(minecraftVersion.get()))) {
                // Update automatically
                UpdateDownloader downloader = new UpdateDownloader(plugin, result, installDestination);
                downloader.downloadSync();
                notifyServer(new UpdateResult(Status.AUTOMATICALLY_UPDATED, result));
            } else {
                // Server version no longer supported
                notifyServer(new UpdateResult(Status.UNSUPPORTED_SERVER, result));
            }
        } else {
            // Don't update automatically, but show notification
            notifyServer(new UpdateResult(Status.MANUAL_UPDATE, result));
        }
    }

    /**
     * Blocking update method, must <b>not</b> be called from the server thread.
     */
    private void updateSync() {
        try {
            UpdateChecker checker = new UpdateChecker();
            UpdateCheckResult result = checker.checkForUpdatesSync(plugin);
            if (result.needsUpdate()) {
                updateInstallSync(result);
            } else {
                notifyServer(new UpdateResult(Status.NO_UPDATE, result));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error during update check"
                    + " (you can disable automatic updates in the config file)", e);
            notifyServer(UpdateResult.failed());
        }
    }

}
