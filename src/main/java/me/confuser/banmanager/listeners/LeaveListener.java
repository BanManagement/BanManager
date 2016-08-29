package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerHistoryData;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;

public class LeaveListener extends Listeners<BanManager> {

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    UUID id = UUIDUtils.getUUID(event.getPlayer());

    if (plugin.getConfiguration().isLogIpsEnabled()) {
      final PlayerHistoryData data = plugin.getPlayerHistoryStorage().remove(id);

      if (data == null) {
        plugin.getLogger().warning("Could not find " + event.getPlayer().getName() + " session history, perhaps they " +
                "disconnected too quickly?");
        return;
      }

      data.setLeave(System.currentTimeMillis() / 1000L);

      plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

        @Override
        public void run() {
          try {
            plugin.getPlayerHistoryStorage().create(data);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }
}
