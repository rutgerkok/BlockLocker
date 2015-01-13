package nl.rutgerkok.blocklocker;

import nl.rutgerkok.blocklocker.protection.ContainerProtection;
import nl.rutgerkok.blocklocker.protection.DoorProtection;
import nl.rutgerkok.blocklocker.protection.TrapDoorProtection;

/**
 * The different types of protections.
 *
 */
public enum ProtectionType {
    /**
     * A container, represented by {@link ContainerProtection}.
     */
    CONTAINER,
    /**
     * A door, represented by {@link DoorProtection}.
     */
    DOOR,
    /**
     * A trap door, represented by {@link TrapDoorProtection}.
     */
    TRAP_DOOR;
}