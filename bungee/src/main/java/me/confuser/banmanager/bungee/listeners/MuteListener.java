package me.confuser.banmanager.bungee.listeners;


import me.confuser.banmanager.bungee.api.events.IpMutedEvent;
import me.confuser.banmanager.bungee.api.events.PlayerMutedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class MuteListener implements Listener {
  private final CommonMuteListener listener;

  public MuteListener(BanManagerPlugin plugin) {
    this.listener = new CommonMuteListener(plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnMute(PlayerMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnMute(IpMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }
}
