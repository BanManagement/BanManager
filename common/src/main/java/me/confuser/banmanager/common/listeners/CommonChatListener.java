package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.net.InetAddress;
import java.sql.SQLException;

public class CommonChatListener {
  private BanManagerPlugin plugin;

  public CommonChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean onPlayerChat(CommonPlayer player, CommonChatHandler handler, String chatMessage) {
    if (!plugin.getPlayerMuteStorage().isMuted(player.getUniqueId())) {
      if (plugin.getPlayerWarnStorage().isMuted(player.getUniqueId())) {
        PlayerWarnData warning = plugin.getPlayerWarnStorage().getMute(player.getUniqueId());

        if (warning.getReason().toLowerCase().equals(chatMessage.toLowerCase())) {
          plugin.getPlayerWarnStorage().removeMute(player.getUniqueId());
          Message.get("warn.player.disallowed.removed").sendTo(player);
        } else {
          Message.get("warn.player.disallowed.header").sendTo(player);
          Message.get("warn.player.disallowed.reason").set("reason", warning.getReason()).sendTo(player);
        }

        return true;
      }

      return false;
    }

    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUniqueId());

    if (mute.hasExpired()) {
      plugin.getPlayerMuteStorage().removeMute(mute);

      plugin.getScheduler().runAsync(() -> {
        try {
          plugin.getPlayerMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          e.printStackTrace();
        }
      });

      return false;
    }

    Message broadcast = Message.get("mute.player.broadcast")
        .set("message", chatMessage)
        .set("displayName", player.getDisplayName())
        .set("player", player.getName())
        .set("playerId", player.getUniqueId().toString())
        .set("reason", mute.getReason())
        .set("id", mute.getId())
        .set("actor", mute.getActor().getName());

    plugin.getServer().broadcast(broadcast.toString(), "bm.notify.muted");

    if (mute.isSoft()) {
      handler.handleSoftMute();
      return false;
    }

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("mute.player.disallowed");
    } else {
      message = Message.get("tempmute.player.disallowed")
          .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", player.getDisplayName())
        .set("player", player.getName())
        .set("playerId", player.getUniqueId().toString())
        .set("reason", mute.getReason())
        .set("id", mute.getId())
        .set("actor", mute.getActor().getName());

    player.sendMessage(message);

    return true;
  }

  public boolean onIpChat(CommonPlayer player, InetAddress address, CommonChatHandler handler, String chatMessage) {
    if (!plugin.getIpMuteStorage().isMuted(address)) {
      return false;
    }

    IpMuteData mute = plugin.getIpMuteStorage().getMute(address);

    if (mute.hasExpired()) {
      plugin.getIpMuteStorage().removeMute(mute);

      plugin.getScheduler().runAsync(() -> {
        try {
          plugin.getIpMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
        } catch (SQLException e) {
          e.printStackTrace();
        }
      });

      return false;
    }

    Message broadcast = Message.get("muteip.ip.broadcast")
        .set("message", chatMessage)
        .set("displayName", player.getDisplayName())
        .set("player", player.getName())
        .set("playerId", player.getUniqueId().toString())
        .set("reason", mute.getReason())
        .set("id", mute.getId())
        .set("actor", mute.getActor().getName());

    plugin.getServer().broadcast(broadcast.toString(), "bm.notify.mutedip");

    if (mute.isSoft()) {
      handler.handleSoftMute();
      return false;
    }

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("muteip.ip.disallowed");
    } else {
      message = Message.get("tempmuteip.ip.disallowed")
          .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", player.getDisplayName())
        .set("player", player.getName())
        .set("playerId", player.getUniqueId().toString())
        .set("reason", mute.getReason())
        .set("actor", mute.getActor().getName())
        .set("id", mute.getId())
        .set("ip", mute.getIp().toString());

    player.sendMessage(message.toString());

    return true;
  }
}
