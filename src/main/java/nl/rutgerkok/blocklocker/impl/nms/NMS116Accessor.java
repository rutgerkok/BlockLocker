package nl.rutgerkok.blocklocker.impl.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Implementation of methods required by
 * nl.rutgerkok.chestsignprotect.impl.NMSAccessor for Minecraft 1.16.
 *
 */
public final class NMS116Accessor implements ServerSpecific {

    static Object call(Object on, Method method, Object... parameters) {
        try {
            return method.invoke(on, parameters);
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

    static Object getStaticFieldValue(Class<?> clazz, Class<?> typeOfField) {
        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC
                        && typeOfField.isAssignableFrom(field.getType())) {
                    return field.get(null);
                }
            } catch (SecurityException | IllegalAccessException e) {
                // Ignore, if we're only looking for public fields we can safely ignore errors
                // on accessing private fields.
                // If we're accessing a private field, we'll get an error at the end of the
                // method.
            }

        }
        throw new RuntimeException("No accessible static field found on " + clazz + " of type " + typeOfField);
    }

    static Object getStaticFieldValue(Class<?> clazz, String name) {
        try {
            return getField(clazz, name).get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
    final Method ChatHoverable_getContents;
    final Constructor<?> ChatHoverable_new;
    final Class<?> ChatModifier;
    final Method ChatModifier_getGetHoverEvent;
    final Object ChatModifier_defaultModifier;
    final Class<?> CraftChatMessage;
    final Method CraftChatMessage_fromComponent;
    final Class<?> CraftWorld;
    final Method CraftWorld_getHandle;
    /**
     * Since Minecraft 1.16, this is not actually an enum anymore. Spigot didn't
     * update the name, however.
     */
    final Class<?> EnumHoverAction;
    final Object EnumHoverAction_SHOW_TEXT;
    final Class<?> IChatBaseComponent;
    final Method IChatBaseComponent_getChatModifier;
    final Method IChatBaseComponent_mutableCopy;
    final Class<?> IChatMutableComponent;
    final Method IChatMutableComponent_setChatModifier;
    final Method ChatModifier_setChatHoverable;
    final Class<?> TileEntitySign;
    final Field TileEntitySign_lines;
    final Class<?> WorldServer;
    final Method WorldServer_getTileEntity;

    public NMS116Accessor() {
        String version = getMinecraftClassVersion();
        nmsPrefix = "net.minecraft.server." + version + ".";
        obcPrefix = "org.bukkit.craftbukkit." + version + ".";

        BlockPosition = getNMSClass("BlockPosition");
        WorldServer = getNMSClass("WorldServer");
        ChatModifier = getNMSClass("ChatModifier");
        ChatHoverable = getNMSClass("ChatHoverable");
        IChatBaseComponent = getNMSClass("IChatBaseComponent");
        IChatMutableComponent = getNMSClass("IChatMutableComponent");
        EnumHoverAction = getNMSClass("ChatHoverable$EnumHoverAction");
        TileEntitySign = getNMSClass("TileEntitySign");
        ChatComponentText = getNMSClass("ChatComponentText");

        CraftWorld = getOBCClass("CraftWorld");
        CraftChatMessage = getOBCClass("util.CraftChatMessage");

        CraftWorld_getHandle = getMethod(CraftWorld, "getHandle");
        CraftChatMessage_fromComponent = getMethod(CraftChatMessage, "fromComponent", IChatBaseComponent);
        WorldServer_getTileEntity = getMethod(WorldServer, "getTileEntity", BlockPosition);
        IChatBaseComponent_getChatModifier = getMethod(IChatBaseComponent, "getChatModifier");
        IChatBaseComponent_mutableCopy = getMethod(IChatBaseComponent, "mutableCopy");
        IChatMutableComponent_setChatModifier = getMethod(IChatMutableComponent, "setChatModifier", ChatModifier);
        ChatModifier_setChatHoverable = getMethod(ChatModifier, "setChatHoverable", ChatHoverable);
        ChatModifier_getGetHoverEvent = getMethod(ChatModifier, "getHoverEvent");
        ChatHoverable_getContents = getMethod(ChatHoverable, "a", EnumHoverAction);

        ChatComponentText_new = getConstructor(ChatComponentText, String.class);
        BlockPosition_new = getConstructor(BlockPosition, int.class, int.class, int.class);
        ChatHoverable_new = getConstructor(ChatHoverable, EnumHoverAction, Object.class);

        ChatModifier_defaultModifier = getStaticFieldValue(ChatModifier, ChatModifier);
        TileEntitySign_lines = getField(TileEntitySign, "lines");

        EnumHoverAction_SHOW_TEXT = getStaticFieldValue(EnumHoverAction, "SHOW_TEXT");
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
        JsonElement data = JsonParser.parseString(secretData.get());
        if (data.isJsonArray()) {
            return new JsonSign(firstLine, data.getAsJsonArray());
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
            return Optional.empty();
        }

        Object chatModifier = call(line, IChatBaseComponent_getChatModifier);
        if (chatModifier == null) {
            return Optional.empty();
        }

        Object chatHoverable = call(chatModifier, ChatModifier_getGetHoverEvent);
        if (chatHoverable == null) {
            return Optional.empty();
        }

        return Optional
                .of(chatComponentToString(call(chatHoverable, ChatHoverable_getContents, EnumHoverAction_SHOW_TEXT)));
    }

    @Override
    public void setJsonData(Sign sign, JsonArray jsonArray) {
        Optional<?> nmsSign = toNmsSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        if (!nmsSign.isPresent()) {
            throw new RuntimeException("No sign at " + sign.getLocation());
        }

        setSecretData(nmsSign.get(), jsonArray.toString());
    }

    private void setSecretData(Object tileEntitySign, String data) {
        Object[] lines = ((Object[]) retrieve(tileEntitySign, TileEntitySign_lines));
        Object line = lines[0];
        Object modifier = call(line, IChatBaseComponent_getChatModifier);
        if (modifier == null) {
            modifier = ChatModifier_defaultModifier;
        }
        Object chatComponentText = newInstance(ChatComponentText_new, data);
        Object hoverable = newInstance(ChatHoverable_new, EnumHoverAction_SHOW_TEXT, chatComponentText);
        modifier = call(modifier, ChatModifier_setChatHoverable, hoverable);
        Object mutableLine = call(line, IChatBaseComponent_mutableCopy);
        call(mutableLine, IChatMutableComponent_setChatModifier, modifier);
        lines[0] = mutableLine;
    }

    private Optional<?> toNmsSign(World world, int x, int y, int z) {
        Object nmsWorld = call(world, CraftWorld_getHandle);

        Object tileEntity = call(nmsWorld, WorldServer_getTileEntity, getBlockPosition(x, y, z));
        if (!TileEntitySign.isInstance(tileEntity)) {
            return Optional.empty();
        }

        return Optional.of(tileEntity);
    }

}
