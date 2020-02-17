package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.util.List;

public class CommonMuteListener {
  private BanManagerPlugin plugin;

  public CommonMuteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void notifyOnMute(PlayerMuteData data, boolean silent) {
    String broadcastPermission;
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.mute";
      message = Message.get("mute.notify");
    } else {
      broadcastPermission = "bm.notify.tempmute";
      message = Message.get("tempmute.notify");
      message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
    }

    message
        .set("player", data.getPlayer().getName())
        .set("playerId", data.getPlayer().getUUID().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason());

    if (silent) {
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

  public void notifyOnMute(IpMuteData data, boolean silent) {
    String broadcastPermission;
    Message message;

    if (data.getExpires() == 0) {
      broadcastPermission = "bm.notify.muteip";
      message = Message.get("muteip.notify");
    } else {
      broadcastPermission = "bm.notify.tempmuteip";
      message = Message.get("tempmuteip.notify");
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
}
