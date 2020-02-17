package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.bukkit.api.events.IpMutedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerMutedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MuteListener implements Listener {
  private final CommonMuteListener listener;

  public MuteListener(BanManagerPlugin plugin) {
    this.listener = new CommonMuteListener(plugin);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnMute(PlayerMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnMute(IpMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }
}
