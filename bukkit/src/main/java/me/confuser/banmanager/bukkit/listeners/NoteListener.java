package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.bukkit.api.events.PlayerNoteCreatedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonNoteListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NoteListener implements Listener {
  private final CommonNoteListener listener;

  public NoteListener(BanManagerPlugin plugin) {
    this.listener = new CommonNoteListener(plugin);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnNote(PlayerNoteCreatedEvent event) {
    listener.notifyOnNote(event.getNote());
  }

}
