package me.confuser.banmanager.bungee.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configs.Config;

import java.io.File;

public class BungeeConfig extends Config {
  @Getter
  private boolean commandsEnabled = false;

  public BungeeConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "bungeecord.yml", logger);
  }

  @Override
  public void afterLoad() {
    commandsEnabled = conf.getBoolean("features.commands", false);
  }

  @Override
  public void onSave() {
  }
}
