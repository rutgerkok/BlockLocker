package nl.rutgerkok.blocklocker;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bukkit.block.Block;

/** Represents all settings of the plugin. */
public interface ChestSettings extends ProtectableBlocksSettings {

  /**
   * Gets whether the given attack type is allowed to destroy protections.
   *
   * @param attackType The attack type.
   * @return True if the attack type is allowed to destroy protections, false otherwise.
   */
  boolean allowDestroyBy(AttackType attackType);

  /**
   * Returns whether containers of the same type require only one sign.
   *
   * @return True if containers are connected, false otherwise.
   */
  boolean getConnectContainers();

  /**
   * Gets the actual date that chests must have activity after. If a chest doesn't have activity
   * after this date, it is considered expired.
   *
   * @return The date, or absent if chests never expire.
   */
  Optional<Date> getChestExpireDate();

  /**
   * Gets the default amount of ticks a door stays open before closing automatically. When set to
   * less than 1, the door is never closed automatically. Players can override this value for a
   * door.
   *
   * @return The amount.
   */
  int getDefaultDoorOpenSeconds();

  /**
   * Gets a mutable list of objects that specify additional protectables. You can add your own
   * instances here (that implement {@link ProtectableBlocksSettings}).
   *
   * @return The mutable list.
   */
  List<ProtectableBlocksSettings> getExtraProtectables();

  /**
   * Gets the localized header for the given sign type, includes brackets and colors.
   *
   * @param signType The type of the sign.
   * @param header The first line of the sign, to see which header to return.
   * @return The header.
   */
  String getFancyLocalizedHeader(SignType signType, String header);

  /**
   * Gets the type of the protection.
   *
   * @param block The main protection block.
   * @return Type of the protection.
   */
  Optional<ProtectionType> getProtectionType(Block block);

  /**
   * Gets the localized headers for the given sign type, without colors.
   *
   * @param signType The type of the sign.
   * @return All possible headers.
   */
  List<String> getSimpleLocalizedHeaders(SignType signType);
}
