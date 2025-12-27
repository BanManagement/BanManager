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
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Plugin("banmanager")
public class BMSpongePlugin {

    private CommonLogger logger;
    private BanManagerPlugin plugin;
    private SpongeScheduler scheduler;
    private SpongeServer server;
    private ChatListener chatListener;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path dataFolder;

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    private Game game;

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
    public BMSpongePlugin(Logger logger) {
        this.logger = new PluginLogger(logger);
    }

    @Listener
    public void onServerStarted(StartedEngineEvent<Server> event) {
        this.server = new SpongeServer();
        this.scheduler = new SpongeScheduler(pluginContainer);

        PluginInfo pluginInfo;
        try {
            pluginInfo = setupConfigs();
        } catch (IOException e) {
            this.logger.severe("Failed to setup configs: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        this.plugin = new BanManagerPlugin(pluginInfo, this.logger, dataFolder.toFile(), scheduler, server, null);

        server.enable(plugin, event.engine());

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

        plugin.getLogger().info("The following commands are blocked whilst muted:");
        plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getMutedBlacklistCommands());

        plugin.getLogger().info("The following commands are blocked whilst soft muted:");
        plugin.getConfig().handleBlockedCommands(plugin, plugin.getConfig().getSoftMutedBlacklistCommands());
    }

    @Listener
    public void onServerStopping(StoppingEngineEvent<Server> event) {
        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (plugin != null) {
            plugin.disable();
        }
    }

    public void setupCommands() {
        for (CommonCommand cmd : plugin.getCommands()) {
            SpongeCommand spongeCmd = new SpongeCommand(plugin, cmd, pluginContainer);
            spongeCmd.register();
        }

        if (plugin.getGlobalConn() != null) {
            for (CommonCommand cmd : plugin.getGlobalCommands()) {
                SpongeCommand spongeCmd = new SpongeCommand(plugin, cmd, pluginContainer);
                spongeCmd.register();
            }
        }
    }

    public CommonLogger getLogger() {
        return logger;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public BanManagerPlugin getPlugin() {
        return plugin;
    }

    private PluginInfo setupConfigs() throws IOException {
        File dataDir = dataFolder.toFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        for (String name : configs) {
            File file = new File(dataDir, name);

            if (!file.exists()) {
                try (InputStream in = getResourceAsStream(name)) {
                    if (in != null) {
                        Files.copy(in, file.toPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try (InputStream in = getResourceAsStream(name);
                     Reader defConfigStream = in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null) {
                    if (defConfigStream != null) {
                        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
                        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                        conf.setDefaults(defConfig);
                        conf.options().copyDefaults(true);
                        conf.save(file);
                    }
                }
            }
        }

        PluginInfo pluginInfo = new PluginInfo();
        try (InputStream in = getResourceAsStream("plugin.yml");
             Reader defConfigStream = in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null) {
            if (defConfigStream == null) {
                throw new IOException("plugin.yml not found in resources");
            }
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
            ConfigurationSection commands = conf.getConfigurationSection("commands");

            if (commands != null) {
                for (String command : commands.getKeys(false)) {
                    ConfigurationSection cmd = commands.getConfigurationSection(command);
                    pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
                }
            }
        }

        return pluginInfo;
    }

    public void setupListeners() {
        Sponge.eventManager().registerListeners(pluginContainer, new JoinListener(plugin));
        Sponge.eventManager().registerListeners(pluginContainer, new LeaveListener(plugin));
        Sponge.eventManager().registerListeners(pluginContainer, new CommandListener(plugin));
        Sponge.eventManager().registerListeners(pluginContainer, new HookListener(plugin));

        registerChatListener();

        if (plugin.getConfig().isDisplayNotificationsEnabled()) {
            Sponge.eventManager().registerListeners(pluginContainer, new BanListener(plugin));
            Sponge.eventManager().registerListeners(pluginContainer, new MuteListener(plugin));
            Sponge.eventManager().registerListeners(pluginContainer, new NoteListener(plugin));
            Sponge.eventManager().registerListeners(pluginContainer, new ReportListener(plugin));
        }

        if (plugin.getDiscordConfig().isHooksEnabled()) {
            Sponge.eventManager().registerListeners(pluginContainer, new DiscordListener(plugin));
        }
    }

    public void registerChatListener() {
        unregisterChatListener();

        String chatPriority = plugin.getConfig().getChatPriority();
        if (!chatPriority.equals("NONE")) {
            chatListener = new ChatListener(plugin);
            Sponge.eventManager().registerListeners(pluginContainer, chatListener);
        }
    }

    private void unregisterChatListener() {
        if (chatListener != null) {
            Sponge.eventManager().unregisterListeners(chatListener);
            chatListener = null;
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

        Duration runnerPeriod = Duration.ofSeconds(1);
        plugin.getScheduler().runAsyncRepeating(syncRunner, runnerPeriod, runnerPeriod);

        int saveLastCheckedSeconds = plugin.getSchedulesConfig().getSchedule("saveLastChecked");
        if (saveLastCheckedSeconds > 0) {
            Duration period = Duration.ofSeconds(saveLastCheckedSeconds);
            Duration initialDelay = period.plusMillis(50);
            plugin.getScheduler().runAsyncRepeating(new SaveLastChecked(plugin), initialDelay, period);
        }

        plugin.getScheduler().runAsync(new Purge(plugin));
    }

    private InputStream getResourceAsStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream("assets/banmanager/" + resource);
    }
}
