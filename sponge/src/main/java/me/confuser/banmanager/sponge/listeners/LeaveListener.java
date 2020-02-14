package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonLeaveListener;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class LeaveListener {
  private final CommonLeaveListener listener;

  public LeaveListener(BanManagerPlugin plugin) {
    this.listener = new CommonLeaveListener(plugin);
  }

  @Listener
  public void onLeave(ClientConnectionEvent.Disconnect event) {
    listener.onLeave(event.getTargetEntity().getUniqueId(), event.getTargetEntity().getName());
  }
}
