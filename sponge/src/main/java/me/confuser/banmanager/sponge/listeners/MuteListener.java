package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.sponge.api.events.IpMutedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerMutedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

import java.util.List;

public class MuteListener {

  private BanManagerPlugin plugin;

  public MuteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
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
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(mute.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
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

    message.set("ip", mute.getIp().toString())
        .set("actor", mute.getActor().getName())
        .set("reason", mute.getReason())
        .set("players", playerNames.toString());

    if (!event.isSilent()) {
      plugin.getServer().broadcast(message.toString(), broadcastPermission);
    }

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(mute.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (event.isSilent() || !player.hasPermission(broadcastPermission)) {
      message.sendTo(player);
    }
  }
}
