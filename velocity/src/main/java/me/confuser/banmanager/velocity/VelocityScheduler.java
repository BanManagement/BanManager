package me.confuser.banmanager.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import me.confuser.banmanager.common.CommonScheduler;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class VelocityScheduler implements CommonScheduler {
  private Object plugin;
  private ProxyServer server;
  private final List<ScheduledTask> repeatingTasks = new CopyOnWriteArrayList<>();

  public VelocityScheduler(Object plugin, ProxyServer server) {
    this.plugin = plugin;
    this.server = server;
  }

  @Override
  public void runAsync(Runnable task) {
    server.getScheduler().buildTask(plugin, task).schedule();
  }

  @Override
  public void runAsyncLater(Runnable task, Duration delay) {
    server.getScheduler().buildTask(plugin, task).delay(delay).schedule();
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
    repeatingTasks.add(server.getScheduler().buildTask(plugin, task).delay(initialDelay).repeat(period).schedule());
  }

  @Override
  public void cancelAll() {
    for (ScheduledTask task : repeatingTasks) {
      task.cancel();
    }
    repeatingTasks.clear();
  }
}
