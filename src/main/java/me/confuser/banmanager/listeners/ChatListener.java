package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;

public class ChatListener extends Listeners<BanManager> {

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    if (!plugin.getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId())) {
      return;
    }

    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(event.getPlayer().getUniqueId());

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
                               .set("playerId", event.getPlayer().getUniqueId().toString())
                               .set("reason", mute.getReason())
                               .set("actor", mute.getActor().getName());

    CommandUtils.broadcast(broadcast.toString(), "bm.notify.muted");

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("mute.player.disallowed");
    } else {
      message = Message.get("tempmute.player.disallowed")
                       .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", event.getPlayer().getDisplayName())
           .set("player", event.getPlayer().getName())
           .set("playerId", event.getPlayer().getUniqueId().toString())
           .set("reason", mute.getReason())
           .set("actor", mute.getActor().getName());

    event.getPlayer().sendMessage(message.toString());
  }

  @EventHandler
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

    CommandUtils.broadcast(broadcast.toString(), "bm.notify.mutedip");

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
