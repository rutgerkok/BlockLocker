package nl.rutgerkok.blocklocker.protection;


/**
 * Represents a protection for a door. Keep in mind that trapdoors are doors
 * too.
 *
 */
public interface DoorProtection extends Protection {

    /**
     * When to play a sound for opening a door.
     *
     */
    enum SoundCondition {
        /**
         * Never play a sound.
         */
        NEVER,
        /**
         * Only play a sound for doors that Minecraft doesn't play a sound for a
         * player opens it.
         */
        AUTOMATIC,
        /**
         * Always play a sound.
         */
        ALWAYS
    }

    /**
     * Gets the amount of seconds the door should stay open, before closing
     * automatically. If no amount of seconds was specified on the door, -1 is
     * returned.
     *
     * @return The amount of ticks, or -1 if unspecified.
     */
    int getOpenSeconds();

    /**
     * Gets whether the door is currently opened. If only parts of the door are
     * open, the result of this method is undefined.
     * 
     * @return Whether the door is currently opened.
     */
    boolean isOpen();

    /**
     * Opens or closes the door, as specified. Plays no sound.
     *
     * @param open
     *            True to open the door, false otherwise.
     * @see #setOpen(boolean, SoundCondition) Control over sound playing.
     */
    void setOpen(boolean open);

    /**
     * Opens or closes the door, as specified.
     *
     * @param open
     *            True to open the door, false otherwise.
     * @param playSound Whether to play a sound.
     */
    void setOpen(boolean open, SoundCondition playSound);
}
