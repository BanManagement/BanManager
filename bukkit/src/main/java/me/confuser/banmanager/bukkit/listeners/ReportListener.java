package me.confuser.banmanager.bukkit.listeners;

import com.j256.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.bukkit.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerReportedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.SQLException;

public class ReportListener implements Listener {

  private BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnReport(PlayerReportedEvent event) {
    PlayerReportData report = event.getReport();

    Message message = Message.get("report.notify");

    message.set("player", report.getPlayer().getName())
           .set("playerId", report.getPlayer().getUUID().toString())
           .set("actor", report.getActor().getName())
           .set("reason", report.getReason())
           .set("id", report.getId());

    plugin.getServer().broadcast(message.toString(), "bm.notify.report");

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(report.getActor().getUUID());

    if (player == null || player.isOnline()) {
      return;
    }

    if (!player.hasPermission("bm.notify.report")) {
      message.sendTo(player);
    }
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
    int id = event.getReport().getId();


    try {
      DeleteBuilder location = plugin.getPlayerReportLocationStorage().deleteBuilder();
      location.where().eq("report_id", id);
      location.delete();

      DeleteBuilder commands = plugin.getPlayerReportCommandStorage().deleteBuilder();
      commands.where().eq("report_id", id);
      commands.delete();

      DeleteBuilder comments = plugin.getPlayerReportCommentStorage().deleteBuilder();
      comments.where().eq("report_id", id);
      comments.delete();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
