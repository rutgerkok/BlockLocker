package nl.rutgerkok.blocklocker.impl.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;

import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.Translator.Translation;

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
        return false;
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



}
