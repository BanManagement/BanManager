package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonLeaveListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {
  private final CommonLeaveListener listener;

  public LeaveListener(BanManagerPlugin plugin) {
    this.listener = new CommonLeaveListener(plugin);
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    listener.onLeave(event.getPlayer().getUniqueId(), event.getPlayer().getName());
  }
}
