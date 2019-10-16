package me.confuser.banmanager.bukkit;

import lombok.Getter;
import me.confuser.banmanager.bukkit.listeners.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.runnables.*;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BMBukkitPlugin extends JavaPlugin {

  @Getter
  private BanManagerPlugin plugin;

  @Override
  public void onEnable() {
    BukkitServer server = new BukkitServer();
    plugin = new BanManagerPlugin(new PluginLogger(getLogger()), getDataFolder(), new BukkitScheduler(this), server);

    server.enable(plugin);

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
    registerEvent(new JoinListener(plugin));
    registerEvent(new LeaveListener(plugin));
    registerEvent(new CommandListener(plugin));
    registerEvent(new HookListener(plugin));

    ChatListener chatListener = new ChatListener(plugin);

    // Set custom priority
    getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, EventPriority.valueOf(plugin.getConfig().getChatPriority()),
      (listener, event) -> {
        ((ChatListener) listener).onPlayerChat((AsyncPlayerChatEvent) event);
        ((ChatListener) listener).onIpChat((AsyncPlayerChatEvent) event);
      }, this);

    if (plugin.getConfig().isDisplayNotificationsEnabled()) {
      registerEvent(new BanListener(plugin));
      registerEvent(new MuteListener(plugin));
      registerEvent(new NoteListener(plugin));
      registerEvent(new ReportListener(plugin));
    }
  }

  private void registerEvent(Listener listener) {
    getServer().getPluginManager().registerEvents(listener, this);
  }

  public void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      new BukkitCommand(cmd);
    }

    if (plugin.getGlobalConn() != null) {
      for (CommonCommand cmd : plugin.getGlobalCommands()) {
        new BukkitCommand(cmd);
      }
    }
  }

  public void setupRunnables() {
    Runner syncRunner;

    if (plugin.getGlobalConn() == null) {
      syncRunner = new Runner(new BanSync(plugin), new MuteSync(plugin), new IpSync(plugin), new IpRangeSync(plugin), new ExpiresSync(plugin),
              new WarningSync(plugin), new RollbackSync(plugin), new NameSync(plugin));
    } else {
      syncRunner = new Runner(new BanSync(plugin), new MuteSync(plugin), new IpSync(plugin), new IpRangeSync(plugin), new ExpiresSync(plugin),
              new WarningSync(plugin), new RollbackSync(plugin), new NameSync(plugin),
              new GlobalBanSync(plugin), new GlobalMuteSync(plugin), new GlobalIpSync(plugin), new GlobalNoteSync(plugin));
    }

    plugin.setSyncRunner(syncRunner);

    setupAsyncRunnable(10L, syncRunner);

    /*
     * This task should be ran last with a 1L offset as it gets modified
     * above.
     */
    setupAsyncRunnable((plugin.getSchedulesConfig()
                              .getSchedule("saveLastChecked") * 20L) + 1L, new SaveLastChecked(plugin));

    // Purge
    plugin.getScheduler().runAsync(new Purge(plugin));

    // TODO Refactor
    if (!plugin.getConfig().isCheckForUpdates()) return;

    plugin.getScheduler().runAsync(() -> {
      Updater updater = new Updater(this, 41473, getFile(), Updater.UpdateType.NO_DOWNLOAD, false);

      if (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
        plugin.getScheduler().runSync(() -> registerEvent(new UpdateListener(plugin)));
      }
    });
  }

  private void setupAsyncRunnable(long length, Runnable runnable) {
    if (length <= 0) return;

    getServer().getScheduler().runTaskTimerAsynchronously(this, runnable, length, length);
  }
}
