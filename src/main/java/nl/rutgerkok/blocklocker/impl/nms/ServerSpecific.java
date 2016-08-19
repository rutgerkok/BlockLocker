package nl.rutgerkok.blocklocker.impl.nms;

import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.block.Sign;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

public interface ServerSpecific {

    /**
     * Holds the JSON data on a sign.
     */
    public static class JsonSign implements Iterable<JSONObject> {
        public static final JsonSign EMPTY = new JsonSign("", new JSONArray());

        private final String firstLine;
        private final JSONArray jsonData;

        public JsonSign(String firstLine, JSONArray jsonData) {
            this.firstLine = Preconditions.checkNotNull(firstLine);
            this.jsonData = Preconditions.checkNotNull(jsonData);
        }

        /**
         * Gets the text on the first line of a sign, may be an empty string.
         * 
         * @return The text.
         */
        public String getFirstLine() {
            return firstLine;
        }

        /**
         * Gets whether there is data on this sign.
         * 
         * @return True if there is data, false otherwise.
         */
        public boolean hasData() {
            return this != EMPTY;
        }

        @Override
        public Iterator<JSONObject> iterator() {
            return Iterators.filter(jsonData.iterator(), JSONObject.class);
        }
    }

    /**
     * Gets the stored {@link JSONObject}s. If the sign contains no extra data
     * at all, an empty {@link Optional} will be returned. Otherwise, all
     * non-null {@link JSONObject}s stored in the sign will be added to the
     * list.
     * 
     * @param world
     *            The world the sign is in.
     * @param x
     *            The x position of the sign.
     * @param y
     *            The y position of the sign.
     * @param z
     *            The z position of the sign.
     * @return The extra data, or empty if not found.
     */
    JsonSign getJsonData(World world, int x, int y, int z);

    /**
     * Sets the given JSON array on the sign. The JSON array can have as many
     * elements as you want, and can contain anything that can be serialized as
     * JSON.
     *
     * @param sign
     *            The sign to set the text on.
     * @param jsonArray
     *            The array to store.
     */
    void setJsonData(Sign sign, JSONArray jsonArray);

}