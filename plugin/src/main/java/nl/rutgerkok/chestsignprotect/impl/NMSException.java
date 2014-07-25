package nl.rutgerkok.chestsignprotect.impl;

import org.apache.commons.lang.Validate;

/**
 * Thrown when an action could not be completed due to missing NMS access.
 *
 */
public class NMSException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String className;

    /**
     * Creates a new NMSException for the given class name that was not found.
     *
     * @param className
     *            The class name that was not found.
     */
    public NMSException(String className) {
        super("Missing class: " + className);
        Validate.notNull(className);
        this.className = className;
    }

    /**
     * Gets the name of the class that was not found.
     *
     * @return The name.
     */
    public String getClassName() {
        return className;
    }

}
