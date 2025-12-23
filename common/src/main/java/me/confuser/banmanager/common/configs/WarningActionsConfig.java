package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;

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

            delay = delay * 1000L; // Convert from seconds to milliseconds
          }

          String timeframe = "";

          if (map.get("pointsTimeframe") != null) {
            try {
              DateUtils.parseDateDiff(timeframe, false);

              timeframe = (String) map.get("pointsTimeframe");
            } catch (Exception e) {
              logger.severe("Invalid pointsTimeframe for " + map.get("cmd"));
              continue;
            }
          }

          actionCommands.add(new ActionCommand((String) map.get("cmd"), delay, timeframe));
        }

        this.actions.put(amountDbl, actionCommands);
      } else {
        List<String> actions = actionsConf.getStringList(amount);
        if (actions.size() == 0) continue;

        logger
                .warning("warningActions amount " + amount + " is using a deprecated list, please use new cmd and delay syntax");
        List<ActionCommand> actionCommands = new ArrayList<>(actions.size());

        for (String action : actions) {
          actionCommands.add(new ActionCommand(action, 0, ""));
        }

        this.actions.put(amountDbl, actionCommands);
      }
    }
  }

  public List<ActionCommand> getCommands(PlayerData player, double overallPoints) {
    List<ActionCommand> commands = new ArrayList<>();

    for (Map.Entry<Double, List<ActionCommand>> entry : actions.entrySet()) {
      for (ActionCommand actionCommand : entry.getValue()) {
        double totalPoints = overallPoints;

        if (!actionCommand.getPointsTimeframe().isEmpty()) {
          try {
            totalPoints = BanManagerPlugin.getInstance().getPlayerWarnStorage().getPointsCount(player, DateUtils.parseDateDiff(actionCommand.getPointsTimeframe(), false));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        if (totalPoints == entry.getKey()) {
          commands.add(actionCommand);
        }
      }
    }

    return commands;
  }

}
