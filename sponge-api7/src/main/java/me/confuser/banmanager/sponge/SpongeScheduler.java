package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonScheduler;
import me.confuser.banmanager.common.util.SchedulerTime;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;

public class SpongeScheduler implements CommonScheduler {
    private Object plugin;

    public SpongeScheduler(Object plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable task) {
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.async().execute(task).submit(plugin);
    }

    @Override
    public void runAsyncLater(Runnable task, Duration delay) {
        long ticks = SchedulerTime.durationToTicksCeil(delay);
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.async().execute(task).delayTicks(ticks).submit(plugin);
    }

    @Override
    public void runSync(Runnable task) {
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.execute(task).submit(plugin);
    }

    @Override
    public void runSyncLater(Runnable task, Duration delay) {
        long ticks = SchedulerTime.durationToTicksCeil(delay);
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.execute(task).delayTicks(ticks).submit(plugin);
    }

    @Override
    public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
        long initialTicks = SchedulerTime.durationToTicksCeil(initialDelay);
        long periodTicks = SchedulerTime.durationToTicksCeil(period);
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.async().execute(task).delayTicks(initialTicks).intervalTicks(periodTicks).submit(plugin);
    }
}
