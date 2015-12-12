package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.events.PlayerNoteCreatedEvent;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class NoteListener extends Listeners<BanManager> {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnNote(PlayerNoteCreatedEvent event) {
    PlayerNoteData note = event.getNote();

    Message message = Message.get("notes.notify");

    message.set("player", note.getPlayer().getName())
           .set("playerId", note.getPlayer().getUUID().toString())
           .set("actor", note.getActor().getName())
           .set("message", note.getMessage());

    CommandUtils.broadcast(message.toString(), "bm.notify.notes");

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = plugin.getServer().getPlayer(note.getActor().getUUID())) == null) {
      return;
    }

    if (!player.hasPermission("bm.notify.notes")) {
      message.sendTo(player);
    }
  }

}
