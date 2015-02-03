package nl.rutgerkok.blocklocker.impl.nms;

import static org.junit.Assert.assertTrue;

import org.bukkit.Location;
import org.junit.Before;
import org.junit.Test;

public class NMSAccessorTest {

    @Before
    public void setupGameRegitry() {
        // Initialize game registry first
        Class<?> dispenserRegistry = NMSAccessor.getNMSClass("DispenserRegistry");
        NMSAccessor.invokeStatic(NMSAccessor.getMethod(dispenserRegistry, "c"));
    }

    @Test
    public void testGetBlockPos() {
        NMSAccessor accessor = new NMSAccessor();
        Object blockPosition = accessor.getBlockPosition(new Location(null, 0, 0, 0));
        assertTrue(accessor.BlockPosition.isInstance(blockPosition));
    }

}
