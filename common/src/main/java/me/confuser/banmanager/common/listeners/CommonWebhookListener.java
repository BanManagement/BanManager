package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.WebhookConfig.WebhookHookConfig;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import java.net.HttpURLConnection;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;

public class CommonWebhookListener {
  private BanManagerPlugin plugin;

  public CommonWebhookListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  private String toISO8601(long timestamp) {
    return DateTimeFormatter.ISO_INSTANT
        .format(java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
  }

  public List<WebhookData> notifyOnBan(PlayerBanData ban) {
    String type = ban.getExpires() == 0 ? "ban" : "tempban";
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks(type);
    List<WebhookData> results = new ArrayList<>();

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", ban.getPlayer().getName());
    replacements.put("[playerId]", ban.getPlayer().getUUID().toString());
    replacements.put("[actor]", ban.getActor().getName());
    replacements.put("[actorId]", ban.getActor().getUUID().toString());
    replacements.put("[id]", String.valueOf(ban.getId()));
    replacements.put("[created]", toISO8601(ban.getCreated()));
    replacements.put("[reason]", ban.getReason());

    if (ban.getExpires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnBan(IpBanData ban) {
    String type = ban.getExpires() == 0 ? "banip" : "tempbanip";
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks(type);
    List<WebhookData> results = new ArrayList<>();

    List<PlayerData> players = plugin.getPlayerStorage().getDuplicatesInTime(ban.getIp(),
        plugin.getConfig().getTimeAssociatedAlts());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() >= 2)
      playerNames.setLength(playerNames.length() - 2);

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[ip]", ban.getIp().toString());
    replacements.put("[actor]", ban.getActor().getName());
    replacements.put("[actorId]", ban.getActor().getUUID().toString());
    replacements.put("[reason]", ban.getReason());
    replacements.put("[created]", toISO8601(ban.getCreated()));
    replacements.put("[players]", playerNames.toString());

    if (ban.getExpires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnKick(PlayerKickData kick) {
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks("kick");
    List<WebhookData> results = new ArrayList<>();

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", kick.getPlayer().getName());
    replacements.put("[playerId]", kick.getPlayer().getUUID().toString());
    replacements.put("[actor]", kick.getActor().getName());
    replacements.put("[actorId]", kick.getActor().getUUID().toString());
    replacements.put("[id]", String.valueOf(kick.getId()));
    replacements.put("[created]", toISO8601(kick.getCreated()));
    replacements.put("[reason]", kick.getReason());

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnMute(PlayerMuteData mute) {
    String type = mute.getExpires() == 0 ? "mute" : "tempmute";
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks(type);
    List<WebhookData> results = new ArrayList<>();

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", mute.getPlayer().getName());
    replacements.put("[playerId]", mute.getPlayer().getUUID().toString());
    replacements.put("[actor]", mute.getActor().getName());
    replacements.put("[actorId]", mute.getActor().getUUID().toString());
    replacements.put("[id]", String.valueOf(mute.getId()));
    replacements.put("[created]", toISO8601(mute.getCreated()));
    replacements.put("[reason]", mute.getReason());

    if (mute.getExpires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnWarn(PlayerWarnData warn) {
    String type = warn.getExpires() == 0 ? "warning" : "tempwarning";
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks(type);
    List<WebhookData> results = new ArrayList<>();

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", warn.getPlayer().getName());
    replacements.put("[playerId]", warn.getPlayer().getUUID().toString());
    replacements.put("[actor]", warn.getActor().getName());
    replacements.put("[actorId]", warn.getActor().getUUID().toString());
    replacements.put("[id]", String.valueOf(warn.getId()));
    replacements.put("[created]", toISO8601(warn.getCreated()));
    replacements.put("[points]", String.valueOf(warn.getPoints()));
    replacements.put("[reason]", warn.getReason());

    if (warn.getExpires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(warn.getExpires()));
    }

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnUnban(PlayerBanData ban, PlayerData actor, String reason) {
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks("unban");
    List<WebhookData> results = new ArrayList<>();

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", ban.getPlayer().getName());
    replacements.put("[playerId]", ban.getPlayer().getUUID().toString());
    replacements.put("[actor]", actor.getName());
    replacements.put("[actorId]", actor.getUUID().toString());
    replacements.put("[id]", String.valueOf(ban.getId()));
    replacements.put("[created]", toISO8601(ban.getCreated()));
    replacements.put("[reason]", reason);

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnUnban(IpBanData ban, PlayerData actor, String reason) {
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks("unbanip");
    List<WebhookData> results = new ArrayList<>();

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[ip]", ban.getIp().toString());
    replacements.put("[actor]", actor.getName());
    replacements.put("[actorId]", actor.getUUID().toString());
    replacements.put("[id]", String.valueOf(ban.getId()));
    replacements.put("[created]", toISO8601(ban.getCreated()));
    replacements.put("[reason]", reason);

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnUnmute(PlayerMuteData mute, PlayerData actor, String reason) {
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks("unmute");
    List<WebhookData> results = new ArrayList<>();

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", mute.getPlayer().getName());
    replacements.put("[playerId]", mute.getPlayer().getUUID().toString());
    replacements.put("[actor]", actor.getName());
    replacements.put("[actorId]", actor.getUUID().toString());
    replacements.put("[id]", String.valueOf(mute.getId()));
    replacements.put("[created]", toISO8601(mute.getCreated()));
    replacements.put("[reason]", reason);

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  public List<WebhookData> notifyOnReport(PlayerReportData report, PlayerData actor, String reason) {
    List<WebhookHookConfig> hooks = plugin.getWebhookConfig().getHooks("report");
    List<WebhookData> results = new ArrayList<>();

    List<PlayerReportLocationData> locations = null;
    try {
      locations = plugin.getPlayerReportLocationStorage().getByReport(report);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", report.getPlayer().getName());
    replacements.put("[playerId]", report.getPlayer().getUUID().toString());
    replacements.put("[actor]", actor.getName());
    replacements.put("[actorId]", actor.getUUID().toString());
    replacements.put("[id]", String.valueOf(report.getId()));
    replacements.put("[created]", toISO8601(report.getCreated()));
    replacements.put("[reason]", reason);

    if (locations != null && locations.size() > 0) {
      PlayerReportLocationData playerLocation = null;
      PlayerReportLocationData actorLocation = null;

      for (PlayerReportLocationData location : locations) {
        if (location.getPlayer().equals(actor)) {
          actorLocation = location;
        } else {
          playerLocation = location;
        }
      }

      if (playerLocation != null) {
        replacements.put("[playerWorld]", playerLocation.getWorld());
        replacements.put("[playerX]", String.valueOf(playerLocation.getX()));
        replacements.put("[playerY]", String.valueOf(playerLocation.getY()));
        replacements.put("[playerZ]", String.valueOf(playerLocation.getZ()));
        replacements.put("[playerYaw]", String.valueOf(playerLocation.getYaw()));
        replacements.put("[playerPitch]", String.valueOf(playerLocation.getPitch()));
      }

      if (actorLocation != null) {
        replacements.put("[actorWorld]", actorLocation.getWorld());
        replacements.put("[actorX]", String.valueOf(actorLocation.getX()));
        replacements.put("[actorY]", String.valueOf(actorLocation.getY()));
        replacements.put("[actorZ]", String.valueOf(actorLocation.getZ()));
        replacements.put("[actorYaw]", String.valueOf(actorLocation.getYaw()));
        replacements.put("[actorPitch]", String.valueOf(actorLocation.getPitch()));
      }
    }

    for (WebhookHookConfig hook : hooks) {
      results.add(createWebhookData(hook, replacements));
    }

    return results;
  }

  private WebhookData createWebhookData(WebhookHookConfig hook, Map<String, String> replacements) {
    String payload = applyReplacements(hook.getPayload(), replacements);
    Map<String, String> headers = new HashMap<>();
    for (Map.Entry<String, String> entry : hook.getHeaders().entrySet()) {
      headers.put(entry.getKey(), applyReplacements(entry.getValue(), replacements));
    }
    return new WebhookData(hook.getName(), hook.getUrl(), hook.getMethod(), headers, payload, hook.isIgnoreSilent());
  }

  private String applyReplacements(String input, Map<String, String> replacements) {
    if (input == null) return "";
    String result = input;
    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public void sendAsync(WebhookData data) {
    CompletableFuture.runAsync(() -> send(data));
  }

  public void send(WebhookData data) {
    if (plugin.getConfig().isDebugEnabled()) {
      plugin.getLogger().info("Sending webhook '" + data.name + "' to " + data.url + " with method " + data.method);
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) new URL(data.url).openConnection();
      connection.addRequestProperty("Content-Type", "application/json");
      connection.addRequestProperty("User-Agent", "BanManager");

      // Apply custom headers
      for (Map.Entry<String, String> header : data.headers.entrySet()) {
        connection.addRequestProperty(header.getKey(), header.getValue());
      }

      connection.setRequestMethod(data.method);

      // Only set output for methods that support a body
      if (!"GET".equals(data.method) && !"DELETE".equals(data.method)) {
        connection.setDoOutput(true);
        try (OutputStream stream = connection.getOutputStream()) {
          stream.write(data.payload.getBytes());
          stream.flush();
        }
      }

      int responseCode = connection.getResponseCode();
      if (responseCode > 299) {
        plugin.getLogger().warning("Failed to send webhook '" + data.name + "'");
        plugin.getLogger().warning("Response code: " + responseCode);

        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
              responseBody.append(line);
            }
            plugin.getLogger().warning("Response body: " + responseBody.toString());
          }
        }
      } else {
        try (InputStream in = connection.getInputStream()) {
          while (in.read() != -1) {}
        }
      }
    } catch (Exception e) {
      plugin.getLogger().warning("Failed to send webhook '" + data.name + "'");
      plugin.getLogger().warning("Error: " + e.getMessage());
      if (plugin.getConfig().isDebugEnabled()) {
        e.printStackTrace();
      }
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /**
   * Data class to hold all information needed to send a webhook request.
   */
  public static class WebhookData {
    public final String name;
    public final String url;
    public final String method;
    public final Map<String, String> headers;
    public final String payload;
    public final boolean ignoreSilent;

    public WebhookData(String name, String url, String method, Map<String, String> headers, String payload, boolean ignoreSilent) {
      this.name = name;
      this.url = url;
      this.method = method;
      this.headers = headers;
      this.payload = payload;
      this.ignoreSilent = ignoreSilent;
    }
  }
}
