package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;
import me.confuser.banmanager.sponge.api.events.IpMutedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerMutedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

public class MuteListener {
  private final CommonMuteListener listener;

  public MuteListener(BanManagerPlugin plugin) {
    this.listener = new CommonMuteListener(plugin);
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnMute(PlayerMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnMute(IpMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }
}
