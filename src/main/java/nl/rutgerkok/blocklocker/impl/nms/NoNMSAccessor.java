package nl.rutgerkok.blocklocker.impl.nms;

import org.bukkit.World;
import org.bukkit.block.Sign;

import com.google.gson.JsonArray;

/**
 * Used when no NMS access is possible.
 *
 */
final class NoNMSAccessor implements ServerSpecific {

    @Override
    public JsonSign getJsonData(World world, int x, int y, int z) {
        return JsonSign.EMPTY; // Not implemented
    }

    @Override
    public void setJsonData(Sign sign, JsonArray jsonArray) {
        // Ignored
    }

}
