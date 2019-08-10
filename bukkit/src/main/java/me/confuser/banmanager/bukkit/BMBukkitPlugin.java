package me.confuser.banmanager.bukkit;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.BanCommand;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class BMBukkitPlugin extends JavaPlugin {

  @Getter
  private BanManagerPlugin plugin;

  @Override
  public void onEnable() {
    plugin = new BanManagerPlugin(new PluginLogger(getLogger()), getDataFolder());

    try {
      plugin.enable();
    } catch (Exception e) {
      getPluginLoader().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    setupListeners();
    setupCommands();
    setupRunnables();
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelTasks(this);

    plugin.disable();
  }

  public void setupListeners() {
    new JoinListener().register();
    new LeaveListener().register();
    new CommandListener().register();
    new HookListener().register();

    ChatListener chatListener = new ChatListener();

    // Set custom priority
    getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, plugin.getConfig()
            .getChatPriority(), new EventExecutor() {

      @Override
      public void execute(Listener listener, Event event) throws EventException {
        ((ChatListener) listener).onPlayerChat((AsyncPlayerChatEvent) event);
        ((ChatListener) listener).onIpChat((AsyncPlayerChatEvent) event);
      }
    }, plugin);

    if (configuration.isDisplayNotificationsEnabled()) {
      new BanListener().register();
      new MuteListener().register();
      new NoteListener().register();
      new ReportListener().register();
    }
  }

  public void setupCommands() {
    new BukkitCommand(new BanCommand(plugin));
  }

  public void setupRunnables() {
    if (globalConn == null) {
      syncRunner = new Runner(new BanSync(), new MuteSync(), new IpSync(), new IpRangeSync(), new ExpiresSync(),
              new WarningSync(), new RollbackSync(), new NameSync());
    } else {
      syncRunner = new Runner(new BanSync(), new MuteSync(), new IpSync(), new IpRangeSync(), new ExpiresSync(),
              new WarningSync(), new RollbackSync(), new NameSync(),
              new GlobalBanSync(), new GlobalMuteSync(), new GlobalIpSync(), new GlobalNoteSync());
    }

    setupAsyncRunnable(10L, syncRunner);

    /*
     * This task should be ran last with a 1L offset as it gets modified
     * above.
     */
    setupAsyncRunnable((schedulesConfig.getSchedule("saveLastChecked") * 20L) + 1L, new SaveLastChecked());

    // Purge
    getServer().getScheduler().runTaskAsynchronously(plugin, new Purge());

    // TODO Refactor
    if (!plugin.getConfig().isCheckForUpdates()) return;

    getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        if (UpdateUtils.isUpdateAvailable(getFile())) {
          getServer().getScheduler().runTask(plugin, new Runnable() {

            @Override
            public void run() {
              new UpdateListener().register();
            }
          });
        }
      }
    });
  }

  private void setupAsyncRunnable(long length, Runnable runnable) {
    if (length <= 0) return;

    getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, length, length);
  }
}
