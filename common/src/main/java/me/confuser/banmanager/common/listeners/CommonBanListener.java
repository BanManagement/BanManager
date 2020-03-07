package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.util.List;

public class CommonBanListener {
  private BanManagerPlugin plugin;

  public CommonBanListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void notifyOnBan(PlayerBanData data, boolean silent) {
    String broadcastPermission;
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.ban";
      message = Message.get("ban.notify");
    } else {
      broadcastPermission = "bm.notify.tempban";
      message = Message.get("tempban.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message
        .set("player", data.getPlayer().getName())
        .set("playerId", data.getPlayer().getUUID().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason());

    if (!silent) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
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
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.banip";
      message = Message.get("banip.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanip";
      message = Message.get("tempbanip.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(data.getIp());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() == 0) return;
    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    message
        .set("ip", data.getIp().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason())
        .set("players", playerNames.toString());

    if (!silent) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
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
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.baniprange";
      message = Message.get("baniprange.notify");
    } else {
      broadcastPermission = "bm.notify.tempbaniprange";
      message = Message.get("tempbaniprange.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message
        .set("from", data.getFromIp().toString())
        .set("to", data.getToIp().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason());

    if (!silent) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
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
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.banname";
      message = Message.get("banname.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanname";
      message = Message.get("tempbanname.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message
        .set("name", data.getName())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason());

    if (!silent) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.getActor().getUUID())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(data.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
