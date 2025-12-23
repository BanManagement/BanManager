package me.confuser.banmanager.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import me.confuser.banmanager.common.CommonScheduler;
import net.md_5.bungee.api.ProxyServer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class BungeeScheduler implements CommonScheduler {
  private Plugin plugin;

  public BungeeScheduler(Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void runAsync(Runnable task) {
    ProxyServer.getInstance().getScheduler().runAsync(plugin, task);
  }

  @Override
  public void runAsyncLater(Runnable task, Duration delay) {
    ProxyServer.getInstance().getScheduler().schedule(plugin, task, delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void runSync(Runnable task) {
    runAsync(task);
  }

  @Override
  public void runSyncLater(Runnable task, Duration delay) {
    runAsyncLater(task, delay);
  }

  @Override
  public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
    ProxyServer.getInstance().getScheduler().schedule(plugin, task, initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
  }
}
