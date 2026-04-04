package me.confuser.banmanager.common.util;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.PlaceholderResolver;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.configs.Config;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public class Message {

  private static volatile MessageRegistry registry;
  private static volatile CommonLogger logger;

  @Getter
  private String key;
  private final LinkedHashMap<String, String[]> replacements = new LinkedHashMap<>();

  public Message(String key) {
    this.key = key;

    if (registry != null && registry.getMessage(key) == null) {
      if (logger != null) logger.warning("Missing " + key + " message");
    }
  }

  public Message(String key, String message) {
    this.key = key;

    if (registry != null) {
      if (registry.getMessage(key) != null) {
        if (logger != null) logger.warning(key + " message already exists");
        return;
      }
      registry.putMessage(key, message);
    }
  }

  public static void init(MessageRegistry messageRegistry, CommonLogger commonLogger) {
    registry = messageRegistry;
    logger = commonLogger;
  }

  public static Message get(String key) {
    return new Message(key);
  }

  public static String getString(String key) {
    if (registry == null) return null;
    return registry.getMessage(key);
  }

  public static String getString(String key, String locale) {
    if (registry == null) return null;
    return registry.getMessage(key, locale);
  }

  /**
   * @deprecated Use {@link #init(MessageRegistry, CommonLogger)} instead.
   * Loads messages from the YAML config into the active registry for backwards compatibility.
   */
  @Deprecated
  public static void load(YamlConfiguration config, CommonLogger commonLogger) {
    logger = commonLogger;

    if (registry != null && config.getConfigurationSection("messages") != null) {
      for (String key : config.getConfigurationSection("messages").getKeys(true)) {
        String value = config.getString("messages." + key);
        if (value != null) {
          registry.putMessage(key, value.replace("\\n", "\n").replaceAll("(?<=\\n)(?=\\n)", " "));
        }
      }
    }
  }

  /**
   * @deprecated Use {@link #init(MessageRegistry, CommonLogger)} instead.
   */
  @Deprecated
  public static void load(Config config) {
    load(config.conf, config.getLogger());
  }

  public Message replace(CharSequence oldChar, CharSequence newChar) {
    replacements.put("__replace__" + replacements.size(), new String[]{oldChar.toString(), newChar.toString()});
    return this;
  }

  public Message set(String token, String replace) {
    replacements.put(token, new String[]{"[" + token + "]", replace});
    return this;
  }

  public Message set(String token, Integer replace) {
    return set(token, replace.toString());
  }

  public Message set(String token, Double replace) {
    return set(token, replace.toString());
  }

  public Message set(String token, Long replace) {
    return set(token, replace.toString());
  }

  public Message set(String token, Float replace) {
    return set(token, replace.toString());
  }

  public String resolve(String locale) {
    if (registry == null) return "";

    String template = registry.getMessage(key, locale);
    if (template == null) return "";

    return applyReplacements(template);
  }

  public String resolveFor(CommonPlayer player) {
    if (player == null) return resolve(getDefaultLocale());

    BanManagerPlugin plugin = BanManagerPlugin.getInstance();
    if (plugin != null && plugin.getConfig() != null && plugin.getConfig().isPerPlayerLocale()) {
      return resolve(player.getLocale());
    }
    return resolve(getDefaultLocale());
  }

  public boolean sendTo(CommonSender sender) {
    if (sender == null) return false;

    sender.sendMessage(resolve(getDefaultLocale()));

    return true;
  }

  public boolean sendTo(CommonPlayer player) {
    if (player == null) return false;
    if (!player.isOnline()) return false;

    String resolved = resolveFor(player);
    PlaceholderResolver resolver = BanManagerPlugin.getInstance().getPlaceholderResolver();
    if (resolver != null) {
      resolved = resolver.resolve(player, resolved);
    }

    player.sendMessage(resolved);

    return true;
  }

  public static boolean isJSONMessage(String message) {
    return message.startsWith("{") && message.endsWith("}") || message.startsWith("[") && message.endsWith("]");
  }

  @Override
  public String toString() {
    return resolve(getDefaultLocale());
  }

  private String applyReplacements(String template) {
    String result = template;
    for (Map.Entry<String, String[]> entry : replacements.entrySet()) {
      String[] pair = entry.getValue();
      result = result.replace(pair[0], pair[1]);
    }
    return result;
  }

  private static String getDefaultLocale() {
    if (registry == null) return "en";
    return registry.getDefaultLocale();
  }
}
