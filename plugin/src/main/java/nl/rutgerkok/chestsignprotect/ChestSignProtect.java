package nl.rutgerkok.chestsignprotect;

public interface ChestSignProtect {
    /**
     * Gets the profile factory, used to create profiles.
     *
     * @return The profile factory.
     */
    ProfileFactory getProfileFactory();

    /**
     * Gets the protection finder, used to find protections in the world.
     *
     * @return The protection finder.
     */
    ProtectionFinder getProtectionFinder();
}
