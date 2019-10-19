package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarningActionsConfig {

  @Getter
  private boolean isEnabled = false;
  private HashMap<Double, List<ActionCommand>> actions;

  public WarningActionsConfig(ConfigurationSection config, CommonLogger logger) {
    isEnabled = config.getBoolean("enabled", false);
    actions = new HashMap<>();

    if (!isEnabled) return;

    ConfigurationSection actionsConf = config.getConfigurationSection("actions");

    if (actionsConf == null) return;

    for (String amount : actionsConf.getKeys(false)) {
      double amountDbl;

      try {
        amountDbl = Double.parseDouble(amount);
      } catch (NumberFormatException e) {
        logger.warning("Invalid warning action, " + amount + " is not numeric");
        continue;
      }

      // New check
      List<Map<?, ?>> mapList = actionsConf.getMapList(amount);
      if (mapList != null && mapList.size() != 0) {
        List<ActionCommand> actionCommands = new ArrayList<>(mapList.size());

        for (Map<?, ?> map : mapList) {
          if (map.get("cmd") == null) {
            logger.severe("Missing cmd from warningActions " + amount);
            continue;
          }

          long delay = 0;

          if (map.get("delay") != null) {
            try {
              delay = Long.valueOf((Integer) map.get("delay"));
            } catch (NumberFormatException e) {
              logger.severe("Invalid delay for " + map.get("cmd"));
              continue;
            }

            delay = delay * 20L; // Convert from seconds to ticks
          }

          actionCommands.add(new ActionCommand((String) map.get("cmd"), delay));
        }

        this.actions.put(amountDbl, actionCommands);
      } else {
        List<String> actions = actionsConf.getStringList(amount);
        if (actions.size() == 0) continue;

        logger
                .warning("warningActions amount " + amount + " is using a deprecated list, please use new cmd and delay syntax");
        List<ActionCommand> actionCommands = new ArrayList<>(actions.size());

        for (String action : actions) {
          actionCommands.add(new ActionCommand(action, 0));
        }

        this.actions.put(amountDbl, actionCommands);
      }
    }
  }

  public List<ActionCommand> getCommand(double amount) {
    return actions.get(amount);
  }

}
