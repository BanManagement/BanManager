package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.events.*;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

public class BanListener extends Listeners<BanManager> {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnBan(PlayerBannedEvent event) {
    PlayerBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.ban";
      message = Message.get("ban.notify");
    } else {
      broadcastPermission = "bm.notify.tempban";
      message = Message.get("tempban.notify");
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    message.set("player", ban.getPlayer().getName())
           .set("playerId", ban.getPlayer().getUUID().toString())
           .set("actor", ban.getActor().getName())
           .set("reason", ban.getReason());

    if (!event.isSilent()) {
      CommandUtils.broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = CommandUtils.getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnIpBan(IpBannedEvent event) {
    IpBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.banip";
      message = Message.get("banip.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanip";
      message = Message.get("tempbanip.notify");
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(ban.getIp());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() == 0) return;
    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    message.set("ip", IPUtils.toString(ban.getIp())).set("actor", ban.getActor().getName())
           .set("reason", ban.getReason())
           .set("players", playerNames.toString());

    if (!event.isSilent()) {
      CommandUtils.broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = CommandUtils.getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnIpRangeBan(IpRangeBannedEvent event) {
    IpRangeBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.baniprange";
      message = Message.get("baniprange.notify");
    } else {
      broadcastPermission = "bm.notify.tempbaniprange";
      message = Message.get("tempbaniprange.notify");
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    message.set("from", IPUtils.toString(ban.getFromIp()))
           .set("to", IPUtils.toString(ban.getToIp()))
           .set("actor", ban.getActor().getName())
           .set("reason", ban.getReason());

    if (!event.isSilent()) {
      CommandUtils.broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = CommandUtils.getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
