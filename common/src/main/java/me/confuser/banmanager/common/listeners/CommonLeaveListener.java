package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerHistoryData;

import java.sql.SQLException;
import java.util.UUID;

public class CommonLeaveListener {
  private BanManagerPlugin plugin;

  public CommonLeaveListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void onLeave(UUID id, String name) {
    if (plugin.getConfig().isLogIpsEnabled()) {
      final PlayerHistoryData data = plugin.getPlayerHistoryStorage().remove(id);

      if (data == null) {
        plugin.getLogger().warning("Could not find " + name + " session history, perhaps they " +
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
