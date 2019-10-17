package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.sponge.api.events.PlayerNoteCreatedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

public class NoteListener {

  private BanManagerPlugin plugin;

  public NoteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
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
