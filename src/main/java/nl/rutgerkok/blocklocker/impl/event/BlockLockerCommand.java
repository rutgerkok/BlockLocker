package nl.rutgerkok.blocklocker.impl.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.protection.Protection;

public final class BlockLockerCommand implements TabExecutor {

    private final BlockLockerPlugin plugin;

    public BlockLockerCommand(BlockLockerPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return reloadCommand(sender);
        }
        return signChangeCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            return null;
        }
        if (args.length > 2) {
            return Collections.emptyList();
        }
        return Arrays.asList("2", "3", "4");
    }

    private boolean reloadCommand(CommandSender sender) {
        if (!sender.hasPermission(Permissions.CAN_RELOAD)) {
            plugin.getTranslator().sendMessage(sender, Translation.COMMAND_NO_PERMISSION);
            return true;
        }

        plugin.reload();
        plugin.getLogger().info(plugin.getTranslator().getWithoutColor(Translation.COMMAND_PLUGIN_RELOADED));
        if (!(sender instanceof ConsoleCommandSender)) {
            // Avoid sending message twice to the console
            plugin.getTranslator().sendMessage(sender, Translation.COMMAND_PLUGIN_RELOADED);
        }
        return true;
    }

    private boolean signChangeCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getTranslator().sendMessage(sender, Translation.COMMAND_CANNOT_BE_USED_BY_CONSOLE);
            return true;
        }

        Player player = (Player) sender;
        Optional<Sign> selectedSign = plugin.getSignSelector().getSelectedSign(player);
        if (!selectedSign.isPresent()) {
            plugin.getTranslator().sendMessage(player, Translation.COMMAND_NO_SIGN_SELECTED);
            return true;
        }

        if (args.length > 2) {
            return false;
        }

        // Parse line number
        int lineNumber;
        try {
            lineNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            if (args[0].equals("~")) {
                // Place on first available line, or else the last line
                lineNumber = selectedSign.get().getLine(2).isEmpty() ? 3 : 4;
            } else {
                return false;
            }
        }
        if (lineNumber < 2 || lineNumber > 4) {
            plugin.getTranslator().sendMessage(player, Translation.COMMAND_LINE_NUMBER_OUT_OF_BOUNDS);
            return true;
        }

        // Parse name
        String name = args.length > 1 ? args[1] : "";
        if (name.length() > 25) {
            plugin.getTranslator().sendMessage(player, Translation.COMMAND_PLAYER_NAME_TOO_LONG);
            return true;
        }

        // Check protection
        Sign sign = selectedSign.get();
        Optional<SignType> signType = plugin.getSignParser().getSignType(sign);
        Optional<Protection> protection = plugin.getProtectionFinder().findProtection(sign.getBlock());

        if (!protection.isPresent() || !signType.isPresent()) {
            plugin.getTranslator().sendMessage(player, Translation.COMMAND_SIGN_NO_LONGER_PART_OF_PROTECTION);
            return true;
        }

        // Check line number in combination with sign type
        if (signType.get().isMainSign() && lineNumber == 2 && !player.hasPermission(Permissions.CAN_BYPASS)) {
            plugin.getTranslator().sendMessage(player, Translation.COMMAND_CANNOT_EDIT_OWNER);
            return true;
        }

        // Update protection
        sign.setLine(lineNumber - 1, name);
        sign.update();
        plugin.getProtectionFinder().findProtection(sign.getBlock()).ifPresent(protectio -> {
            plugin.getProtectionUpdater().update(protection.get(), true);
        });
        plugin.getTranslator().sendMessage(player, Translation.COMMAND_UPDATED_SIGN);

        return true;
    }

}
