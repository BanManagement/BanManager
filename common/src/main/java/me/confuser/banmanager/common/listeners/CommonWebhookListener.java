package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.api.event.ip.IpBannedEvent;
import me.confuser.banmanager.api.event.ip.IpUnbannedEvent;
import me.confuser.banmanager.api.event.player.PlayerBannedEvent;
import me.confuser.banmanager.api.event.player.PlayerKickedEvent;
import me.confuser.banmanager.api.event.player.PlayerMutedEvent;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.api.event.player.PlayerUnbannedEvent;
import me.confuser.banmanager.api.event.player.PlayerUnmutedEvent;
import me.confuser.banmanager.api.event.player.PlayerWarnedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.data.Webhook;
import me.confuser.banmanager.common.impl.IpAddressMapper;
import me.confuser.banmanager.common.util.DateUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonWebhookListener {
  private final BanManagerPlugin plugin;

  public CommonWebhookListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    plugin.getEventBus().subscribe(PlayerBannedEvent.class, e -> sendAll(notifyOnBan(e.ban()), e.silent()));
    plugin.getEventBus().subscribe(PlayerMutedEvent.class, e -> sendAll(notifyOnMute(e.mute()), e.silent()));
    plugin.getEventBus().subscribe(IpBannedEvent.class, e -> sendAll(notifyOnBan(e.ban()), e.silent()));
    plugin.getEventBus().subscribe(PlayerKickedEvent.class,
        e -> sendAll(notifyOnKick(e.id(), e.player(), e.actor(), e.reason(), e.created()), e.silent()));
    plugin.getEventBus().subscribe(PlayerWarnedEvent.class, e -> sendAll(notifyOnWarn(e.warn()), e.silent()));
    plugin.getEventBus().subscribe(PlayerUnbannedEvent.class,
        e -> sendAll(notifyOnUnban(e.ban(), e.actor(), e.reason()), e.silent()));
    plugin.getEventBus().subscribe(IpUnbannedEvent.class,
        e -> sendAll(notifyOnUnban(e.ban(), e.actor(), e.reason()), e.silent()));
    plugin.getEventBus().subscribe(PlayerUnmutedEvent.class,
        e -> sendAll(notifyOnUnmute(e.mute(), e.actor(), e.reason()), e.silent()));
    plugin.getEventBus().subscribe(PlayerReportedEvent.class,
        e -> sendAll(notifyOnReport(e.report(), e.report().actor(), e.report().reason()), false));
  }

  private String toISO8601(long timestamp) {
    return DateTimeFormatter.ISO_INSTANT
        .format(java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
  }

  public List<Webhook> notifyOnBan(PlayerBan ban) {
    String type = ban.expires() == 0 ? "ban" : "tempban";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", ban.player().name());
    replacements.put("[playerId]", ban.player().uuid().toString());
    replacements.put("[actor]", ban.actor().name());
    replacements.put("[actorId]", ban.actor().uuid().toString());
    replacements.put("[id]", String.valueOf(ban.id()));
    replacements.put("[created]", toISO8601(ban.created()));
    replacements.put("[reason]", ban.reason());

    if (ban.expires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(ban.expires()));
    }

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnBan(IpBan ban) {
    String type = ban.expires() == 0 ? "banip" : "tempbanip";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

    me.confuser.banmanager.common.ipaddr.IPAddress internalIp = IpAddressMapper.toInternal(ban.ip());
    List<PlayerData> players = plugin.getPlayerStorage().getDuplicatesInTime(internalIp,
        plugin.getConfig().getTimeAssociatedAlts());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() >= 2)
      playerNames.setLength(playerNames.length() - 2);

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[ip]", ban.ip().toString());
    replacements.put("[actor]", ban.actor().name());
    replacements.put("[actorId]", ban.actor().uuid().toString());
    replacements.put("[reason]", ban.reason());
    replacements.put("[created]", toISO8601(ban.created()));
    replacements.put("[players]", playerNames.toString());

    if (ban.expires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(ban.expires()));
    }

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnKick(int id, Player player, Player actor, String reason, long created) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("kick");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", player.name());
    replacements.put("[playerId]", player.uuid().toString());
    replacements.put("[actor]", actor.name());
    replacements.put("[actorId]", actor.uuid().toString());
    replacements.put("[id]", String.valueOf(id));
    replacements.put("[created]", toISO8601(created));
    replacements.put("[reason]", reason);

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnMute(PlayerMute mute) {
    String type = mute.expires() == 0 ? "mute" : "tempmute";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", mute.player().name());
    replacements.put("[playerId]", mute.player().uuid().toString());
    replacements.put("[actor]", mute.actor().name());
    replacements.put("[actorId]", mute.actor().uuid().toString());
    replacements.put("[id]", String.valueOf(mute.id()));
    replacements.put("[created]", toISO8601(mute.created()));
    replacements.put("[reason]", mute.reason());

    if (mute.expires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(mute.expires()));
    }

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnWarn(PlayerWarn warn) {
    String type = warn.expires() == 0 ? "warning" : "tempwarning";
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks(type);

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", warn.player().name());
    replacements.put("[playerId]", warn.player().uuid().toString());
    replacements.put("[actor]", warn.actor().name());
    replacements.put("[actorId]", warn.actor().uuid().toString());
    replacements.put("[id]", String.valueOf(warn.id()));
    replacements.put("[created]", toISO8601(warn.created()));
    replacements.put("[points]", String.valueOf(warn.points()));
    replacements.put("[reason]", warn.reason());

    if (warn.expires() != 0) {
      replacements.put("[expires]", DateUtils.getDifferenceFormat(warn.expires()));
    }

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnUnban(PlayerBan ban, Player actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("unban");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", ban.player().name());
    replacements.put("[playerId]", ban.player().uuid().toString());
    replacements.put("[actor]", actor.name());
    replacements.put("[actorId]", actor.uuid().toString());
    replacements.put("[id]", String.valueOf(ban.id()));
    replacements.put("[created]", toISO8601(ban.created()));
    replacements.put("[reason]", reason);

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnUnban(IpBan ban, Player actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("unbanip");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[ip]", ban.ip().toString());
    replacements.put("[actor]", actor.name());
    replacements.put("[actorId]", actor.uuid().toString());
    replacements.put("[id]", String.valueOf(ban.id()));
    replacements.put("[created]", toISO8601(ban.created()));
    replacements.put("[reason]", reason);

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnUnmute(PlayerMute mute, Player actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("unmute");

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", mute.player().name());
    replacements.put("[playerId]", mute.player().uuid().toString());
    replacements.put("[actor]", actor.name());
    replacements.put("[actorId]", actor.uuid().toString());
    replacements.put("[id]", String.valueOf(mute.id()));
    replacements.put("[created]", toISO8601(mute.created()));
    replacements.put("[reason]", reason);

    return resolve(hooks, replacements);
  }

  public List<Webhook> notifyOnReport(PlayerReport report, Player actor, String reason) {
    List<Webhook> hooks = plugin.getWebhookConfig().getHooks("report");

    List<PlayerReportLocationData> locations = null;
    try {
      locations = plugin.getPlayerReportLocationStorage().getAllByReportId(report.id());
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to load report locations for webhook", e);
    }

    Map<String, String> replacements = new HashMap<>();
    replacements.put("[player]", report.player().name());
    replacements.put("[playerId]", report.player().uuid().toString());
    replacements.put("[actor]", actor.name());
    replacements.put("[actorId]", actor.uuid().toString());
    replacements.put("[id]", String.valueOf(report.id()));
    replacements.put("[created]", toISO8601(report.created()));
    replacements.put("[reason]", reason);

    if (locations != null && !locations.isEmpty()) {
      PlayerReportLocationData playerLocation = null;
      PlayerReportLocationData actorLocation = null;

      for (PlayerReportLocationData location : locations) {
        if (location.getPlayer() != null && location.getPlayer().getUUID().equals(actor.uuid())) {
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

  private void sendAll(List<Webhook> webhooks, boolean isSilent) {
    for (Webhook data : webhooks) {
      if (isSilent && data.ignoreSilent()) continue;
      if (data.url() == null || data.payload() == null || data.url().isEmpty() || data.payload().isEmpty()) continue;
      sendAsync(data);
    }
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
