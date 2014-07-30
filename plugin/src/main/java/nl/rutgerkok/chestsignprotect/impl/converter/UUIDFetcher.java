package nl.rutgerkok.chestsignprotect.impl.converter;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nl.rutgerkok.chestsignprotect.NameAndId;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.ImmutableList;

class UUIDFetcher {
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

    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: "
                    + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12)
                + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-"
                + id.substring(20, 32));
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
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

    // ChestSignProtect - made method package private, accept Collection
    UUIDFetcher(Collection<String> names) {
        this(names, true);
    }

    // ChestSignProtect - made method package-private, accept Collection
    UUIDFetcher(Collection<String> names, boolean rateLimiting) {
        System.out.println("Looking up: " + names);
        this.names = ImmutableList.copyOf(names);
        this.rateLimiting = rateLimiting;
    }

    // ChestSignProtect - changed return type to String(lowercase),NameAndId
    public Map<String, NameAndId> call() throws Exception {
        Map<String, NameAndId> uuidMap = new HashMap<String, NameAndId>();
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
                // ChestSignProtect - change key and value
                uuidMap.put(name.toLowerCase(), NameAndId.of(name, uuid));
            }
            if (rateLimiting && i != requests - 1) {
                Thread.sleep(100L);
            }
        }
        return uuidMap;
    }

}
