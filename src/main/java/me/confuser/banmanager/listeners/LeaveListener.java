package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener extends Listeners<BanManager> {

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    if (plugin.getConfiguration().isWarningMutesEnabled()) {
      plugin.getPlayerWarnStorage().removeMute(event.getPlayer().getUniqueId());
    }
  }
}
