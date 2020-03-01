package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.CommonScheduler;
import net.md_5.bungee.api.ProxyServer;

import java.util.concurrent.TimeUnit;

public class BungeeScheduler implements CommonScheduler {
  private BMBungeePlugin plugin;

  public BungeeScheduler(BMBungeePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void runAsync(Runnable task) {
    ProxyServer.getInstance().getScheduler().runAsync(plugin, task);
  }

  @Override
  public void runAsyncLater(Runnable task, long delay) {
    ProxyServer.getInstance().getScheduler().schedule(plugin, task, (delay / 20L), TimeUnit.SECONDS);
  }

  @Override
  public void runSync(Runnable task) {
    runAsync(task);
  }

  @Override
  public void runSyncLater(Runnable task, long delay) {
    runAsyncLater(task, delay);
  }
}
