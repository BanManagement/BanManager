package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;

public class CommonDiscordListener {
  private BanManagerPlugin plugin;

  public CommonDiscordListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  private String toISO8601(long timestamp) {
    return DateTimeFormatter.ISO_INSTANT
        .format(java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
  }

  public Object[] notifyOnBan(PlayerBanData ban) {
    String url;
    String payload;
    boolean ignoreSilent;

    if (ban.getExpires() == 0) {
      url = plugin.getDiscordConfig().getType("ban").getUrl();
      payload = plugin.getDiscordConfig().getType("ban").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("ban").isIgnoreSilent();
    } else {
      url = plugin.getDiscordConfig().getType("tempban").getUrl();
      payload = plugin.getDiscordConfig().getType("tempban").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("tempban").isIgnoreSilent();
    }

    payload = payload.replace("[player]", ban.getPlayer().getName())
        .replace("[playerId]", ban.getPlayer().getUUID().toString())
        .replace("[actor]", ban.getActor().getName())
        .replace("[actorId]", ban.getActor().getUUID().toString())
        .replace("[id]", String.valueOf(ban.getId()))
        .replace("[created]", toISO8601(ban.getCreated()))
        .replace("[reason]", ban.getReason());

    if (ban.getExpires() != 0) {
      payload = payload.replace("[expires]", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    return new Object[] { url, payload, ignoreSilent };
  }

  public Object[] notifyOnBan(IpBanData ban) {
    String url;
    String payload;
    boolean ignoreSilent;
    List<PlayerData> players = plugin.getPlayerStorage().getDuplicatesInTime(ban.getIp(),
        plugin.getConfig().getTimeAssociatedAlts());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() >= 2)
      playerNames.setLength(playerNames.length() - 2);

    if (ban.getExpires() == 0) {
      url = plugin.getDiscordConfig().getType("banip").getUrl();
      payload = plugin.getDiscordConfig().getType("banip").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("banip").isIgnoreSilent();
    } else {
      url = plugin.getDiscordConfig().getType("tempbanip").getUrl();
      payload = plugin.getDiscordConfig().getType("tempbanip").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("tempbanip").isIgnoreSilent();
    }

    payload = payload.replace("[ip]", ban.getIp().toString())
        .replace("[actor]", ban.getActor().getName())
        .replace("[actorId]", ban.getActor().getUUID().toString())
        .replace("[reason]", ban.getReason())
        .replace("[created]", toISO8601(ban.getCreated()))
        .replace("[players]", playerNames.toString());

    if (ban.getExpires() != 0) {
      payload = payload.replace("[expires]", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    return new Object[] { url, payload, ignoreSilent };
  }

  public Object[] notifyOnKick(PlayerKickData kick) {
    String url = plugin.getDiscordConfig().getType("kick").getUrl();
    String payload = plugin.getDiscordConfig().getType("kick").getPayload();
    boolean ignoreSilent = plugin.getDiscordConfig().getType("kick").isIgnoreSilent();

    payload = payload.replace("[player]", kick.getPlayer().getName())
        .replace("[playerId]", kick.getPlayer().getUUID().toString())
        .replace("[actor]", kick.getActor().getName())
        .replace("[actorId]", kick.getActor().getUUID().toString())
        .replace("[id]", String.valueOf(kick.getId()))
        .replace("[created]", toISO8601(kick.getCreated()))
        .replace("[reason]", kick.getReason());

    return new Object[] { url, payload, ignoreSilent };
  }

  public Object[] notifyOnMute(PlayerMuteData mute) {
    String url;
    String payload;
    boolean ignoreSilent;

    if (mute.getExpires() == 0) {
      url = plugin.getDiscordConfig().getType("mute").getUrl();
      payload = plugin.getDiscordConfig().getType("mute").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("mute").isIgnoreSilent();
    } else {
      url = plugin.getDiscordConfig().getType("tempmute").getUrl();
      payload = plugin.getDiscordConfig().getType("tempmute").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("tempmute").isIgnoreSilent();
    }

    payload = payload.replace("[player]", mute.getPlayer().getName())
        .replace("[playerId]", mute.getPlayer().getUUID().toString())
        .replace("[actor]", mute.getActor().getName())
        .replace("[actorId]", mute.getActor().getUUID().toString())
        .replace("[id]", String.valueOf(mute.getId()))
        .replace("[created]", toISO8601(mute.getCreated()))
        .replace("[reason]", mute.getReason());

    if (mute.getExpires() != 0) {
      payload = payload.replace("[expires]", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    return new Object[] { url, payload, ignoreSilent };
  }

  public Object[] notifyOnWarn(PlayerWarnData warn) {
    String url;
    String payload;
    boolean ignoreSilent;

    if (warn.getExpires() == 0) {
      url = plugin.getDiscordConfig().getType("warning").getUrl();
      payload = plugin.getDiscordConfig().getType("warning").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("warning").isIgnoreSilent();
    } else {
      url = plugin.getDiscordConfig().getType("tempwarning").getUrl();
      payload = plugin.getDiscordConfig().getType("tempwarning").getPayload();
      ignoreSilent = plugin.getDiscordConfig().getType("tempwarning").isIgnoreSilent();
    }

    payload = payload.replace("[player]", warn.getPlayer().getName())
        .replace("[playerId]", warn.getPlayer().getUUID().toString())
        .replace("[actor]", warn.getActor().getName())
        .replace("[actorId]", warn.getActor().getUUID().toString())
        .replace("[id]", String.valueOf(warn.getId()))
        .replace("[created]", toISO8601(warn.getCreated()))
        .replace("[points]", String.valueOf(warn.getPoints()))
        .replace("[reason]", warn.getReason());

    if (warn.getExpires() != 0) {
      payload = payload.replace("[expires]", DateUtils.getDifferenceFormat(warn.getExpires()));
    }

    return new Object[] { url, payload, ignoreSilent };
  }

  public Object[] notifyOnUnban(PlayerBanData ban, PlayerData actor, String reason) {
    String url = plugin.getDiscordConfig().getType("unban").getUrl();
    String payload = plugin.getDiscordConfig().getType("unban").getPayload();

    payload = payload.replace("[player]", ban.getPlayer().getName())
        .replace("[playerId]", ban.getPlayer().getUUID().toString())
        .replace("[actor]", actor.getName())
        .replace("[actorId]", actor.getUUID().toString())
        .replace("[id]", String.valueOf(ban.getId()))
        .replace("[created]", toISO8601(ban.getCreated()))
        .replace("[reason]", reason);

    return new Object[] { url, payload };
  }

  public Object[] notifyOnUnban(IpBanData ban, PlayerData actor, String reason) {
    String url = plugin.getDiscordConfig().getType("unbanip").getUrl();
    String payload = plugin.getDiscordConfig().getType("unbanip").getPayload();

    payload = payload.replace("[ip]", ban.getIp().toString())
        .replace("[actor]", actor.getName())
        .replace("[actorId]", actor.getUUID().toString())
        .replace("[id]", String.valueOf(ban.getId()))
        .replace("[created]", toISO8601(ban.getCreated()))
        .replace("[reason]", reason);

    return new Object[] { url, payload };
  }

  public Object[] notifyOnUnmute(PlayerMuteData mute, PlayerData actor, String reason) {
    String url = plugin.getDiscordConfig().getType("unmute").getUrl();
    String payload = plugin.getDiscordConfig().getType("unmute").getPayload();

    payload = payload.replace("[player]", mute.getPlayer().getName())
        .replace("[playerId]", mute.getPlayer().getUUID().toString())
        .replace("[actor]", actor.getName())
        .replace("[actorId]", actor.getUUID().toString())
        .replace("[id]", String.valueOf(mute.getId()))
        .replace("[created]", toISO8601(mute.getCreated()))
        .replace("[reason]", reason);

    return new Object[] { url, payload };
  }

  public Object[] notifyOnReport(PlayerReportData report, PlayerData actor, String reason) {
    String url = plugin.getDiscordConfig().getType("report").getUrl();
    String payload = plugin.getDiscordConfig().getType("report").getPayload();
    boolean ignoreSilent = plugin.getDiscordConfig().getType("report").isIgnoreSilent();
    List<PlayerReportLocationData> locations = null;
    try {
      locations = plugin.getPlayerReportLocationStorage().getByReport(report);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    payload = payload.replace("[player]", report.getPlayer().getName())
        .replace("[playerId]", report.getPlayer().getUUID().toString())
        .replace("[actor]", actor.getName())
        .replace("[actorId]", actor.getUUID().toString())
        .replace("[id]", String.valueOf(report.getId()))
        .replace("[created]", toISO8601(report.getCreated()))
        .replace("[reason]", reason);

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
        payload = payload.replace("[playerWorld]", playerLocation.getWorld())
            .replace("[playerX]", String.valueOf(playerLocation.getX()))
            .replace("[playerY]", String.valueOf(playerLocation.getY()))
            .replace("[playerZ]", String.valueOf(playerLocation.getZ()))
            .replace("[playerYaw]", String.valueOf(playerLocation.getYaw()))
            .replace("[playerPitch]", String.valueOf(playerLocation.getPitch()));
      }

      if (actorLocation != null) {
        payload = payload.replace("[actorWorld]", actorLocation.getWorld())
            .replace("[actorX]", String.valueOf(actorLocation.getX()))
            .replace("[actorY]", String.valueOf(actorLocation.getY()))
            .replace("[actorZ]", String.valueOf(actorLocation.getZ()))
            .replace("[actorYaw]", String.valueOf(actorLocation.getYaw()))
            .replace("[actorPitch]", String.valueOf(actorLocation.getPitch()));
      }
    }

    return new Object[] { url, payload, ignoreSilent };
  }

  public void send(String url, String payload) {
    if (plugin.getConfig().isDebugEnabled()) {
      plugin.getLogger().info("Sending Discord webhook to " + url + " with payload:" + payload);
    }

    HttpsURLConnection connection = null;
    try {
      connection = (HttpsURLConnection) new URL(url).openConnection();
      connection.addRequestProperty("Content-Type", "application/json");
      connection.addRequestProperty("User-Agent", "BanManager");
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      try (OutputStream stream = connection.getOutputStream()) {
        stream.write(payload.getBytes());
        stream.flush();
      }

      int responseCode = connection.getResponseCode();
      if (responseCode > 299) {
        plugin.getLogger().warning("Failed to send Discord webhook");
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
      plugin.getLogger().warning("Failed to send Discord message with payload: " + payload);
      plugin.getLogger().warning("Error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
