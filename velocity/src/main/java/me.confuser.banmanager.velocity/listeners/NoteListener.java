package me.confuser.banmanager.velocity.listeners;


import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.api.events.PlayerNoteCreatedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonNoteListener;

public class NoteListener extends Listener {
  private final CommonNoteListener listener;

  public NoteListener(BanManagerPlugin plugin) {
    this.listener = new CommonNoteListener(plugin);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void notifyOnNote(PlayerNoteCreatedEvent event) {
    listener.notifyOnNote(event.getNote());
  }

}
