package nl.rutgerkok.chestsignprotect.impl.nms;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.ChatComponentText;
import net.minecraft.server.v1_8_R1.ChatHoverable;
import net.minecraft.server.v1_8_R1.ChatModifier;
import net.minecraft.server.v1_8_R1.EnumHoverAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.TileEntitySign;
import net.minecraft.server.v1_8_R1.WorldServer;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.util.CraftChatMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Implementation of methods required by
 * nl.rutgerkok.chestsignprotect.impl.NMSAccessor for Minecraft 1.7.8 and 1.7.9.
 *
 */
public final class NMSAccessor {

    private BlockPosition getBlockPosition(Location location) {
        return new BlockPosition(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    /**
     * Gets the stored {@link JSONObject}s. If the sign contains no extra data
     * at all, an empty {@link Optional} will be returned. Otherwise, all
     * non-null {@link JSONObject}s stored in the sign will be added to the
     * list.
     * 
     * @param sign
     *            The sign.
     * @return The extra data, or empty if not found.
     */
    public Optional<List<JSONObject>> getJsonData(Sign sign) {
        // Find sign
        Optional<TileEntitySign> nmsSign = toNmsSign(sign);
        if (!nmsSign.isPresent()) {
            return Optional.absent();
        }

        // Find strings stored in hovertext
        Optional<String> secretData = getSecretData(nmsSign.get());
        if (!secretData.isPresent()) {
            return Optional.absent();
        }

        // Parse and sanitize the sting
        Object data = JSONValue.parse(secretData.get());
        if (data instanceof JSONArray) {
            List<JSONObject> result = new ArrayList<JSONObject>();
            for (Object object : (JSONArray) data) {
                if (object instanceof JSONObject) {
                    result.add((JSONObject) object);
                }
            }
            return Optional.of(result);
        }
        return Optional.absent();
    }

    private Optional<String> getSecretData(TileEntitySign nmsSign) {
        IChatBaseComponent line = nmsSign.lines[0];
        ChatModifier modifier = line.getChatModifier();
        if (modifier != null) {
            ChatHoverable hoverable = modifier.i();
            if (hoverable != null) {
                return Optional.of(CraftChatMessage.fromComponent(hoverable.b()));

            }
        }

        return Optional.absent();
    }

    /**
     * Sets the given JSON array on the sign. The JSON array can have as many
     * elements as you want, and can contain anything that can be serialized as
     * JSON.
     * 
     * @param sign
     *            The sign.
     * @param jsonArray
     *            The array to store.
     */
    public void setJsonData(Sign sign, JSONArray jsonArray) {
        Optional<TileEntitySign> nmsSign = toNmsSign(sign);
        if (!nmsSign.isPresent()) {
            throw new RuntimeException("No sign at " + sign.getLocation());
        }

        setSecretData(nmsSign.get(), jsonArray.toJSONString());
    }

    private void setSecretData(TileEntitySign nmsSign, String data) {
        IChatBaseComponent line = nmsSign.lines[0];
        ChatModifier modifier = Objects.firstNonNull(line.getChatModifier(), new ChatModifier());
        ChatHoverable hoverable = new ChatHoverable(EnumHoverAction.SHOW_TEXT, new ChatComponentText(data));
        modifier.setChatHoverable(hoverable);
    }

    private Optional<TileEntitySign> toNmsSign(Sign sign) {
        Location location = sign.getLocation();
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        TileEntity tileEntity = nmsWorld.getTileEntity(getBlockPosition(location));
        if (!(tileEntity instanceof TileEntitySign)) {
            return Optional.absent();
        }

        return Optional.of((TileEntitySign) tileEntity);
    }

}
