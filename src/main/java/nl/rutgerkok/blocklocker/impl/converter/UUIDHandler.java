package nl.rutgerkok.blocklocker.impl.converter;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.spigotmc.SpigotConfig;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
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
         * Converts the name (which may be missing a character at the end,
         * because of Minecraft's sign limitations) to a {@link Result} that
         * only contains the name, with an absent UUID.
         */
        private static Function<String, Result> OFFLINE_MODE_LOOKUP = new Function<String, Result>() {
            @Override
            public Result apply(String name) {
                name = COMPLETE_NAME.apply(name);
                return new Result(name);
            }
        };

        /**
         * Converts the name (assumed to be complete) to a {@link Result} that
         * only contains the name and the zero UUID ({@code new UUID(0, 0)}.
         */
        private static Function<String, Result> ONLINE_MODE_PLACEHOLDERS = new Function<String, Result>() {
            @Override
            public Result apply(String name) {
                return new Result(name, new UUID(0, 0));
            }
        };
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
         * Gets the name of the player. If a lookup was successfull (the player
         * is online, or Mojang's API responded) this name will be the
         * correct-cased, full name of the player.
         *
         * @return The name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the uuid of the player with the name {@link #getName()}. If the
         * server is in offline mode, this method returns an empty optional. If
         * the uuid could not be looked up, this method returns an empty
         * optional too.
         *
         * @return The unique id, if found.
         */
        public Optional<UUID> getUniqueId() {
            return uuid;
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
         *            If set to true, {@link #accept(Map)} will be called on the
         *            server thread, otherwise {@link #accept(Map)} can be
         *            called on any thread, including the server thread.
         */
        public ResultConsumer(boolean resultsOnServerThread) {
            this.resultsOnServerThread = resultsOnServerThread;
        }

        /**
         * This method is called once for each call to
         * {@link UUIDHandler#fetchUniqueIds(Collection, ResultConsumer)}. If
         * the constructor of {@link ResultConsumer} was called with the
         * parameter set to true, this method will be called from the server
         * thread, otherwise it can be called from any thread (including the
         * server thread).
         *
         * @param results
         *            The results. The keys of the map will be the lowercased,
         *            but otherwise unaltered, names that were passed to the
         *            fetch method.
         */
        public abstract void accept(Map<String, Result> results);
    }

    /**
     * Class based on EvilMidget's UUIDFetcher.
     *
     */
    private static class UUIDFetcher {
        private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
        private static final double PROFILES_PER_REQUEST = 100;

        private static HttpURLConnection createConnection() throws Exception {
            URL url = new URL(PROFILE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            return connection;
        }

        private static UUID getUUID(String id) {
            return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12)
                    + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-"
                    + id.substring(20, 32));
        }

        private static void writeBody(HttpURLConnection connection, String body)
                throws Exception {
            OutputStream stream = connection.getOutputStream();
            stream.write(body.getBytes());
            stream.flush();
            stream.close();
        }

        private final JSONParser jsonParser = new JSONParser();
        private final List<String> names;
        private final boolean rateLimiting;

        UUIDFetcher(List<String> names) {
            this.names = names;
            this.rateLimiting = true;
        }

        // ChestSignProtect - changed return type to String(lowercase),NameAndId
        public Map<String, UUID> call() throws Exception {
            Map<String, UUID> uuidMap = new HashMap<String, UUID>();
            int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
            for (int i = 0; i < requests; i++) {
                HttpURLConnection connection = createConnection();
                String body = JSONArray.toJSONString(names.subList(i * 100,
                        Math.min((i + 1) * 100, names.size())));
                writeBody(connection, body);
                JSONArray array = (JSONArray) jsonParser
                        .parse(new InputStreamReader(connection.getInputStream()));
                for (Object profile : array) {
                    JSONObject jsonProfile = (JSONObject) profile;
                    String id = (String) jsonProfile.get("id");
                    String name = (String) jsonProfile.get("name");
                    UUID uuid = UUIDFetcher.getUUID(id);
                    uuidMap.put(name, uuid);
                }
                if (rateLimiting && i != requests - 1) {
                    Thread.sleep(100L);
                }
            }
            return uuidMap;
        }

    }

    /**
     * Names that have this length may be cut off versions of longer names.
     */
    private static final int MAX_SIGN_LINE_LENGTH = 15;

    private static Player getPlayerFromNameOnSign(String name) {
        if (name.length() == MAX_SIGN_LINE_LENGTH) {
            return Bukkit.getPlayer(name);
        }
        else {
            return Bukkit.getPlayerExact(name);
        }
    }

    /**
     * Creates a mutable hash map from the given player names and function that
     * returns a value for each key. The player names will be put as lowercase
     * keys in the map.
     *
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

    private final Logger logger;
    private final boolean onlineMode;

    public UUIDHandler(Logger logger) {
        this.logger = logger;
        this.onlineMode = fetchOnlineMode();
    }

    /**
     * Called on another thread than the server thread. Calls the consumer
     * directly, or if the consumer requires that, goes back to the server
     * thread and then calls the consumer.
     * 
     * @param results
     * @param consumer
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

    private boolean fetchOnlineMode() {
        boolean onlineMode = Bukkit.getServer().getOnlineMode();
        try {
            onlineMode |= SpigotConfig.bungee;
        } catch (NoClassDefFoundError e) {
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
     * Looks up the UUIDs belonging to the given name. Don't call this method on
     * the server thread!
     * 
     * @param names
     *            The names.
     * @param results
     *            The results already found by looking at the online player
     *            list.
     * @param consumer
     *            The consumer that will eventually accept the results.
     */
    private void fetchUniqueIdsAsync(List<String> names, Map<String, Result> results, ResultConsumer consumer) {
        UUIDFetcher fetcher = new UUIDFetcher(names);
        try {
            Map<String, UUID> uuidFetcherResults = fetcher.call();
            for (Entry<String, UUID> entry : uuidFetcherResults.entrySet()) {
                results.put(entry.getKey().toLowerCase(), new Result(entry.getKey(), entry.getValue()));
            }
            acceptResultsAsync(results, consumer);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching names: " + names, e);
        }
    }

    private void fetchUniqueIdsOnlineMode(final List<String> namesList, final ResultConsumer consumer) {
        // Fill map with empty results, will be overwritten when we find
        // something
        final Map<String, Result> results = newPlayerNameMap(namesList, Functions.ONLINE_MODE_PLACEHOLDERS);

        // Get as much results as possible using the online player list
        for (ListIterator<String> it = namesList.listIterator(); it.hasNext();) {
            String providedName = it.next();
            Player player = getPlayerFromNameOnSign(providedName);
            if (player != null) {
                // Success! Player is online, no UUID lookup needed
                // By checking for matching players, we also correct for any
                // truncated names
                results.put(providedName.toLowerCase(), new Result(player.getName(), player.getUniqueId()));
                it.remove();
            }
        }

        // Check if we are already done using the online player trick
        if (namesList.isEmpty()) {
            consumer.accept(results);
            return;
        }

        // Fetch other names async using UUIDFetcher
        Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                fetchUniqueIdsAsync(namesList, results, consumer);
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
