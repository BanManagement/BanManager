package me.confuser.banmanager.bukkit;

import lombok.Getter;
import me.confuser.banmanager.bukkit.listeners.*;
import me.confuser.banmanager.common.config.adapter.ConfigurationAdapter;
import me.confuser.banmanager.common.plugin.AbstractBanManagerPlugin;
import me.confuser.banmanager.runnables.*;
import me.confuser.banmanager.util.UpdateUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getServer;

public class BMBukkitPlugin extends AbstractBanManagerPlugin {

    private final BMBukkitBootstrap bootstrap;

    @Getter
    private BukkitSenderFactory senderFactory;

    @Getter
    private BukkitCommandExecutor commandManager;


    private BukkitBanListener banListener = new BukkitBanListener(this);
    private BukkitChatListener chatListener = new BukkitChatListener(this);
    private BukkitCommandListener commandListener = new BukkitCommandListener(this);
    private BukkitHookListener hookListener = new BukkitHookListener(this);
    private BukkitJoinListener joinListener = new BukkitJoinListener(this);
    private BukkitLeaveListener leaveListener = new BukkitLeaveListener(this);
    private BukkitMuteListener muteListener = new BukkitMuteListener(this);
    private BukkitNoteListener noteListener = new BukkitNoteListener(this);
    private BukkitReportListener reportListener = new BukkitReportListener(this);

    @Getter
    private Runner syncRunner;


    public BMBukkitPlugin(BMBukkitBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public BMBukkitBootstrap getBootstrap() {
        return this.bootstrap;
    }


    @Override
    protected void setupSenderFactory() {
        this.senderFactory = new BukkitSenderFactory(this);
    }

    @Override
    protected void registerPlatformListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(joinListener, this.bootstrap);
        Bukkit.getServer().getPluginManager().registerEvents(leaveListener, this.bootstrap);
        Bukkit.getServer().getPluginManager().registerEvents(commandListener, this.bootstrap);
        Bukkit.getServer().getPluginManager().registerEvents(hookListener, this.bootstrap);

        // Set custom priority
        getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, getConfiguration()
                .getChatPriority(), (EventExecutor) (listener, event) -> {
                    ((BukkitChatListener) listener).onPlayerChat((AsyncPlayerChatEvent) event);
                    ((BukkitChatListener) listener).onIpChat((AsyncPlayerChatEvent) event);
                }, this.bootstrap);

        if (getConfiguration().isDisplayNotificationsEnabled()) {
            Bukkit.getServer().getPluginManager().registerEvents(banListener, this.bootstrap);
            Bukkit.getServer().getPluginManager().registerEvents(muteListener, this.bootstrap);
            Bukkit.getServer().getPluginManager().registerEvents(noteListener, this.bootstrap);
            Bukkit.getServer().getPluginManager().registerEvents(reportListener, this.bootstrap);
        }
    }

    @Override
    protected void registerRunnables() {
        if (getGlobalConn() == null) {
            syncRunner = new Runner(new BanSync(this), new MuteSync(this), new IpSync(this), new IpRangeSync(this), new ExpiresSync(this),
                    new WarningSync(this), new RollbackSync(this), new NameSync(this));
        } else {
            syncRunner = new Runner(new BanSync(this), new MuteSync(this), new IpSync(this), new IpRangeSync(this), new ExpiresSync(this),
                    new WarningSync(this), new RollbackSync(this), new NameSync(this),
                    new GlobalBanSync(this), new GlobalMuteSync(this), new GlobalIpSync(this), new GlobalNoteSync(this));
        }

        setupAsyncRunnable(10L, syncRunner);

        /*
         * This task should be ran last with a 1L offset as it gets modified
         * above.
         */
        setupAsyncRunnable((schedulesConfig.getSchedule("saveLastChecked") * 20L) + 1L, new SaveLastChecked(this));

        // Purge
        getBootstrap().getScheduler().executeAsync(new Purge(this));

        // TODO Refactor
        if (!getConfiguration().isCheckForUpdates()) return;

        getBootstrap().getScheduler().executeAsync(() -> {
            if (UpdateUtils.isUpdateAvailable(getFile())) {
                getBootstrap().getScheduler().executeSync(() -> {
                    BukkitUpdateListener listener = new BukkitUpdateListener(this);
                    Bukkit.getServer().getPluginManager().registerEvents(listener, this.bootstrap);
                });
            }
        });
    }

    private void setupAsyncRunnable(long length, Runnable runnable) {
        if (length <= 0) return;

        getBootstrap().getScheduler().asyncRepeating(runnable, length / 20L, TimeUnit.SECONDS);
    }

    @Override
    protected ConfigurationAdapter provideConfigurationAdapter() {
        return new BukkitConfigAdapter(this, resolveConfig());
    }

    private File resolveConfig() {
        File configFile = new File(this.bootstrap.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.bootstrap.getDataFolder().mkdirs();
            this.bootstrap.saveResource("config.yml", false);
        }
        return configFile;
    }

    @Override
    protected void registerCommands() {
        this.commandManager = new BukkitCommandExecutor(this);
    }

}
