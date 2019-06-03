package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.utils.BukkitCommandUtils;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.events.IpBannedEvent;
import me.confuser.banmanager.events.IpRangeBannedEvent;
import me.confuser.banmanager.events.NameBannedEvent;
import me.confuser.banmanager.events.PlayerBannedEvent;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class BukkitBanListener implements Listener {

  private final BMBukkitPlugin plugin;

  public BukkitBanListener(BMBukkitPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnBan(PlayerBannedEvent event) {
    PlayerBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.ban";
      message = Message.BAN_NOTIFY;
    } else {
      broadcastPermission = "bm.notify.tempban";
      message = Message.TEMPBAN_NOTIFY;
    }

    String s = message.asString(plugin.getLocaleManager(),
            "player", ban.getPlayer().getName(),
           "playerId", ban.getPlayer().getUUID().toString(),
           "actor", ban.getActor().getName(),
           "reason", ban.getReason(),
            "expires", DateUtils.getDifferenceFormat(ban.getExpires()));

    if (!event.isSilent()) {
      CommandUtils.broadcast(s, broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission

    plugin.getBootstrap().getPlayer(ban.getActor().getUUID()).ifPresent(player -> {
      if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
        player.sendMessage(s);
      }
    });

  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnIpBan(IpBannedEvent event) {
    IpBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.banip";
      message = Message.BANIP_NOTIFY;
    } else {
      broadcastPermission = "bm.notify.tempbanip";
      message = Message.TEMPBANIP_NOTIFY;
    }

    List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(ban.getIp());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() == 0) return;
    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    String s = message.asString(plugin.getLocaleManager(),
            "ip", IPUtils.toString(ban.getIp()),
            "actor", ban.getActor().getName(),
            "reason", ban.getReason(),
            "players", playerNames.toString(),
            "expires", DateUtils.getDifferenceFormat(ban.getExpires()));


    if (!event.isSilent()) {
      CommandUtils.broadcast(s, broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = BukkitCommandUtils.getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      player.sendMessage(s);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnIpRangeBan(IpRangeBannedEvent event) {
    IpRangeBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.baniprange";
      message= Message.BANIPRANGE_NOTIFY;
    } else {
      broadcastPermission = "bm.notify.tempbaniprange";
      message= Message.TEMPBANIPRANGE_NOTIFY;
    }

    String s =message.asString(plugin.getLocaleManager(),
            "from", IPUtils.toString(ban.getFromIp()),
           "to", IPUtils.toString(ban.getToIp()),
           "actor", ban.getActor().getName(),
           "reason", ban.getReason(),
            "expires", DateUtils.getDifferenceFormat(ban.getExpires()));

    if (!event.isSilent()) {
      CommandUtils.broadcast(s, broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = BukkitCommandUtils.getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      player.sendMessage(s);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnNameBan(NameBannedEvent event) {
    NameBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.banname";
      message = Message.BANNAME_NOTIFY;
    } else {
      broadcastPermission = "bm.notify.tempbanname";
      message = Message.TEMPBANNAME_NOTIFY;

    }

    String s = message.asString(plugin.getLocaleManager(),
            "name", ban.getName(),
            "actor", ban.getActor().getName(),
            "reason", ban.getReason(),
            "expires", DateUtils.getDifferenceFormat(ban.getExpires()));

    if (!event.isSilent()) {
      CommandUtils.broadcast(s, broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = BukkitCommandUtils.getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      player.sendMessage(s);
    }
  }
}
