package nl.rutgerkok.chestsignprotect;

/**
 * The different types of signs.
 *
 */
public enum SignType {
    MORE_USERS, PRIVATE;

    /**
     * Gets whether this sign is a main sign: the sign type where exactly
     * one is required for each protection.
     *
     * @return True if this is a main sign, false otherwise.
     */
    public boolean isMainSign() {
        return this == PRIVATE;
    }
}