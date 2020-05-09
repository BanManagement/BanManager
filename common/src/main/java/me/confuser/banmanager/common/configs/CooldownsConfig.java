package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CooldownsConfig {
  private static HashSet<String> validCommands = new HashSet<String>() {

    {
      add("ban");
      add("tempban");
      add("mute");
      add("tempmute");
      add("banip");
      add("tempbanip");
      add("warn");
      add("tempwarn");
      add("report");
    }
  };

  private CommonLogger logger;
  private HashMap<String, Long> cooldowns;

  public CooldownsConfig(ConfigurationSection config, CommonLogger logger) {
    this.logger = logger;
    cooldowns = new HashMap<>();

    if (config == null) {
      return;
    }

    for (String command : config.getKeys(false)) {
      if (!validCommands.contains(command)) {
        logger.warning("Invalid cooldown command " + command);
        continue;
      }

      cooldowns.put(command, config.getLong(command, 0));
    }
  }

  public long getCommand(String name) {
    return cooldowns.getOrDefault(name, (long) 0);
  }

}
