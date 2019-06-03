package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.utils.BukkitCommandUtils;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.events.IpMutedEvent;
import me.confuser.banmanager.events.PlayerMutedEvent;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
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
    Message messagem;

    if (mute.getExpires() == 0) {
      broadcastPermission = "bm.notify.mute";
      messagem = Message.MUTE_NOTIFY;
    } else {
      broadcastPermission = "bm.notify.tempmute";
      messagem = Message.TEMPMUTE_NOTIFY;
    }

    String message = messagem.asString(plugin.getLocaleManager(),
            "player", mute.getPlayer().getName(),
            "playerId", mute.getPlayer().getUUID().toString(),
            "actor", mute.getActor().getName(),
            "reason", mute.getReason(),
            "expires", DateUtils.getDifferenceFormat(mute.getExpires()));

    if (!event.isSilent()) {
      CommandUtils.broadcast(message, broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = BukkitCommandUtils.getPlayer(mute.getActor().getUUID())) == null) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      player.sendMessage(message);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnMute(IpMutedEvent event) {
    IpMuteData mute = event.getMute();

    String broadcastPermission;
    Message messagem;

    if (mute.getExpires() == 0) {
      broadcastPermission = "bm.notify.muteip";
      messagem = Message.MUTEIP_NOTIFY;
    } else {
      broadcastPermission = "bm.notify.tempmuteip";
      messagem = Message.TEMPMUTEIP_NOTIFY;
    }

    List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(mute.getIp());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() == 0) return;
    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    String message = messagem.asString(plugin.getLocaleManager(),
            "ip", IPUtils.toString(mute.getIp()),
           "actor", mute.getActor().getName(),
           "reason", mute.getReason(),
           "players", playerNames.toString(),
            "expires", DateUtils.getDifferenceFormat(mute.getExpires()));

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
      player.sendMessage(message);
    }
  }
}
