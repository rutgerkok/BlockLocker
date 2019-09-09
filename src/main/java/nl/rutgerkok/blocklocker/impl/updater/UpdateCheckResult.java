package nl.rutgerkok.blocklocker.impl.updater;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The result of an update check.
 */
final class UpdateCheckResult {
    private static final String DOWNLOAD_URL_KEY = "downloadUrl";
    private static final String ERROR_KEY = "error";
    private static final String INFO_URL_KEY = "infoUrl";
    private static final String NEEDS_UPDATE_KEY = "needsUpdate";
    private static final String REQUIREMENTS_KEY = "requirements";
    private static final String VERSION_KEY = "version";
    private static final String MD5_KEY = "fileMd5";

    private final Optional<URL> downloadUrl;
    private final Optional<URL> infoUrl;
    private final Optional<String> latestVersion;
    private final Optional<String> fileMd5;
    private final Set<String> minecraftVersions;
    private final boolean needsUpdate;

    UpdateCheckResult(JsonObject object) throws IOException, ClassCastException {
        // Look for errors
        Object error = object.get(ERROR_KEY);
        if (error instanceof String) {
            throw new IOException((String) error);
        }

        // Parse all information
        needsUpdate = getBoolean(object, NEEDS_UPDATE_KEY);
        latestVersion = Optional.fromNullable(object.get(VERSION_KEY).getAsString());
        fileMd5 = Optional.fromNullable(object.get(MD5_KEY).getAsString());
        downloadUrl = getUrl(object, DOWNLOAD_URL_KEY);
        infoUrl = getUrl(object, INFO_URL_KEY);
        this.minecraftVersions = getStringSet(object, REQUIREMENTS_KEY);
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
        if (!fileMd5.equals(other.fileMd5)) {
            return false;
        }
        if (needsUpdate != other.needsUpdate) {
            return false;
        }
        return true;
    }

    private boolean getBoolean(JsonObject object, String key) {
    	JsonElement element = object.get(key);
    	if (element == null) return false;
        Boolean bool = element.getAsBoolean();
        return bool.booleanValue();
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
     * Gets the correct MD5 of the downloaded file. Make sure that they match.
     * @return The correct MD5.
     */
    public Optional<String> getFileMD5() {
        return fileMd5;
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

    private Set<String> getStringSet(JsonObject object, String key) {
    	if (!object.has(key)) return Collections.emptySet();
    	
    	JsonArray array = object.get(key).getAsJsonArray();
        if (array.size() > 0 && array.get(0).isJsonPrimitive() && array.get(0).getAsJsonPrimitive().isString()) {
        	Builder<String> builder = ImmutableSet.builder();
        	array.forEach(el -> builder.add(el.getAsString()));
        	return builder.build();
        }
        return Collections.emptySet();
    }

    private Optional<URL> getUrl(JsonObject object, String key) throws ClassCastException {
        JsonElement element = object.get(key);
        if (element == null || element.getAsString().isEmpty()) {
            return Optional.absent();
        }
        try {
            return Optional.of(new URL(element.getAsString()));
        } catch (MalformedURLException e) {
            return Optional.absent();
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
        result = prime * result + fileMd5.hashCode();
        result = prime * result + (needsUpdate ? 1231 : 1237);
        return result;
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
                    + ", fileMd5=" + fileMd5.orNull()
                    + ", infoUrl=" + infoUrl.orNull()
                    + ", minecraftVersions=" + minecraftVersions
                    + "}";
        }

    }
}