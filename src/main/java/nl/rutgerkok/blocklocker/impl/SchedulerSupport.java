package nl.rutgerkok.blocklocker.impl;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/** Code to support both the standard schedulers of Bukkit, and Folia's scheduler. */
final class SchedulerSupport {

  private static boolean folia;

  static {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      folia = true;
    } catch (ClassNotFoundException e) {
      folia = false;
    }
  }

  private final Plugin plugin;

  SchedulerSupport(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  public void runLater(Block block, Runnable runnable) {
    if (folia) {
      plugin
          .getServer()
          .getRegionScheduler()
          .run(
              plugin,
              block.getLocation(),
              task -> {
                runnable.run();
              });
    } else {
      plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
  }

  public void runLater(Block block, Runnable runnable, int ticks) {
    if (folia) {
      plugin
          .getServer()
          .getRegionScheduler()
          .runDelayed(
              plugin,
              block.getLocation(),
              task -> {
                runnable.run();
              },
              ticks);
    } else {
      plugin.getServer().getScheduler().runTaskLater(plugin, runnable, ticks);
    }
  }

  void runLaterGlobally(Runnable runnable, int ticks) {
    if (folia) {
      plugin
          .getServer()
          .getGlobalRegionScheduler()
          .runDelayed(
              plugin,
              task -> {
                runnable.run();
              },
              ticks);
    } else {
      plugin.getServer().getScheduler().runTaskLater(plugin, runnable, ticks);
    }
  }

  void runTimerAsync(Consumer<BukkitTask> task, long checkInterval) {
    if (folia) {
      plugin
          .getServer()
          .getAsyncScheduler()
          .runAtFixedRate(
              plugin,
              foliaTask -> {
                task.accept(
                    new BukkitTask() {

                      @Override
                      public void cancel() {
                        foliaTask.cancel();
                      }

                      @Override
                      public Plugin getOwner() {
                        return foliaTask.getOwningPlugin();
                      }

                      @Override
                      public int getTaskId() {
                        throw new UnsupportedOperationException();
                      }

                      @Override
                      public boolean isCancelled() {
                        return foliaTask.isCancelled();
                      }

                      @Override
                      public boolean isSync() {
                        return false;
                      }
                    });
              },
              1,
              checkInterval * 50,
              TimeUnit.MILLISECONDS);
    } else {
      BukkitTask[] bukkitTask = new BukkitTask[1];
      bukkitTask[0] =
          plugin
              .getServer()
              .getScheduler()
              .runTaskTimerAsynchronously(
                  plugin,
                  () -> {
                    task.accept(bukkitTask[0]);
                  },
                  1,
                  checkInterval);
    }
  }
}
