package me.confuser.banmanager.configs;

import lombok.Getter;
import me.confuser.banmanager.BanManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;

public class HooksConfig {

  private static HashSet<String> validEvents = new HashSet<String>() {

    {
      add("ban");
      add("tempban");
      add("mute");
      add("tempmute");
      add("ipban");
      add("tempipban");
      add("iprangeban");
      add("tempiprangeban");
      add("note");
      add("warn");
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

      Hook hook = new Hook(eventsConf.getStringList(event + ".pre"), eventsConf.getStringList(event + ".post"));

      hooks.put(event, hook);
    }
  }

  public Hook getHook(String event) {
    return hooks.get(event);
  }
}
