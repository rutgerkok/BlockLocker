package nl.rutgerkok.blocklocker.impl;

import java.util.List;
import java.util.function.Consumer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.TextComponent;

public class TextComponents {

    /**
     * Used to add legacy text to a list of text components, correctly inheriting
     * the formatting of previous parts.
     *
     * @param components
     *            The components.
     * @param message
     *            The message to add, may be full of legacy color codes.
     */
    public static void addLegacyText(List<BaseComponent> components, String message) {
        addLegacyText(components, message, part -> {
        });
    }

    /**
     * Used to add legacy text to a list of text components, correctly inheriting
     * the formatting of previous parts.
     *
     * @param components
     *            The components.
     * @param message
     *            The message to add, may be full of legacy color codes.
     * @param modifier
     *            A modifier, that is called on each individual component. Useful
     *            for adding tooltips for example.
     */
    public static void addLegacyText(List<BaseComponent> components, String message, Consumer<BaseComponent> modifier) {
        if (message.length() == 0) {
            return; // Nothing to do
        }

        BaseComponent previousPart;
        if (components.isEmpty() || message.startsWith(ChatColor.RESET.toString())) {
            previousPart = new TextComponent(); // Start fresh
        } else {
            previousPart = components.get(components.size() - 1); // Use this to copy formatting
        }

        BaseComponent[] parts = TextComponent.fromLegacyText(message, previousPart.getColor());

        for (BaseComponent part : parts) {
            part.copyFormatting(previousPart, FormatRetention.FORMATTING, false);

            modifier.accept(part);
            components.add(part);
        }
    }

    private TextComponents() {
        // No instances!
    }
}
