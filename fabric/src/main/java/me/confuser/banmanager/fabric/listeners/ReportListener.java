package me.confuser.banmanager.fabric.listeners;

import java.sql.SQLException;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonReportListener;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.banmanager.fabric.BMFabricPlugin;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.fabric.FabricServer;
import me.confuser.banmanager.fabric.BanManagerEvents.SilentValue;
import net.minecraft.server.network.ServerPlayerEntity;
import me.confuser.banmanager.common.data.*;

public class ReportListener {

  private final CommonReportListener listener;
  private FabricServer server;
  private BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin, FabricServer server) {
    this.listener = new CommonReportListener(plugin);
    this.plugin = plugin;
    this.server = server;

    BanManagerEvents.PLAYER_REPORT_EVENT.register(this::notifyOnReport);
    BanManagerEvents.PLAYER_REPORTED_EVENT.register(this::notifyOnReported);
    BanManagerEvents.PLAYER_REPORT_DELETED_EVENT.register(this::notifyOnReportDeleted);
  }

  private boolean notifyOnReport(PlayerReportData data, SilentValue silent) {
    listener.notifyOnReport(data);

    return true;
  }

  private void notifyOnReported(PlayerReportData reportData, boolean silent) {
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
            player.getWorld().getRegistryKey().getValue().toString(), player.getX(), player.getY(), player
                .getZ(),
            player.getPitch(), player.getYaw()));
  }
}
