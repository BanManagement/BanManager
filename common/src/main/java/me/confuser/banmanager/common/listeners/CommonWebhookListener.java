package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
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

  public List<Webhook> notifyOnBan(PlayerBanData ban) {
    String type = ban.getExpires() == 0 ? "ban" : "tempban";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

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

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnBan(IpBanData ban) {
    String type = ban.getExpires() == 0 ? "banip" : "tempbanip";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

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

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnKick(PlayerKickData kick) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("kick");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", kick.getPlayer().getName());
    replacements.put("[playerId]", kick.getPlayer().getUUID().toString());
    replacements.put("[actor]", kick.getActor().getName());
    replacements.put("[actorId]", kick.getActor().getUUID().toString());
    replacements.put("[id]", String.valueOf(kick.getId()));
    replacements.put("[created]", toISO8601(kick.getCreated()));
    replacements.put("[reason]", kick.getReason());

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnMute(PlayerMuteData mute) {
    String type = mute.getExpires() == 0 ? "mute" : "tempmute";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

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

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnWarn(PlayerWarnData warn) {
    String type = warn.getExpires() == 0 ? "warning" : "tempwarning";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

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

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnUnban(PlayerBanData ban, PlayerData actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("unban");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", ban.getPlayer().getName());
    replacements.put("[playerId]", ban.getPlayer().getUUID().toString());
    replacements.put("[actor]", actor.getName());
    replacements.put("[actorId]", actor.getUUID().toString());
    replacements.put("[id]", String.valueOf(ban.getId()));
    replacements.put("[created]", toISO8601(ban.getCreated()));
    replacements.put("[reason]", reason);

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnUnban(IpBanData ban, PlayerData actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("unbanip");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[ip]", ban.getIp().toString());
    replacements.put("[actor]", actor.getName());
    replacements.put("[actorId]", actor.getUUID().toString());
    replacements.put("[id]", String.valueOf(ban.getId()));
    replacements.put("[created]", toISO8601(ban.getCreated()));
    replacements.put("[reason]", reason);

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnUnmute(PlayerMuteData mute, PlayerData actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("unmute");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", mute.getPlayer().getName());
    replacements.put("[playerId]", mute.getPlayer().getUUID().toString());
    replacements.put("[actor]", actor.getName());
    replacements.put("[actorId]", actor.getUUID().toString());
    replacements.put("[id]", String.valueOf(mute.getId()));
    replacements.put("[created]", toISO8601(mute.getCreated()));
    replacements.put("[reason]", reason);

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnReport(PlayerReportData report, PlayerData actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("report");

    List<PlayerReportLocationData> locations = null;
    try {
      locations = plugin.getPlayerReportLocationStorage().getByReport(report);
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to load report locations for webhook", e);
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

    return resolve(hooks, replacements);
  }

  private List<Webhook> resolve(List<Webhook> hooks, Map<String, String> replacements) {
    List<Webhook> results = new ArrayList<>(hooks.size());
    for (Webhook hook : hooks) {
      results.add(applyReplacements(hook, replacements));
    }
    return results;
  }

  private Webhook applyReplacements(Webhook hook, Map<String, String> replacements) {
    String payload = applyReplacements(hook.payload(), replacements);
    Map<String, String> headers = new HashMap<>();
    for (Map.Entry<String, String> entry : hook.headers().entrySet()) {
      headers.put(entry.getKey(), applyReplacements(entry.getValue(), replacements));
    }
    return hook.withResolved(headers, payload);
  }

  private String applyReplacements(String input, Map<String, String> replacements) {
    if (input == null) return "";
    String result = input;
    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public void sendAsync(Webhook data) {
    if (plugin.getConfig().isDebugEnabled()) {
      plugin.getLogger().info("Sending webhook '" + data.name() + "' to " + data.url() + " with method " + data.method());
    }

    HttpRequest request;
    try {
      request = buildRequest(data);
    } catch (IllegalArgumentException e) {
      plugin.getLogger().warning("Failed to send webhook '" + data.name() + "': invalid URL or method - " + e.getMessage());
      return;
    }

    plugin.getHttpClient()
        .sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        .whenComplete((response, throwable) -> {
          if (throwable != null) {
            plugin.getLogger().warning("Failed to send webhook '" + data.name() + "'", throwable);
            return;
          }

          int responseCode = response.statusCode();
          if (responseCode > 299) {
            plugin.getLogger().warning("Failed to send webhook '" + data.name() + "'");
            plugin.getLogger().warning("Response code: " + responseCode);
            String body = response.body();
            if (body != null && !body.isEmpty()) {
              plugin.getLogger().warning("Response body: " + body);
            }
          }
        });
  }

  private HttpRequest buildRequest(Webhook data) {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(data.url()))
        .timeout(Duration.ofSeconds(15))
        .header("Content-Type", "application/json")
        .header("User-Agent", "BanManager");

    for (Map.Entry<String, String> header : data.headers().entrySet()) {
      builder.header(header.getKey(), header.getValue());
    }

    HttpRequest.BodyPublisher body = data.hasBody()
        ? HttpRequest.BodyPublishers.ofString(data.payload(), StandardCharsets.UTF_8)
        : HttpRequest.BodyPublishers.noBody();

    return builder.method(data.method(), body).build();
  }
}
