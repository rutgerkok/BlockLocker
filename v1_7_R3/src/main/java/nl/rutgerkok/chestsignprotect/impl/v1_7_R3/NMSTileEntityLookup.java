package nl.rutgerkok.chestsignprotect.impl.v1_7_R3;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.server.v1_7_R3.TileEntity;
import net.minecraft.server.v1_7_R3.WorldServer;

import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;

import com.google.common.base.Optional;

/**
 * Implementation of methods required by
 * nl.rutgerkok.chestsignprotect.impl.NMSAccessor for Minecraft 1.7.8 and 1.7.9.
 *
 */
public class NMSTileEntityLookup {

    private static Map<String, Class<? extends TileEntity>> i;
    private static Map<Class<? extends TileEntity>, String> j;

    private static final void copyFields() throws IllegalArgumentException,
            IllegalAccessException, SecurityException, NoSuchFieldException {
        for (Field inOurClass : NMSTileEntityLookup.class.getDeclaredFields()) {
            Field inOtherClass = TileEntity.class.getDeclaredField(inOurClass
                    .getName());
            inOurClass.setAccessible(true);
            inOtherClass.setAccessible(true);
            inOurClass.set(null, inOtherClass.get(null));
        }
    }

    public static final Optional<String> getTextData(Sign sign) {
        CraftWorld world = (CraftWorld) sign.getWorld();
        WorldServer nmsWorld = world.getHandle();
        TileEntity tileEntity = nmsWorld.getTileEntity(sign.getX(),
                sign.getY(), sign.getZ());
        if (tileEntity instanceof ProtectedTileEntitySign) {
            String text = ((ProtectedTileEntitySign) tileEntity).getExtraText();
            return Optional.of(text);
        }
        return Optional.absent();
    }

    public static final void init() throws SecurityException,
            NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        copyFields();

        // Register our custom sign
        i.put("Sign", ProtectedTileEntitySign.class);
        j.put(ProtectedTileEntitySign.class, "Sign");
    }
}
