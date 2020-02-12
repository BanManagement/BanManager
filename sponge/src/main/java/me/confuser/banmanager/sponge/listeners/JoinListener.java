package me.confuser.banmanager.sponge.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.j256.ormlite.dao.CloseableIterator;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.NotesCommand;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.*;
import me.confuser.banmanager.sponge.SpongeServer;
import org.apache.commons.lang3.time.FastDateFormat;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinListener {

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

  @Listener(order = Order.LAST)
  public void banCheck(final ClientConnectionEvent.Auth event) {
    InetAddress address = event.getConnection().getAddress().getAddress();
    if (plugin.getConfig().isCheckOnJoin()) {
      // Check for new bans/mutes
      if (!plugin.getIpBanStorage().isBanned(address)) {
        try {
          IpBanData ban = plugin.getIpBanStorage().retrieveBan(IPUtils.toIPAddress(address));

          if (ban != null) plugin.getIpBanStorage().addBan(ban);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (!plugin.getPlayerBanStorage().isBanned(event.getProfile().getUniqueId())) {
        try {
          PlayerBanData ban = plugin.getPlayerBanStorage().retrieveBan(event.getProfile().getUniqueId());

          if (ban != null) plugin.getPlayerBanStorage().addBan(ban);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      if (!plugin.getPlayerMuteStorage().isMuted(event.getProfile().getUniqueId())) {
        try {
          PlayerMuteData mute = plugin.getPlayerMuteStorage().retrieveMute(event.getProfile().getUniqueId());

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

      message.set("ip", address.toString());
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      event.setCancelled(true);
      event.setMessage(SpongeServer.formatMessage(message.toString()));
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

      message.set("ip", address.toString());
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      event.setCancelled(true);
      event.setMessage(SpongeServer.formatMessage(message.toString()));
      handleJoinDeny(address.toString(), data.getReason());
      return;
    }

    String name = event.getProfile().getName().get();

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

      message.set("name", name);
      message.set("reason", data.getReason());
      message.set("actor", data.getActor().getName());

      event.setCancelled(true);
      event.setMessage(SpongeServer.formatMessage(message.toString()));
      return;
    }

    PlayerBanData data = plugin.getPlayerBanStorage().getBan(event.getProfile().getUniqueId());

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

    event.setCancelled(true);
    event.setMessage(SpongeServer.formatMessage(message.toString()));
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

  @Listener(order = Order.LAST)
  public void onJoin(ClientConnectionEvent.Auth event) {
    PlayerData player = new PlayerData(event.getProfile().getUniqueId(), event.getProfile().getName().get(), IPUtils.toIPAddress(event.getConnection().getAddress().getAddress()));

    try {
      plugin.getPlayerStorage().createOrUpdate(player);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    if (plugin.getConfig().isLogIpsEnabled()) plugin.getPlayerHistoryStorage().create(player);

  }

  @Listener(order = Order.LAST)
  public void onJoin(final ClientConnectionEvent.Join event) {
    final Player player = event.getTargetEntity();
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
              .set("displayName", player.getDisplayNameData().displayName().get().toString())
              .set("player", player.getName())
              .set("reason", warning.getReason())
              .set("actor", warning.getActor().getName())
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

  @Listener(order = Order.LAST)
  public void onPlayerLogin(final ClientConnectionEvent.Login event) {
    User user = event.getTargetUser();
    final IPAddress ip = IPUtils.toIPAddress(event.getConnection().getAddress().getAddress());

    if (plugin.getGeoIpConfig().isEnabled() && !user.hasPermission("bm.exempt.country")) {
      try {
        CountryResponse countryResponse = plugin.getGeoIpConfig().getCountryDatabase().country(event.getConnection().getAddress().getAddress());

        if (!plugin.getGeoIpConfig().isCountryAllowed(countryResponse)) {
          Message message = Message.get("deniedCountry")
              .set("country", countryResponse.getCountry().getName())
              .set("countryIso", countryResponse.getCountry().getIsoCode());
          event.setMessage(SpongeServer.formatMessage(message.toString()));
          event.setCancelled(true);
          return;
        }

      } catch (IOException | GeoIp2Exception e) {
      }
    }

    if (plugin.getConfig().getMaxOnlinePerIp() > 0) {
      int count = 0;

      for (CommonPlayer player : plugin.getServer().getOnlinePlayers()) {
        if (IPUtils.toIPAddress(player.getAddress()).equals(ip)) count++;
      }

      if (count >= plugin.getConfig().getMaxOnlinePerIp()) {
        event.setMessage(SpongeServer.formatMessage(Message.getString("deniedMaxIp")));
        event.setCancelled(true);
        return;
      }
    }

    if (plugin.getConfig().getMaxMultiaccountsRecently() > 0) {
      long timediff = plugin.getConfig().getMultiaccountsTime();

      List<PlayerData> multiaccountPlayers = plugin.getPlayerStorage().getDuplicatesInTime(ip, timediff);

      if (multiaccountPlayers.size() > plugin.getConfig().getMaxMultiaccountsRecently()) {
        event.setMessage(SpongeServer.formatMessage(Message.getString("deniedMultiaccounts")));
        event.setCancelled(true);
        return;
      }
    }

    if (!plugin.getConfig().isDuplicateIpCheckEnabled()) {
      return;
    }

    if (user.hasPermission("bm.exempt.alts")) return;

    plugin.getScheduler().runAsyncLater(() -> {
      final UUID uuid = user.getUniqueId();
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
      message.set("player", user.getName());
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