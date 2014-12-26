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
     * Gets whether the door is currently opened. If only parts of the door are
     * open, the result of this method is undefined.
     * 
     * @return Whether the door is currently opened.
     */
    boolean isOpen();

    /**
     * Gets the amount of seconds the door should stay open, before closing
     * automatically. If no amount of seconds was specified on the door, -1 is
     * returned.
     *
     * @return The amount of ticks, or -1 if unspecified.
     */
    int getOpenSeconds();
}
