package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.event.ip.IpMutedEvent;
import me.confuser.banmanager.api.event.player.PlayerMutedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.IpAddressMapper;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.NotificationUtils;

import java.util.List;

public class CommonMuteListener {
  private final BanManagerPlugin plugin;

  public CommonMuteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    plugin.getEventBus().subscribe(PlayerMutedEvent.class, e -> notifyOnMute(e.mute(), e.silent()));
    plugin.getEventBus().subscribe(IpMutedEvent.class, e -> notifyOnMute(e.mute(), e.silent()));
  }

  public void notifyOnMute(PlayerMute data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.expires() == 0 && !data.onlineOnly()) {
      broadcastPermission = "bm.notify.mute";
      event = "mute";
      message = Message.get("mute.notify");
    } else if (data.onlineOnly()) {
      broadcastPermission = "bm.notify.tempmute";
      event = "tempmute";
      message = Message.get("tempmute.notifyOnline");
      if (data.isPaused()) {
        message.set("expires", DateUtils.formatDifference(data.pausedRemaining()));
      } else {
        message.set("expires", DateUtils.getDifferenceFormat(data.expires()));
      }
    } else {
      broadcastPermission = "bm.notify.tempmute";
      event = "tempmute";
      message = Message.get("tempmute.notify");
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

  public void notifyOnMute(IpMute data, boolean silent) {
    String broadcastPermission;
    String event;
    Message message;

    if (data.expires() == 0) {
      broadcastPermission = "bm.notify.muteip";
      event = "mute";
      message = Message.get("muteip.notify");
    } else {
      broadcastPermission = "bm.notify.tempmuteip";
      event = "tempmute";
      message = Message.get("tempmuteip.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.expires()));
    }

    me.confuser.banmanager.common.ipaddr.IPAddress internalIp = IpAddressMapper.toInternal(data.ip());
    List<PlayerData> players = plugin.getPlayerStorage().getDuplicatesInTime(internalIp,
        plugin.getConfig().getTimeAssociatedAlts());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() == 0) return;
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
}
