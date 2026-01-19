package nl.rutgerkok.blocklocker.impl.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import nl.rutgerkok.blocklocker.impl.ProtectionLimitManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/** Commands for managing protection limits using standard Bukkit API. */
public final class ProtectionLimitCommands implements CommandExecutor, TabCompleter {

  private final BlockLockerPluginImpl plugin;

  public ProtectionLimitCommands(BlockLockerPluginImpl plugin) {
    this.plugin = plugin;
  }

  /** Registers commands by setting executor and tab completer. */
  public void register() {
    plugin.getCommand("blocklocker").setExecutor(this);
    plugin.getCommand("blocklocker").setTabCompleter(this);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sendHelp(sender);
      return true;
    }

    // /blocklocker reload
    if (args[0].equalsIgnoreCase("reload")) {
      return reloadCommand(sender);
    }

    // /blocklocker limit ...
    if (args[0].equalsIgnoreCase("limit")) {
      if (!sender.hasPermission("blocklocker.admin")) {
        sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
        return true;
      }

      if (args.length < 2) {
        sendHelp(sender);
        return true;
      }

      String subcommand = args[1].toLowerCase();
      switch (subcommand) {
        case "player":
          return handlePlayerCommand(sender, Arrays.copyOfRange(args, 2, args.length));
        case "team":
          return handleTeamCommand(sender, Arrays.copyOfRange(args, 2, args.length));
        case "default":
          return handleDefaultCommand(sender, Arrays.copyOfRange(args, 2, args.length));
        default:
          sendHelp(sender);
          return true;
      }
    }

