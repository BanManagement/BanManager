package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class LeaveListener implements Listener {

  private BanManagerPlugin plugin;

  public LeaveListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    if (plugin.getConfig().isLogIpsEnabled()) {
      final PlayerHistoryData data = plugin.getPlayerHistoryStorage().remove(event.getPlayer().getUniqueId());

      if (data == null) {
        plugin.getLogger().warning("Could not find " + event.getPlayer().getName() + " session history, perhaps they " +
                "disconnected too quickly?");
        return;
      }

      data.setLeave(System.currentTimeMillis() / 1000L);

      plugin.getScheduler().runAsync(() -> {
        try {
          plugin.getPlayerHistoryStorage().create(data);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      });
    }
  }
}
