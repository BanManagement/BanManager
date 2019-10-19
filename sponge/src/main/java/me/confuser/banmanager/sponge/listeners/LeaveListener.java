package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.sql.SQLException;

public class LeaveListener {

  private BanManagerPlugin plugin;

  public LeaveListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Listener
  public void onLeave(ClientConnectionEvent.Disconnect event) {
    if (plugin.getConfig().isLogIpsEnabled()) {
      final PlayerHistoryData data = plugin.getPlayerHistoryStorage().remove(event.getTargetEntity().getUniqueId());

      if (data == null) {
        plugin.getLogger().warning("Could not find " + event.getTargetEntity().getName() + " session history, perhaps they " +
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
