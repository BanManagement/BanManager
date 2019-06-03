package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.utils.BukkitCommandUtils;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.events.PlayerNoteCreatedEvent;
import me.confuser.banmanager.util.CommandUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BukkitNoteListener implements Listener {

  private final BMBukkitPlugin plugin;

  public BukkitNoteListener(BMBukkitPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnNote(PlayerNoteCreatedEvent event) {
    PlayerNoteData note = event.getNote();

    Message messageKey = Message.NOTES_NOTIFY;

    String message = messageKey.asString(plugin.getLocaleManager(),
            "player", note.getPlayer().getName(),
           "playerId", note.getPlayer().getUUID().toString(),
           "actor", note.getActor().getName(),
           "message", note.getMessageColours());

    CommandUtils.broadcast(message.toString(), "bm.notify.notes");

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = BukkitCommandUtils.getPlayer(note.getActor().getUUID())) == null) {
      return;
    }

    if (!player.hasPermission("bm.notify.notes")) {
      player.sendMessage(message);
    }
  }

}
