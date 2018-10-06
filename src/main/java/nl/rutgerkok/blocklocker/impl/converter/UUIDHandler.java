package nl.rutgerkok.blocklocker.impl.converter;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

final class UUIDHandler {

    private static class Functions {
        /**
         * Attempts to find the 16th character of names that were trimmed to 15
         * characters.
         */
        private static Function<String, String> COMPLETE_NAME = new Function<String, String>() {
            @Override
            public String apply(String name) {
                if (name.length() == MAX_SIGN_LINE_LENGTH) {
                    List<Player> matches = Bukkit.matchPlayer(name);
                    if (!matches.isEmpty()) {
                        return matches.get(0).getName();
                    }
                }
                return name;
            }
        };

        /**
         * Converts the name (which may be missing a character at the end, because of
         * Minecraft's sign limitations) to a {@link Result} that only contains the
         * name, with an absent UUID.
         */
        private static Function<String, Result> OFFLINE_MODE_LOOKUP = new Function<String, Result>() {
            @Override
            public Result apply(String name) {
                name = COMPLETE_NAME.apply(name);
                return new Result(name);
            }
        };

        /**
         * Gets a placeholder result for the given name, will only be used if no UUID
         * exists for this name. In other words: some feedback is needed for the player
         * that the name was invalid, and the UUID will need to set to
         * {@code new UUID(0, 0)} so that the plugin doesn't constantly tries to look it
         * up.
         */
        private static Function<String, Result> ONLINE_MODE_PLACEHOLDERS = new Function<String, Result>() {
            @Override
            public Result apply(String name) {
                if (needsPrefixIfInvalidName(name)) {
                    // Adding a prefix makes the name look invalid to the player
                    name = invalidNamePrefixes[0] + name;
                }
                return new Result(name, ZERO_UUID);
            }
        };
    }

    private static class MojangWeb {
        private static final String BULK_UUID_LOOKUP_URL = "https://api.mojang.com/profiles/minecraft";
        private static JSONParser jsonParser = new JSONParser();
        private static final String PAST_PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft";
        private static final double PROFILES_PER_BULK_REQUEST = 100;

        private static HttpURLConnection createConnectionForBulkLookup() throws Exception {
            URL url = new URL(BULK_UUID_LOOKUP_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            return connection;
        }

        Map<String, Result> uuidLookup(List<String> names) throws Exception {
            Map<String, Result> results = new HashMap<>();
            int requests = (int) Math.ceil(names.size() / PROFILES_PER_BULK_REQUEST);
            for (int i = 0; i < requests; i++) {
                HttpURLConnection connection = createConnectionForBulkLookup();
                String body = JSONArray.toJSONString(names.subList(i * 100,
                        Math.min((i + 1) * 100, names.size())));
                writeBody(connection, body);
                JSONArray array = (JSONArray) jsonParser
                        .parse(new InputStreamReader(connection.getInputStream()));
                for (Object profile : array) {
                    Result result = toResult((JSONObject) profile);
                    results.put(result.name.toLowerCase(), result);
                }
                if (i != requests - 1) {
                    Thread.sleep(100L);
                }
            }
            return results;
        }
    }

    /**
     * A result value from the {@link UUID} lookup.
     *
     */
    static class Result {
        private final String name;
        private final Optional<UUID> uuid;

        private Result(String name) {
            this.name = name;
            this.uuid = Optional.absent();
        }

        private Result(String name, UUID uniqueId) {
            this.name = name;
            this.uuid = Optional.of(uniqueId);
        }

        /**
         * Gets the name of the player. If a lookup was successfull (the player is
         * online, or Mojang's API responded) this name will be the correct-cased, full
         * name of the player.
         *
         * @return The name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the uuid of the player with the name {@link #getName()}. If the server
         * is in offline mode, this method returns an empty optional. If the uuid could
         * not be looked up, this method returns an empty optional too.
         *
         * @return The unique id, if found.
         */
        public Optional<UUID> getUniqueId() {
            return uuid;
        }

        /**
         * Gets whether a valid uuid has been specified. The zero uuid is not considered
         * valid, as that indicates no player exists with that name.
         *
         * @return Whether a valid uuid has been specified.
         */
        public boolean hasValidId() {
            return uuid.isPresent() && !uuid.get().equals(ZERO_UUID);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[name=" + name + ",uuid=" + uuid.orNull() + "]";
        }
    }

    /**
     * Consumers of UUID lookups must extend this class.
     *
     */
    static abstract class ResultConsumer {

