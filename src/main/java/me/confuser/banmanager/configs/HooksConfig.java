package me.confuser.banmanager.configs;

import lombok.Getter;
import me.confuser.banmanager.BanManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class HooksConfig {

  private static HashSet<String> validEvents = new HashSet<String>() {

    {
      add("ban");
      add("tempban");
      add("unban");
      add("mute");
      add("tempmute");
      add("unmute");
      add("ipban");
      add("tempipban");
      add("unbanip");
      add("iprangeban");
      add("tempiprangeban");
      add("unbaniprange");
      add("note");
      add("warn");
      add("tempwarn");
    }
  };
  private BanManager plugin = BanManager.getPlugin();

  @Getter
  private boolean isEnabled = false;
  private HashMap<String, Hook> hooks;

  public HooksConfig(ConfigurationSection config) {
    isEnabled = config.getBoolean("enabled", false);
    hooks = new HashMap<>();

    if (!isEnabled) return;

    ConfigurationSection eventsConf = config.getConfigurationSection("events");

    if (eventsConf == null) return;

    for (String event : eventsConf.getKeys(false)) {
      if (!validEvents.contains(event)) {
        plugin.getLogger().warning("Invalid event " + event);
        continue;
      }

      List<ActionCommand> preActionCommands = getActionCommands(event, eventsConf.getMapList(event + ".pre"));
      List<ActionCommand> postActionCommands = getActionCommands(event, eventsConf.getMapList(event + ".post"));

      Hook hook = new Hook(preActionCommands, postActionCommands);

      hooks.put(event, hook);
    }

  }

  private List<ActionCommand> getActionCommands(String event, List<Map<?, ?>> mapList) {
    List<ActionCommand> actionCommands = new ArrayList<>();

    if (mapList != null && mapList.size() != 0) {

      for (Map<?, ?> map : mapList) {
        if (map.get("cmd") == null) {
          plugin.getLogger().severe("Missing cmd from " + event + " hook");
          continue;
        }

        long delay = 0;

        if (map.get("delay") != null) {
          try {
            delay = Long.valueOf((Integer) map.get("delay"));
          } catch (NumberFormatException e) {
            plugin.getLogger().severe("Invalid delay for " + map.get("cmd"));
            continue;
          }

          delay = delay * 20L; // Convert from seconds to ticks
        }

        actionCommands.add(new ActionCommand((String) map.get("cmd"), delay));
      }
    }

    return actionCommands;
  }

  public Hook getHook(String event) {
    return hooks.get(event);
  }
}
