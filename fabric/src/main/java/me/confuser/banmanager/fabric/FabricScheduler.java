package me.confuser.banmanager.fabric;

import me.confuser.banmanager.common.CommonScheduler;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FabricScheduler implements CommonScheduler {
  // Constructed before BanManagerPlugin (it's a constructor argument), so we accept a
  // Logger directly to keep failure reporting available without reaching into the plugin
  // singleton.
  private final Logger logger;
  private final ScheduledExecutorService schedulerService;
  private final ForkJoinPool executorService;
  private MinecraftServer server;

  public FabricScheduler(Logger logger) {
    this.logger = logger;
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
        (t, e) -> logger.warn("Uncaught exception in scheduler worker thread", e),
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
        logger.warn("Exception in async task", e);
      }
    });
  }

  @Override
  public void runAsyncLater(Runnable task, Duration delay) {
    schedulerService.schedule(() -> executorService.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        logger.warn("Exception in delayed async task", e);
      }
    }), delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void runSync(Runnable task) {
    server.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        logger.warn("Exception in sync task", e);
      }
    });
  }

  @Override
  public void runSyncLater(Runnable task, Duration delay) {
    schedulerService.schedule(() -> server.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        logger.warn("Exception in delayed sync task", e);
      }
    }), delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
    schedulerService.scheduleAtFixedRate(() -> executorService.execute(() -> {
      try {
        task.run();
      } catch (Exception e) {
        logger.warn("Exception in repeating async task", e);
      }
    }), initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Fabric dispatches {@link #runSync(Runnable)} via
   * {@link MinecraftServer#execute(Runnable)}, which queues the task to run
   * on the server's main thread. Worldgen and entity APIs that require the
   * server thread are therefore safe to call from inside the submitted task.
   */
  @Override
  public boolean isMainThreadAware() {
    return true;
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