    return false;
  }

  private boolean reloadCommand(CommandSender sender) {
    if (!sender.hasPermission(Permissions.CAN_RELOAD)) {
      plugin.getTranslator().sendMessage(sender, Translation.COMMAND_NO_PERMISSION);
      return true;
    }

    plugin.reload();
    plugin
        .getLogger()
        .info(plugin.getTranslator().getWithoutColor(Translation.COMMAND_PLUGIN_RELOADED));
    if (!(sender instanceof ConsoleCommandSender)) {
      // Avoid sending message twice to the console
      plugin.getTranslator().sendMessage(sender, Translation.COMMAND_PLUGIN_RELOADED);
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (!sender.hasPermission("blocklocker.admin")) {
      return Collections.emptyList();
    }

    if (args.length == 1) {
      return Arrays.asList("limit", "reload");
    }

    if (args.length == 2 && args[0].equalsIgnoreCase("limit")) {
      return Arrays.asList("player", "team", "default");
    }

    if (args.length == 3 && args[0].equalsIgnoreCase("limit")) {
      if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("team")) {
        return Arrays.asList("set", "remove", "check");
      }
      if (args[1].equalsIgnoreCase("default")) {
        return Arrays.asList("player", "team");
      }
    }

    if (args.length == 4) {
      if (args[0].equalsIgnoreCase("limit") && args[1].equalsIgnoreCase("player")) {
        List<String> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
          players.add(p.getName());
        }
        return players;
      }
      if (args[0].equalsIgnoreCase("limit") && args[1].equalsIgnoreCase("team")) {
        List<String> teams = new ArrayList<>();
        for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
          teams.add(t.getName());
        }
        return teams;
      }
    }

    return Collections.emptyList();
  }

  private boolean handlePlayerCommand(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(
          ChatColor.RED + "사용법: /blocklocker limit player <set|remove|check> <플레이어> [제한]");
      return true;
    }

    String action = args[0].toLowerCase();
    switch (action) {
      case "set":
        if (args.length < 3) {
          sender.sendMessage(ChatColor.RED + "사용법: /blocklocker limit player set <플레이어> <제한>");
          return true;
        }
        String playerName = args[1];
        try {
          int limit = Integer.parseInt(args[2]);
          setPlayerLimit(sender, playerName, limit);
        } catch (NumberFormatException e) {
          sender.sendMessage(ChatColor.RED + "올바른 숫자를 입력하세요.");
        }
        return true;

      case "remove":
        removePlayerLimit(sender, args[1]);
        return true;

      case "check":
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
          sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
          return true;
        }
        checkPlayerLimit(sender, target);
        return true;

      default:
        sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다.");
        return true;
    }
  }

  private boolean handleTeamCommand(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(
          ChatColor.RED + "사용법: /blocklocker limit team <set|remove|check> <팀> [제한]");
      return true;
    }

    String action = args[0].toLowerCase();
    String teamName = args[1];

    switch (action) {
      case "set":
        if (args.length < 3) {
          sender.sendMessage(ChatColor.RED + "사용법: /blocklocker limit team set <팀> <제한>");
          return true;
        }
        try {
          int limit = Integer.parseInt(args[2]);
          setTeamLimit(sender, teamName, limit);
        } catch (NumberFormatException e) {
          sender.sendMessage(ChatColor.RED + "올바른 숫자를 입력하세요.");
        }
        return true;

      case "remove":
        removeTeamLimit(sender, teamName);
        return true;

      case "check":
        checkTeamLimit(sender, teamName);
        return true;

      default:
        sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다.");
        return true;
    }
  }

  private boolean handleDefaultCommand(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(ChatColor.RED + "사용법: /blocklocker limit default <player|team> <제한>");
      return true;
    }

    String type = args[0].toLowerCase();
    try {
      int limit = Integer.parseInt(args[1]);
      if (type.equals("player")) {
        setDefaultPlayerLimit(sender, limit);
      } else if (type.equals("team")) {
        setDefaultTeamLimit(sender, limit);
      } else {
        sender.sendMessage(ChatColor.RED + "player 또는 team을 선택하세요.");
      }
    } catch (NumberFormatException e) {
      sender.sendMessage(ChatColor.RED + "올바른 숫자를 입력하세요.");
    }
    return true;
  }

  private void sendHelp(CommandSender sender) {
    sender.sendMessage(ChatColor.GOLD + "=== BlockLocker 보호 제한 명령어 ===");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit player set <플레이어> <제한>");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit player remove <플레이어>");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit player check <플레이어>");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit team set <팀> <제한>");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit team remove <팀>");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit team check <팀>");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit default player <제한>");
    sender.sendMessage(ChatColor.YELLOW + "/blocklocker limit default team <제한>");
    sender.sendMessage(ChatColor.GRAY + "(-1 = 무제한)");
  }

  private void setPlayerLimit(CommandSender sender, String playerName, int limit) {
    FileConfiguration config = plugin.getConfig();
    config.set("protectionLimits.playerLimits." + playerName, limit);
    plugin.saveConfig();
    plugin.reload();

    String limitStr = limit < 0 ? "무제한" : String.valueOf(limit);
    sender.sendMessage(ChatColor.GREEN + playerName + "의 보호 제한을 " + limitStr + "(으)로 설정했습니다.");
  }

  private void removePlayerLimit(CommandSender sender, String playerName) {
    FileConfiguration config = plugin.getConfig();
    config.set("protectionLimits.playerLimits." + playerName, null);
    plugin.saveConfig();
    plugin.reload();

    sender.sendMessage(ChatColor.GREEN + playerName + "의 보호 제한을 제거했습니다.");
  }

  private void checkPlayerLimit(CommandSender sender, Player player) {
    ProtectionLimitManager limitManager = plugin.getProtectionLimitManager();
    if (limitManager == null) {
      sender.sendMessage(ChatColor.RED + "보호 제한이 비활성화되어 있습니다.");
      return;
    }

    int limit = limitManager.getPlayerLimit(player);
    int count = limitManager.getPlayerProtectionCount(player);
    String limitStr = limit < 0 ? "무제한" : String.valueOf(limit);

    sender.sendMessage(
        ChatColor.YELLOW
            + player.getName()
            + "의 보호 블록: "
            + ChatColor.WHITE
            + count
            + ChatColor.GRAY
            + "/"
            + ChatColor.WHITE
            + limitStr);
  }

  private void setTeamLimit(CommandSender sender, String teamName, int limit) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Team team = scoreboard.getTeam(teamName);

    if (team == null) {
      sender.sendMessage(ChatColor.RED + "팀 '" + teamName + "'을(를) 찾을 수 없습니다.");
      return;
    }

    FileConfiguration config = plugin.getConfig();
    config.set("protectionLimits.teamLimits." + teamName, limit);
    plugin.saveConfig();
    plugin.reload();

    String limitStr = limit < 0 ? "무제한" : String.valueOf(limit);
    sender.sendMessage(ChatColor.GREEN + "팀 " + teamName + "의 보호 제한을 " + limitStr + "(으)로 설정했습니다.");
  }

  private void removeTeamLimit(CommandSender sender, String teamName) {
    FileConfiguration config = plugin.getConfig();
    config.set("protectionLimits.teamLimits." + teamName, null);
    plugin.saveConfig();
    plugin.reload();

    sender.sendMessage(ChatColor.GREEN + "팀 " + teamName + "의 보호 제한을 제거했습니다.");
  }

  private void checkTeamLimit(CommandSender sender, String teamName) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Team team = scoreboard.getTeam(teamName);

    if (team == null) {
      sender.sendMessage(ChatColor.RED + "팀 '" + teamName + "'을(를) 찾을 수 없습니다.");
      return;
    }

    ProtectionLimitManager limitManager = plugin.getProtectionLimitManager();
    if (limitManager == null) {
      sender.sendMessage(ChatColor.RED + "보호 제한이 비활성화되어 있습니다.");
      return;
    }

    int limit = limitManager.getTeamLimit(team);
    int count = limitManager.getTeamProtectionCount(team);
    String limitStr = limit < 0 ? "무제한" : String.valueOf(limit);

    sender.sendMessage(
        ChatColor.YELLOW
            + "팀 "
            + teamName
            + "의 보호 블록: "
            + ChatColor.WHITE
            + count
            + ChatColor.GRAY
            + "/"
            + ChatColor.WHITE
            + limitStr);
  }

  private void setDefaultPlayerLimit(CommandSender sender, int limit) {
    FileConfiguration config = plugin.getConfig();
    config.set("protectionLimits.defaultPlayerLimit", limit);
    plugin.saveConfig();
    plugin.reload();

    String limitStr = limit < 0 ? "무제한" : String.valueOf(limit);
    sender.sendMessage(ChatColor.GREEN + "기본 플레이어 제한을 " + limitStr + "(으)로 설정했습니다.");
  }

  private void setDefaultTeamLimit(CommandSender sender, int limit) {
    FileConfiguration config = plugin.getConfig();
    config.set("protectionLimits.defaultTeamLimit", limit);
    plugin.saveConfig();
    plugin.reload();

    String limitStr = limit < 0 ? "무제한" : String.valueOf(limit);
    sender.sendMessage(ChatColor.GREEN + "기본 팀 제한을 " + limitStr + "(으)로 설정했습니다.");
  }
}
