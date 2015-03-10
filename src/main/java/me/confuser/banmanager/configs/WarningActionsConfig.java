package me.confuser.banmanager.configs;

import lombok.Getter;
import me.confuser.banmanager.BanManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;

public class WarningActionsConfig {

  private BanManager plugin = BanManager.getPlugin();
  @Getter
  private boolean isEnabled = false;
  private HashMap<Integer, List<String>> actions;

  public WarningActionsConfig(ConfigurationSection config) {
    isEnabled = config.getBoolean("enabled", false);
    actions = new HashMap<>();
    ConfigurationSection actionsConf = config.getConfigurationSection("actions");

    if (actionsConf == null) return;

    for (String amount : actionsConf.getKeys(false)) {
      if (!StringUtils.isNumeric(amount)) {
        plugin.getLogger().warning("Invalid warning action, " + amount + " is not numeric");
        continue;
      }

      actions.put(NumberUtils.toInt(amount), actionsConf.getStringList(amount));
    }
  }

  public List<String> getCommand(int amount) {
    return actions.get(amount);
  }

}
