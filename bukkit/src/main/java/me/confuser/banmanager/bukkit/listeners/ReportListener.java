package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerReportedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.listeners.CommonReportListener;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.SQLException;

public class ReportListener implements Listener {
  private final CommonReportListener listener;
  private BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonReportListener(plugin);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnReport(PlayerReportedEvent event) {
    listener.notifyOnReport(event.getReport());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void storeLocation(PlayerReportedEvent event) {
    PlayerReportData report = event.getReport();

    Player player = Bukkit.getServer().getPlayer(report.getPlayer().getUUID());
    Player actor = Bukkit.getServer().getPlayer(report.getActor().getUUID());

    try {
      createLocation(report, player);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      createLocation(report, actor);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void createLocation(PlayerReportData report, Player player) throws SQLException {
    if (player == null) return;

    PlayerData playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player.getUniqueId()));
    Location loc = player.getLocation();

    plugin.getPlayerReportLocationStorage()
        .create(new PlayerReportLocationData(report, playerData, loc.getWorld().getName(), loc.getX(), loc.getY(), loc
            .getZ()
            , loc.getPitch(), loc.getYaw()));
  }

  @EventHandler
  public void deleteReferences(PlayerReportDeletedEvent event) {
    listener.deleteReferences(event.getReport());
  }
}
