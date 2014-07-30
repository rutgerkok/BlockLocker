package nl.rutgerkok.chestsignprotect.impl.v1_7_R3;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.server.v1_7_R3.TileEntity;
import net.minecraft.server.v1_7_R3.TileEntitySign;
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

    private static final ProtectedTileEntitySign getHandle(Sign sign) {
        WorldServer nmsWorld = ((CraftWorld) sign.getWorld()).getHandle();

        TileEntity maybeSign = nmsWorld.getTileEntity(sign.getX(), sign.getY(),
                sign.getZ());

        // Sign of our type
        if (maybeSign instanceof ProtectedTileEntitySign) {
            return (ProtectedTileEntitySign) maybeSign;
        }

        // Sign of vanilla type, happens for player placed signs
        if (maybeSign instanceof TileEntitySign) {
            ProtectedTileEntitySign newSign = new ProtectedTileEntitySign();

            nmsWorld.setTileEntity(sign.getX(), sign.getY(), sign.getZ(),
                    newSign);
            return newSign;
        }

        // No sign
        return null;
    }

    public static final Optional<String> getTextData(Sign sign) {
        TileEntity tileEntity = getHandle(sign);
        if (tileEntity instanceof ProtectedTileEntitySign) {
            return ((ProtectedTileEntitySign) tileEntity).getExtraText();
        }
        return Optional.absent();
    }

    public static final void setTextData(Sign sign, String data) {
        TileEntity tileEntity = getHandle(sign);
        if (tileEntity instanceof ProtectedTileEntitySign) {
            ((ProtectedTileEntitySign) tileEntity).setExtraText(data);
        }
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
