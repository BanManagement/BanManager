package me.confuser.banmanager.fabric.listeners;

import java.sql.SQLException;

import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.banmanager.fabric.FabricServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Stores the Fabric-specific player and actor location at the time a report
 * is filed. Notification dispatch and reference cleanup live in the
 * cross-platform {@link me.confuser.banmanager.common.listeners.CommonReportListener}.
 */
public class ReportListener {

  private final FabricServer server;
  private final BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin, FabricServer server) {
    this.plugin = plugin;
    this.server = server;
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

    ServerPlayerEntity player = this.server.getServer().getPlayerManager().getPlayer(report.player().uuid());
    ServerPlayerEntity actor = this.server.getServer().getPlayerManager().getPlayer(report.actor().uuid());

    try {
      createLocation(entity, player, entity.getPlayer());
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to store report location for reported player", e);
    }

    try {
      createLocation(entity, actor, entity.getActor());
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to store report location for actor", e);
    }
  }

  private void createLocation(PlayerReportData report, ServerPlayerEntity player, PlayerData playerData)
      throws SQLException {
    if (player == null || playerData == null)
      return;

    plugin.getPlayerReportLocationStorage()
        .create(new PlayerReportLocationData(report, playerData,
                //? if >=1.21.11 {
                player.getEntityWorld().getRegistryKey().getValue().toString(),
                //? } else {
                /*player.getWorld().getRegistryKey().getValue().toString(),
                *///?}
                player.getX(), player.getY(), player.getZ(),
            player.getPitch(), player.getYaw()));
  }
}
