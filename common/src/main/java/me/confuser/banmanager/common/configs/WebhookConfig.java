package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.gson.Gson;
import me.confuser.banmanager.common.gson.GsonBuilder;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class WebhookConfig extends Config {
  private static final Set<String> ALLOWED_METHODS = new HashSet<>(Arrays.asList(
      "GET", "POST", "PUT", "PATCH", "DELETE"
  ));

  @Getter
  private boolean hooksEnabled = false;
  private Map<String, List<WebhookHookConfig>> hookTypes = new HashMap<>();

  public WebhookConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "webhooks.yml", logger);
    checkForLegacyDiscordYml(dataFolder);
  }

  private void checkForLegacyDiscordYml(File dataFolder) {
    File discordFile = new File(dataFolder, "discord.yml");

    if (discordFile.exists()) {
      logger.warning("Found discord.yml - this file is no longer used.");
      logger.warning("The Discord integration has been replaced with a generalized webhooks feature.");
      logger.warning("Please configure webhooks.yml manually. See https://banmanagement.com/docs/banmanager/configuration/webhooks-yml");
      logger.warning("You can safely delete discord.yml after migrating your configuration.");
    }
  }

  @Override
  public void afterLoad() {
    if (conf.getBoolean("enabled", false)) {
      this.logger.warning("DiscordSRV/MagiBridge integration removed, please switch to hooks in webhooks.yml");
      return;
    }

    hooksEnabled = conf.getBoolean("hooks.enabled", false);

    ConfigurationSection hooks = conf.getConfigurationSection("hooks.punishments");
    hookTypes = new HashMap<>();

    if (hooks == null) {
      return;
    }

    for (String type : hooks.getKeys(false)) {
      List<WebhookHookConfig> webhooksList = new ArrayList<>();
      Object typeValue = hooks.get(type);

      if (typeValue instanceof List) {
        // New array format
        List<?> hookList = (List<?>) typeValue;
        int index = 0;
        for (Object hookObj : hookList) {
          if (hookObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> hookMap = (Map<String, Object>) hookObj;
            WebhookHookConfig config = parseWebhookConfig(hookMap, type, index);
            if (config != null) {
              webhooksList.add(config);
            }
            index++;
          }
        }
      } else {
        // Legacy single-object format - wrap in list
        ConfigurationSection hookSection = hooks.getConfigurationSection(type);
        if (hookSection != null) {
          WebhookHookConfig config = parseLegacyWebhookConfig(hookSection, type);
          if (config != null) {
            webhooksList.add(config);
          }
        }
      }

      hookTypes.put(type, webhooksList);
    }
  }

  private WebhookHookConfig parseWebhookConfig(Map<String, Object> hookMap, String type, int index) {
    String name = (String) hookMap.getOrDefault("name", "webhook-" + index);
    String url = (String) hookMap.get("url");
    String method = ((String) hookMap.getOrDefault("method", "POST")).toUpperCase();
    boolean ignoreSilent = hookMap.containsKey("ignoreSilent") ? (Boolean) hookMap.get("ignoreSilent") : true;

    // Validate URL
    if (!isValidUrl(url)) {
      logger.warning("Invalid URL for webhook '" + name + "' in punishment type '" + type + "': " + url);
      return null;
    }

    // Validate method
    if (!ALLOWED_METHODS.contains(method)) {
      logger.warning("Invalid method '" + method + "' for webhook '" + name + "' in punishment type '" + type + "'. Using POST.");
      method = "POST";
    }

    // Parse headers
    Map<String, String> headers = new HashMap<>();
    Object headersObj = hookMap.get("headers");
    if (headersObj instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> headersMap = (Map<String, Object>) headersObj;
      for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
        headers.put(entry.getKey(), String.valueOf(entry.getValue()));
      }
    }

    // Parse payload
    String payload = "";
    Object payloadObj = hookMap.get("payload");
    if (payloadObj != null) {
      Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
      StringWriter writer = new StringWriter();
      gson.toJson(payloadObj, writer);
      payload = writer.toString();
    }

    return new WebhookHookConfig(name, url, method, headers, payload, ignoreSilent);
  }

  private WebhookHookConfig parseLegacyWebhookConfig(ConfigurationSection hookSection, String type) {
    String url = hookSection.getString("url");
    String method = hookSection.getString("method", "POST").toUpperCase();
    boolean ignoreSilent = hookSection.getBoolean("ignoreSilent", true);

    // Validate URL
    if (!isValidUrl(url)) {
      logger.warning("Invalid URL for webhook in punishment type '" + type + "': " + url);
      return null;
    }

    // Validate method
    if (!ALLOWED_METHODS.contains(method)) {
      logger.warning("Invalid method '" + method + "' for webhook in punishment type '" + type + "'. Using POST.");
      method = "POST";
    }

    // Parse headers
    Map<String, String> headers = new HashMap<>();
    ConfigurationSection headersSection = hookSection.getConfigurationSection("headers");
    if (headersSection != null) {
      for (String key : headersSection.getKeys(false)) {
        headers.put(key, headersSection.getString(key));
      }
    }

    // Parse payload
    String payload = "";
    ConfigurationSection payloadSection = hookSection.getConfigurationSection("payload");
    if (payloadSection != null) {
      Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
      StringWriter writer = new StringWriter();
      gson.toJson(payloadSection.getValues(true), writer);
      payload = writer.toString();
    }

    return new WebhookHookConfig("webhook-0", url, method, headers, payload, ignoreSilent);
  }

  private boolean isValidUrl(String url) {
    if (url == null || url.isEmpty()) {
      return false;
    }
    try {
      URL parsed = new URL(url);
      String protocol = parsed.getProtocol().toLowerCase();
      return "http".equals(protocol) || "https".equals(protocol);
    } catch (MalformedURLException e) {
      return false;
    }
  }

  @Override
  public void onSave() {
  }

  public List<WebhookHookConfig> getHooks(String type) {
    List<WebhookHookConfig> hooks = hookTypes.get(type);
    return hooks != null ? hooks : Collections.emptyList();
  }

  /**
   * @deprecated Use {@link #getHooks(String)} instead for multi-webhook support.
   * This method returns the first webhook config for backwards compatibility.
   */
  @Deprecated
  public WebhookHookConfig getType(String type) {
    List<WebhookHookConfig> hooks = hookTypes.get(type);
    return (hooks != null && !hooks.isEmpty()) ? hooks.get(0) : null;
  }

  @AllArgsConstructor
  public static class WebhookHookConfig {
    @Getter
    private String name;
    @Getter
    private String url;
    @Getter
    private String method;
    @Getter
    private Map<String, String> headers;
    @Getter
    private String payload;
    @Getter
    private boolean ignoreSilent;
  }
}
