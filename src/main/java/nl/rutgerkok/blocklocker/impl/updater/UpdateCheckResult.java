package nl.rutgerkok.blocklocker.impl.updater;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * The result of an update check.
 *
 * @see UpdateChecker
 */
final class UpdateCheckResult {
    private static final String DOWNLOAD_URL_KEY = "downloadUrl";
    private static final String ERROR_KEY = "error";
    private static final String INFO_URL_KEY = "infoUrl";
    private static final String NEEDS_UPDATE_KEY = "needsUpdate";
    private static final String REQUIREMENTS_KEY = "requirements";
    private static final String VERSION_KEY = "version";

    private final Optional<URL> downloadUrl;
    private final Optional<URL> infoUrl;
    private final Optional<String> latestVersion;
    private final Set<String> minecraftVersions;
    private final boolean needsUpdate;

    UpdateCheckResult(JSONObject object) throws IOException, ClassCastException {
        // Look for errors
        Object error = object.get(ERROR_KEY);
        if (error instanceof String) {
            throw new IOException((String) error);
        }

        // Parse all information
        needsUpdate = getBoolean(object, NEEDS_UPDATE_KEY);
        latestVersion = Optional.fromNullable((String) object.get(VERSION_KEY));
        downloadUrl = getUrl(object, DOWNLOAD_URL_KEY);
        infoUrl = getUrl(object, INFO_URL_KEY);
        this.minecraftVersions = getStringSet(object, REQUIREMENTS_KEY);
    }

    private Set<String> getStringSet(JSONObject object, String key) {
        List<?> list = (List<?>) object.get(key);
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        if (list.get(0) instanceof String) {
            @SuppressWarnings("unchecked")
            List<String> strings = (List<String>) list;
            return ImmutableSet.copyOf(strings);
        }
        return Collections.emptySet();
    }

    /**
     * Gets the URL of the file that must be downloaded.
     * 
     * @return The URL of the file.
     */
    public Optional<URL> getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Gets the URL on which information can be found for the update.
     * 
     * @return The URL.
     */
    public Optional<URL> getInfoUrl() {
        return infoUrl;
    }

    /**
     * Gets the name of the latest version of the plugin.
     * 
     * @return The latest version.
     */
    public Optional<String> getLatestVersion() {
        return latestVersion;
    }

    /**
     * Gets the supported Minecraft versions.
     * 
     * @return Supported Minecraft versions.
     */
    public Set<String> getMinecraftVersions() {
        return minecraftVersions;
    }

    private Optional<URL> getUrl(JSONObject object, String key) throws ClassCastException {
        String url = (String) object.get(key);
        if (url == null || url.isEmpty()) {
            return Optional.absent();
        }
        try {
            return Optional.of(new URL(url));
        } catch (MalformedURLException e) {
            return Optional.absent();
        }
    }

    private boolean getBoolean(JSONObject object, String key) {
        Boolean bool = (Boolean) object.get(key);
        if (bool == null) {
            return false;
        }
        return bool.booleanValue();
    }

    /**
     * Gets whether an update is available.
     * 
     * @return True if an update is available, false otherwise.
     */
    public boolean needsUpdate() {
        return needsUpdate;
    }

    @Override
    public String toString() {
        if (needsUpdate) {
            return "UpdateResult{needsUpdate=" + needsUpdate + "}";
        } else {
            return "UpdateResult{needsUpdate=" + needsUpdate
                    + ", latestVersion=" + latestVersion.orNull()
                    + ", downloadUrl=" + downloadUrl.orNull()
                    + ", infoUrl=" + infoUrl.orNull()
                    + ", minecraftVersions=" + minecraftVersions
                    + "}";
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + downloadUrl.hashCode();
        result = prime * result + infoUrl.hashCode();
        result = prime * result + latestVersion.hashCode();
        result = prime * result + minecraftVersions.hashCode();
        result = prime * result + (needsUpdate ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UpdateCheckResult)) {
            return false;
        }
        UpdateCheckResult other = (UpdateCheckResult) obj;
        if (!downloadUrl.equals(other.downloadUrl)) {
            return false;
        }
        if (!infoUrl.equals(other.infoUrl)) {
            return false;
        }
        if (!latestVersion.equals(other.latestVersion)) {
            return false;
        }
        if (!minecraftVersions.equals(other.minecraftVersions)) {
            return false;
        }
        if (needsUpdate != other.needsUpdate) {
            return false;
        }
        return true;
    }
}