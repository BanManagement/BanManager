package me.confuser.banmanager.bukkit.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.j256.ormlite.dao.CloseableIterator;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.bukkit.BukkitServer;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.NotesCommand;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.*;
import org.apache.commons.lang3.time.FastDateFormat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinListener implements Listener {

  // Used for throttling attempted join messages
  Cache<String, Long> joinCache = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .concurrencyLevel(2)
      .maximumSize(100)
      .build();
  private BanManagerPlugin plugin;

  public JoinListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void banCheck(final AsyncPlayerPreLoginEvent event) {
    if (plugin.getConfig().isCheckOnJoin()) {
      // Check for new bans/mutes
      if (!plugin.getIpBanStorage().isBanned(IPUtils.toIPAddress(event.getAddress()))) {
        try {
          IpBanData ban = plugin.getIpBanStorage().retrieveBan(IPUtils.toIPAddress(event.getAddress()));

          if (ban != null) plugin.getIpBanStorage().addBan(ban);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (!plugin.getPlayerBanStorage().isBanned(event.getUniqueId())) {
        try {
          PlayerBanData ban = plugin.getPlayerBanStorage().retrieveBan(event.getUniqueId());

          if (ban != null) plugin.getPlayerBanStorage().addBan(ban);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (!plugin.getPlayerMuteStorage().isMuted(event.getUniqueId())) {
        try {
          PlayerMuteData mute = plugin.getPlayerMuteStorage().retrieveMute(event.getUniqueId());

          if (mute != null) plugin.getPlayerMuteStorage().addMute(mute);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    if (plugin.getIpRangeBanStorage().isBanned(event.getAddress())) {
      IpRangeBanData data = plugin.getIpRangeBanStorage().getBan(event.getAddress());

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

      message.set("ip", event.getAddress().toString());
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
      event.setKickMessage(BukkitServer.formatMessage(message.toString()));
      return;
    }

    if (plugin.getIpBanStorage().isBanned(event.getAddress())) {
      IpBanData data = plugin.getIpBanStorage().getBan(event.getAddress());

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

      message.set("ip", event.getAddress().toString());
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
      event.setKickMessage(BukkitServer.formatMessage(message.toString()));
      handleJoinDeny(event.getAddress().toString(), data.getReason());
      return;
    }

    if (plugin.getNameBanStorage().isBanned(event.getName())) {
      NameBanData data = plugin.getNameBanStorage().getBan(event.getName());

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

      message.set("name", event.getName());
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
      event.setKickMessage(BukkitServer.formatMessage(message.toString()));
      return;
    }

    PlayerBanData data = plugin.getPlayerBanStorage().getBan(event.getUniqueId());

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

    message.set("player", data.getPlayer().getName());
    message.set("reason", data.getReason());
    message.set("actor", data.getActor().getName());

    event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
    event.setKickMessage(BukkitServer.formatMessage(message.toString()));
    handleJoinDeny(data.getPlayer(), data.getReason());
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

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(AsyncPlayerPreLoginEvent event) {
    PlayerData player = new PlayerData(event.getUniqueId(), event.getName(), IPUtils.toIPAddress(event.getAddress()));

    try {
      plugin.getPlayerStorage().createOrUpdate(player);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    if (plugin.getConfig().isLogIpsEnabled()) plugin.getPlayerHistoryStorage().create(player);

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onJoin(final PlayerJoinEvent event) {
    plugin.getScheduler().runAsyncLater(() -> {
      // Handle quick disconnects
      if (event.getPlayer() == null || !event.getPlayer().isOnline()) {
        return;
      }

      UUID id = event.getPlayer().getUniqueId();
      CloseableIterator<PlayerNoteData> notesItr = null;

      try {
        notesItr = plugin.getPlayerNoteStorage().getNotes(id);
        ArrayList<String> notes = new ArrayList<>();
        String dateTimeFormat = Message.getString("notes.dateTimeFormat");
        FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

        while (notesItr != null && notesItr.hasNext()) {
          PlayerNoteData note = notesItr.next();

          Message noteMessage = Message.get("notes.note")
              .set("player", note.getActor().getName())
              .set("message", note.getMessage())
              .set("created", dateFormatter.format(note.getCreated() * 1000L));

          notes.add(noteMessage.toString());
        }

        if (notes.size() != 0) {
          Message noteJoinMessage = Message.get("notes.joinAmount")
              .set("amount", notes.size())
              .set("player", event.getPlayer().getName());

          plugin.getServer().broadcastJSON(NotesCommand.notesAmountMessage(event.getPlayer().getName(), noteJoinMessage), "bm.notify.notes.joinAmount");

          String header = Message.get("notes.header")
              .set("player", event.getPlayer().getName())
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
              .set("displayName", event.getPlayer().getDisplayName())
              .set("player", event.getPlayer().getName())
              .set("reason", warning.getReason())
              .set("actor", warning.getActor().getName())
              .sendTo(plugin.getServer().getPlayer(event.getPlayer().getUniqueId()));

          warning.setRead(true);
          // TODO Move to one update query to set all warnings for player to read
          plugin.getPlayerWarnStorage().update(warning);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (warnings != null) warnings.closeQuietly();
      }

      if (event.getPlayer().hasPermission("bm.notify.reports.open")) {
        try {
          ReportList openReports = plugin.getPlayerReportStorage().getReports(1, 1);

          if (openReports == null || openReports.getList().size() != 0) {
            openReports.send(plugin.getServer().getPlayer(event.getPlayer().getUniqueId()), 1);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (event.getPlayer().hasPermission("bm.notify.reports.assigned")) {
        try {
          ReportList assignedReports = plugin.getPlayerReportStorage().getReports(1, 2, id);

          if (assignedReports == null || assignedReports.getList().size() != 0) {
            assignedReports.send(plugin.getServer().getPlayer(event.getPlayer().getUniqueId()), 1);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

    }, 20L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerLogin(final PlayerLoginEvent event) {
    if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
      return;
    }

    if (plugin.getGeoIpConfig().isEnabled() && !event.getPlayer().hasPermission("bm.exempt.country")) {
      try {
        CountryResponse countryResponse = plugin.getGeoIpConfig().getCountryDatabase().country(event.getAddress());

        if (!plugin.getGeoIpConfig().isCountryAllowed(countryResponse)) {
          Message message = Message.get("deniedCountry")
              .set("country", countryResponse.getCountry().getName())
              .set("countryIso", countryResponse.getCountry().getIsoCode());
          event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message.toString());
          return;
        }

      } catch (IOException | GeoIp2Exception e) {
      }
    }

    final IPAddress ip = IPUtils.toIPAddress(event.getAddress());

    if (plugin.getConfig().getMaxOnlinePerIp() > 0) {
      int count = 0;

      for (CommonPlayer player : plugin.getServer().getOnlinePlayers()) {
        if (IPUtils.toIPAddress(player.getAddress()).equals(ip)) count++;
      }

      if (count >= plugin.getConfig().getMaxOnlinePerIp()) {
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Message.getString("deniedMaxIp"));
        return;
      }

    }

    if (plugin.getConfig().getMaxMultiaccountsRecently() > 0) {
      long timediff = plugin.getConfig().getMultiaccountsTime();

      List<PlayerData> multiaccountPlayers = plugin.getPlayerStorage().getDuplicatesInTime(ip, timediff);

      if (multiaccountPlayers.size() > plugin.getConfig().getMaxMultiaccountsRecently()) {
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Message.getString("deniedMultiaccounts"));
        return;
      }

    }

    if (!plugin.getConfig().isDuplicateIpCheckEnabled()) {
      return;
    }

    if (event.getPlayer().hasPermission("bm.exempt.alts")) return;

    plugin.getScheduler().runAsyncLater(() -> {
      final UUID uuid = event.getPlayer().getUniqueId();
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

      for (PlayerData player : duplicates) {
        if (player.getUUID().equals(uuid)) {
          continue;
        }

        sb.append(player.getName());
        sb.append(", ");
      }

      if (sb.length() == 0) return;
      if (sb.length() >= 2) sb.setLength(sb.length() - 2);

      Message message = Message.get("duplicateIP");
      message.set("player", event.getPlayer().getName());
      message.set("players", sb.toString());

      plugin.getServer().broadcast(message.toString(), "bm.notify.duplicateips");
    }, 20L);
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