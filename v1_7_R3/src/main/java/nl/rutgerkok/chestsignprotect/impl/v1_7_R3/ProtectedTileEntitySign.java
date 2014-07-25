package nl.rutgerkok.chestsignprotect.impl.v1_7_R3;

import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.minecraft.server.v1_7_R3.TileEntitySign;

import com.google.common.base.Optional;

/**
 * Overrides the default sign to store a custom field, used for the UUID.
 *
 */
public class ProtectedTileEntitySign extends TileEntitySign {

    private static final String SAVE_KEY = "ChestSignProtect";

    private Optional<String> extraText = Optional.absent();

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType(SAVE_KEY, 10)) {
            extraText = Optional.of(nbttagcompound.getString(SAVE_KEY));
        }
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (extraText.isPresent()) {
            nbttagcompound.setString(SAVE_KEY, extraText.get());
        }
    }

    public String getExtraText() {
        return extraText.or("");
    }
}
