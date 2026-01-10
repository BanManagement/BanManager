package me.confuser.banmanager.bukkit;

import lombok.Getter;
import me.confuser.banmanager.bukkit.listeners.*;
import me.confuser.banmanager.bukkit.placeholders.PAPIPlaceholderResolver;
import me.confuser.banmanager.bukkit.placeholders.PAPIPlaceholders;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.runnables.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class BMBukkitPlugin extends JavaPlugin {

  @Getter
  private BanManagerPlugin plugin;

  private ChatListener chatListener;

  private String[] configs = new String[]{
      "config.yml",
      "console.yml",
      "webhooks.yml",
      "exemptions.yml",
      "geoip.yml",
      "messages.yml",
      "reasons.yml",
      "schedules.yml"
  };
  private Metrics metrics;

  @Override
  public void onEnable() {
    BukkitServer server = new BukkitServer();
    PluginInfo pluginInfo;
    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      getPluginLoader().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    metrics = new Metrics(this, 6455);
    plugin = new BanManagerPlugin(pluginInfo, new PluginLogger(getLogger()), getDataFolder(), new BukkitScheduler(this), server, new BukkitMetrics(metrics));

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

    if (plugin != null) plugin.disable();
  }

  private PluginInfo setupConfigs() throws IOException {
    for (String name : configs) {
      if (!new File(getDataFolder(), name).exists()) {
        this.saveResource(name, false);
      } else {
        File file = new File(getDataFolder(), name);
        try (InputStream in = getResource(file.getName());
             Reader defConfigStream = new InputStreamReader(in, StandardCharsets.UTF_8)) {
          YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
          YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
          conf.setDefaults(defConfig);
          conf.options().copyDefaults(true);
          conf.save(file);
        }
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    try (InputStream in = getResource("plugin.yml");
         Reader defConfigStream = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
      ConfigurationSection commands = conf.getConfigurationSection("commands");
      String pluginName = conf.getString("name");

      if (!pluginName.equals("BanManager")) {
        throw new IOException("Unable to start BanManager as " + pluginName + " has broken resource loading forcing BanManager to load their plugin.yml file; please alert the author to resolve this issue");
      }

      for (String command : commands.getKeys(false)) {
        ConfigurationSection cmd = commands.getConfigurationSection(command);

        pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
      }
    }

    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
      plugin.getLogger().info("The following commands are blocked whilst muted:");
      plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getMutedBlacklistCommands());

      plugin.getLogger().info("The following commands are blocked whilst soft muted:");
      plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getSoftMutedBlacklistCommands());
    });

    return pluginInfo;
  }

  public void setupListeners() {
    registerEvent(new JoinListener(plugin));
    registerEvent(new LeaveListener(plugin));
    registerEvent(new CommandListener(plugin));
    registerEvent(new HookListener(plugin));

    registerChatListener();

    registerEvent(new ReloadListener(this));

    if (plugin.getConfig().isDisplayNotificationsEnabled()) {
      registerEvent(new BanListener(plugin));
      registerEvent(new MuteListener(plugin));
      registerEvent(new NoteListener(plugin));
      registerEvent(new ReportListener(plugin));
    }

    if (plugin.getWebhookConfig().isHooksEnabled()) {
      registerEvent(new WebhookListener(plugin));
    }

    if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new PAPIPlaceholders(plugin).register();
      plugin.setPlaceholderResolver(new PAPIPlaceholderResolver());
      plugin.getLogger().info("PlaceholderAPI support enabled for messages");
    }
  }

  private void unregisterChatListener() {
    if (chatListener != null) {
      AsyncPlayerChatEvent.getHandlerList().unregister(chatListener);
      chatListener = null;
      plugin.getLogger().info("Chat listener unregistered");
    }
  }

  public void registerChatListener() {
    unregisterChatListener();

    String chatPriority = plugin.getConfig().getChatPriority();
    if (!chatPriority.equals("NONE")) {
      chatListener = new ChatListener(plugin);
      // Set custom priority
      getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, EventPriority.valueOf(chatPriority),
        (listener, event) -> {
          ((ChatListener) listener).onPlayerChat((AsyncPlayerChatEvent) event);
          ((ChatListener) listener).onIpChat((AsyncPlayerChatEvent) event);
        }, this);
      plugin.getLogger().info("Chat listener registered with priority: " + chatPriority);
    } else {
      plugin.getLogger().info("Chat listener disabled (priority: NONE)");
    }
  }

  private void registerEvent(Listener listener) {
    getServer().getPluginManager().registerEvents(listener, this);
  }

  public void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      try {
        getCommand(cmd.getCommandName()).setExecutor(new BukkitCommand(cmd));
      } catch (NullPointerException e) {
        plugin.getLogger().severe("Failed to register /" + cmd.getCommandName() + " command");
      }
    }

    if (plugin.getGlobalConn() != null) {
      for (CommonCommand cmd : plugin.getGlobalCommands()) {
        try {
          getCommand(cmd.getCommandName()).setExecutor(new BukkitCommand(cmd));
        } catch (NullPointerException e) {
          plugin.getLogger().severe("Failed to register /" + cmd.getCommandName() + " command");
        }
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

    // Runner loop: run every 1 second on all platforms
    Duration runnerPeriod = Duration.ofSeconds(1);
    plugin.getScheduler().runAsyncRepeating(syncRunner, runnerPeriod, runnerPeriod);

    /*
     * This task should be ran last with a small offset as it gets modified
     * above. Use +50ms (1 tick) offset for consistency across platforms.
     */
    int saveLastCheckedSeconds = plugin.getSchedulesConfig().getSchedule("saveLastChecked");
    if (saveLastCheckedSeconds > 0) {
      Duration period = Duration.ofSeconds(saveLastCheckedSeconds);
      Duration initialDelay = period.plusMillis(50);
      plugin.getScheduler().runAsyncRepeating(new SaveLastChecked(plugin), initialDelay, period);
    }

    // Purge
    plugin.getScheduler().runAsync(new Purge(plugin));
  }
}
