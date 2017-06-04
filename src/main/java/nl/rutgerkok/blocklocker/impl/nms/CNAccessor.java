package nl.rutgerkok.blocklocker.impl.nms;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.google.common.base.Strings;

/**
 * Server-specific calls for cn.nukkit.
 *
 */
public final class CNAccessor implements ServerSpecific {

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
                    return new JsonSign(sign.getLine(0), (JSONArray) JSONValue.parseWithException(data));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return JsonSign.EMPTY;
    }

    @Override
    public void setJsonData(Sign sign, JSONArray jsonArray) {
        String jsonString = jsonArray.toJSONString();
        try {
            sign.getClass().getMethod("setHiddenData", String.class).invoke(sign, jsonString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
