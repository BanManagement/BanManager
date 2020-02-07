package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.sponge.api.events.IpBannedEvent;
import me.confuser.banmanager.sponge.api.events.IpRangeBannedEvent;
import me.confuser.banmanager.sponge.api.events.NameBannedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerBannedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

import java.util.List;

public class BanListener {

  private BanManagerPlugin plugin;

  public BanListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
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
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(ban.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
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

    message.set("ip", ban.getIp().toString()).set("actor", ban.getActor().getName())
           .set("reason", ban.getReason())
           .set("players", playerNames.toString());

    if (!event.isSilent()) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(ban.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
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

    message.set("from", ban.getFromIp().toString())
           .set("to", ban.getToIp().toString())
           .set("actor", ban.getActor().getName())
           .set("reason", ban.getReason());

    if (!event.isSilent()) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(ban.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnNameBan(NameBannedEvent event) {
    NameBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.banname";
      message = Message.get("banname.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanname";
      message = Message.get("tempbanname.notify");
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    message.set("name", ban.getName())
           .set("actor", ban.getActor().getName())
           .set("reason", ban.getReason());

    if (!event.isSilent()) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(ban.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
