package me.confuser.banmanager.fabric.listeners;

import java.sql.SQLException;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonReportListener;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.fabric.FabricServer;
import net.minecraft.server.network.ServerPlayerEntity;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;

public class ReportListener {

  private final CommonReportListener listener;
  private FabricServer server;
  private BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin, FabricServer server) {
    this.listener = new CommonReportListener(plugin);
    this.plugin = plugin;
    this.server = server;

    BanManagerEvents.PLAYER_REPORTED_EVENT.register(this::notifyOnReported);
    BanManagerEvents.PLAYER_REPORT_DELETED_EVENT.register(this::notifyOnReportDeleted);
  }

  private void notifyOnReported(PlayerReportData reportData, boolean silent) {
    listener.notifyOnReport(reportData);
    ServerPlayerEntity player = this.server.getServer().getPlayerManager().getPlayer(reportData.getPlayer().getUUID());
    ServerPlayerEntity actor = this.server.getServer().getPlayerManager().getPlayer(reportData.getActor().getUUID());

    try {
      createLocation(reportData, player, reportData.getPlayer());
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      createLocation(reportData, actor, reportData.getActor());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void notifyOnReportDeleted(PlayerReportData reportData) {
    listener.deleteReferences(reportData);
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