        private boolean resultsOnServerThread;

        /**
         * Creates a new {@link ResultConsumer}.
         *
         * @param resultsOnServerThread
         *            If set to true, {@link #accept(Map)} will be called on the server
         *            thread, otherwise {@link #accept(Map)} can be called on any
         *            thread, including the server thread.
         */
        public ResultConsumer(boolean resultsOnServerThread) {
            this.resultsOnServerThread = resultsOnServerThread;
        }

        /**
         * This method is called once for each call to
         * {@link UUIDHandler#fetchUniqueIds(Collection, ResultConsumer, boolean)} . If
         * the constructor of {@link ResultConsumer} was called with the parameter set
         * to true, this method will be called from the server thread, otherwise it can
         * be called from any thread (including the server thread).
         *
         * @param results
         *            The results. The keys of the map will be the lowercased, but
         *            otherwise unaltered, names that were passed to the fetch method.
         */
        public abstract void accept(Map<String, Result> results);
    }

    /**
     * Normally, the first entry of this array is added before each line with an
     * invalid name. When the line already starts with such a character, this prefix
     * isn't added.
     */
    private static final char[] invalidNamePrefixes = { '~', '=', '+', '-', '#', '/', '\\', '{', ':', '.', '<' };

    /**
     * Names that have this length may be cut off versions of longer names.
     */
    private static final int MAX_SIGN_LINE_LENGTH = 15;

    private static final Pattern validUserPattern = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");
    private static final UUID ZERO_UUID = new UUID(0, 0);

    private static Player getPlayerFromNameOnSign(String name) {
        if (name.length() == MAX_SIGN_LINE_LENGTH) {
            // This assumes Bukkit.getPlayer still does name matching
            return Bukkit.getPlayer(name);
        } else {
            return Bukkit.getPlayerExact(name);
        }
    }

    private static boolean needsPrefixIfInvalidName(String line) {
        if (line.isEmpty()) {
            // Prefixing empty lines would be silly
            return false;
        }
        char firstChar = line.charAt(0);
        for (char possiblePrefix : invalidNamePrefixes) {
            // Prefixing already prefixed lines would be silly too
            if (firstChar == possiblePrefix) {
                return false;
            }
        }
        // But everything else needs a prefix to indicate that the name lookup
        // failed
        return true;
    }

    /**
     * Creates a mutable hash map from the given player names and function that
     * returns a value for each key. The player names will be put as lowercase keys
     * in the map.
     *
     * @param <V>
     *            Type of the values.
     * @param keys
     *            The keys.
     * @param valueFunction
     *            Function that returns a value for each key.
     * @return The mutable hash map.
     */
    private static <V> Map<String, V> newPlayerNameMap(Collection<String> keys, Function<String, V> valueFunction) {
        Map<String, V> map = Maps.newHashMapWithExpectedSize(keys.size());
        for (String key : keys) {
            map.put(key.toLowerCase(), valueFunction.apply(key));
        }
        return map;
    }

    private static UUID parseMojangUniqueId(String withourDashes) {
        return UUID.fromString(withourDashes.substring(0, 8) + "-" + withourDashes.substring(8, 12)
        + "-" + withourDashes.substring(12, 16) + "-" + withourDashes.substring(16, 20) + "-"
        + withourDashes.substring(20, 32));
    }

    /**
     * Transforms an JSON object received from mojang.com to a {@link Result}.
     *
     * @param mojangObject
     *            The object from mojang.com.
     * @return The result.
     */
    private static Result toResult(JSONObject mojangObject) {
        String id = (String) mojangObject.get("id");
        String name = (String) mojangObject.get("name");
        UUID uuid = parseMojangUniqueId(id);
        return new Result(name, uuid);
    }

    private static void writeBody(HttpURLConnection connection, String body)
            throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private final Logger logger;

    private final MojangWeb mojangWeb = new MojangWeb();
    private final boolean onlineMode;
    private final Cache<String, Result> uuidCache = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    public UUIDHandler(Logger logger) {
        this.logger = logger;
        this.onlineMode = fetchOnlineMode();
    }

