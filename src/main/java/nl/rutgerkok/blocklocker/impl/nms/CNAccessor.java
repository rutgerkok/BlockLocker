package nl.rutgerkok.blocklocker.impl.nms;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Server-specific calls for cn.nukkit.
 *
 */
public final class CNAccessor implements ServerSpecific {
	
	private final JsonParser jsonparser = new JsonParser();

    /**
     * Creates a server-specific accessor for Nukkit.
     *
     * @throws ClassNotFoundException
     *             When the server is not a Nukkit server.
     */
    public CNAccessor() throws ClassNotFoundException {
        // Check if Nukkit is running
        Class.forName("cn.nukkit.block.Block");
    }

    @Override
    public JsonSign getJsonData(World world, int x, int y, int z) {
        BlockState blockState = world.getBlockAt(x, y, z).getState();
        if (blockState instanceof Sign) {
            Sign sign = (Sign) blockState;
            try {
                String data = (String) sign.getClass().getMethod("getHiddenData").invoke(blockState);
                if (!Strings.isNullOrEmpty(data)) {
                	JsonElement element = jsonparser.parse(data);
                    return new JsonSign(sign.getLine(0), element.getAsJsonArray());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return JsonSign.EMPTY;
    }

    @Override
    public void setJsonData(Sign sign, JsonArray jsonArray) {
        String jsonString = jsonArray.toString();
        try {
            sign.getClass().getMethod("setHiddenData", String.class).invoke(sign, jsonString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
