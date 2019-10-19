package me.confuser.banmanager.common.configs;


import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.util.Message;

import java.io.File;

public class MessagesConfig extends Config {

  public MessagesConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "messages.yml", logger);
  }

  public void afterLoad() {
    Message.load(this);
  }

  public void onSave() {

  }

}
