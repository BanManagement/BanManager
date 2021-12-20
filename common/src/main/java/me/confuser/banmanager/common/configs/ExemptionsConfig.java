package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerExemptionsData;

import java.io.File;
import java.util.*;

public class ExemptionsConfig extends Config {

  private static ArrayList<String> types = new ArrayList<String>() {

    {
      add("alts");
      add("ban");
      add("tempban");
      add("baniprange");
      add("tempbaniprange");
      add("mute");
      add("tempmute");
      add("warn");
      add("tempwarn");
    }
  };
  private HashMap<UUID, PlayerExemptionsData> players;

  public ExemptionsConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "exemptions.yml", logger);
  }

  @Override
  public void afterLoad() {
    players = new HashMap<>();
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
    return isExempt(player.getUUID(), type);
  }

  public boolean isExempt(UUID uuid, String type) {
    PlayerExemptionsData exemptionsData = players.get(uuid);

    return exemptionsData != null && exemptionsData.isExempt(type);
  }
}
