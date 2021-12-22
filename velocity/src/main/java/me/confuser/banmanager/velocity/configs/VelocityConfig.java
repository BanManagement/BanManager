package me.confuser.banmanager.velocity.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configs.Config;

import java.io.File;
import java.nio.file.Path;

public class VelocityConfig extends Config {
  @Getter
  private boolean commandsEnabled = false;

  public VelocityConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "velocity.yml", logger);
  }

  @Override
  public void afterLoad() {
    commandsEnabled = conf.getBoolean("features.commands", false);
  }

  @Override
  public void onSave() {
  }
}
