package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.UUID;

public class ChatListener implements Listener {

  private BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void onPlayerChat(AsyncPlayerChatEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();

    if (!plugin.getPlayerMuteStorage().isMuted(uuid)) {
      if (plugin.getPlayerWarnStorage().isMuted(uuid)) {
        PlayerWarnData warning = plugin.getPlayerWarnStorage().getMute(uuid);
        CommonPlayer player = plugin.getServer().getPlayer(event.getPlayer().getUniqueId());

        if (warning.getReason().toLowerCase().equals(event.getMessage().toLowerCase())) {
          plugin.getPlayerWarnStorage().removeMute(uuid);
          Message.get("warn.player.disallowed.removed").sendTo(player);
        } else {
          Message.get("warn.player.disallowed.header").sendTo(player);
          Message.get("warn.player.disallowed.reason").set("reason", warning.getReason()).sendTo(player);
        }

        event.setCancelled(true);
      }

      return;
    }

    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(uuid);

    if (mute.hasExpired()) {
      try {
        plugin.getPlayerMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }

    if (mute.isSoft()) {
      event.getRecipients().clear();
      event.getRecipients().add(event.getPlayer());
      return;
    }

    event.setCancelled(true);

    Message broadcast = Message.get("mute.player.broadcast")
                               .set("message", event.getMessage())
                               .set("displayName", event.getPlayer().getDisplayName())
                               .set("player", event.getPlayer().getName())
                               .set("playerId", uuid.toString())
                               .set("reason", mute.getReason())
                               .set("actor", mute.getActor().getName());

    plugin.getServer().broadcast(broadcast.toString(), "bm.notify.muted");

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("mute.player.disallowed");
    } else {
      message = Message.get("tempmute.player.disallowed")
                       .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", event.getPlayer().getDisplayName())
           .set("player", event.getPlayer().getName())
           .set("playerId", uuid.toString())
           .set("reason", mute.getReason())
           .set("actor", mute.getActor().getName());

    event.getPlayer().sendMessage(message.toString());
  }

  public void onIpChat(AsyncPlayerChatEvent event) {
    if (!plugin.getIpMuteStorage().isMuted(event.getPlayer().getAddress().getAddress())) {
      return;
    }

    IpMuteData mute = plugin.getIpMuteStorage().getMute(event.getPlayer().getAddress().getAddress());

    if (mute.hasExpired()) {
      try {
        plugin.getIpMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }

    if (mute.isSoft()) {
      event.getRecipients().clear();
      event.getRecipients().add(event.getPlayer());
      return;
    }

    event.setCancelled(true);

    Message broadcast = Message.get("muteip.ip.broadcast")
                               .set("message", event.getMessage())
                               .set("displayName", event.getPlayer().getDisplayName())
                               .set("player", event.getPlayer().getName())
                               .set("playerId", event.getPlayer().getUniqueId().toString())
                               .set("reason", mute.getReason())
                               .set("actor", mute.getActor().getName());

    plugin.getServer().broadcast(broadcast.toString(), "bm.notify.mutedip");

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("muteip.ip.disallowed");
    } else {
      message = Message.get("tempmuteip.ip.disallowed")
                       .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", event.getPlayer().getDisplayName())
           .set("player", event.getPlayer().getName())
           .set("playerId", event.getPlayer().getUniqueId().toString())
           .set("reason", mute.getReason())
           .set("actor", mute.getActor().getName())
           .set("ip", IPUtils.toString(mute.getIp()));

    event.getPlayer().sendMessage(message.toString());
  }
}
