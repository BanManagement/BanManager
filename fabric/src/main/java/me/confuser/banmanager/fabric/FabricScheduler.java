package me.confuser.banmanager.fabric;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonScheduler;
import net.minecraft.server.MinecraftServer;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FabricScheduler implements CommonScheduler {
  private final ScheduledExecutorService schedulerService;
  private final ForkJoinPool executorService;
  private MinecraftServer server;

  public FabricScheduler() {
    this.schedulerService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("banmanager-scheduler");
        return thread;
      }
    });
    this.executorService = new ForkJoinPool(
        Runtime.getRuntime().availableProcessors(),
        pool -> {
          ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
          worker.setName("banmanager-worker-" + worker.getPoolIndex());
          return worker;
        },
        (t, e) -> BanManagerPlugin.getInstance().getLogger().warning("Uncaught exception in scheduler worker thread", e),
        false);
  }

  public void enable(MinecraftServer server) {
    this.server = server;
  }

  @Override
  public void runAsync(Runnable task) {
    executorService.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        BanManagerPlugin.getInstance().getLogger().warning("Exception in async task", e);
      }
    });
  }

  @Override
  public void runAsyncLater(Runnable task, Duration delay) {
    schedulerService.schedule(() -> executorService.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        BanManagerPlugin.getInstance().getLogger().warning("Exception in delayed async task", e);
      }
    }), delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void runSync(Runnable task) {
    server.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        BanManagerPlugin.getInstance().getLogger().warning("Exception in sync task", e);
      }
    });
  }

  @Override
  public void runSyncLater(Runnable task, Duration delay) {
    schedulerService.schedule(() -> server.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        BanManagerPlugin.getInstance().getLogger().warning("Exception in delayed sync task", e);
      }
    }), delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
    schedulerService.scheduleAtFixedRate(() -> executorService.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        BanManagerPlugin.getInstance().getLogger().warning("Exception in repeating async task", e);
      }
    }), initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
  }

  public void shutdown() {
    schedulerService.shutdown();
    executorService.shutdown();
    try {
      if (!schedulerService.awaitTermination(60, TimeUnit.SECONDS)) {
        schedulerService.shutdownNow();
      }
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      schedulerService.shutdownNow();
      executorService.shutdownNow();
    }
  }
}
