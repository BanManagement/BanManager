package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.util.Message;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DiscordConfig extends Config {
  @Getter
  private boolean enabled = false;
  private Map<String, DiscordPunishmentConfig> types;

  public DiscordConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "discord.yml", logger);
  }

  @Override
  public void afterLoad() {
    types = new HashMap<>();

    enabled = conf.getBoolean("enabled", false);
    ConfigurationSection punishments = conf.getConfigurationSection("punishments");

    for (String type : punishments.getKeys(false)) {
      types.put(type, new DiscordPunishmentConfig(
          punishments.getString(type + ".channel"),
          new Message("discord." + type, punishments.getString(type + ".message"))
      ));
    }
  }

  @Override
  public void onSave() {

  }

  public DiscordPunishmentConfig getType(String type) {
    return types.get(type);
  }

  @AllArgsConstructor
  public class DiscordPunishmentConfig {
    @Getter
    private String channel;
    private Message message;

    public Message getMessage() {
      return Message.get(message.getKey());
    }
  }
}
