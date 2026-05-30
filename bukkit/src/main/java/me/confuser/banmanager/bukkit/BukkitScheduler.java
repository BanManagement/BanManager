package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.CommonScheduler;
import me.confuser.banmanager.common.util.SchedulerTime;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class BukkitScheduler implements CommonScheduler {

  private JavaPlugin plugin;

  public BukkitScheduler(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void runAsync(Runnable task) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
  }

  @Override
  public void runAsyncLater(Runnable task, Duration delay) {
    long ticks = SchedulerTime.durationToTicksCeil(delay);
    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, ticks);
  }

  @Override
  public void runSync(Runnable task) {
    Bukkit.getScheduler().runTask(plugin, task);
  }

  @Override
  public void runSyncLater(Runnable task, Duration delay) {
    long ticks = SchedulerTime.durationToTicksCeil(delay);
    Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
  }

  @Override
  public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
    long initialTicks = SchedulerTime.durationToTicksCeil(initialDelay);
    long periodTicks = SchedulerTime.durationToTicksCeil(period);
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, initialTicks, periodTicks);
  }

  /**
   * Bukkit pins {@link #runSync(Runnable)} to the server tick thread via
   * {@code BukkitScheduler#runTask(Plugin, Runnable)}, so main-thread-only
   * APIs (Bukkit {@code World} mutations, inventory changes, etc.) are safe
   * to call from inside the submitted task.
   */
  @Override
  public boolean isMainThreadAware() {
    return true;
  }
}
