package nl.rutgerkok.blocklocker.impl.nms;

public class NMSAccessorProvider {
    /**
     * Gets the appropriate server-specific code.
     *
     * @return The code.
     */
    public static ServerSpecific create() {
        try {
            return new OldNMSAccessor();
        } catch (Exception e) {
            try {
                return new NMS116Accessor();
            } catch (Exception e2) {
                return new NMSAccessor();
            }
        }
    }
}
