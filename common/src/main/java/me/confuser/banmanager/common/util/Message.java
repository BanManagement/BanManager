package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.Config;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;

import java.util.HashMap;

public class Message {

  private static HashMap<String, String> messages = new HashMap<>(10);
  private static CommonLogger logger;

  private String message;

  public Message(String key) {
    this.message = messages.get(key);

    if (this.message == null) {
      logger.warning("Missing " + key + " message");
      this.message = "";
    }
  }

  public static Message get(String key) {
    return new Message(key);
  }

  public static String getString(String key) {
    return messages.get(key);
  }

  public static void load(YamlConfiguration config, CommonLogger commonLogger) {
    logger = commonLogger;
    messages.clear();

    for (String key : config.getConfigurationSection("messages").getKeys(true)) {
      messages.put(key, config.getString("messages." + key).replace("\\n", "\n"));
    }
  }

  public static void load(Config config) {
    load(config.conf, config.getLogger());
  }

  public Message replace(CharSequence oldChar, CharSequence newChar) {
    message = this.message.replace(oldChar, newChar);

    return this;
  }

  public Message set(String token, String replace) {
    return replace("[" + token + "]", replace);
  }

  public Message set(String token, Integer replace) {
    return replace("[" + token + "]", replace.toString());
  }

  public Message set(String token, Double replace) {
    return replace("[" + token + "]", replace.toString());
  }

  public Message set(String token, Long replace) {
    return replace("[" + token + "]", replace.toString());
  }

  public Message set(String token, Float replace) {
    return replace("[" + token + "]", replace.toString());
  }

  public boolean sendTo(CommonPlayer player) {
    if (player == null || message.isEmpty())
      return false;

    player.sendMessage(message);

    return true;
  }

  @Override
  public String toString() {
    return message;
  }
}

