package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.util.DateUtils;

import java.util.HashMap;

public class TimeLimitsConfig {

  private HashMap<String, HashMap<String, String>> limits;

  public TimeLimitsConfig(ConfigurationSection config, CommonLogger logger) {
    limits = new HashMap<>();

    for (TimeLimitType type : TimeLimitType.values()) {
      ConfigurationSection typeSection = config.getConfigurationSection(type.getName());

      if (typeSection == null) continue;

      HashMap<String, String> groups = new HashMap<>();

      for (String name : typeSection.getKeys(false)) {
        String time = typeSection.getString(name);

        try {
          DateUtils.parseDateDiff(time, true);
        } catch (Exception e) {
          logger.warning("Ignored " + type.getName() + " " + name + " due to invalid time");
          continue;
        }

        groups.put(name, time);
      }

      limits.put(type.getName(), groups);
    }
  }

//  public boolean isPastLimit(CommandSender sender, TimeLimitType type, long expires) {
//    if (sender.hasPermission("bm.timelimit." + type.getName() + ".bypass") || sender.hasPermission("bm.*")) {
//      return false;
//    }
//
//    HashMap<String, String> groups = limits.get(type.getName());
//
//    if (groups == null) return false;
//
//    for (String group : groups.keySet()) {
//      if (sender.hasPermission("bm.timelimit." + type.getName() + "." + group)) {
//        try {
//          if (expires > DateUtils.parseDateDiff(groups.get(group), true)) {
//            return true;
//          }
//        } catch (Exception e) {
//          e.printStackTrace();
//        }
//      }
//    }
//
//    return false;
//  }
}
