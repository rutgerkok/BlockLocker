package nl.rutgerkok.blocklocker.location;

import java.util.Objects;

import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.Translator.Translation;

public final class IllegalLocationException extends Exception {

    private static final long serialVersionUID = -8935206826802987169L;

    private final Translation translation;

    /**
     * Constructor used if you don't want a translated message.
     *
     * @param message
     *            The message.
     */
    public IllegalLocationException(String message) {
        super(Objects.requireNonNull(message, "message"));
        this.translation = null;
    }

    /**
     * Constructor used if you want a translated message.
     *
     * @param translation
     *            The translation.
     */
    public IllegalLocationException(Translation translation) {
        this.translation = Objects.requireNonNull(translation, "translation");
    }

    /**
     * Gets a translated message, or the original message if no translation was
     * specified.
     *
     * @param translator
     *            The translator to use.
     * @return The message.
     */
    public String getTranslatedMessage(Translator translator) {
        if (this.translation != null) {
            return translator.get(translation);
        }
        return getLocalizedMessage();
    }
}
