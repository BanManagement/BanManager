package me.confuser.banmanager.common.configs;


import me.confuser.banmanager.common.CommonLogger;

import java.io.File;

/**
 * Retained for backwards compatibility. Message loading is now handled
 * by {@link me.confuser.banmanager.common.BanManagerPlugin#setupConfigs()}
 * via the {@link me.confuser.banmanager.common.util.MessageRegistry}.
 */
public class MessagesConfig extends Config {

  public MessagesConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "messages.yml", logger);
  }

  public void afterLoad() {
  }

  public void onSave() {
  }
}
