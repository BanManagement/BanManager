package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.Message;

public class CommonNoteListener {
  private BanManagerPlugin plugin;

  public CommonNoteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void notifyOnNote(PlayerNoteData data) {
    Message message = Message.get("notes.notify");

    message.set("player", data.getPlayer().getName())
        .set("playerId", data.getPlayer().getUUID().toString())
        .set("actor", data.getActor().getName())
        .set("id", data.getId())
        .set("message", data.getMessage());

    plugin.getServer().broadcast(message.toString(), "bm.notify.notes");

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(data.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (!player.hasPermission("bm.notify.notes")) {
      player.sendMessage(message);
    }
  }
}
