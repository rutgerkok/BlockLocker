package nl.rutgerkok.blocklocker.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import nl.rutgerkok.blocklocker.ProtectionFinder;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.protection.Protection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/**
 * Manages protection limits for players and teams. This class handles checking whether a player or
 * team can create new protections based on configured limits.
 */
public final class ProtectionLimitManager {

  private final BlockLockerPluginImpl plugin;
  private final Config config;
  private boolean enabled;
  private int defaultPlayerLimit;
  private Map<String, Integer> playerLimits;
  private boolean teamLimitsEnabled;
  private int defaultTeamLimit;
  private Map<String, Integer> teamLimits;

  public ProtectionLimitManager(BlockLockerPluginImpl plugin, Config config) {
    this.plugin = plugin;
    this.config = config;
    loadConfig();
  }

  /** Loads the protection limit configuration from the config. */
  private void loadConfig() {
    this.enabled = config.isProtectionLimitsEnabled();
    this.defaultPlayerLimit = config.getDefaultPlayerLimit();
    this.playerLimits = config.getPlayerLimits();
    this.teamLimitsEnabled = config.isTeamLimitsEnabled();
    this.defaultTeamLimit = config.getDefaultTeamLimit();
    this.teamLimits = config.getTeamLimits();
  }

  /** Reloads the configuration. Call this after the config has been reloaded. */
  public void reload() {
    loadConfig();
  }

  /**
   * Checks if the player can create a new protection based on their current protection count and
   * limit.
   *
   * @param player The player to check.
   * @return True if the player can create a new protection, false otherwise.
   */
  public boolean canCreateProtection(Player player) {
    if (!enabled) {
      return true;
    }

    // Check bypass permission
    if (player.hasPermission("blocklocker.limit.bypass")) {
      plugin.getLogger().info("Player " + player.getName() + " bypassed limits (permission).");
      return true;
    }

    // Check team limits first if enabled
    if (teamLimitsEnabled) {
      Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
      if (team != null) {
        int teamLimit = getTeamLimit(team);
        if (teamLimit >= 0) {
          int teamCount = getTeamProtectionCount(team);
          plugin
              .getLogger()
              .info("Team " + team.getName() + " count: " + teamCount + "/" + teamLimit);
          if (teamCount >= teamLimit) {
            return false;
          }
        }
      }
    }

    // Check player limits
    int playerLimit = getPlayerLimit(player);
    if (playerLimit < 0) {
      return true; // Unlimited
    }

    int playerCount = getPlayerProtectionCount(player);
    return playerCount < playerLimit;
  }

  /**
   * Gets the protection limit for the specified player. This checks for per-player overrides,
   * permission-based limits, and falls back to the default limit.
   *
   * @param player The player to check.
   * @return The limit for the player, or -1 for unlimited.
   */
  public int getPlayerLimit(Player player) {
    // Check permission-based limits (e.g., blocklocker.limit.10,
    // blocklocker.limit.50)
    // Higher number wins
    int permissionLimit = -1;
    for (int testLimit = 1; testLimit <= 10000; testLimit++) {
      if (player.hasPermission("blocklocker.limit." + testLimit)) {
        if (testLimit > permissionLimit) {
          permissionLimit = testLimit;
        }
      }
    }
    if (permissionLimit > 0) {
      return permissionLimit;
    }

    // Check per-player override in config
    Integer override = playerLimits.get(player.getName());
    if (override != null) {
      return override;
    }

    // Fall back to default
    return defaultPlayerLimit;
  }

  /**
   * Gets the current number of protections owned by the player across all worlds.
   *
   * @param player The player to count protections for.
   * @return The number of protections owned by the player.
   */
  public int getPlayerProtectionCount(Player player) {
    PlayerProfile playerProfile = plugin.getProfileFactory().fromPlayer(player);
    ProtectionFinder finder = plugin.getProtectionFinder();
    AtomicInteger count = new AtomicInteger();

    // Count protections across all worlds
    for (World world : Bukkit.getWorlds()) {
      // We need to scan all loaded chunks in the world
      // This is potentially expensive, but necessary for accurate counting
      for (var chunk : world.getLoadedChunks()) {
        for (var blockState : chunk.getTileEntities()) {
          Block block = blockState.getBlock();
          finder
              .findProtection(block)
              .ifPresent(
                  protection -> {
                    if (protection.isOwner(playerProfile)) {
                      // Only count this protection once (not for each block in it)
                      // We'll use a simple heuristic: only count if this is the "main" block
                      if (isMainProtectionBlock(protection, block)) {
                        count.getAndIncrement();
                      }
                    }
                  });
        }
      }
    }

    return count.get();
  }

