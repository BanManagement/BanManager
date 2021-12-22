package me.confuser.banmanager.velocity.listeners;


import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.api.events.IpMutedEvent;
import me.confuser.banmanager.velocity.api.events.PlayerMutedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;


public class MuteListener extends Listener {
  private final CommonMuteListener listener;

  public MuteListener(BanManagerPlugin plugin) {
    this.listener = new CommonMuteListener(plugin);
  }

  @Subscribe(order = PostOrder.LATE)
  public void notifyOnMute(PlayerMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @Subscribe(order = PostOrder.LATE)
  public void notifyOnMute(IpMutedEvent event) {listener.notifyOnMute(event.getMute(), event.isSilent()); }
}
