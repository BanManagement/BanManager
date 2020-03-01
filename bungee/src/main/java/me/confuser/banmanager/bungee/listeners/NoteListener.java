package me.confuser.banmanager.bungee.listeners;


import me.confuser.banmanager.bungee.api.events.PlayerNoteCreatedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonNoteListener;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class NoteListener implements Listener {
  private final CommonNoteListener listener;

  public NoteListener(BanManagerPlugin plugin) {
    this.listener = new CommonNoteListener(plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnNote(PlayerNoteCreatedEvent event) {
    listener.notifyOnNote(event.getNote());
  }

}
