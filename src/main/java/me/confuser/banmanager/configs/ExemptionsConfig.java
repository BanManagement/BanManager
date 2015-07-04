package me.confuser.banmanager.configs;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerExemptionsData;
import me.confuser.bukkitutil.configs.Config;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ExemptionsConfig extends Config<BanManager> {

  private static ArrayList<String> types = new ArrayList<String>() {

    {
      add("ban");
      add("tempban");
      add("mute");
      add("tempmute");
      add("warn");
      add("tempwarn");
    }
  };
  private HashMap<UUID, PlayerExemptionsData> players;

  public ExemptionsConfig() {
    super("exemptions.yml");
  }

  @Override
  public void afterLoad() {
    players =  new HashMap<>();
    Set<String> keys = conf.getKeys(false);

    if (keys == null || keys.size() == 0) return;

    for (String uuidStr : keys) {
      UUID uuid;
      try {
        uuid = UUID.fromString(uuidStr);
      } catch (IllegalArgumentException e) {
        continue;
      }

      ConfigurationSection exemptionsSection = conf.getConfigurationSection(uuidStr);
      HashSet<String> exemptions = new HashSet<>();

      for (String type : types) {
        if (exemptionsSection.getBoolean(type, false)) exemptions.add(type);
      }

      players.put(uuid, new PlayerExemptionsData(exemptions));
    }
  }

  @Override
  public void onSave() {
  }

  public boolean isExempt(PlayerData player, String type) {
    PlayerExemptionsData exemptionsData = players.get(player.getUUID());

    if (exemptionsData == null) return false;

    return exemptionsData.isExempt(type);
  }
}
