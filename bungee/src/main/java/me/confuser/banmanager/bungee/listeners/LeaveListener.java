package me.confuser.banmanager.bungee.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonLeaveListener;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LeaveListener implements Listener {
  private final CommonLeaveListener listener;

  public LeaveListener(BanManagerPlugin plugin) {
    this.listener = new CommonLeaveListener(plugin);
  }

  @EventHandler
  public void onLeave(PlayerDisconnectEvent event) {
    listener.onLeave(event.getPlayer().getUniqueId(), event.getPlayer().getName());
  }
}
