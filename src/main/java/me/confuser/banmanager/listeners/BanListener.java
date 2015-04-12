package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.events.IpBanEvent;
import me.confuser.banmanager.events.PlayerBanEvent;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class BanListener extends Listeners<BanManager> {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnBan(PlayerBanEvent event) {
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

    message.set("player", ban.getPlayer().getName()).set("actor", ban.getActor().getName())
           .set("reason", ban.getReason());

    CommandUtils.broadcast(message.toString(), broadcastPermission);

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = plugin.getServer().getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (!player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnIpBan(IpBanEvent event) {
    IpBanData ban = event.getBan();

    String broadcastPermission;
    Message message;

    if (ban.getExpires() == 0) {
      broadcastPermission = "bm.notify.ipban";
      message = Message.get("banip.notify");
    } else {
      broadcastPermission = "bm.notify.iptempban";
      message = Message.get("tempbanip.notify");
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    message.set("ip", IPUtils.toString(ban.getIp())).set("actor", ban.getActor().getName())
           .set("reason", ban.getReason());

    CommandUtils.broadcast(message.toString(), broadcastPermission);

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = plugin.getServer().getPlayer(ban.getActor().getUUID())) == null) {
      return;
    }

    if (!player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
