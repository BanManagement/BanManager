package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.NotesCommand;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.google.guava.cache.Cache;
import me.confuser.banmanager.common.google.guava.cache.CacheBuilder;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.maxmind.db.model.CountryResponse;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.util.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Duration;
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

  // Caches multiaccounts count during async pre-login for use in sync PlayerLoginEvent
  // Short expiry since it's only needed between AsyncPlayerPreLoginEvent and PlayerLoginEvent
  private final Cache<UUID, Integer> multiaccountsCache = CacheBuilder.newBuilder()
      .expireAfterWrite(30, TimeUnit.SECONDS)
      .maximumSize(500)
      .build();

  private BanManagerPlugin plugin;

  public CommonJoinListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void banCheck(UUID id, String name, IPAddress address, CommonJoinHandler handler) {
    // Pre-fetch multiaccounts count (async-safe) and cache for later permission check in PlayerLoginEvent
    if (plugin.getConfig().getMaxMultiaccountsRecently() > 0) {
      try {
        long timeDiff = plugin.getConfig().getMultiaccountsTime();
        List<PlayerData> multiAccountPlayers = plugin.getPlayerStorage().getDuplicatesInTime(address, timeDiff);
        multiaccountsCache.put(id, multiAccountPlayers.size());
      } catch (Exception e) {
        // DB error - don't cache anything; check will be skipped in PlayerLoginEvent (fail-open)
        plugin.getLogger().warning("Failed to pre-check multiaccounts for " + name + ": " + e.getMessage());
      }
    }

    if (plugin.getConfig().isCheckOnJoin()) {
      // Check for new bans/mutes
      if (!plugin.getIpBanStorage().isBanned(address)) {
        try {
          IpBanData ban = plugin.getIpBanStorage().retrieveBan(address);

          if (ban != null) plugin.getIpBanStorage().addBan(ban);
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }
      }

      if (!plugin.getPlayerBanStorage().isBanned(id)) {
        try {
          PlayerBanData ban = plugin.getPlayerBanStorage().retrieveBan(id);

          if (ban != null) plugin.getPlayerBanStorage().addBan(ban);
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }
      }

      if (!plugin.getPlayerMuteStorage().isMuted(id)) {
        try {
          PlayerMuteData mute = plugin.getPlayerMuteStorage().retrieveMute(id);

          if (mute != null) plugin.getPlayerMuteStorage().addMute(mute);
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }
      }
    }

    IpRangeBanData ipRangeBan = plugin.getIpRangeBanStorage().getBan(address);
    if (ipRangeBan != null) {
      if (ipRangeBan.hasExpired()) {
        try {
          plugin.getIpRangeBanStorage().unban(ipRangeBan, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }

        return;
      }

      if (ipRangeBan.getExpires() == 0 && plugin.getExemptionsConfig().isExempt(id, "baniprange")) {
        return;
      } else if (ipRangeBan.getExpires() != 0 && plugin.getExemptionsConfig().isExempt(id, "tempbaniprange")) {
        return;
      }

      String dateTimeFormat;
      Message message;

      if (ipRangeBan.getExpires() == 0) {
        message = Message.get("baniprange.ip.disallowed");
        dateTimeFormat = Message.getString("baniprange.ip.dateTimeFormat");
      } else {
        message = Message.get("tempbaniprange.ip.disallowed");
        message.set("expires", DateUtils.getDifferenceFormat(ipRangeBan.getExpires()));

        dateTimeFormat = Message.getString("tempbaniprange.ip.dateTimeFormat");
      }

      message.set("id", ipRangeBan.getId());
      message.set("ip", address.toString());
      message.set("reason", ipRangeBan.getReason());
      message.set("actor", ipRangeBan.getActor().getName());
      message.set("created", DateUtils.format(dateTimeFormat, ipRangeBan.getCreated()));

      handler.handleDeny(message);
      return;
    }

    IpBanData ipBan = plugin.getIpBanStorage().getBan(address);
    if (ipBan != null) {
      if (ipBan.hasExpired()) {
        try {
          plugin.getIpBanStorage().unban(ipBan, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }

        return;
      }

      String dateTimeFormat;
      Message message;

      if (ipBan.getExpires() == 0) {
        message = Message.get("banip.ip.disallowed");
        dateTimeFormat = Message.getString("banip.ip.dateTimeFormat");
      } else {
        message = Message.get("tempbanip.ip.disallowed");
        message.set("expires", DateUtils.getDifferenceFormat(ipBan.getExpires()));

        dateTimeFormat = Message.getString("tempbanip.ip.dateTimeFormat");
      }

      message.set("id", ipBan.getId());
      message.set("ip", address.toString());
      message.set("reason", ipBan.getReason());
      message.set("actor", ipBan.getActor().getName());
      message.set("created", DateUtils.format(dateTimeFormat, ipBan.getCreated()));

      handler.handleDeny(message);
      handleJoinDeny(address.toString(), ipBan.getActor(), ipBan.getReason());
      return;
    }

    NameBanData nameBan = plugin.getNameBanStorage().getBan(name);
    if (nameBan != null) {
      if (nameBan.hasExpired()) {
        try {
          plugin.getNameBanStorage().unban(nameBan, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }

        return;
      }

      String dateTimeFormat;
      Message message;

      if (nameBan.getExpires() == 0) {
        message = Message.get("banname.name.disallowed");
        dateTimeFormat = Message.getString("banname.name.dateTimeFormat");
      } else {
        message = Message.get("tempbanname.name.disallowed");
        message.set("expires", DateUtils.getDifferenceFormat(nameBan.getExpires()));

        dateTimeFormat = Message.getString("tempbanname.name.dateTimeFormat");
      }

      message.set("id", nameBan.getId());
      message.set("name", name);
      message.set("reason", nameBan.getReason());
      message.set("actor", nameBan.getActor().getName());
      message.set("created", DateUtils.format(dateTimeFormat, nameBan.getCreated()));

      handler.handleDeny(message);
      return;
    }

    PlayerBanData data = plugin.getPlayerBanStorage().getBan(id);

    if (data != null && data.hasExpired()) {
      try {
        plugin.getPlayerBanStorage().unban(data, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        plugin.getLogger().warning("Failed to process player join", e);
      }

      return;
    }

    if (data == null) {
      return;
    }

    String locale = data.getPlayer().getLocale();
    String dateTimeFormat;
    Message message;

    if (data.getExpires() == 0) {
      message = Message.get("ban.player.disallowed");
      dateTimeFormat = Message.getString("ban.player.dateTimeFormat", locale);
    } else {
      message = Message.get("tempban.player.disallowed");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires(), locale));

      dateTimeFormat = Message.getString("tempban.player.dateTimeFormat", locale);
    }

    message.set("id", data.getId());
    message.set("player", data.getPlayer().getName());
    message.set("reason", data.getReason());
    message.set("actor", data.getActor().getName());
    message.set("created", DateUtils.format(dateTimeFormat != null ? dateTimeFormat : "dd-MM-yyyy kk:mm:ss", data.getCreated()));

    handler.handlePlayerDeny(data.getPlayer(), message);
    handleJoinDeny(data.getPlayer(), data.getActor(), data.getReason());
  }

  public void onPreJoin(UUID id, String name, IPAddress address) {
    PlayerData player = new PlayerData(id, name, address);

    try {
      plugin.getPlayerStorage().createOrUpdate(player);
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to process player join", e);
      return;
    }

    try {
      plugin.getPlayerHistoryStorage().startSession(player, plugin.getConfig().isLogIpsEnabled());
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to process player join", e);
    }
  }

  public void onJoin(final CommonPlayer player) {
    plugin.getScheduler().runAsyncLater(() -> {
      // Handle quick disconnects
      if (player == null || !player.isOnline()) {
        return;
      }

      UUID id = player.getUniqueId();

      if (plugin.getConfig().isPerPlayerLocale()) {
        try {
          PlayerData playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(id));
          if (playerData != null) {
            String locale = player.getLocale();
            if (locale != null && !locale.equals(playerData.getLocale())) {
              playerData.setLocale(locale);
              plugin.getPlayerStorage().update(playerData);
            }
          }
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to update player locale", e);
        }
      }

      PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(id);
      if (mute != null && mute.isOnlineOnly() && mute.isPaused()) {
        try {
          plugin.getPlayerMuteStorage().resumeMute(mute);
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }
      }

      CloseableIterator<PlayerNoteData> notesItr = null;

      try {
        notesItr = plugin.getPlayerNoteStorage().getNotes(id);
        ArrayList<Message> notes = new ArrayList<>();
        String dateTimeFormat = Message.getString("notes.dateTimeFormat");

        while (notesItr != null && notesItr.hasNext()) {
          PlayerNoteData note = notesItr.next();

          Message noteMessage = Message.get("notes.note")
              .set("player", note.getActor().getName())
              .set("message", note.getMessage())
              .set("id", note.getId())
              .set("created", DateUtils.format(dateTimeFormat, note.getCreated()));

          notes.add(noteMessage);
        }

        if (notes.size() != 0) {
          Message noteJoinMessage = Message.get("notes.joinAmount")
              .set("amount", notes.size())
              .set("player", player.getName());

          plugin.getServer().broadcast(NotesCommand.notesAmountMessage(player.getName(), noteJoinMessage), "bm.notify.notes.joinAmount");

          Message header = Message.get("notes.header")
              .set("player", player.getName());

          plugin.getServer().broadcast(header, "bm.notify.notes.join");

          for (Message message : notes) {
            plugin.getServer().broadcast(message, "bm.notify.notes.join");
          }

        }
      } catch (SQLException e) {
        plugin.getLogger().warning("Failed to process player join", e);
      } finally {
        if (notesItr != null) notesItr.closeQuietly();
      }

      CloseableIterator<PlayerWarnData> warnings = null;
      try {
        warnings = plugin.getPlayerWarnStorage().getUnreadWarnings(id);
        boolean hasWarnings = false;

        while (warnings.hasNext()) {
          hasWarnings = true;
          PlayerWarnData warning = warnings.next();

          Message.get("warn.player.warned")
              .set("displayName", player.getDisplayName())
              .set("player", player.getName())
              .set("reason", warning.getReason())
              .set("actor", warning.getActor().getName())
              .set("id", warning.getId())
              .sendTo(plugin.getServer().getPlayer(player.getUniqueId()));
        }

        if (hasWarnings) {
          plugin.getPlayerWarnStorage().markAllRead(id);
        }
      } catch (SQLException e) {
        plugin.getLogger().warning("Failed to process player join", e);
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
          plugin.getLogger().warning("Failed to process player join", e);
        }
      }

      if (player.hasPermission("bm.notify.reports.assigned")) {
        try {
          ReportList assignedReports = plugin.getPlayerReportStorage().getReports(1, 2, id);

          if (assignedReports == null || assignedReports.getList().size() != 0) {
            assignedReports.send(plugin.getServer().getPlayer(player.getUniqueId()), 1);
          }
        } catch (SQLException e) {
          plugin.getLogger().warning("Failed to process player join", e);
        }
      }

    }, Duration.ofSeconds(1));
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

    if (plugin.getConfig().getMaxOnlinePerIp() > 0 && !player.hasPermission("bm.exempt.maxonlineperip")) {
      int count = 0;

      for (CommonPlayer onlinePlayer : plugin.getServer().getOnlinePlayers()) {
        if (IPUtils.toIPAddress(onlinePlayer.getAddress()).equals(ip)) count++;
      }

      if (count >= plugin.getConfig().getMaxOnlinePerIp()) {
        handler.handleDeny(Message.get("deniedMaxIp"));
        return;
      }

    }

    if (plugin.getConfig().getMaxMultiaccountsRecently() > 0 && !player.hasPermission("bm.exempt.maxmultiaccountsrecently")) {
      // Use cached count from banCheck() instead of querying DB on main thread
      Integer cachedCount = multiaccountsCache.getIfPresent(player.getUniqueId());

      if (cachedCount != null && cachedCount > plugin.getConfig().getMaxMultiaccountsRecently()) {
        handler.handleDeny(Message.get("deniedMultiaccounts"));
        return;
      }
      // If not cached (DB was down during async phase), fail-open to avoid blocking
    }

    if (!plugin.getConfig().isDuplicateIpCheckEnabled()) {
      return;
    }

    if (player.hasPermission("bm.exempt.alts")) return;

    plugin.getScheduler().runAsyncLater(() -> {
      // Handle quick disconnects
      if (!player.isOnline()) {
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
          plugin.getLogger().warning("Failed to process player join", e);
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

      plugin.getServer().broadcast(message, "bm.notify.duplicateips");
    }, Duration.ofSeconds(1));

    plugin.getScheduler().runAsyncLater(() -> {
      // Handle quick disconnects
      if (!player.isOnline()) {
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

      plugin.getServer().broadcast(message, "bm.notify.alts");
    }, Duration.ofSeconds(1));
  }

  private void handleJoinDeny(PlayerData player, PlayerData actor, String reason) {
    if (joinCache.getIfPresent(player.getName()) != null) return;
    if (plugin.getExemptionsConfig().isExempt(player, "deniedNotify")) return;

    joinCache.put(player.getName(), System.currentTimeMillis());
    Message message = Message.get("deniedNotify.player")
      .set("player", player.getName())
      .set("reason", reason)
      .set("actor", actor.getName());

    plugin.getServer().broadcast(message, "bm.notify.denied.player");
  }

  private void handleJoinDeny(String ip, PlayerData actor, String reason) {
    if (joinCache.getIfPresent(ip) != null) return;

    joinCache.put(ip, System.currentTimeMillis());
    Message message = Message.get("deniedNotify.ip")
      .set("ip", ip)
      .set("reason", reason)
      .set("actor", actor.getName());

    plugin.getServer().broadcast(message, "bm.notify.denied.ip");
  }

  private void denyAlts(List<PlayerData> duplicates, final UUID uuid) {
    if (plugin.getPlayerBanStorage().isBanned(uuid)) return;

    for (final PlayerData player : duplicates) {
      if (player.getUUID().equals(uuid)) continue;

      final PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

      if (ban == null) continue;
      if (ban.hasExpired()) continue;

      CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(uuid);
      if(bukkitPlayer == null) continue;

      plugin.getScheduler().runSync(() -> {
        if (!bukkitPlayer.isOnline()) {
          return;
        }

        Message kickMessage = Message.get("denyalts.player.disallowed")
            .set("player", player.getName())
            .set("reason", ban.getReason())
            .set("id", ban.getId())
            .set("actor", ban.getActor().getName());

        bukkitPlayer.kick(kickMessage);
      });
    }
  }

  private void punishAlts(List<PlayerData> duplicates, UUID uuid) throws SQLException {
    PlayerData targetPlayer = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    PlayerData console = plugin.getPlayerStorage().getConsole();

    if (!plugin.getPlayerBanStorage().isBanned(uuid)) {
      for (PlayerData player : duplicates) {
        if (player.getUUID().equals(uuid)) {
          continue;
        }

        PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

        if (ban == null) continue;
        if (ban.hasExpired()) continue;

        final PlayerBanData newBan = new PlayerBanData(targetPlayer, console,
            ban.getReason(),
            ban.isSilent(),
            ban.getExpires());

        try {
            plugin.getPlayerBanStorage().ban(newBan);
        } catch (SQLIntegrityConstraintViolationException e) {
          plugin.getPlayerBanStorage().addBan(newBan);
        }

        plugin.getScheduler().runSync(() -> {
          CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(newBan.getPlayer().getUUID());

          String kickDateTimeFormat = Message.getString("ban.player.dateTimeFormat");
          Message kickMessage = Message.get("ban.player.kick")
              .set("displayName", bukkitPlayer.getDisplayName())
              .set("player", newBan.getPlayer().getName())
              .set("reason", newBan.getReason())
              .set("id", newBan.getId())
              .set("actor", newBan.getActor().getName())
              .set("created", DateUtils.format(kickDateTimeFormat != null ? kickDateTimeFormat : "yyyy-MM-dd HH:mm:ss", newBan.getCreated()));

          bukkitPlayer.kick(kickMessage);
        });
      }
    } else if (!plugin.getPlayerMuteStorage().isMuted(uuid)) {
      for (PlayerData player : duplicates) {
        if (player.getUUID().equals(uuid)) {
          continue;
        }

        PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());

        if (mute == null) continue;
        if (mute.hasExpired()) continue;

        PlayerMuteData newMute = new PlayerMuteData(targetPlayer, console,
            mute.getReason(),
            mute.isSilent(),
            mute.isSoft(),
            mute.getExpires());

        try {
          plugin.getPlayerMuteStorage().mute(newMute);
        } catch (SQLIntegrityConstraintViolationException e) {
          plugin.getPlayerMuteStorage().addMute(newMute);
        }
      }
    }
  }
}
