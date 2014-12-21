package nl.rutgerkok.chestsignprotect.protection;

/**
 * Represents a protection for a door.
 *
 */
public interface DoorProtection extends Protection {

    /**
     * Opens or closes the door. If the door is open, the door is closed and
     * vice versa.
     *
     */
    public void toggleOpen();
}
