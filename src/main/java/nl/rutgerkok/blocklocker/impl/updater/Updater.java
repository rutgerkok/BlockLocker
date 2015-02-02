package nl.rutgerkok.blocklocker.impl.updater;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.impl.updater.UpdateResult.Status;

import org.bukkit.craftbukkit.libs.jline.internal.Preconditions;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Optional;

/**
 * Handles the complete update procedure.
 *
 */
public final class Updater {

    private final File installDestination;
    private final UpdatePreference preference;
    private final Plugin plugin;
    private final Translator translator;

    public Updater(UpdatePreference preference, Translator translator, Plugin plugin) {
        this.preference = Preconditions.checkNotNull(preference);
        this.translator = Preconditions.checkNotNull(translator);
        this.plugin = Preconditions.checkNotNull(plugin);
        this.installDestination = new File(plugin.getServer().getUpdateFolderFile(), plugin.getName() + ".jar");
    }

    /**
     * Starts the update process. Does nothing if updates have been disabled.
     */
    public void tryUpdateAsync() {
        if (!preference.checkForUpdates()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                updateSync();
            }
        });
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
        UpdateNotifier notifier = new UpdateNotifier(translator, result);

        // Notify players
        plugin.getServer().getPluginManager().registerEvents(notifier, plugin);

        // Notify console
        notifier.sendNotification(plugin.getServer().getConsoleSender());
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

    private Optional<String> getMinecraftVersion() {
        String serverVersion = plugin.getServer().getVersion();
        String regex = "MC\\: *([A-Za-z0-9\\._\\-]+)";
        Matcher matcher = Pattern.compile(regex).matcher(serverVersion);
        if (matcher.find() && matcher.groupCount() == 1) {
            return Optional.of(matcher.group(1));
        } else {
            return Optional.absent();
        }
    }

    private void updateInstallSync(UpdateCheckResult result) throws IOException {
        if (preference.installUpdates() && result.getDownloadUrl().isPresent()) {
            Optional<String> minecraftVersion = getMinecraftVersion();
            if (result.getMinecraftVersions().containsAll(minecraftVersion.asSet())) {
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

}
