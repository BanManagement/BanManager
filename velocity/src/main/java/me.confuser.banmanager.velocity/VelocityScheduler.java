package me.confuser.banmanager.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import me.confuser.banmanager.common.CommonScheduler;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class VelocityScheduler implements CommonScheduler {
  private BMVelocityPlugin plugin;
  private ProxyServer server;

  public VelocityScheduler(BMVelocityPlugin plugin, ProxyServer server) {
    this.plugin = plugin;
    this.server = server;
  }

  @Override
  public void runAsync(Runnable task) {
    server.getScheduler().buildTask(plugin, task).schedule();
  }

  @Override
  public void runAsyncLater(Runnable task, long delay) {
    server.getScheduler().buildTask(plugin, task).delay(Duration.ofMillis(delay)).schedule();
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
