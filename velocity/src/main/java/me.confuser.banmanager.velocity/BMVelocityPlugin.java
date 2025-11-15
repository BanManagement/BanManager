package me.confuser.banmanager.velocity;

import com.google.inject.Inject;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import lombok.SneakyThrows;
import lombok.Getter;

import me.confuser.banmanager.velocity.configs.VelocityConfig;
import me.confuser.banmanager.velocity.listeners.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.runnables.*;

import org.slf4j.Logger;
import org.bstats.velocity.Metrics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "banmanager",
        name = "BanManager",
        version = "${projectVersion}",
        url = "https://banmanagement.com",
        description = "A suite of moderation plugins & apps for Minecraft servers",
        authors = {
                "confuser",
                "Lorias-Jak"
        },
        dependencies = {
                @Dependency(id = "signedvelocity", optional = true)
        }
)
public class BMVelocityPlugin {
  @Getter
  private BanManagerPlugin plugin;
  @Getter
  private VelocityConfig velocityConfig;
  private final String[] configs = new String[]{
      "config.yml",
      "velocity.yml",
      "console.yml",
      "discord.yml",
      "exemptions.yml",
      "geoip.yml",
      "messages.yml",
      "reasons.yml",
      "schedules.yml"
  };
  private final Metrics.Factory metricsFactory;
  public ProxyServer server;
  private final Logger logger;
  private final File dataDirectory;
  public static BMVelocityPlugin instance;
  private boolean isMuteAllowed = false;

  @Inject
  public BMVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory final Path directory, Metrics.Factory metricsFactory) {
    this.server = server;
    this.logger = logger;
    instance = this;
    this.dataDirectory = directory.toFile();
    this.metricsFactory = metricsFactory;
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    Metrics metrics = metricsFactory.make(this, 14188);
    VelocityServer server = new VelocityServer();
    PluginInfo pluginInfo;
    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      getPlugin().disable();
      e.printStackTrace();
      return;
    }

    plugin = new BanManagerPlugin(pluginInfo, new PluginLogger(logger), dataDirectory, new VelocityScheduler(this, this.server), server, new VelocityMetrics(metrics));

    server.enable(plugin, this.server);

    try {
      plugin.enable();
    } catch (Exception e) {
      getPlugin().disable();
      e.printStackTrace();
      return;
    }

    velocityConfig = new VelocityConfig(dataDirectory, plugin.getLogger());
    velocityConfig.load();

    checkMuteRequirements();
    setupListeners();

    if (velocityConfig.isCommandsEnabled()) setupCommands();

    setupRunnables();

    plugin.getLogger().info("The following commands are blocked whilst muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getMutedBlacklistCommands());

    plugin.getLogger().info("The following commands are blocked whilst soft muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getSoftMutedBlacklistCommands());
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) {
    if (plugin != null) plugin.disable();
  }

  private void checkMuteRequirements() {
    int maxVersionStringLength = this.server.getVersion().getVersion().length();
    int buildVersion = 0;
    if (maxVersionStringLength > 4) {
      String versionAsString = this.server.getVersion().getVersion().substring(maxVersionStringLength - 4, maxVersionStringLength - 1);
      try{
        buildVersion = Integer.parseInt(versionAsString);
        if (buildVersion <= 140) isMuteAllowed = true;
        } catch (NumberFormatException e) {
          // Don't crash the plugin
        }
    }
    if (server.getPluginManager().getPlugin("signedvelocity").isPresent() && buildVersion >= 235) isMuteAllowed = true;
    if (velocityConfig.isForceEnableMute()) isMuteAllowed = true;
  }

  private void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      // Ignore reports as not compatible yet
      if (checkCommandSkipping(cmd)) continue;

      new VelocityCommand(cmd, this);
    }

    if (plugin.getGlobalConn() != null) {
      for (CommonCommand cmd : plugin.getGlobalCommands()) {
        new VelocityCommand(cmd, this);
      }
    }

    logger.info("Registered commands");
  }

  private boolean checkCommandSkipping(CommonCommand cmd) {
    if (cmd.getCommandName().startsWith("report")) return true;
    if (cmd.getCommandName().startsWith("mute") && !isMuteAllowed) return true;
    if (cmd.getCommandName().startsWith("muteip") && !isMuteAllowed) return true;
    if (cmd.getCommandName().startsWith("unmute") && !isMuteAllowed) return true;
    if (cmd.getCommandName().startsWith("unmuteip") && !isMuteAllowed) return true;

    return false;
  }

  private PluginInfo setupConfigs() throws IOException {
    if (!dataDirectory.exists()) dataDirectory.mkdir();

    for (String name : configs) {
      File file = new File(dataDirectory, name);

      if (!file.exists()) {
        try (InputStream in = getResourceAsStream(name)) {
          Files.copy(in, file.toPath());
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        Reader defConfigStream = new InputStreamReader(getResourceAsStream(file.getName()), StandardCharsets.UTF_8);

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        conf.setDefaults(defConfig);
        conf.options().copyDefaults(true);
        conf.save(file);
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    Reader defConfigStream = new InputStreamReader(getResourceAsStream("plugin.yml"), StandardCharsets.UTF_8);
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
    ConfigurationSection commands = conf.getConfigurationSection("commands");

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

    if (!plugin.getConfig().getChatPriority().equals("NONE") && isMuteAllowed) {
      registerEvent(new ChatListener(plugin));
    }

    if (plugin.getConfig().isDisplayNotificationsEnabled()) {
      registerEvent(new BanListener(plugin));
      registerEvent(new MuteListener(plugin));
      registerEvent(new NoteListener(plugin));
    }

    if (plugin.getDiscordConfig().isHooksEnabled()) {
      registerEvent(new DiscordListener(plugin));
    }
  }

  private void registerEvent(Listener listener) {
    server.getEventManager().register(this, listener);
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

    server.getScheduler().buildTask(this, runnable).repeat(length, TimeUnit.SECONDS).schedule();
  }

  @SneakyThrows
  private InputStream getResourceAsStream(String resource) {

      Class<?> cls = getClass();

      // returns the ClassLoader object associated with this Class
      ClassLoader cLoader = cls.getClassLoader();

      return cLoader.getResourceAsStream(resource);
  }
}
