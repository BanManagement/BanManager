package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonScheduler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class SpongeScheduler implements CommonScheduler {
    private final PluginContainer plugin;
    private final ScheduledExecutorService schedulerService;
    private final ForkJoinPool executorService;

    public SpongeScheduler(PluginContainer plugin) {
        this.plugin = plugin;
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
            (t, e) -> e.printStackTrace(),
            false);
    }

    @Override
    public void runAsync(Runnable task) {
        executorService.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void runAsyncLater(Runnable task, Duration delay) {
        schedulerService.schedule(() -> executorService.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }), delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSync(Runnable task) {
        Sponge.server().scheduler().submit(
            Task.builder()
                .plugin(plugin)
                .execute(() -> {
                    try {
                        task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .build()
        );
    }

    @Override
    public void runSyncLater(Runnable task, Duration delay) {
        long ticks = delay.toMillis() / 50; // 50ms per tick
        Sponge.server().scheduler().submit(
            Task.builder()
                .plugin(plugin)
                .delay(Ticks.of(ticks))
                .execute(() -> {
                    try {
                        task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .build()
        );
    }

    @Override
    public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
        schedulerService.scheduleAtFixedRate(() -> executorService.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
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

