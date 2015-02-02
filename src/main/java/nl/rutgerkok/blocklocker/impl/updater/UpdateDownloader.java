package nl.rutgerkok.blocklocker.impl.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import org.bukkit.plugin.Plugin;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

/**
 * Downloads the file for the update.
 *
 */
final class UpdateDownloader {

    private static final Set<String> ALLOWED_HOSTS = ImmutableSet.of("www.spigotmc.org", "spigotmc.org");

    private final Plugin plugin;
    private final URL url;
    private final File downloadTo;

    UpdateDownloader(Plugin plugin, UpdateCheckResult result, File downloadTo) {
        Optional<URL> url = result.getDownloadUrl();
        Preconditions.checkArgument(url.isPresent(), "No file present in " + result);
        String host = url.get().getHost();
        Preconditions.checkArgument(ALLOWED_HOSTS.contains(host), "Can only download from " + ALLOWED_HOSTS + ", " + host + " is not allowed");

        this.plugin = Preconditions.checkNotNull(plugin);
        this.url = url.get();
        this.downloadTo = Preconditions.checkNotNull(downloadTo);
    }

    /**
     * Starts the download. Blocking method. After this method completes without
     * throwing an exception the file will have been downloaded.
     *
     * @throws IOException
     *             If an IO error occurs.
     */
    void downloadSync() throws IOException {
        downloadTo.getParentFile().mkdirs();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        boolean threw = true;
        try {
            URLConnection connection = url.openConnection();
            UserAgent.setFor(plugin, connection);
            inputStream = connection.getInputStream();
            outputStream = new FileOutputStream(downloadTo);
            ByteStreams.copy(inputStream, outputStream);
            threw = false;
        } finally {
            Closeables.closeQuietly(inputStream);
            Closeables.close(outputStream, threw);
        }
    }

}
