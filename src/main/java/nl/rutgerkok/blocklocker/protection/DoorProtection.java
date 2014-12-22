package nl.rutgerkok.blocklocker.protection;


/**
 * Represents a protection for a door.
 *
 */
public interface DoorProtection extends Protection {

    /**
     * Opens or closes the door, as specified.
     *
     * @param open
     *            True to open the door, false otherwise.
     */
    void setOpen(boolean open);

    /**
     * Opens or closes the door. If the door is open, the door is closed and
     * vice versa.
     *
     */
    void toggleOpen();

    boolean isOpen();

    int getOpenTicks();
}
