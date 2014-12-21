package nl.rutgerkok.blocklocker.impl.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.blocklocker.BlockLockerPlugin;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public final class BlockLockerCommand implements TabExecutor {

    private final BlockLockerPlugin plugin;

    public BlockLockerCommand(BlockLockerPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin);
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

        if (args.length != 2 && args.length != 1) {
            return false;
        }

        // Parse line number
        int lineNumber;
        try {
            lineNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (lineNumber < 2 || lineNumber > 4) {
            plugin.getTranslator().sendMessage(player, Translation.COMMAND_LINE_NUMBER_OUT_OF_BOUNDS);
            return true;
        }

        // Parse name
        String name = args.length > 1 ? args[1] : "";
        if (name.length() > 16) {
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
        if (signType.get().isMainSign() && lineNumber == 2) {
            plugin.getTranslator().sendMessage(player, Translation.COMMAND_CANNOT_EDIT_OWNER);
            return true;
        }

        // Update protection
        sign.setLine(lineNumber - 1, name);
        sign.update();
        plugin.fixMissingUniqueIds(protection.get());
        plugin.getTranslator().sendMessage(player, Translation.COMMAND_UPDATED_SIGN);

        return true;
    }

}
