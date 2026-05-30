package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.event.player.PlayerNoteCreatedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.util.Message;

public class CommonNoteListener {
  private final BanManagerPlugin plugin;

  public CommonNoteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    plugin.getEventBus().subscribe(PlayerNoteCreatedEvent.class, e -> notifyOnNote(e.note(), false));
  }

  public void notifyOnNote(PlayerNote data) {
    notifyOnNote(data, false);
  }

  public void notifyOnNote(PlayerNote data, boolean silent) {
    final String broadcastPermission = "bm.notify.notes";
    Message message = Message.get("notes.notify");

    message.set("player", data.player().name())
        .set("playerId", data.player().uuid().toString())
        .set("actor", data.actor().name())
        .set("id", data.id())
        .set("message", data.message());

    if (!silent) {
      plugin.getServer().broadcast(message, broadcastPermission);
    } else if (plugin.getPlayerStorage().getConsole().getUUID().equals(data.actor().uuid())) {
      plugin.getServer().getConsoleSender().sendMessage(message);
      return;
    }

    CommonPlayer player = plugin.getServer().getPlayer(data.actor().uuid());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (silent || !player.hasPermission(broadcastPermission)) {
      player.sendMessage(message);
    }
  }
}
