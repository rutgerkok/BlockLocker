package nl.rutgerkok.blocklocker.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import nl.rutgerkok.blocklocker.TestPlayer;

import org.bukkit.entity.Player;
import org.junit.Test;

public class CombinedGroupSystemTest {

    private class ReturnsTrueGroupSystem extends GroupSystem {

        @Override
        public boolean isInGroup(Player player, String groupName) {
            return true;
        }

        @Override
        public boolean keepOnReload() {
            return true;
        }

    }

    private class ReturnsFalseGroupSystem extends GroupSystem {

        @Override
        public boolean isInGroup(Player player, String groupName) {
            return false;
        }

        @Override
        public boolean keepOnReload() {
            return false;
        }

    }

    @Test
    public void testEmptyGroupSystem() {
        assertFalse(new CombinedGroupSystem().isInGroup(new TestPlayer(), "GroupName"));
    }

    @Test
    public void testCombined() {
        CombinedGroupSystem combinedGroupSystem = new CombinedGroupSystem();
        combinedGroupSystem.addSystems(Arrays.asList(
                new ReturnsFalseGroupSystem(),
                new ReturnsTrueGroupSystem()));

        // Must return true, because ReturnsTrueGroupSystem returns true
        assertTrue(combinedGroupSystem.isInGroup(new TestPlayer(), "GroupName"));
    }

    @Test
    public void testSingleReturnsFalse() {
        CombinedGroupSystem combinedGroupSystem = new CombinedGroupSystem();
        combinedGroupSystem.addSystem(new ReturnsFalseGroupSystem());

        assertFalse(combinedGroupSystem.isInGroup(new TestPlayer(), "GroupName"));
    }

    @Test
    public void testReloadSurvivors() {
        GroupSystem keptOnReload = new ReturnsTrueGroupSystem();
        GroupSystem removedOnReload = new ReturnsFalseGroupSystem();

        // Add two systems, one surviving the reload, one not
        CombinedGroupSystem combinedGroupSystem = new CombinedGroupSystem();
        combinedGroupSystem.addSystem(keptOnReload);
        combinedGroupSystem.addSystem(removedOnReload);

        Collection<GroupSystem> survivors = combinedGroupSystem.getReloadSurvivors();

        // Survivors must be collection of exactly one group, the keptOnReload
        // group
        assertEquals(1, survivors.size());
        assertEquals(keptOnReload, survivors.iterator().next());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullSystems() {
        // Collections containing null systems are not allowed
        Collection<GroupSystem> containingNull = Arrays.asList(
                new ReturnsFalseGroupSystem(),
                new ReturnsTrueGroupSystem(),
                null);
        new CombinedGroupSystem().addSystems(containingNull);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullSystem() {
        // Null systems are not allowed
        new CombinedGroupSystem().addSystem(null);
    }
}
