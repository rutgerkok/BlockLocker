package nl.rutgerkok.blocklocker.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Horrible reflection code to access the Folia scheduler if we're on Folia.
 *
 * <p>
 * I guess the "best" solution would be to create a submodule that can access
 * Folia code. However, that complicates the project setup. I guess at some
 * point Paper will offer a better solution, so that you don't need two code
 * paths.
 *
 * <p>
 * Some Java purists will say that you should separate this class into three: an
 * interface, and two implementations (Folia and Bukkit). However, since the
 * reflection code is quite unreadable, I think it's better to keep the
 * reference Bukkit code close by.
 */
final class SchedulerSupport {

    private static Method getRegionScheduler;
    private static Method getGlobalRegionScheduler;
    private static Method getAsyncScheduler;

    private static Class<?> globalRegionScheduler;
    private static Class<?> regionScheduler;
    private static Class<?> asyncScheduler;

    private static Method runDelayedOnGlobal;
    private static Method runDelayedOnRegion;
    private static Method runOnRegion;
    private static Method runAtFixedRateAsync;

    static {
        try {
            getRegionScheduler = Server.class.getMethod("getRegionScheduler");
            getGlobalRegionScheduler = Server.class.getMethod("getGlobalRegionScheduler");
            getAsyncScheduler = Server.class.getMethod("getAsyncScheduler");

            globalRegionScheduler = getGlobalRegionScheduler.getReturnType();
            regionScheduler = getRegionScheduler.getReturnType();
            asyncScheduler = getAsyncScheduler.getReturnType();

            runDelayedOnGlobal = globalRegionScheduler
                    .getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            runDelayedOnRegion = regionScheduler
                    .getMethod("runDelayed", Plugin.class, World.class, int.class, int.class, Consumer.class, long.class);
            runOnRegion = regionScheduler
                    .getMethod("run", Plugin.class, Location.class, Consumer.class);
            runAtFixedRateAsync = asyncScheduler
                    .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
            folia = true;
        } catch (NoSuchMethodException e) {
            folia = false;
        }
    }

    private static boolean folia;

    /**
     * Used to invoke a parameterless instance method.
     *
     * @param on
     *            The instance.
     * @param name
     *            Name of the method.
     * @return Return value of the method (null for void methods).
     */
    private static Object invoke(Object on, String name) {
        try {
            return on.getClass().getMethod(name).invoke(on);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new RuntimeException("Cannot invoke instance." + name + "()", e);
        }
    }

    private final Plugin plugin;

    SchedulerSupport(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void runLater(Block block, Runnable runnable) {
        if (folia) {
            try {
                Object regionScheduler = getRegionScheduler.invoke(plugin.getServer());
                Consumer<?> consumer = task -> {
                    runnable.run();
                };
                runOnRegion.invoke(regionScheduler, this.plugin, block.getLocation(), consumer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            plugin.getServer().getScheduler().runTask(plugin, runnable);
        }
    }

    public void runLater(Block block, Runnable runnable, int ticks) {
        if (folia) {
            try {
                Object regionScheduler = getRegionScheduler.invoke(plugin.getServer());
                Consumer<?> consumer = task -> {
                    runnable.run();
                };
                runDelayedOnRegion.invoke(regionScheduler, plugin, block
                        .getWorld(), block.getX() >> 4, block.getZ() >> 4, consumer, ticks);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, ticks);
        }
    }

    void runLaterGlobally(Runnable runnable, int ticks) {
        if (folia) {
            try {
                Object globalRegionScheduler = getGlobalRegionScheduler.invoke(plugin.getServer());
                Consumer<?> consumer = task -> {
                    runnable.run();
                };
                runDelayedOnGlobal.invoke(globalRegionScheduler, plugin, consumer, ticks);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, ticks);
        }
    }

    void runTimerAsync(Consumer<BukkitTask> task, long checkInterval) {
        if (folia) {
            try {
                Object asyncScheduler = getAsyncScheduler.invoke(plugin.getServer());

                Consumer<?> consumer = foliaTask -> {
                    task.accept(new BukkitTask() {

                        @Override
                        public void cancel() {
                            invoke(foliaTask, "cancel");
                        }

                        @Override
                        public Plugin getOwner() {
                            return (Plugin) invoke(foliaTask, "getOwningPlugin");
                        }

                        @Override
                        public int getTaskId() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public boolean isCancelled() {
                            return (Boolean) invoke(foliaTask, "isCancelled");
                        }

                        @Override
                        public boolean isSync() {
                            return false;
                        }
                    });
                };
                runAtFixedRateAsync
                        .invoke(asyncScheduler, plugin, consumer, 1, checkInterval * 50, TimeUnit.MILLISECONDS);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            BukkitTask[] bukkitTask = new BukkitTask[1];
            bukkitTask[0] = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                task.accept(bukkitTask[0]);
            }, 1, checkInterval);
        }
    }
}
