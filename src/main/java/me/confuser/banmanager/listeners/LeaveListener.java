package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener extends Listeners<BanManager> {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onLeave(PlayerQuitEvent event) {
    plugin.getPlayerStorage().removeOnline(event.getPlayer().getUniqueId());
  }
}
