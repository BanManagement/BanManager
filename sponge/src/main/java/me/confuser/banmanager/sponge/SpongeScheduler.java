package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonScheduler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

public class SpongeScheduler implements CommonScheduler {
    private BMSpongePlugin plugin;

    public SpongeScheduler(BMSpongePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable task) {
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.async().execute(task).submit(plugin);
    }

    @Override
    public void runAsyncLater(Runnable task, long delay) {
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.async().execute(task).delayTicks(delay).submit(plugin);
    }

    @Override
    public void runSync(Runnable task) {
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.execute(task).submit(plugin);
    }

    @Override
    public void runSyncLater(Runnable task, long delay) {
        Task.Builder builder = Sponge.getGame().getScheduler().createTaskBuilder();
        builder.execute(task).delayTicks(delay).submit(plugin);
    }
}
