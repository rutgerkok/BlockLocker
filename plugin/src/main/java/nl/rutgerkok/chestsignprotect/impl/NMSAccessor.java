package nl.rutgerkok.chestsignprotect.impl;

import java.lang.reflect.Method;
import java.util.Collection;

import nl.rutgerkok.chestsignprotect.profile.Profile;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

/**
 * Internal class to retrieve hidden text on signs. Classes providing data for
 * {@link #getJsonData(Sign)} must have:
 *
 * <ul>
 * <li>a fully qualified name in the form
 * <code>nl.rutgerkok.chestsignprotect.impl.VERSION.NMSTileEntityLookup</code>,
 * as that is what is used to find the class.
 * <li>a public static method called getTextData with one argument of
 * {@link Sign}, that returns an {@link Optional} of type {@link String}.
 * <li>a public static method called init with no arguments and no return type.
 * <li>a public static method called setTextData with two arguments, one of type
 * {@link Sign} and a second one of type {@link String}.
 * </ul>
 */
public class NMSAccessor {

    private static final String GET_TEXT_DATA = "getTextData";
    private static final String INIT = "init";
    private static final String SET_TEXT_DATA = "setTextData";

    private static final String SIMPLE_CLASS_NAME = "NMSTileEntityLookup";

    private final Method getTextData;
    private final JSONParser parser;

    private final Method setTextData;

    NMSAccessor() throws NMSException {
        parser = new JSONParser();
        getTextData = fetchMethod(GET_TEXT_DATA, Sign.class);
        setTextData = fetchMethod(SET_TEXT_DATA, Sign.class, String.class);
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
    Optional<JSONArray> getJsonData(Sign sign) {
        try {
            @SuppressWarnings("unchecked")
            Optional<String> string = (Optional<String>) getTextData.invoke(
                    null, sign);
            if (string.isPresent()) {
                System.out.println("Parsing: " + string);
                return Optional.of((JSONArray) parser.parse(string.get()));
            }
            return Optional.absent();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    // ^ JSONArray is not generic
    public void setTextData(Sign sign, Collection<Profile> profiles) {
        JSONArray list = new JSONArray();
        for (Profile profile : profiles) {
            list.add(profile.getSaveObject());
        }
        String jsonString = list.toJSONString();
        System.out.println("Saving " + jsonString);
        setTextData0(sign, jsonString);
    }

    private void setTextData0(Sign sign, String string) {
        try {
            setTextData.invoke(null, sign, string);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
