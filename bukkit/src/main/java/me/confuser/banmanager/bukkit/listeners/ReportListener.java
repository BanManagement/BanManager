package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;

/**
 * Stores the Bukkit-specific player and actor location at the time a report
 * is filed. Notification dispatch and reference cleanup live in the
 * cross-platform {@link me.confuser.banmanager.common.listeners.CommonReportListener}.
 */
public class ReportListener {
  private final BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    plugin.getEventBus().subscribe(PlayerReportedEvent.class, this::storeLocation);
  }

  private void storeLocation(PlayerReportedEvent event) {
    PlayerReport report = event.report();
    PlayerReportData entity;
    try {
      entity = plugin.getPlayerReportStorage().queryForId(report.id());
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to load report entity for location storage", e);
      return;
    }
    if (entity == null) return;

    Player player = Bukkit.getServer().getPlayer(report.player().uuid());
    Player actor = Bukkit.getServer().getPlayer(report.actor().uuid());

    try {
      createLocation(entity, player);
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to store report location for reported player", e);
    }

    try {
      createLocation(entity, actor);
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to store report location for actor", e);
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
}
