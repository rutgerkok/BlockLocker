package nl.rutgerkok.blocklocker.impl.updater;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.plugin.Plugin;

/**
 * Checks whether an update is available.
 *
 */
final class UpdateChecker {

    private static final String UPDATE_URL = "http://rutgerkok.nl/tools/updater/blocklocker.php";

    private final JsonParser jsonParser = new JsonParser();

    /**
     * Checks online for updates. Blocking method.
     *
     * @param plugin
     *            Plugin to check for.
     * @return The update result.
     * @throws IOException
     *             If an IO error occurs.
     */
    public UpdateCheckResult checkForUpdatesSync(Plugin plugin) throws IOException {
        String currentVersionEncoded = URLEncoder.encode(plugin.getDescription().getVersion(), "UTF-8");
        URL url = new URL(UPDATE_URL + "?version=" + currentVersionEncoded);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        UserAgent.setFor(plugin, connection);

        try (InputStream stream = connection.getInputStream()) {
            JsonObject object = jsonParser.parse(new InputStreamReader(stream, Charsets.UTF_8)).getAsJsonObject();
            return new UpdateCheckResult(object);
        } catch (IOException e) {
            // Just rethrow, don't wrap
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
