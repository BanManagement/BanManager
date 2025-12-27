package me.confuser.banmanager.fabric;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.SneakyThrows;
import me.confuser.banmanager.fabric.listeners.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.runnables.BanSync;
import me.confuser.banmanager.common.runnables.ExpiresSync;
import me.confuser.banmanager.common.runnables.GlobalBanSync;
import me.confuser.banmanager.common.runnables.GlobalIpSync;
import me.confuser.banmanager.common.runnables.GlobalMuteSync;
import me.confuser.banmanager.common.runnables.GlobalNoteSync;
import me.confuser.banmanager.common.runnables.IpRangeSync;
import me.confuser.banmanager.common.runnables.IpSync;
import me.confuser.banmanager.common.runnables.MuteSync;
import me.confuser.banmanager.common.runnables.NameSync;
import me.confuser.banmanager.common.runnables.Purge;
import me.confuser.banmanager.common.runnables.RollbackSync;
import me.confuser.banmanager.common.runnables.Runner;
import me.confuser.banmanager.common.runnables.SaveLastChecked;
import me.confuser.banmanager.common.runnables.WarningSync;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class BMFabricPlugin implements DedicatedServerModInitializer {

  @Getter
  private BanManagerPlugin plugin;
  private String[] configs = new String[] {
      "config.yml",
      "console.yml",
      "discord.yml",
      "exemptions.yml",
      "geoip.yml",
      "messages.yml",
      "reasons.yml",
      "schedules.yml"
  };
  private FabricScheduler scheduler;
  private PluginInfo pluginInfo;
  private FabricServer server;

  @Override
  public void onInitializeServer() {
    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    this.server = new FabricServer();
    this.scheduler = new FabricScheduler();

    plugin = new BanManagerPlugin(pluginInfo, new PluginLogger(LogManager.getLogger("BanManager")), getDataFolder(),
        scheduler, this.server, null);

    try {
      plugin.enable();
    } catch (Exception e) {
      this.scheduler.shutdown();

      plugin.disable();
      e.printStackTrace();
      return;
    }

    setupListeners();
    setupCommands();
    setupRunnables();

    ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
    ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
  }

  private void onServerStarting(MinecraftServer server) {
    this.scheduler.enable(server);
    this.server.enable(plugin, server);
  }

  private void onServerStarted(MinecraftServer server) {
    plugin.getLogger().info("The following commands are blocked whilst muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getMutedBlacklistCommands());

    plugin.getLogger().info("The following commands are blocked whilst soft muted:");
    plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getSoftMutedBlacklistCommands());
  }

  private void onServerStopping(MinecraftServer server) {
    this.scheduler.shutdown();

    if (plugin != null) {
      plugin.disable();
    }
  }

  private void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      new FabricCommand(cmd).register();
    }

    if (plugin.getGlobalConn() != null) {
      for (CommonCommand cmd : plugin.getGlobalCommands()) {
        new FabricCommand(cmd).register();
      }
    }
  }

  private File getDataFolder() {
    File dataDirectory = FabricLoader.getInstance().getConfigDir().resolve("banmanager").toFile();

    if (!dataDirectory.exists()) {
      dataDirectory.mkdir();
    }

    return dataDirectory;
  }

  private PluginInfo setupConfigs() throws IOException {
    for (String name : configs) {
      File file = new File(getDataFolder(), name);

      if (!file.exists()) {
        try (InputStream in = getResourceAsStream(name)) {
          Files.copy(in, file.toPath());
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        try (InputStream in = getResourceAsStream(file.getName());
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
    try (InputStream in = getResourceAsStream("plugin.yml");
         Reader defConfigStream = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
      ConfigurationSection commands = conf.getConfigurationSection("commands");

      for (String command : commands.getKeys(false)) {
        ConfigurationSection cmd = commands.getConfigurationSection(command);

        pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"),
            cmd.getStringList("aliases")));
      }
    }

    return pluginInfo;
  }

  public void setupListeners() {
    new JoinListener(plugin);
    new LeaveListener(plugin);
    new HookListener(plugin);

    if (!plugin.getConfig().getChatPriority().equals("NONE")) {
      new ChatListener(plugin);
    }

    if (plugin.getConfig().isDisplayNotificationsEnabled()) {
      new BanListener(plugin);
      new MuteListener(plugin);
      new NoteListener(plugin);
      new ReportListener(plugin, this.server);
    }

    if (plugin.getDiscordConfig().isHooksEnabled()) {
      new DiscordListener(plugin);
    }
  }

  public void setupRunnables() {
    Runner syncRunner;

    if (plugin.getGlobalConn() == null) {
      syncRunner = new Runner(new BanSync(plugin), new MuteSync(plugin), new IpSync(plugin), new IpRangeSync(plugin),
          new ExpiresSync(plugin),
          new WarningSync(plugin), new RollbackSync(plugin), new NameSync(plugin));
    } else {
      syncRunner = new Runner(new BanSync(plugin), new MuteSync(plugin), new IpSync(plugin), new IpRangeSync(plugin),
          new ExpiresSync(plugin),
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

  @SneakyThrows
  private InputStream getResourceAsStream(String resource) {
    Class<?> cls = getClass();

    // returns the ClassLoader object associated with this Class
    ClassLoader cLoader = cls.getClassLoader();

    return cLoader.getResourceAsStream(resource);
  }
}
