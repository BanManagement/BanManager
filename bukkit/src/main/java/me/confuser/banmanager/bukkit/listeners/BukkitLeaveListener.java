package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.data.PlayerHistoryData;
import me.confuser.banmanager.util.BukkitUUIDUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;

public class BukkitLeaveListener implements Listener {

  private final BMBukkitPlugin plugin;

  public BukkitLeaveListener(BMBukkitPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    UUID id = BukkitUUIDUtils.getUUID(event.getPlayer());

    if (plugin.getConfiguration().isLogIpsEnabled()) {
      final PlayerHistoryData data = plugin.getPlayerHistoryStorage().remove(id);

      if (data == null) {
        plugin.getLogger().warn("Could not find " + event.getPlayer().getName() + " session history, perhaps they " +
                "disconnected too quickly?");
        return;
      }

      data.setLeave(System.currentTimeMillis() / 1000L);

      plugin.getBootstrap().getScheduler().executeAsync(() -> {
        try {
          plugin.getPlayerHistoryStorage().create(data);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      });
    }
  }
}
