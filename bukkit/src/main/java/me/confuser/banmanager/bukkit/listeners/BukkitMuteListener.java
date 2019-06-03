package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.utils.BukkitCommandUtils;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.events.IpMutedEvent;
import me.confuser.banmanager.events.PlayerMutedEvent;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class BukkitMuteListener implements Listener {

  private final BMBukkitPlugin plugin;

  public BukkitMuteListener(BMBukkitPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnMute(PlayerMutedEvent event) {
    PlayerMuteData mute = event.getMute();

    String broadcastPermission;
    Message message;

    if (mute.getExpires() == 0) {
      broadcastPermission = "bm.notify.mute";
      message = Message.get("mute.notify");
    } else {
      broadcastPermission = "bm.notify.tempmute";
      message = Message.get("tempmute.notify");
      message.set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("player", mute.getPlayer().getName())
           .set("playerId", mute.getPlayer().getUUID().toString())
           .set("actor", mute.getActor().getName())
           .set("reason", mute.getReason());

    if (!event.isSilent()) {
      CommandUtils.broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = BukkitCommandUtils.getPlayer(mute.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnMute(IpMutedEvent event) {
    IpMuteData mute = event.getMute();

    String broadcastPermission;
    Message message;

    if (mute.getExpires() == 0) {
      broadcastPermission = "bm.notify.muteip";
      message = Message.get("muteip.notify");
    } else {
      broadcastPermission = "bm.notify.tempmuteip";
      message = Message.get("tempmuteip.notify");
      message.set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(mute.getIp());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() == 0) return;
    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    message.set("ip", IPUtils.toString(mute.getIp()))
           .set("actor", mute.getActor().getName())
           .set("reason", mute.getReason())
           .set("players", playerNames.toString());

    if (!event.isSilent()) {
      CommandUtils.broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = BukkitCommandUtils.getPlayer(mute.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
