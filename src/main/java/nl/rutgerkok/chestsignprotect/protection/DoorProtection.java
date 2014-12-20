package nl.rutgerkok.chestsignprotect.protection;

/**
 * Represents a protection for a door.
 *
 */
public interface DoorProtection extends Protection {

    /**
     * Opens or closes the door.
     * 
     * @param open
     *            True to open the door, false to close the door.
     */
    public void setOpen(boolean open);
}