    /**
     * Called on another thread than the server thread. Calls the consumer directly,
     * or if the consumer requires that, goes back to the server thread and then
     * calls the consumer.
     *
     * @param results
     *            The results of the UUID lookup.
     * @param consumer
     *            The consumer to call.
     */
    private void acceptResultsAsync(final Map<String, Result> results, final ResultConsumer consumer) {
        if (consumer.resultsOnServerThread) {
            Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    consumer.accept(results);
                }
            });
        } else {
            consumer.accept(results);
        }
    }

    /**
     * Adds all results with {@link Result#hasValidId() a valid id} to the cache.
     * Can be called from any thread.
     *
     * @param results
     *            The results to add.
     */
    private void addToCache(Map<String, Result> results) {
        for (Entry<String, Result> entry : results.entrySet()) {
            if (entry.getValue().hasValidId()) {
                uuidCache.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private boolean fetchOnlineMode() {
        boolean onlineMode = Bukkit.getServer().getOnlineMode();
        try {
            onlineMode |= Class.forName("org.spigotmc.SpigotConfig").getField("bungee").getBoolean(null);
        } catch (ClassNotFoundException e) {
            // No Spigot, ignore
        } catch (Throwable t) {
            // Change in Spigot internals, log and move on
            logger.log(Level.SEVERE, "Couldn't lookup whether online mode or not", t);
            onlineMode = true;
        }
        return onlineMode;
    }

    /**
     * Fetches the unique ids for the given names, and posts the results to the
     * provided {@link ResultConsumer}.
     *
     * @param names
     *            The names.
     * @param consumer
     *            The result consumer.
     */
    void fetchUniqueIds(Collection<String> names, ResultConsumer consumer) {
        Preconditions.checkState(Bukkit.isPrimaryThread(), "Method must be called on primary thread");

        // Get safe names list
        List<String> namesList = Lists.newArrayList(names);

        if (this.onlineMode) {
            fetchUniqueIdsOnlineMode(namesList, consumer);
        } else {
            consumer.accept(newPlayerNameMap(namesList, Functions.OFFLINE_MODE_LOOKUP));
        }

    }

    /**
     * Looks up the UUIDs belonging to the given name. Don't call this method on the
     * server thread!
     *
     * @param names
     *            The names.
     * @param results
     *            The results already found by looking at the online player list.
     * @throws Exception
     *             When something goes wrong while contacting Mojang.
     */
    private void fetchUniqueIdsAtMojang(List<String> names, Map<String, Result> results) throws Exception {
        Map<String, Result> newResults = mojangWeb.uuidLookup(names);

        // Remove names that were found
        for (Iterator<String> it = names.iterator(); it.hasNext();) {
            String name = it.next();
            if (newResults.containsKey(name.toLowerCase())) {
                it.remove();
            }
        }

        // Modify results array
        results.putAll(newResults);
    }

    private void fetchUniqueIdsOnlineMode(final List<String> namesList,
            final ResultConsumer consumer) {
        // Fill map with empty results, will be overwritten when we find
        // something
        Map<String, Result> results = newPlayerNameMap(namesList, Functions.ONLINE_MODE_PLACEHOLDERS);

        // Get as much results as possible without a web request
        for (ListIterator<String> it = namesList.listIterator(); it.hasNext();) {
            String providedName = it.next();
            String nameLowercase = providedName.toLowerCase();

            // Check for name pattern
            if (!validUserPattern.matcher(providedName).matches()) {
                // Lookup will fail, so remove it from pending names
                it.remove();
                continue;
            }

            // Get from cache
            Result cached = uuidCache.getIfPresent(nameLowercase);
            if (cached != null) {
                // Success in cache!
                results.put(nameLowercase, cached);
                it.remove();
                continue;
            }

            // Get from online players
            Player player = getPlayerFromNameOnSign(providedName);
            if (player != null) {
                // Success! Player is online, local lookup succeeded, no need
                // to contact mojang.com for this name
                // By checking for matching players, we also correct for any
                // truncated names
                results.put(nameLowercase, new Result(player.getName(), player.getUniqueId()));
                it.remove();
                continue;
            }
        }

        // Check if we are already done using the online player trick
        if (namesList.isEmpty()) {
            consumer.accept(results);
            return;
        }

        // Fetch other names async using UUIDFetcher
        final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                fetchUniqueIdsAtMojang(namesList, results);

                addToCache(results);

                acceptResultsAsync(results, consumer);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error fetching UUIDs", e);
            }
        });
    }

    /**
     * Gets whether this handler is in online mode. If the handler is in offline
     * mode, no UUIDs will be looked up.
     *
     * @return True for online mode, false of offline mode.
     */
    boolean isOnlineMode() {
        return this.onlineMode;
    }

}
