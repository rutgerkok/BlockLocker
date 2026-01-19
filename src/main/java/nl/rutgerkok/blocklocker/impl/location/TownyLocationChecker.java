package nl.rutgerkok.blocklocker.impl.location;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.location.IllegalLocationException;
import nl.rutgerkok.blocklocker.location.LocationChecker;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TownyLocationChecker implements LocationChecker {

  /**
   * Tests if the Towny plugin is installed.
   *
   * @return True if the factions plugin is installed, false otherwise.
   */
  public static boolean isAvailable() {
    try {
      JavaPlugin.getProvidingPlugin(Towny.class);
      return true;
    } catch (NoClassDefFoundError e) {
      return false;
    }
  }

  @Override
  public void checkLocation(Player player, Block block) throws IllegalLocationException {
    if (TownyAPI.getInstance().isWilderness(block)) {
      throw new IllegalLocationException(Translation.PROTECTION_IN_WILDERNESS);
    }
  }

  @Override
  public boolean keepOnReload() {
    return false; // built-in, so it will be re-added on reload
  }
}
