package me.confuser.banmanager.common.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.j256.ormlite.dao.CloseableIterator;
import com.maxmind.db.model.CountryResponse;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.NotesCommand;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class CommonJoinListener {
  // Used for throttling attempted join messages
  Cache<String, Long> joinCache = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .concurrencyLevel(2)
      .maximumSize(100)
      .build();
  private BanManagerPlugin plugin;

  public CommonJoinListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void banCheck(UUID id, String name, IPAddress address, CommonJoinHandler handler) {
    if (plugin.getConfig().isCheckOnJoin()) {
      // Check for new bans/mutes
      if (!plugin.getIpBanStorage().isBanned(address)) {
        try {
          IpBanData ban = plugin.getIpBanStorage().retrieveBan(address);

          if (ban != null) plugin.getIpBanStorage().addBan(ban);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (!plugin.getPlayerBanStorage().isBanned(id)) {
        try {
          PlayerBanData ban = plugin.getPlayerBanStorage().retrieveBan(id);

          if (ban != null) plugin.getPlayerBanStorage().addBan(ban);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (!plugin.getPlayerMuteStorage().isMuted(id)) {
        try {
          PlayerMuteData mute = plugin.getPlayerMuteStorage().retrieveMute(id);

          if (mute != null) plugin.getPlayerMuteStorage().addMute(mute);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    if (plugin.getIpRangeBanStorage().isBanned(address)) {
      IpRangeBanData data = plugin.getIpRangeBanStorage().getBan(address);

      if (data.hasExpired()) {
        try {
          plugin.getIpRangeBanStorage().unban(data, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          e.printStackTrace();
        }

        return;
      }

      Message message;

      if (data.getExpires() == 0) {
        message = Message.get("baniprange.ip.disallowed");
      } else {
        message = Message.get("tempbaniprange.ip.disallowed");
        message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
      }

      message.set("id", data.getId());
      message.set("ip", address.toString());
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      handler.handleDeny(message);
      return;
    }

    if (plugin.getIpBanStorage().isBanned(address)) {
      IpBanData data = plugin.getIpBanStorage().getBan(address);

      if (data.hasExpired()) {
        try {
          plugin.getIpBanStorage().unban(data, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          e.printStackTrace();
        }

        return;
      }

      Message message;

      if (data.getExpires() == 0) {
        message = Message.get("banip.ip.disallowed");
      } else {
        message = Message.get("tempbanip.ip.disallowed");
        message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
      }

      message.set("id", data.getId());
      message.set("ip", address.toString());
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      handler.handleDeny(message);
      handleJoinDeny(address.toString(), data.getReason());
      return;
    }

    if (plugin.getNameBanStorage().isBanned(name)) {
      NameBanData data = plugin.getNameBanStorage().getBan(name);

      if (data.hasExpired()) {
        try {
          plugin.getNameBanStorage().unban(data, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          e.printStackTrace();
        }

        return;
      }

      Message message;

      if (data.getExpires() == 0) {
        message = Message.get("banname.name.disallowed");
      } else {
        message = Message.get("tempbanname.name.disallowed");
        message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
      }

      message.set("id", data.getId());
      message.set("name", name);
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      handler.handleDeny(message);
      return;
    }

    PlayerBanData data = plugin.getPlayerBanStorage().getBan(id);

    if (data != null && data.hasExpired()) {
      try {
        plugin.getPlayerBanStorage().unban(data, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        e.printStackTrace();
      }

      return;
    }

    if (data == null) {
      return;
    }

    Message message;

    if (data.getExpires() == 0) {
      message = Message.get("ban.player.disallowed");
    } else {
      message = Message.get("tempban.player.disallowed");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message.set("id", data.getId());
    message.set("player", data.getPlayer().getName());
    message.set("reason", data.getReason());
    message.set("actor", data.getActor().getName());

    handler.handleDeny(message);
    handleJoinDeny(data.getPlayer(), data.getReason());
  }

  public void onPreJoin(UUID id, String name, IPAddress address) {
    PlayerData player = new PlayerData(id, name, address);

    try {
      plugin.getPlayerStorage().createOrUpdate(player);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    if (plugin.getConfig().isLogIpsEnabled()) plugin.getPlayerHistoryStorage().create(player);
  }

  public void onJoin(final CommonPlayer player) {
    plugin.getScheduler().runAsyncLater(() -> {
      // Handle quick disconnects
      if (player == null || !player.isOnline()) {
        return;
      }

      UUID id = player.getUniqueId();
      CloseableIterator<PlayerNoteData> notesItr = null;

      try {
        notesItr = plugin.getPlayerNoteStorage().getNotes(id);
        ArrayList<String> notes = new ArrayList<>();
        String dateTimeFormat = Message.getString("notes.dateTimeFormat");

        while (notesItr != null && notesItr.hasNext()) {
          PlayerNoteData note = notesItr.next();

          Message noteMessage = Message.get("notes.note")
              .set("player", note.getActor().getName())
              .set("message", note.getMessage())
              .set("id", note.getId())
              .set("created", DateUtils.format(dateTimeFormat, note.getCreated()));

          notes.add(noteMessage.toString());
        }

        if (notes.size() != 0) {
          Message noteJoinMessage = Message.get("notes.joinAmount")
              .set("amount", notes.size())
              .set("player", player.getName());

          plugin.getServer().broadcastJSON(NotesCommand.notesAmountMessage(player.getName(), noteJoinMessage), "bm.notify.notes.joinAmount");

          String header = Message.get("notes.header")
              .set("player", player.getName())
              .toString();

          plugin.getServer().broadcast(header, "bm.notify.notes.join");

          for (String message : notes) {
            plugin.getServer().broadcast(message, "bm.notify.notes.join");
          }

        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (notesItr != null) notesItr.closeQuietly();
      }

      CloseableIterator<PlayerWarnData> warnings = null;
      try {
        warnings = plugin.getPlayerWarnStorage().getUnreadWarnings(id);

        while (warnings.hasNext()) {
          PlayerWarnData warning = warnings.next();

          Message.get("warn.player.warned")
              .set("displayName", player.getDisplayName())
              .set("player", player.getName())
              .set("reason", warning.getReason())
              .set("actor", warning.getActor().getName())
              .set("id", warning.getId())
              .sendTo(plugin.getServer().getPlayer(player.getUniqueId()));

          warning.setRead(true);
          // TODO Move to one update query to set all warnings for player to read
          plugin.getPlayerWarnStorage().update(warning);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (warnings != null) warnings.closeQuietly();
      }

      if (player.hasPermission("bm.notify.reports.open")) {
        try {
          ReportList openReports = plugin.getPlayerReportStorage().getReports(1, 1);

          if (openReports == null || openReports.getList().size() != 0) {
            openReports.send(plugin.getServer().getPlayer(player.getUniqueId()), 1);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (player.hasPermission("bm.notify.reports.assigned")) {
        try {
          ReportList assignedReports = plugin.getPlayerReportStorage().getReports(1, 2, id);

          if (assignedReports == null || assignedReports.getList().size() != 0) {
            assignedReports.send(plugin.getServer().getPlayer(player.getUniqueId()), 1);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

    }, 20L);
  }

  public void onPlayerLogin(final CommonPlayer player, CommonJoinHandler handler) {
    if (plugin.getGeoIpConfig().isEnabled() && !player.hasPermission("bm.exempt.country")) {
      try {
        CountryResponse countryResponse = plugin.getGeoIpConfig().getCountryDatabase().getCountry(player.getAddress());

        if (!plugin.getGeoIpConfig().isCountryAllowed(countryResponse)) {
          Message message = Message.get("deniedCountry")
              .set("country", countryResponse.getCountry().getName())
              .set("countryIso", countryResponse.getCountry().getIsoCode());
          handler.handleDeny(message);
          return;
        }

      } catch (IOException e) {
      }
    }

    final IPAddress ip = IPUtils.toIPAddress(player.getAddress());

    if (plugin.getConfig().getMaxOnlinePerIp() > 0) {
      int count = 0;

      for (CommonPlayer onlinePlayer : plugin.getServer().getOnlinePlayers()) {
        if (IPUtils.toIPAddress(onlinePlayer.getAddress()).equals(ip)) count++;
      }

      if (count >= plugin.getConfig().getMaxOnlinePerIp()) {
        handler.handleDeny(Message.get("deniedMaxIp"));
        return;
      }

    }

    if (plugin.getConfig().getMaxMultiaccountsRecently() > 0) {
      long timeDiff = plugin.getConfig().getMultiaccountsTime();

      List<PlayerData> multiAccountPlayers = plugin.getPlayerStorage().getDuplicatesInTime(ip, timeDiff);

      if (multiAccountPlayers.size() > plugin.getConfig().getMaxMultiaccountsRecently()) {
        handler.handleDeny(Message.get("deniedMultiaccounts"));
        return;
      }

    }

    if (!plugin.getConfig().isDuplicateIpCheckEnabled()) {
      return;
    }

    if (player.hasPermission("bm.exempt.alts")) return;

    plugin.getScheduler().runAsyncLater(() -> {
      // Handle quick disconnects
      if (player == null || !player.isOnline()) {
        return;
      }

      final UUID uuid = player.getUniqueId();
      List<PlayerData> duplicates = plugin.getPlayerBanStorage().getDuplicates(ip);

      if (duplicates.isEmpty()) {
        return;
      }

      if (plugin.getConfig().isDenyAlts()) {
        denyAlts(duplicates, uuid);
      }

      if (plugin.getConfig().isPunishAlts()) {
        try {
          punishAlts(duplicates, uuid);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      StringBuilder sb = new StringBuilder();

      for (PlayerData playerData : duplicates) {
        if (playerData.getUUID().equals(uuid)) {
          continue;
        }

        sb.append(playerData.getName());
        sb.append(", ");
      }

      if (sb.length() == 0) return;
      if (sb.length() >= 2) sb.setLength(sb.length() - 2);

      Message message = Message.get("duplicateIP");
      message.set("player", player.getName());
      message.set("players", sb.toString());

      plugin.getServer().broadcast(message.toString(), "bm.notify.duplicateips");
    }, 20L);

    plugin.getScheduler().runAsyncLater(() -> {
      // Handle quick disconnects
      if (player == null || !player.isOnline()) {
        return;
      }

      final UUID uuid = player.getUniqueId();
      List<PlayerData> duplicates = plugin.getPlayerStorage().getDuplicatesInTime(ip, plugin.getConfig().getTimeAssociatedAlts());

      if (duplicates.isEmpty()) {
        return;
      }

      StringBuilder sb = new StringBuilder();

      for (PlayerData playerData : duplicates) {
        if (playerData.getUUID().equals(uuid)) {
          continue;
        }

        sb.append(playerData.getName());
        sb.append(", ");
      }

      if (sb.length() == 0) return;
      if (sb.length() >= 2) sb.setLength(sb.length() - 2);

      Message message = Message.get("duplicateIPAlts");
      message.set("player", player.getName());
      message.set("players", sb.toString());

      plugin.getServer().broadcast(message.toString(), "bm.notify.alts");
    }, 20L);
  }

  private void handleJoinDeny(PlayerData player, String reason) {
    if (joinCache.getIfPresent(player.getName()) != null) return;

    joinCache.put(player.getName(), System.currentTimeMillis());
    Message message = Message.get("deniedNotify.player").set("player", player.getName()).set("reason", reason);

    plugin.getServer().broadcast(message.toString(), "bm.notify.denied.player");
  }

  private void handleJoinDeny(String ip, String reason) {
    if (joinCache.getIfPresent(ip) != null) return;

    joinCache.put(ip, System.currentTimeMillis());
    Message message = Message.get("deniedNotify.ip").set("ip", ip).set("reason", reason);

    plugin.getServer().broadcast(message.toString(), "bm.notify.denied.ip");
  }

  private void denyAlts(List<PlayerData> duplicates, final UUID uuid) {
    if (plugin.getPlayerBanStorage().isBanned(uuid)) return;

    for (final PlayerData player : duplicates) {
      if (player.getUUID().equals(uuid)) continue;

      final PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

      if (ban == null) continue;
      if (ban.hasExpired()) continue;

      plugin.getScheduler().runSync(() -> {
        CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(uuid);

        Message kickMessage = Message.get("denyalts.player.disallowed")
            .set("player", player.getName())
            .set("reason", ban.getReason())
            .set("id", ban.getId())
            .set("actor", ban.getActor().getName());

        bukkitPlayer.kick(kickMessage.toString());
      });
    }
  }

  private void punishAlts(List<PlayerData> duplicates, UUID uuid) throws SQLException {
    if (!plugin.getPlayerBanStorage().isBanned(uuid)) {
      // Auto ban
      for (PlayerData player : duplicates) {
        if (player.getUUID().equals(uuid)) {
          continue;
        }

        PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

        if (ban == null) continue;
        if (ban.hasExpired()) continue;

        final PlayerBanData newBan = new PlayerBanData(plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid)),
            plugin.getPlayerStorage().getConsole(),
            ban.getReason(),
            ban.isSilent(),
            ban.getExpires());

        plugin.getPlayerBanStorage().ban(newBan);

        plugin.getScheduler().runSync(() -> {
          CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(newBan.getPlayer().getUUID());

          Message kickMessage = Message.get("ban.player.kick")
              .set("displayName", bukkitPlayer.getDisplayName())
              .set("player", newBan.getPlayer().getName())
              .set("reason", newBan.getReason())
              .set("id", newBan.getId())
              .set("actor", newBan.getActor().getName());

          bukkitPlayer.kick(kickMessage.toString());
        });
      }
    } else if (!plugin.getPlayerMuteStorage().isMuted(uuid)) {
      // Auto mute
      for (PlayerData player : duplicates) {
        if (player.getUUID().equals(uuid)) {
          continue;
        }

        PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());

        if (mute == null) continue;
        if (mute.hasExpired()) continue;

        PlayerMuteData newMute = new PlayerMuteData(plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid)),
            plugin.getPlayerStorage().getConsole(),
            mute.getReason(),
            mute.isSilent(),
            mute.isSoft(),
            mute.getExpires());

        plugin.getPlayerMuteStorage().mute(newMute);
      }
    }
  }
}