  /**
   * Gets the protection limit for the specified team.
   *
   * @param team The team to check.
   * @return The limit for the team, or -1 for unlimited.
   */
  public int getTeamLimit(Team team) {
    // Check per-team override in config
    Integer override = teamLimits.get(team.getName());
    if (override != null) {
      return override;
    }

    // Fall back to default
    return defaultTeamLimit;
  }

  /**
   * Gets a formatted message describing the limit status for a player.
   *
   * @param player The player.
   * @param current The current protection count.
   * @param limit The limit.
   * @return A formatted message.
   */
  public String getLimitMessage(Player player, int current, int limit) {
    if (limit < 0) {
      return "Unlimited";
    }
    return current + "/" + limit;
  }

  /**
   * Gets whether a player is blocked by team limits.
   *
   * @param player The player to check.
   * @return True if blocked by team limits, false otherwise.
   */
  public boolean isBlockedByTeamLimit(Player player) {
    if (!enabled || !teamLimitsEnabled) {
      return false;
    }

    if (player.hasPermission("blocklocker.limit.bypass")) {
      return false;
    }

    Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
    if (team == null) {
      return false;
    }

    int teamLimit = getTeamLimit(team);
    if (teamLimit < 0) {
      return false; // Unlimited
    }

    int teamCount = getTeamProtectionCount(team);
    return teamCount >= teamLimit;
  }

  /**
   * Helper method to determine if a block is the "main" block of a protection, to avoid counting
   * the same protection multiple times.
   *
   * @param protection The protection.
   * @param block The block to check.
   * @return True if this is likely the main block of the protection.
   */
  private boolean isMainProtectionBlock(Protection protection, Block block) {
    // Use the "getSomeProtectedBlock" as a canonical representative
    Block mainBlock = protection.getSomeProtectedBlock();
    return block.equals(mainBlock);
  }

  /**
   * Gets the current number of protections owned by any member of the team across all worlds.
   *
   * @param team The team to count protections for.
   * @return The number of protections owned by team members.
   */
  public int getTeamProtectionCount(Team team) {
    ProtectionFinder finder = plugin.getProtectionFinder();
    AtomicInteger count = new AtomicInteger();

    // Count protections across all worlds
    for (World world : Bukkit.getWorlds()) {
      for (var chunk : world.getLoadedChunks()) {
        for (var blockState : chunk.getTileEntities()) {
          Block block = blockState.getBlock();
          finder
              .findProtection(block)
              .ifPresent(
                  protection -> {
                    protection
                        .getOwner()
                        .ifPresent(
                            owner -> {
                              if (owner instanceof PlayerProfile playerProfile) {
                                String ownerName = playerProfile.getDisplayName();
                                Optional<UUID> uuid = playerProfile.getUniqueId();
                                if (uuid.isPresent()) {
                                  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid.get());
                                  if (offlinePlayer.getName() != null) {
                                    ownerName = offlinePlayer.getName();
                                  }
                                }

                                boolean isMember = team.hasEntry(ownerName);

                                if (ownerName.isEmpty()) {
                                  // Fallback: Check if any of the profiles on the
                                  // signs are the team group
                                  boolean hasTeamGroup = false;
                                  for (nl.rutgerkok.blocklocker.ProtectionSign sign :
                                      protection.getSigns()) {
                                    for (nl.rutgerkok.blocklocker.profile.Profile profile :
                                        sign.getProfiles()) {
                                      if (profile
                                          instanceof
                                          nl.rutgerkok.blocklocker.profile.GroupProfile) {
                                        // GroupProfile.getDisplayName() returns
                                        // [Name], so we must account for
                                        // brackets
                                        String displayName = profile.getDisplayName();
                                        if (displayName.equalsIgnoreCase(team.getName())
                                            || displayName.equalsIgnoreCase(
                                                "[" + team.getName() + "]")) {
                                          hasTeamGroup = true;
                                          break;
                                        }
                                      }
                                    }
                                    if (hasTeamGroup) break;
                                  }

                                  if (hasTeamGroup) {
                                    if (isMainProtectionBlock(protection, block)) {
                                      count.getAndIncrement();
                                    }
                                  }
                                } else if (isMember) {
                                  if (isMainProtectionBlock(protection, block)) {
                                    count.getAndIncrement();
                                  }
                                }
                              }
                            });
                  });
        }
      }
    }

    return count.get();
  }
}
