package nl.rutgerkok.blocklocker.impl.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.google.common.base.Optional;

/**
 * Implementation of methods required by
 * nl.rutgerkok.chestsignprotect.impl.NMSAccessor for Minecraft 1.7.8 and 1.7.9.
 *
 */
public final class NMSAccessor implements ServerSpecific {



    static Object call(Object on, Method method, Object... parameters) {
        try {
            return method.invoke(on, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Object enumField(Class<Enum<?>> enumClass, String name) {
        try {
            Method valueOf = getMethod(Enum.class, "valueOf", Class.class, String.class);
            return invokeStatic(valueOf, enumClass, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        try {
            return clazz.getConstructor(paramTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getField(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(name, parameterTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the Minecraft class version of the server, like "v1_8_R2".
     *
     * @return Minecraft class version.
     */
    private static String getMinecraftClassVersion() {
        String serverClassName = Bukkit.getServer().getClass().getName();
        String version = serverClassName.split("\\.")[3];
        if (!version.startsWith("v")) {
            throw new AssertionError("Failed to detect Minecraft version, found " + version + " in " + serverClassName);
        }
        return version;
    }

    static Object invokeStatic(Method method, Object... parameters) {
        return call(null, method, parameters);
    }

    static Object newInstance(Constructor<?> constructor, Object... params) {
        try {
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Object retrieve(Object on, Field field) {
        try {
            return field.get(on);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final String nmsPrefix;

    private final String obcPrefix;

    final Class<?> BlockPosition;
    final Constructor<?> BlockPosition_new;
    final Class<?> ChatComponentText;
    final Constructor<?> ChatComponentText_new;
    final Class<?> ChatHoverable;
    final Method ChatHoverable_getChatComponent;
    final Constructor<?> ChatHoverable_new;
    final Class<?> ChatModifier;
    final Method ChatModifier_getChatHoverable;
    final Constructor<?> ChatModifier_new;
    final Class<?> CraftChatMessage;
    final Method CraftChatMessage_fromComponent;
    final Class<?> CraftWorld;
    final Method CraftWorld_getHandle;
    final Class<Enum<?>> EnumHoverAction;
    final Object EnumHoverAction_SHOW_TEXT;
    final Class<?> IChatBaseComponent;
    final Method IChatBaseComponent_getChatModifier;
    final Method ChatModifier_setChatHoverable;
    final Class<?> TileEntitySign;
    final Field TileEntitySign_lines;
    final Class<?> WorldServer;
    final Method WorldServer_getTileEntity;

    public NMSAccessor() {
        String version = getMinecraftClassVersion();
        nmsPrefix = "net.minecraft.server." + version + ".";
        obcPrefix = "org.bukkit.craftbukkit." + version + ".";

        BlockPosition = getNMSClass("BlockPosition");
        WorldServer = getNMSClass("WorldServer");
        ChatModifier = getNMSClass("ChatModifier");
        ChatHoverable = getNMSClass("ChatHoverable");
        IChatBaseComponent = getNMSClass("IChatBaseComponent");
        EnumHoverAction = getAnyNMSEnum("EnumHoverAction", "ChatHoverable$EnumHoverAction");
        TileEntitySign = getNMSClass("TileEntitySign");
        ChatComponentText = getNMSClass("ChatComponentText");

        CraftWorld = getOBCClass("CraftWorld");
        CraftChatMessage = getOBCClass("util.CraftChatMessage");

        CraftWorld_getHandle = getMethod(CraftWorld, "getHandle");
        CraftChatMessage_fromComponent = getMethod(CraftChatMessage, "fromComponent", IChatBaseComponent);
        WorldServer_getTileEntity = getMethod(WorldServer, "getTileEntity", BlockPosition);
        IChatBaseComponent_getChatModifier = getMethod(IChatBaseComponent, "getChatModifier");
        ChatModifier_setChatHoverable = getMethod(ChatModifier, "setChatHoverable", ChatHoverable);
        ChatModifier_getChatHoverable = getMethod(ChatModifier, "i");
        ChatHoverable_getChatComponent = getMethod(ChatHoverable, "b");

        ChatComponentText_new = getConstructor(ChatComponentText, String.class);
        BlockPosition_new = getConstructor(BlockPosition, int.class, int.class, int.class);
        ChatModifier_new = getConstructor(ChatModifier);
        ChatHoverable_new = getConstructor(ChatHoverable, EnumHoverAction, IChatBaseComponent);

        TileEntitySign_lines = getField(TileEntitySign, "lines");

        EnumHoverAction_SHOW_TEXT = enumField(EnumHoverAction, "SHOW_TEXT");
    }

    private String chatComponentToString(Object chatComponent) {
        return (String) invokeStatic(CraftChatMessage_fromComponent, chatComponent);
    }

    Class<Enum<?>> getAnyNMSEnum(String... possibleNames) {
        Exception lastException = null;
        for (String name : possibleNames) {
            try {
                return getNMSEnum(name);
            } catch (Exception e) {
                lastException = e;
            }
        }
        throw new RuntimeException(lastException);
    }

    Object getBlockPosition(int x, int y, int z) {
        return newInstance(BlockPosition_new, x, y, z);
    }

    @Override
    public JsonSign getJsonData(World world, int x, int y, int z) {
        // Find sign
        Optional<?> nmsSign = toNmsSign(world, x, y, z);
        if (!nmsSign.isPresent()) {
            return JsonSign.EMPTY;
        }

        // Find strings stored in hovertext
        Optional<String> secretData = getSecretData(nmsSign.get());
        if (!secretData.isPresent()) {
            return JsonSign.EMPTY;
        }

        // Find first line
        Object firstLineObj = ((Object[]) retrieve(nmsSign.get(), TileEntitySign_lines))[0];
        String firstLine = firstLineObj == null ? "" : chatComponentToString(firstLineObj);

        // Parse and sanitize the sting
        Object data = JSONValue.parse(secretData.get());
        if (data instanceof JSONArray) {
            return new JsonSign(firstLine, (JSONArray) data);
        }
        return JsonSign.EMPTY;
    }

    Class<?> getNMSClass(String name) {
        try {
            return Class.forName(nmsPrefix + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    Class<Enum<?>> getNMSEnum(String name) {
        Class<?> clazz = getNMSClass(name);
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException(clazz + " is not an enum");
        }
        @SuppressWarnings("unchecked")
        Class<Enum<?>> enumClazz = (Class<Enum<?>>) clazz;
        return enumClazz;
    }

    Class<?> getOBCClass(String name) {
        try {
            return Class.forName(obcPrefix + name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<String> getSecretData(Object tileEntitySign) {
        Object line = ((Object[]) retrieve(tileEntitySign, TileEntitySign_lines))[0];
        if (line == null) {
            return Optional.absent();
        }

        Object chatModifier = call(line, IChatBaseComponent_getChatModifier);
        if (chatModifier == null) {
            return Optional.absent();
        }

        Object chatHoverable = call(chatModifier, ChatModifier_getChatHoverable);
        if (chatHoverable == null) {
            return Optional.absent();
        }

        return Optional.of(chatComponentToString(call(chatHoverable, ChatHoverable_getChatComponent)));
    }

    @Override
    public void setJsonData(Sign sign, JSONArray jsonArray) {
        Optional<?> nmsSign = toNmsSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        if (!nmsSign.isPresent()) {
            throw new RuntimeException("No sign at " + sign.getLocation());
        }

        setSecretData(nmsSign.get(), jsonArray.toJSONString());
    }

    private void setSecretData(Object tileEntitySign, String data) {
        Object line = ((Object[]) retrieve(tileEntitySign, TileEntitySign_lines))[0];
        Object modifier = call(line, IChatBaseComponent_getChatModifier);
        if (modifier == null) {
            modifier = newInstance(ChatModifier_new);
        }
        Object chatComponentText = newInstance(ChatComponentText_new, data);
        Object hoverable = newInstance(ChatHoverable_new, EnumHoverAction_SHOW_TEXT, chatComponentText);
        call(modifier, ChatModifier_setChatHoverable, hoverable);
    }

    private Optional<?> toNmsSign(World world, int x, int y, int z) {
        Object nmsWorld = call(world, CraftWorld_getHandle);

        Object tileEntity = call(nmsWorld, WorldServer_getTileEntity, getBlockPosition(x, y, z));
        if (!TileEntitySign.isInstance(tileEntity)) {
            return Optional.absent();
        }

        return Optional.of(tileEntity);
    }

}
