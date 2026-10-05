package nl.rutgerkok.blocklocker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Parses the messages of this plugin.
 *
 * <p>
 * A message may be written in two formats, which can even be mixed within a
 * single message:
 *
 * <ul>
 * <li><b>MiniMessage</b>, for example {@code <red>Hello <bold>world</bold>} or
 * {@code <gradient:red:blue>Rainbow</gradient>}. This is the recommended
 * format, see
 * <a href="https://docs.advntr.dev/minimessage/format.html">the MiniMessage
 * documentation</a> for everything that is possible.</li>
 * <li><b>Legacy color codes</b>, for example {@code &4Hello &lworld} or
 * {@code §4Hello §lworld}. This is the format that older versions of this
 * plugin used in all of their translation files, it keeps working so that
 * existing translation files don't need to be rewritten.</li>
 * </ul>
 */
public final class Messages {

    /** All legacy color and formatting codes, in the order of {@link #LEGACY_TAG_NAMES}. */
    private static final String LEGACY_CODES = "0123456789abcdefklmnor";

    /** The MiniMessage tag belonging to each code in {@link #LEGACY_CODES}. */
    private static final String[] LEGACY_TAG_NAMES = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white",
            "obfuscated", "bold", "strikethrough", "underlined", "italic", "reset"
    };

    private static final char SECTION_SIGN = '\u00a7';

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(SECTION_SIGN)
            .hexColors()
            .build();

    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private Messages() {
        // No instances
    }

    /**
     * Escapes the given plain text, so that MiniMessage doesn't read tags in it.
     * Use this for any text that comes from a player or from another plugin.
     *
     * @param text
     *            The text, may be null.
     * @return The escaped text, never null.
     */
    public static String escapeTags(String text) {
        if (text == null) {
            return "";
        }
        return MINI_MESSAGE.escapeTags(text);
    }

    /**
     * Replaces the placeholders {@code {0}}, {@code {1}}, ... in the given
     * MiniMessage template with the given parameters.
     *
     * <p>
     * The parameters are escaped, so that they cannot inject formatting or
     * MiniMessage tags.
     *
     * @param miniMessageTemplate
     *            The template, in MiniMessage format.
     * @param parameters
     *            The parameters. {@code {0}} is replaced by the first parameter,
     *            and so on.
     * @return The filled in template.
     */
    public static String fillIn(String miniMessageTemplate, String... parameters) {
        String result = miniMessageTemplate;
        for (int i = 0; i < parameters.length; i++) {
            result = result.replace("{" + i + "}", escapeTags(parameters[i]));
        }
        return result;
    }

    /**
     * Parses a message that is already in MiniMessage format.
     *
     * @param miniMessage
     *            The message.
     * @return The parsed component. If the message cannot be parsed, the message
     *         is returned as plain text.
     */
    public static Component fromMiniMessage(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return Component.empty();
        }
        try {
            return MINI_MESSAGE.deserialize(miniMessage);
        } catch (RuntimeException e) {
            // A broken message should never take down the plugin
            return Component.text(miniMessage);
        }
    }

    /**
     * Parses a message, supporting both MiniMessage tags and legacy color codes.
     *
     * @param message
     *            The message, may be null.
     * @return The parsed component, never null.
     */
    public static Component toComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        if (!containsFormatting(message)) {
            // Fast path, avoids parsing text without any formatting at all
            return Component.text(message);
        }
        return fromMiniMessage(toMiniMessage(message));
    }

    /**
     * Renders a message as plain text, without any colors or formatting.
     * MiniMessage tags and legacy color codes are both removed.
     *
     * @param component
     *            The message.
     * @return The message as plain text.
     */
    public static String toPlainText(Component component) {
        if (component == null) {
            return "";
        }
        return PLAIN_SERIALIZER.serialize(component);
    }

    /**
     * Renders a message as plain text, without any colors or formatting.
     *
     * @param message
     *            The message, may be null.
     * @return The message as plain text, never null.
     */
    public static String toPlainText(String message) {
        if (message == null) {
            return "";
        }
        if (!containsFormatting(message)) {
            // Fast path, avoids parsing text without any formatting at all
            return message;
        }
        return toPlainText(toComponent(message));
    }

    /**
     * Renders a message using section-sign color codes, the format that signs
     * use.
     *
     * @param component
     *            The message.
     * @return The message, using section-sign color codes.
     */
    public static String toLegacyText(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Renders a message using section-sign color codes, the format that signs
     * use.
     *
     * @param message
     *            The message, may be null.
     * @return The message, using section-sign color codes, never null.
     */
    public static String toLegacyText(String message) {
        return toLegacyText(toComponent(message));
    }

    /**
     * Converts all legacy color codes ({@code &4}, {@code §4}, {@code &#rrggbb},
     * {@code &x&r&r&g&g&b&b}) in the given message to their MiniMessage
     * equivalents. MiniMessage tags that are already in the message are left
     * alone.
     *
     * @param message
     *            The message, may be null.
     * @return The message with MiniMessage tags, never null.
     */
    public static String toMiniMessage(String message) {
        if (message == null) {
            return "";
        }

        StringBuilder result = new StringBuilder(message.length() + 16);
        int i = 0;
        while (i < message.length()) {
            char current = message.charAt(i);
            if (current != '&' && current != SECTION_SIGN) {
                result.append(current);
                i++;
                continue;
            }

            // &#rrggbb
            if (i + 7 < message.length() && message.charAt(i + 1) == '#' && isHex(message, i + 2, 6)) {
                result.append("<#").append(message, i + 2, i + 8).append('>');
                i += 8;
                continue;
            }

            // &x&r&r&g&g&b&b
            if (i + 13 < message.length() && (message.charAt(i + 1) == 'x' || message.charAt(i + 1) == 'X')
                    && isRepeatedHex(message, i)) {
                result.append("<#");
                for (int digit = 0; digit < 6; digit++) {
                    result.append(message.charAt(i + 3 + digit * 2));
                }
                result.append('>');
                i += 14;
                continue;
            }

            // &4, &l, and so on
            if (i + 1 < message.length()) {
                int index = LEGACY_CODES.indexOf(Character.toLowerCase(message.charAt(i + 1)));
                if (index >= 0) {
                    result.append('<').append(LEGACY_TAG_NAMES[index]).append('>');
                    i += 2;
                    continue;
                }
            }

            result.append(current);
            i++;
        }
        return result.toString();
    }

    /**
     * Parses a message and fills in the given parameters, supporting both
     * MiniMessage tags and legacy color codes.
     *
     * @param message
     *            The message, may be null.
     * @param parameters
     *            The parameters. {@code {0}} is replaced by the first parameter,
     *            and so on. Parameters are escaped, so that they cannot inject
     *            formatting.
     * @return The parsed component, never null.
     */
    public static Component render(String message, String... parameters) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return fromMiniMessage(fillIn(toMiniMessage(message), parameters));
    }

    private static boolean containsFormatting(String message) {
        return message.indexOf('&') >= 0 || message.indexOf(SECTION_SIGN) >= 0 || message.indexOf('<') >= 0;
    }

    private static boolean isHex(String message, int start, int length) {
        for (int i = 0; i < length; i++) {
            if (Character.digit(message.charAt(start + i), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRepeatedHex(String message, int start) {
        for (int digit = 0; digit < 6; digit++) {
            char separator = message.charAt(start + 2 + digit * 2);
            if (separator != '&' && separator != SECTION_SIGN) {
                return false;
            }
            if (Character.digit(message.charAt(start + 3 + digit * 2), 16) < 0) {
                return false;
            }
        }
        return true;
    }
}
