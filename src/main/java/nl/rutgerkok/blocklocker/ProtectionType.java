package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.protection.AttachedProtection;
import nl.rutgerkok.blocklocker.protection.ContainerProtection;
import nl.rutgerkok.blocklocker.protection.DoorProtection;

/** The different types of protections. */
public enum ProtectionType {
  /** A container, represented by {@link ContainerProtection}. */
  CONTAINER,
  /** A door, represented by {@link DoorProtection}. */
  DOOR,
  /**
   * A block where signs can also be attached to the block it is hanging/standing on. Represented
   * {@link AttachedProtection}.
   */
  ATTACHABLE;
}
