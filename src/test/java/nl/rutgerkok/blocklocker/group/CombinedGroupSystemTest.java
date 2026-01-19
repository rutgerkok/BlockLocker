package nl.rutgerkok.blocklocker.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import nl.rutgerkok.blocklocker.TestPlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CombinedGroupSystemTest {

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

  @Test
  public void testAddNullSystem() {
    // Null systems are not allowed
    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          new CombinedGroupSystem().addSystem(null);
        });
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testAddNullSystems() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          // Collections containing null systems are not allowed
          Collection<GroupSystem> containingNull =
              Arrays.asList(new ReturnsFalseGroupSystem(), new ReturnsTrueGroupSystem(), null);
          new CombinedGroupSystem().addSystems(containingNull);
        });
  }

  @Test
  public void testCombined() {
    CombinedGroupSystem combinedGroupSystem = new CombinedGroupSystem();
    combinedGroupSystem.addSystem(new ReturnsFalseGroupSystem());
    combinedGroupSystem.addSystem(new ReturnsTrueGroupSystem());

    // Must return true, because ReturnsTrueGroupSystem returns true
    assertTrue(combinedGroupSystem.isInGroup(TestPlayer.create(), "GroupName"));
  }

  @Test
  public void testEmptyGroupSystem() {
    assertFalse(new CombinedGroupSystem().isInGroup(TestPlayer.create(), "GroupName"));
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

  @Test
  public void testSingleReturnsFalse() {
    CombinedGroupSystem combinedGroupSystem = new CombinedGroupSystem();
    combinedGroupSystem.addSystem(new ReturnsFalseGroupSystem());

    assertFalse(combinedGroupSystem.isInGroup(TestPlayer.create(), "GroupName"));
  }
}
