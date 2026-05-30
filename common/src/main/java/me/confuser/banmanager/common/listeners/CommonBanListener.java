package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.event.ip.IpBannedEvent;
import me.confuser.banmanager.api.event.ip.IpRangeBannedEvent;
import me.confuser.banmanager.api.event.name.NameBannedEvent;
import me.confuser.banmanager.api.event.player.PlayerBannedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.NotificationUtils;

import java.util.List;

/**
 * Subscribes to ban post-events and broadcasts notifications to staff.
 *
 * <p>Constructed once during plugin enable; the constructor wires up
 * subscriptions through the {@link me.confuser.banmanager.api.event.EventBus}
 * so platform listener wrappers are no longer required.</p>
 */
public class CommonBanListener {
  private final BanManagerPlugin plugin;

  public CommonBanListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    plugin.getEventBus().subscribe(PlayerBannedEvent.class, e -> notifyOnBan(e.ban(), e.silent()));
    plugin.getEventBus().subscribe(IpBannedEvent.class, e -> notifyOnBan(e.ban(), e.silent()));
    plugin.getEventBus().subscribe(IpRangeBannedEvent.class, e -> notifyOnBan(e.ban(), e.silent()));
    plugin.getEventBus().subscribe(NameBannedEvent.class, e -> notifyOnBan(e.ban(), e.silent()));
  }

  public void notifyOnBan(PlayerBan data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.expires() == 0) {
      broadcastPermission = "bm.notify.ban";
      event = "ban";
      message = Message.get("ban.notify");
    } else {
      broadcastPermission = "bm.notify.tempban";
      event = "tempban";
      message = Message.get("tempban.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.expires()));
    }

    message
        .set("id", data.id())
        .set("player", data.player().name())
        .set("playerId", data.player().uuid().toString())
        .set("actor", data.actor().name())
        .set("reason", data.reason());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.actor().uuid())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.actor().uuid());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  public void notifyOnBan(IpBan data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.expires() == 0) {
      broadcastPermission = "bm.notify.banip";
      event = "ban";
      message = Message.get("banip.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanip";
      event = "tempban";
      message = Message.get("tempbanip.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.expires()));
    }

    me.confuser.banmanager.common.ipaddr.IPAddress internalIp =
        me.confuser.banmanager.common.impl.IpAddressMapper.toInternal(data.ip());
    List<PlayerData> players = plugin.getPlayerStorage().getDuplicatesInTime(internalIp,
        plugin.getConfig().getTimeAssociatedAlts());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    message
        .set("id", data.id())
        .set("ip", data.ip().toString())
        .set("actor", data.actor().name())
        .set("reason", data.reason())
        .set("players", playerNames.toString());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.actor().uuid())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.actor().uuid());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  public void notifyOnBan(IpRangeBan data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.expires() == 0) {
      broadcastPermission = "bm.notify.baniprange";
      event = "ban";
      message = Message.get("baniprange.notify");
    } else {
      broadcastPermission = "bm.notify.tempbaniprange";
      event = "tempban";
      message = Message.get("tempbaniprange.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.expires()));
    }

    message
        .set("id", data.id())
        .set("from", data.fromIp().toString())
        .set("to", data.toIp().toString())
        .set("actor", data.actor().name())
        .set("reason", data.reason());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.actor().uuid())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.actor().uuid());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  public void notifyOnBan(NameBan data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.expires() == 0) {
      broadcastPermission = "bm.notify.banname";
      event = "ban";
      message = Message.get("banname.notify");
    } else {
      broadcastPermission = "bm.notify.tempbanname";
      event = "tempban";
      message = Message.get("tempbanname.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.expires()));
    }

    message
        .set("id", data.id())
        .set("name", data.name())
        .set("actor", data.actor().name())
        .set("reason", data.reason());

    if (!silent) {
      NotificationUtils.notifyStaff(plugin, event, message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.actor().uuid())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.actor().uuid());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
