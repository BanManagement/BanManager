package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.CommonScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
  public void runAsyncLater(Runnable task, long delay) {
    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
  }

  @Override
  public void runSync(Runnable task) {
    Bukkit.getScheduler().runTask(plugin, task);
  }

  @Override
  public void runSyncLater(Runnable task, long delay) {
    Bukkit.getScheduler().runTaskLater(plugin, task, delay);
  }
}
