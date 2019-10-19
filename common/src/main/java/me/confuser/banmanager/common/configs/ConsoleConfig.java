package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;

import java.io.File;
import java.util.UUID;

public class ConsoleConfig extends Config {

  @Getter
  private String name;
  @Getter
  private UUID uuid;

  public ConsoleConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "console.yml", logger);
  }

  @Override
  public void afterLoad() {
    if (conf.getString("uuid", "0").equals("0")) {
      uuid = UUID.randomUUID();
      save();
    } else {
      uuid = UUID.fromString(conf.getString("uuid"));
    }

    name = conf.getString("name");
  }

  @Override
  public void onSave() {
    if (uuid == null) {
      return;
    }

    conf.set("uuid", uuid.toString());
    conf.set("name", name);
  }
}
