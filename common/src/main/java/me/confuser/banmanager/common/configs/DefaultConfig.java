package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.util.IPUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class DefaultConfig extends Config {

  @Getter
  private DatabaseConfig localDb;
  @Getter
  private DatabaseConfig globalDb;
  @Getter
  private TimeLimitsConfig timeLimits;
  @Getter
  private HashSet<String> mutedBlacklistCommands;
  @Getter
  private HashSet<String> softMutedBlacklistCommands;
  @Getter
  private boolean duplicateIpCheckEnabled = true;
  @Getter
  private HashSet<String> bypassPlayerIps;
  @Getter
  private boolean kickLoggingEnabled = false;
  @Getter
  private boolean debugEnabled = false;
  @Getter
  private long warningCooldown;
  @Getter
  private long reportCooldown;
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
  private boolean denyAlts = false;
  @Getter
  private HashMap<String, CleanUp> cleanUps;
  @Getter
  private int maxOnlinePerIp = 0;
  @Getter
  private int maxMultiaccountsRecently = 0;
  @Getter
  private long multiaccountsTime = 300L;
  @Getter
  private HooksConfig hooksConfig;
  @Getter
  private boolean broadcastOnSync = false;
  @Getter
  private boolean checkOnJoin = false;
  @Getter
  private boolean createNoteReasons = false;
  @Getter
  private boolean warningMutesEnabled = false;
  @Getter
  private boolean logIpsEnabled = true;
  @Getter
  private String chatPriority;

  public DefaultConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "config.yml", logger);
  }

  @Override
  public void afterLoad() {
    localDb = new LocalDatabaseConfig(conf.getConfigurationSection("databases.local"));

    globalDb = new GlobalDatabaseConfig(conf.getConfigurationSection("databases.global"));
    timeLimits = new TimeLimitsConfig(conf.getConfigurationSection("timeLimits"), logger);
    duplicateIpCheckEnabled = conf.getBoolean("duplicateIpCheck", true);
    onlineMode = conf.getBoolean("onlineMode", true);
    checkForUpdates = conf.getBoolean("checkForUpdates", false);
    offlineAutoComplete = conf.getBoolean("offlineAutoComplete", true);

    bypassPlayerIps = new HashSet<>();
    bypassPlayerIps.addAll(conf.getStringList("bypassDuplicateChecks"));

    reportCooldown = conf.getLong("reportCooldown", 0);
    warningCooldown = conf.getLong("warningCooldown", 0);
    warningActions = new WarningActionsConfig(conf.getConfigurationSection("warningActions"), logger);
    kickLoggingEnabled = conf.getBoolean("logKicks", false);
    debugEnabled = conf.getBoolean("debug", false);
    displayNotificationsEnabled = conf.getBoolean("displayNotifications", true);
    punishAlts = conf.getBoolean("punishAlts", false);
    denyAlts = conf.getBoolean("denyAlts", false);

    cleanUps = new HashMap<>(6);
    for (String type : conf.getConfigurationSection("cleanUp").getKeys(false)) {
      cleanUps.put(type, new CleanUp(conf.getInt("cleanUp." + type)));
    }

    maxOnlinePerIp = conf.getInt("maxOnlinePerIp", 0);
    maxMultiaccountsRecently = conf.getInt("maxMultiaccountsRecently", 0);
    multiaccountsTime = conf.getLong("multiaccountsTime", 300L);

    hooksConfig = new HooksConfig(conf.getConfigurationSection("hooks"), logger);

    broadcastOnSync = conf.getBoolean("broadcastOnSync", false);
    checkOnJoin = conf.getBoolean("checkOnJoin", false);
    createNoteReasons = conf.getBoolean("createNoteReasons", false);
    warningMutesEnabled = conf.getBoolean("warningMute", true);
    logIpsEnabled = conf.getBoolean("logIps", true);

    mutedBlacklistCommands = new HashSet<>();
    softMutedBlacklistCommands = new HashSet<>();

    chatPriority = conf.getString("chatPriority", "NORMAL").toUpperCase();

    // Run this after startup to ensure all aliases are found
//    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
//
//      @Override
//      public void run() {
//        plugin.getLogger().info("The following commands are blocked whilst muted:");
//        handleBlockedCommands(conf.getStringList("mutedCommandBlacklist"), mutedBlacklistCommands);
//
//        plugin.getLogger().info("The following commands are blocked whilst soft muted:");
//        handleBlockedCommands(conf.getStringList("softMutedCommandBlacklist"), softMutedBlacklistCommands);
//      }
//
//    });
  }

//  private void handleBlockedCommands(List<String> blocked, HashSet<String> set) {
//    for (String cmd : blocked) {
//      set.add(cmd);
//      StringBuilder infoBuilder = new StringBuilder(cmd);
//
//      // Check for aliases
//      PluginCommand command = plugin.getServer().getPluginCommand(cmd);
//      if (command == null) {
//        plugin.getLogger().info(cmd);
//        continue;
//      }
//
//      infoBuilder.append(" - ");
//
//      if (!set.contains(command.getName())) {
//        set.add(command.getName());
//        infoBuilder.append(command.getName()).append(' ');
//      }
//
//      for (String aliasCmd : command.getAliases()) {
//        infoBuilder.append(aliasCmd).append(' ');
//        set.add(aliasCmd);
//        // Block the annoying /plugin:cmd too
//        set.add(command.getPlugin().getDescription().getName().toLowerCase() + ":" + aliasCmd);
//      }
//
//      plugin.getLogger().info(infoBuilder.toString());
//    }
//  }

  @Override
  public void onSave() {

  }

  public boolean isBlockedCommand(String cmd) {
    return mutedBlacklistCommands.contains(cmd);
  }

  public boolean isSoftBlockedCommand(String cmd) {
    return softMutedBlacklistCommands.contains(cmd);
  }

}
