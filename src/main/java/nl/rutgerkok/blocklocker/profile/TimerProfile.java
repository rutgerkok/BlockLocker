package nl.rutgerkok.blocklocker.profile;

/**
 * A profile that allows nobody, but sets the time how long the door may be
 * open.
 *
 */
public interface TimerProfile extends Profile {

    /**
     * Gets the amount of ticks the door can be open before it is closed again.
     * 
     * @return The amount of ticks.
     */
    int getOpenSeconds();
}
