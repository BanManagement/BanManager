package me.confuser.banmanager.listeners;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JoinListener extends Listeners<BanManager> {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void banCheck(final AsyncPlayerPreLoginEvent event) {
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
      event.setKickMessage(message.toString());
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
      event.setKickMessage(message.toString());
      handleJoinDeny(event.getAddress().toString(), data.getReason());
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
    event.setKickMessage(message.toString());
    handleJoinDeny(data.getPlayer(), data.getReason());
  }

  private void handleJoinDeny(PlayerData player, String reason) {
    Message message = Message.get("deniedNotify.player").set("player", player.getName()).set("reason", reason);

    CommandUtils.broadcast(message.toString(), "bm.notify.denied.player");
  }

  private void handleJoinDeny(String ip, String reason) {
    Message message = Message.get("deniedNotify.ip").set("ip", ip).set("reason", reason);

    CommandUtils.broadcast(message.toString(), "bm.notify.denied.ip");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(AsyncPlayerPreLoginEvent event) {
    PlayerData player = new PlayerData(event.getUniqueId(), event.getName(), event.getAddress());

    try {
      plugin.getPlayerStorage().createOrUpdate(player);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

    plugin.getPlayerStorage().addOnline(player);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onJoin(final PlayerJoinEvent event) {
    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {

      public void run() {
        // Handle quick disconnects
        if (event.getPlayer() == null || !event.getPlayer().isOnline()) {
          return;
        }

        PlayerData onlinePlayer = plugin.getPlayerStorage().getOnline(event.getPlayer());

        if (onlinePlayer == null) {
          return;
        }

        CloseableIterator<PlayerNoteData> notesItr = null;

        try {
          notesItr = plugin.getPlayerNoteStorage().getNotes(onlinePlayer);
          ArrayList<String> notes = new ArrayList<String>();

          while (notesItr.hasNext()) {
            PlayerNoteData note = notesItr.next();

            Message noteMessage = Message.get("notes.note")
                                         .set("player", note.getActor().getName())
                                         .set("message", note.getMessage());
            notes.add(noteMessage.toString());
          }

          if (notes.size() != 0) {
            String header = Message.get("notes.header")
                                   .set("player", onlinePlayer.getName())
                                   .toString();

            CommandUtils.broadcast(header, "bm.notify.notes.join");

            for (String message : notes) {
              CommandUtils.broadcast(message, "bm.notify.notes.join");
            }

          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          notesItr.closeQuietly();
        }

        CloseableIterator<PlayerWarnData> warnings = null;
        try {
          warnings = plugin.getPlayerWarnStorage().getUnreadWarnings(onlinePlayer);

          while (warnings.hasNext()) {
            PlayerWarnData warning = warnings.next();

            Message.get("warn.player.warned")
                   .set("displayName", event.getPlayer().getDisplayName())
                   .set("player", event.getPlayer().getName())
                   .set("reason", warning.getReason())
                   .set("actor", warning.getActor().getName())
                   .sendTo(event.getPlayer());

            warning.setRead(true);
            // TODO Move to one update query to set all warnings for player to read
            plugin.getPlayerWarnStorage().update(warning);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          warnings.closeQuietly();
        }
      }
    }, 20L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerLogin(final PlayerLoginEvent event) {
    if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
      return;
    }

    if (plugin.getConfiguration().getMaxOnlinePerIp() > 0) {
      long ip = IPUtils.toLong(event.getPlayer().getAddress().getAddress());
      int count = 1;

      for (Player player : plugin.getServer().getOnlinePlayers()) {
        if (IPUtils.toLong(player.getAddress().getAddress()) == ip) count++;
      }

      if (count >= plugin.getConfiguration().getMaxOnlinePerIp()) {
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Message.getString("deniedMaxIp"));
        return;
      }

    }

    if (!plugin.getConfiguration().isDuplicateIpCheckEnabled()) {
      return;
    }

    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {

      public void run() {
        final long ip = IPUtils.toLong(event.getAddress());
        final UUID uuid = event.getPlayer().getUniqueId();
        List<PlayerData> duplicates = plugin.getPlayerBanStorage().getDuplicates(ip);

        if (duplicates.isEmpty()) {
          return;
        }

        if (plugin.getConfiguration().isPunishAlts()) {
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

        CommandUtils.broadcast(message.toString(), "bm.notify.duplicateips");
      }
    }, 20L);
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

        final PlayerBanData newBan = new PlayerBanData(plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid))
                , plugin.getPlayerStorage().getConsole()
                , ban.getReason()
                , ban.getExpires());

        plugin.getPlayerBanStorage().ban(newBan, false);

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Player bukkitPlayer = plugin.getServer().getPlayer(newBan.getPlayer().getUUID());

            Message kickMessage = Message.get("ban.player.kick")
                                         .set("displayName", bukkitPlayer.getDisplayName())
                                         .set("player", newBan.getPlayer().getName())
                                         .set("reason", newBan.getReason())
                                         .set("actor", newBan.getActor().getName());

            bukkitPlayer.kickPlayer(kickMessage.toString());
          }
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

        PlayerMuteData newMute = new PlayerMuteData(plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid))
                , plugin.getPlayerStorage().getConsole()
                , mute.getReason()
                , mute.getExpires());

        plugin.getPlayerMuteStorage().mute(newMute, false);
      }
    }
  }
}