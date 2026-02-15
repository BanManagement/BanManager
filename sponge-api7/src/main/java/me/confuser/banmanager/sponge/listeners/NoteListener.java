package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonNoteListener;
import me.confuser.banmanager.sponge.api.events.PlayerNoteCreatedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

public class NoteListener {
  private final CommonNoteListener listener;

  public NoteListener(BanManagerPlugin plugin) {
    this.listener = new CommonNoteListener(plugin);
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnNote(PlayerNoteCreatedEvent event) {
    listener.notifyOnNote(event.getNote(), event.isSilent());
  }

}
