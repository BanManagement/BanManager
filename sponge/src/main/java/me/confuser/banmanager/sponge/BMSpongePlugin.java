package me.confuser.banmanager.sponge;

import com.google.inject.Inject;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.runnables.*;
import me.confuser.banmanager.sponge.listeners.*;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;

@Plugin(
    id = "banmanager",
    name = "BanManager",
    version = "${internalVersion}",
    authors = "confuser",
    description = "A punishment plugin",
    url = "https://banmanagement.com",
    dependencies = {
        @Dependency(id = "magibridge", optional = true),
    }
)
public class BMSpongePlugin {

  private CommonLogger logger;
  private BanManagerPlugin plugin;
  private Metrics metrics;

  @Inject
  @ConfigDir(sharedRoot = false)
  private Path dataFolder;

  @Inject
  private PluginContainer pluginContainer;

  private String[] configs = new String[]{
      "config.yml",
      "console.yml",
      "discord.yml",
      "exemptions.yml",
      "geoip.yml",
      "messages.yml",
      "reasons.yml",
      "schedules.yml"
  };

  @Inject
  public BMSpongePlugin(Logger logger, Metrics.Factory metrics) {
    this.logger = new PluginLogger(logger);
    this.metrics = metrics.make(6413);
  }

  @Listener
  public void onDisable(GameStoppingServerEvent event) {
    // @TODO Disable scheduled tasks somehow

    if (plugin != null) plugin.disable();
  }

  @Listener
  public void onEnable(GamePreInitializationEvent event) {
    SpongeServer server = new SpongeServer();
    PluginInfo pluginInfo;
    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    this.plugin = new BanManagerPlugin(pluginInfo, this.logger, dataFolder.toFile(), new SpongeScheduler(this), server, new SpongeMetrics(metrics));

    server.enable(plugin);

    try {
      plugin.enable();
    } catch (Exception e) {
      logger.severe("Unable to start BanManager");
      e.printStackTrace();
      return;
    }


    setupListeners();
    setupCommands();
    setupRunnables();
  }

  @Listener
  public void onStart(GameLoadCompleteEvent event) {
    plugin.getLogger().info("The following commands are blocked whilst muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getMutedBlacklistCommands());

    plugin.getLogger().info("The following commands are blocked whilst soft muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getSoftMutedBlacklistCommands());
  }

  public CommonLogger getLogger() {
    return logger;
  }

  private PluginInfo setupConfigs() throws IOException {
    for (String name : configs) {
      File file = new File(dataFolder.toFile(), name);
      if (file.exists()) {
        // YAMLConfigurationLoader messes with format and makes it unreadable
        Reader defConfigStream = new InputStreamReader(pluginContainer.getAsset(name).get().getUrl().openStream());

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        conf.setDefaults(defConfig);
        conf.options().copyDefaults(true);
        conf.save(file);
      } else {
        pluginContainer.getAsset(name).get().copyToDirectory(dataFolder);
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    Reader defConfigStream = new InputStreamReader(pluginContainer.getAsset("plugin.yml").get().getUrl().openStream());
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
    ConfigurationSection commands = conf.getConfigurationSection("commands");

    for (String command : commands.getKeys(false)) {
      ConfigurationSection cmd = commands.getConfigurationSection(command);

      pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
    }

    return pluginInfo;
  }

  public void setupListeners() {
    registerEvent(new JoinListener(plugin));
    registerEvent(new LeaveListener(plugin));
    registerEvent(new CommandListener(plugin));
    registerEvent(new HookListener(plugin));

    String chatPriority = plugin.getConfig().getChatPriority();
    if(!chatPriority.equals("NONE")) {
      ChatListener chatListener = new ChatListener(plugin);

      // Map Bukkit EventPriority to Sponge Order
      HashMap<String, Order> orders = new HashMap<String, Order>() {{
        put("LOWEST", Order.FIRST);
        put("LOW", Order.EARLY);
        put("NORMAL", Order.DEFAULT);
        put("HIGH", Order.LATE);
        put("HIGHEST", Order.LATE);
        put("MONITOR", Order.LAST);
      }};

      Order priority = orders.getOrDefault(chatPriority, Order.DEFAULT);

      Sponge.getEventManager().registerListener(this, MessageChannelEvent.Chat.class, priority, chatListener);
    }

    if (plugin.getConfig().isDisplayNotificationsEnabled()) {
      registerEvent(new BanListener(plugin));
      registerEvent(new MuteListener(plugin));
      registerEvent(new NoteListener(plugin));
      registerEvent(new ReportListener(plugin));
    }

    if (plugin.getDiscordConfig().isEnabled() && Sponge.getPluginManager().getPlugin("magibridge").isPresent()) {
      registerEvent(new DiscordListener(plugin));
    }
  }

  private void registerEvent(Object listener) {
    Sponge.getEventManager().registerListeners(this, listener);
  }

  public void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      new SpongeCommand(this, cmd);
    }

    if (plugin.getGlobalConn() != null) {
      for (CommonCommand cmd : plugin.getGlobalCommands()) {
        new SpongeCommand(this, cmd);
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

    if (!plugin.getConfig().isCheckForUpdates()) return;

    // @TODO Update checker logic here
  }

  private void setupAsyncRunnable(long length, Runnable runnable) {
    if (length <= 0) return;

    Sponge.getGame().getScheduler().createTaskBuilder().async().execute(runnable).intervalTicks(length).submit(this);
  }
}
