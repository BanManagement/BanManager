package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.gson.Gson;
import me.confuser.banmanager.common.gson.GsonBuilder;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiscordConfig extends Config {
  @Getter
  private boolean hooksEnabled = false;
  private Map<String, DiscordHookConfig> hookTypes;

  public DiscordConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "discord.yml", logger);
  }

  @Override
  public void afterLoad() {
    if (conf.getBoolean("enabled", false)) {
      this.logger.warning("DiscordSRV/MagiBridge integration removed, please switch to hooks in discord.yml");
      return;
    }

    hooksEnabled = conf.getBoolean("hooks.enabled", false);

    ConfigurationSection hooks = conf.getConfigurationSection("hooks.punishments");
    hookTypes = new HashMap<>();

    for (String type : hooks.getKeys(false)) {
      ConfigurationSection payloadSection = hooks.getConfigurationSection(type + ".payload");
      Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
      StringWriter writer = new StringWriter();
      gson.toJson(payloadSection.getValues(true), writer);
      String payload = writer.toString();

      hookTypes.put(type, new DiscordHookConfig(
          hooks.getString(type + ".url"),
          payload,
          hooks.getBoolean(type + ".ignoreSilent", true)
      ));
    }
  }

  @Override
  public void onSave() {

  }

  public DiscordHookConfig getType(String type) {
    return hookTypes.get(type);
  }

  @AllArgsConstructor
  public class DiscordHookConfig {
    @Getter
    private String url;
    @Getter
    private String payload;
    @Getter
    private boolean ignoreSilent;
  }
}
