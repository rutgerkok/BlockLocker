package nl.rutgerkok.blocklocker.impl.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

/**
 * Downloads the file for the update.
 *
 */
final class UpdateDownloader {

    private static final String EXPECTED_URL = "https://dl.dropboxusercontent.com/s/0v7euy3bp7jwcah/blocklocker-VERSION.jar?dl=0";
    private static final Pattern EXPECTED_VERSION_PATTERN = Pattern.compile("^[1-2]\\.[0-9]+\\.[0-9]+$");

    private final Plugin plugin;
    private final URL url;
    private final File downloadTo;
    private final String desiredMD5;
    private final String newVersion;

    UpdateDownloader(Plugin plugin, UpdateCheckResult result, File downloadTo) {
        Optional<URL> url = result.getDownloadUrl();
        Preconditions.checkArgument(url.isPresent(), "No file present in " + result);

        this.plugin = Preconditions.checkNotNull(plugin);
        this.url = url.get();
        this.downloadTo = Preconditions.checkNotNull(downloadTo);
        this.desiredMD5 = result.getFileMD5().or("(no md5 given)");
        this.newVersion = result.getLatestVersion().or("(no version given)");
    }

    private void checkMd5() throws IOException {
        String md5 = getMd5Checksum(downloadTo);
        if (!md5.equalsIgnoreCase(desiredMD5)) {
            if (!downloadTo.delete()) {
                downloadTo.deleteOnExit();
            }
            throw new IOException("MD5 of file " + md5 + " does not match expected md5 of " + desiredMD5);
        }
    }

    private void checkUrl() throws IOException {
        if (!EXPECTED_VERSION_PATTERN.matcher(this.newVersion).matches()) {
            throw new IOException("Invalid version number to update to: " + this.newVersion);
        }
        String expectedUrl = EXPECTED_URL.replace("VERSION", this.newVersion);
        if (!expectedUrl.equals(this.url.toString())) {
            throw new IOException(
                    "Updated file resides at unexpected location. Expected download from " + expectedUrl + ", found "
                            + this.url + ". Refusing to download.");
        }
    }

    /**
     * Starts the download. Blocking method. After this method completes without
     * throwing an exception the file will have been downloaded.
     *
     * @throws IOException
     *             If an IO error occurs.
     */
    void downloadSync() throws IOException {
        checkUrl();

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

        checkMd5();
    }

    private String getMd5Checksum(File file) throws IOException {
        byte[] checkSumBytes = getRawMd5Checksum(file);
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < checkSumBytes.length; i++) {
            result.append(Integer.toString((checkSumBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private byte[] getRawMd5Checksum(File file) throws IOException {
        InputStream inputStream =  new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest complete;
        try {
            complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return complete.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

}
