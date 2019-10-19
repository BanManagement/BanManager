package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.bukkit.api.events.PlayerNoteCreatedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NoteListener implements Listener {

  private BanManagerPlugin plugin;

  public NoteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnNote(PlayerNoteCreatedEvent event) {
    PlayerNoteData note = event.getNote();

    Message message = Message.get("notes.notify");

    message.set("player", note.getPlayer().getName())
           .set("playerId", note.getPlayer().getUUID().toString())
           .set("actor", note.getActor().getName())
           .set("message", note.getMessage());

    plugin.getServer().broadcast(message.toString(), "bm.notify.notes");

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(note.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (!player.hasPermission("bm.notify.notes")) {
      message.sendTo(player);
    }
  }

}
