package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonExternalCommand;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
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
  private long timeAssociatedAlts;
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
  @Getter
  private boolean blockInvalidReasons = false;
  @Getter
  private CooldownsConfig cooldownsConfig;
  @Getter
  private UUIDFetcher uuidFetcher;

  public DefaultConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "config.yml", logger);
  }

  @Override
  public void afterLoad() {
    localDb = new LocalDatabaseConfig(dataFolder, conf.getConfigurationSection("databases.local"));
    globalDb = new GlobalDatabaseConfig(dataFolder, conf.getConfigurationSection("databases.global"));
    timeLimits = new TimeLimitsConfig(conf.getConfigurationSection("timeLimits"), logger);
    duplicateIpCheckEnabled = conf.getBoolean("duplicateIpCheck", true);
    onlineMode = conf.getBoolean("onlineMode", true);
    checkForUpdates = conf.getBoolean("checkForUpdates", false);
    offlineAutoComplete = conf.getBoolean("offlineAutoComplete", true);

    bypassPlayerIps = new HashSet<>();
    bypassPlayerIps.addAll(conf.getStringList("bypassDuplicateChecks"));

    warningActions = new WarningActionsConfig(conf.getConfigurationSection("warningActions"), logger);
    kickLoggingEnabled = conf.getBoolean("logKicks", false);
    debugEnabled = conf.getBoolean("debug", false);
    displayNotificationsEnabled = conf.getBoolean("displayNotifications", true);
    punishAlts = conf.getBoolean("punishAlts", false);
    denyAlts = conf.getBoolean("denyAlts", false);
    timeAssociatedAlts = conf.getLong("timeAssociatedAlts", 0);

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

    chatPriority = conf.getString("chatPriority", "NORMAL").toUpperCase();

    blockInvalidReasons = conf.getBoolean("blockInvalidReasons", false);

    mutedBlacklistCommands = new HashSet<>(conf.getStringList("mutedCommandBlacklist"));
    softMutedBlacklistCommands = new HashSet<>(conf.getStringList("softMutedCommandBlacklist"));

    cooldownsConfig = new CooldownsConfig(conf.getConfigurationSection("cooldowns"), logger);

    Fetcher idToName = new Fetcher(
        conf.getString("uuidFetcher.idToName.url", "https://sessionserver.mojang.com/session/minecraft/profile/[uuid]"),
        conf.getString("uuidFetcher.idToName.key", "name")
    );
    Fetcher nameToId = new Fetcher(
        conf.getString("uuidFetcher.nameToId.url", "https://api.mojang.com/users/profiles/minecraft/[name]"),
        conf.getString("uuidFetcher.nameToId.key", "id")
    );
    uuidFetcher = new UUIDFetcher(idToName, nameToId);
  }

  public void handleBlockedCommands(BanManagerPlugin plugin, HashSet<String> set) {
    for (String cmd : new ArrayList<>(set)) {
      CommonExternalCommand command;
      String[] cmdArgs = null;

      if (cmd.contains(" ")) {
        cmdArgs = cmd.split(" ");
        command = plugin.getServer().getPluginCommand(cmdArgs[0]);
      } else {
        command = plugin.getServer().getPluginCommand(cmd);
      }
      if (command == null) {
        plugin.getLogger().info(cmd);
        continue;
      }

      if (command.getPluginName() != null) {
        set.add(command.getPluginName() + ":" + cmd);
      }

      StringBuilder infoBuilder = new StringBuilder(cmd).append(" - ");

      if (cmdArgs != null) {
        String args = StringUtils.join(cmdArgs, " ", 1, cmdArgs.length);
        String fullCmd = command.getName() + " " + args;

        if (!set.contains(fullCmd)) {
          set.add(fullCmd);
          infoBuilder.append(fullCmd).append(' ');
        }

        for (String aliasCmd : command.getAliases()) {
          String fullAliasCmd = aliasCmd + " " + args;

          set.add(fullAliasCmd);
          infoBuilder.append(fullAliasCmd).append(' ');

          // Block the annoying /plugin:cmd too
          if (command.getPluginName() != null) {
            set.add(command.getPluginName() + ":" + fullAliasCmd);
          }
        }
      } else {
        if (!set.contains(command.getName())) {
          set.add(command.getName());
          infoBuilder.append(command.getName()).append(' ');
        }

        for (String aliasCmd : command.getAliases()) {
          set.add(aliasCmd);
          infoBuilder.append(aliasCmd).append(' ');

          // Block the annoying /plugin:cmd too
          if (command.getPluginName() != null) {
            set.add(command.getPluginName() + ":" + aliasCmd);
          }
        }
      }

      plugin.getLogger().info(infoBuilder.toString());
    }
  }

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
