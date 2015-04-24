package me.confuser.banmanager.configs;

import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.configs.Config;
import org.bukkit.command.PluginCommand;

import java.util.HashMap;
import java.util.HashSet;

public class DefaultConfig extends Config<BanManager> {

  @Getter
  private DatabaseConfig localDb;
  @Getter
  private DatabaseConfig externalDb;
  @Getter
  private TimeLimitsConfig timeLimits;
  @Getter
  private HashSet<String> mutedBlacklistCommands;
  @Getter
  private boolean duplicateIpCheckEnabled = true;
  @Getter
  private HashSet<Long> bypassPlayerIps;
  @Getter
  private boolean kickLoggingEnabled = false;
  @Getter
  private boolean debugEnabled = false;
  @Getter
  private long warningCooldown;
  @Getter
  private WarningActionsConfig warningActions;
  @Getter
  private boolean displayNotificationsEnabled = true;
  @Getter
  private boolean onlineMode = true;
  @Getter
  private boolean checkForUpdates = false;
  @Getter
  private boolean offlineAutoComplete = true;
  @Getter
  private boolean punishAlts = false;
  @Getter
  private HashMap<String, CleanUp> cleanUps;
  @Getter
  private int maxOnlinePerIp = 0;

  public DefaultConfig() {
    super("config.yml");
  }

  @Override
  public void afterLoad() {
    localDb = new LocalDatabaseConfig(conf.getConfigurationSection("databases.local"));
    externalDb = new ExternalDatabaseConfig(conf.getConfigurationSection("databases.external"));
    timeLimits = new TimeLimitsConfig(conf.getConfigurationSection("timeLimits"));
    duplicateIpCheckEnabled = conf.getBoolean("duplicateIpCheck", true);
    onlineMode = conf.getBoolean("onlineMode", true);
    checkForUpdates = conf.getBoolean("checkForUpdates", false);
    offlineAutoComplete = conf.getBoolean("offlineAutoComplete", true);

    bypassPlayerIps = new HashSet<>();
    for (String ip : conf.getStringList("bypassDuplicateChecks")) {
      bypassPlayerIps.add(IPUtils.toLong(ip));
    }

    warningCooldown = conf.getLong("warningCooldown", 0);
    warningActions = new WarningActionsConfig(conf.getConfigurationSection("warningActions"));
    kickLoggingEnabled = conf.getBoolean("logKicks", false);
    debugEnabled = conf.getBoolean("debug", false);
    displayNotificationsEnabled = conf.getBoolean("displayNotifications", true);
    punishAlts = conf.getBoolean("punishAlts", false);

    cleanUps = new HashMap<>(6);
    for (String type : conf.getConfigurationSection("cleanUp").getKeys(false)) {
      cleanUps.put(type, new CleanUp(conf.getInt("cleanUp." + type)));
    }

    maxOnlinePerIp = conf.getInt("maxOnlinePerIp", 0);

    mutedBlacklistCommands = new HashSet<>();
    // Run this after startup to ensure all aliases are found
    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

      @Override
      public void run() {
        plugin.getLogger().info("The following commands are blocked whilst muted:");
        for (String cmd : conf.getStringList("mutedCommandBlacklist")) {
          mutedBlacklistCommands.add(cmd);
          // TODO Use a stringbuilder
          String info = cmd;

          // Check for aliases
          PluginCommand command = plugin.getServer().getPluginCommand(cmd);
          if (command == null) {
            plugin.getLogger().info(info);
            continue;
          }

          info += " - ";

          if (!mutedBlacklistCommands.contains(command.getName())) {
            mutedBlacklistCommands.add(command.getName());
            info += command.getName() + " ";
          }

          for (String aliasCmd : command.getAliases()) {
            info += aliasCmd + " ";
            mutedBlacklistCommands.add(aliasCmd);
            // Block the annoying /plugin:cmd too
            mutedBlacklistCommands.add(command.getPlugin().getDescription().getName().toLowerCase() + ":" + aliasCmd);
          }

          plugin.getLogger().info(info);
        }
      }

    });
  }

  public ConvertDatabaseConfig getConversionDb() {
    return new ConvertDatabaseConfig(conf.getConfigurationSection("databases.convert"));
  }

  @Override
  public void onSave() {

  }

  public boolean isBlockedCommand(String cmd) {
    return mutedBlacklistCommands.contains(cmd);
  }

}
