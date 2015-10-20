package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.DateUtils;
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

    event.setCancelled(true);

    if (mute.isSoft()) return;

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("mute.player.disallowed");
    } else {
      message = Message.get("tempmute.player.disallowed")
                       .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", event.getPlayer().getDisplayName())
           .set("player", event.getPlayer().getName())
           .set("reason", mute.getReason())
           .set("actor", mute.getActor().getName());

    event.getPlayer().sendMessage(message.toString());
  }
}
