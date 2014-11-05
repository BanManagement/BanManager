package me.confuser.banmanager.configs;

import java.util.HashSet;
import lombok.Getter;

import org.bukkit.command.PluginCommand;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.configs.Config;

public class DefaultConfig extends Config<BanManager> {

      @Getter
      private DatabaseConfig localDb;
      @Getter
      private DatabaseConfig externalDb;
      @Getter
      private TimeLimitsConfig timeLimits;
      @Getter
      private HashSet<String> mutedBlacklistCommands = new HashSet<>();
      @Getter
      private boolean duplicateIpCheckEnabled = true;
      @Getter
      private HashSet<Long> bypassPlayerIps = new HashSet<>();
      @Getter
      private boolean kickLoggingEnabled = false;
      @Getter
      private boolean debugEnabled = false;
      @Getter
      private long warningCooldown;

      public DefaultConfig() {
            super("config.yml");
      }

      @Override
      public void afterLoad() {
            localDb = new LocalDatabaseConfig(conf.getConfigurationSection("databases.local"));
            //externalDb = new DatabaseConfig(conf.getConfigurationSection("databases.external"));
            timeLimits = new TimeLimitsConfig(conf.getConfigurationSection("timeLimits"));
            duplicateIpCheckEnabled = conf.getBoolean("duplicateIpCheck", true);
            
            for (String ip : conf.getStringList("bypassDuplicateChecks")) {
            	bypassPlayerIps.add(IPUtils.toLong(ip));
            }
            
            warningCooldown = conf.getLong("warningCooldown", 0);
            kickLoggingEnabled = conf.getBoolean("logKicks", false);
            debugEnabled = conf.getBoolean("debug", false);

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
