package nl.rutgerkok.chestsignprotect.impl;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

/**
 * Internal class to retrieve hidden text on signs. Classes providing data for
 * {@link #getTextData(Sign)} must have:
 *
 * <ul>
 * <li>a fully qualified name in the form
 * <code>nl.rutgerkok.chestsignprotect.impl.VERSION.NMSTileEntityLookup</code>,
 * as that is what is used to find the class.
 * <li>a public static method called getTextData with one argument of
 * {@link Sign}, that returns an {@link Optional} of type {@link String}.
 * <li>a public static method called init with no arguments and no return type.
 * </ul>
 */
class NMSAccessor {

    private static final String GET_TEXT_DATA = "getTextData";

    private static final String INIT = "init";

    private static final String SIMPLE_CLASS_NAME = "NMSTileEntityLookup";
    private final Method getTextData;

    NMSAccessor() throws NMSException {
        getTextData = fetchMethod(GET_TEXT_DATA, Sign.class);
        try {
            fetchMethod(INIT).invoke(null);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Method fetchMethod(String name, Class<?>... parameters)
            throws NMSException {
        try {
            Class<?> clazz = Class.forName(getClassName());
            return clazz.getMethod(name, parameters);
        } catch (ClassNotFoundException e) {
            throw new NMSException(getClassName());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private String getClassName() {
        String bukkitClass = Bukkit.getServer().getClass().getName();
        String version = bukkitClass.replace("org.bukkit.craftbukkit.", "")
                .replace(".CraftServer", "");
        return getClass().getPackage().getName() + "." + version + "."
                + SIMPLE_CLASS_NAME;
    }

    /**
     * Gets the hidden text data at the given block.
     *
     * @param sign
     *            The sign to get the text data at.
     * @return The text data.
     */
    Optional<String> getTextData(Sign sign) {
        try {
            @SuppressWarnings("unchecked")
            Optional<String> returnValue = (Optional<String>) getTextData
                    .invoke(null, sign);
            return returnValue;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
