package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.NotificationUtils;

import java.util.List;

public class CommonBanListener {
  private BanManagerPlugin plugin;

  public CommonBanListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void notifyOnBan(PlayerBanData data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.ban";
      event = "ban";
      message = Message.get("ban.notify");
    } else {
      broadcastPermission = "bm.notify.tempban";
      event = "tempban";
      message = Message.get("tempban.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message
        .set("id", data.getId())
        .set("player", data.getPlayer().getName())
        .set("playerId", data.getPlayer().getUUID().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  public void notifyOnBan(IpBanData data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.banip";
      event = "ban";
      message = Message.get("banip.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanip";
      event = "tempban";
      message = Message.get("tempbanip.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    List<PlayerData> players = plugin.getPlayerStorage().getDuplicatesInTime(data.getIp(), plugin.getConfig().getTimeAssociatedAlts());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    message
        .set("id", data.getId())
        .set("ip", data.getIp().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason())
        .set("players", playerNames.toString());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  public void notifyOnBan(IpRangeBanData data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.baniprange";
      event = "ban";
      message = Message.get("baniprange.notify");
    } else {
      broadcastPermission = "bm.notify.tempbaniprange";
      event = "tempban";
      message = Message.get("tempbaniprange.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message
        .set("id", data.getId())
        .set("from", data.getFromIp().toString())
        .set("to", data.getToIp().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  public void notifyOnBan(NameBanData data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.banname";
      event = "ban";
      message = Message.get("banname.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanname";
      event = "tempban";
      message = Message.get("tempbanname.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message
        .set("id", data.getId())
        .set("name", data.getName())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
