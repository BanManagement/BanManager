package me.confuser.banmanager.bungee;

import lombok.Getter;
import me.confuser.banmanager.bungee.configs.BungeeConfig;
import me.confuser.banmanager.bungee.listeners.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.runnables.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class BMBungeePlugin extends Plugin {
  @Getter
  private BanManagerPlugin plugin;
  @Getter
  private BungeeConfig bungeeConfig;

  private ChatListener chatListener;

  private String[] configs = new String[]{
      "config.yml",
      "bungeecord.yml",
      "console.yml",
      "discord.yml",
      "exemptions.yml",
      "geoip.yml",
      "messages.yml",
      "reasons.yml",
      "schedules.yml"
  };
  private Metrics metrics;

  @Override
  public void onEnable() {
    BungeeServer server = new BungeeServer();
    PluginInfo pluginInfo;

    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    metrics = new Metrics(this, 6530);
    plugin = new BanManagerPlugin(pluginInfo, new PluginLogger(getLogger()), getDataFolder(), new BungeeScheduler(this), server, new BungeeMetrics(metrics));

    server.enable(plugin);

    try {
      plugin.enable();
    } catch (Exception e) {
      if (plugin != null) plugin.disable();

      e.printStackTrace();
      return;
    }

    bungeeConfig = new BungeeConfig(getDataFolder(), plugin.getLogger());
    bungeeConfig.load();

    setupListeners();

    if (bungeeConfig.isCommandsEnabled()) setupCommands();

    setupRunnables();

    plugin.getLogger().info("The following commands are blocked whilst muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getMutedBlacklistCommands());

    plugin.getLogger().info("The following commands are blocked whilst soft muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getSoftMutedBlacklistCommands());
  }

  @Override
  public void onDisable() {
    getProxy().getScheduler().cancel(this);

    if (plugin != null) plugin.disable();
  }

  private void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      // Ignore reports as not compatible yet
      if (cmd.getCommandName().startsWith("report")) continue;

      new BungeeCommand(cmd, this);
    }

    if (plugin.getGlobalConn() != null) {
      for (CommonCommand cmd : plugin.getGlobalCommands()) {
        new BungeeCommand(cmd, this);
      }
    }

    getLogger().info("Registered commands");
  }

  private PluginInfo setupConfigs() throws IOException {
    if (!getDataFolder().exists()) getDataFolder().mkdir();

    for (String name : configs) {
      File file = new File(getDataFolder(), name);

      if (!file.exists()) {
        try (InputStream in = getResourceAsStream(name)) {
          Files.copy(in, file.toPath());
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        Reader defConfigStream = new InputStreamReader(getResourceAsStream(file.getName()), "UTF8");

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        conf.setDefaults(defConfig);
        conf.options().copyDefaults(true);
        conf.save(file);
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    Reader defConfigStream = new InputStreamReader(getResourceAsStream("plugin.yml"), "UTF8");
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

    return pluginInfo;
  }

  public void setupListeners() {
    registerEvent(new JoinListener(this));
    registerEvent(new LeaveListener(plugin));
    registerEvent(new HookListener(plugin));

    registerChatListener();

    registerEvent(new ReloadListener(this));

    if (plugin.getConfig().isDisplayNotificationsEnabled()) {
      registerEvent(new BanListener(plugin));
      registerEvent(new MuteListener(plugin));
      registerEvent(new NoteListener(plugin));
    }

    if (plugin.getDiscordConfig().isHooksEnabled()) {
      registerEvent(new DiscordListener(plugin));
    }
  }

  private void unregisterChatListener() {
    if (chatListener != null) {
      getProxy().getPluginManager().unregisterListener(chatListener);
      chatListener = null;
    }
  }

  public void registerChatListener() {
    unregisterChatListener();

    if (!plugin.getConfig().getChatPriority().equals("NONE")) {
      chatListener = new ChatListener(plugin);
      registerEvent(chatListener);
    }
  }

  private void registerEvent(Listener listener) {
    getProxy().getPluginManager().registerListener(this, listener);
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

    setupAsyncRunnable(1L, syncRunner);

    /*
     * This task should be ran last with a 1L offset as it gets modified
     * above.
     */
    setupAsyncRunnable((plugin.getSchedulesConfig()
        .getSchedule("saveLastChecked")) + 1L, new SaveLastChecked(plugin));

    // Purge
    plugin.getScheduler().runAsync(new Purge(plugin));
  }

  private void setupAsyncRunnable(long length, Runnable runnable) {
    if (length <= 0) return;

    getProxy().getScheduler().schedule(this, runnable, length, length, TimeUnit.SECONDS);
  }
}
