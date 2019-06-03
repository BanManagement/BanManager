package me.confuser.banmanager.bukkit.listeners;

import com.j256.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.data.PlayerReportLocationData;
import me.confuser.banmanager.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.events.PlayerReportedEvent;
import me.confuser.banmanager.util.BukkitUUIDUtils;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.SQLException;

public class BukkitReportListener implements Listener {

  private final BMBukkitPlugin plugin;

  public BukkitReportListener(BMBukkitPlugin plugin) {
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

    CommandUtils.broadcast(message.toString(), "bm.notify.report");

    // Check if the sender is online and does not have the
    // broadcastPermission
    Player player;
    if ((player = CommandUtils.getPlayer(report.getActor().getUUID())) == null) {
      return;
    }

    if (!player.hasPermission("bm.notify.report")) {
      message.sendTo(player);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void storeLocation(PlayerReportedEvent event) {
    PlayerReportData report = event.getReport();

    Player player = CommandUtils.getPlayer(report.getPlayer().getUUID());
    Player actor = CommandUtils.getPlayer(report.getActor().getUUID());

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

    PlayerData playerData = plugin.getPlayerStorage().queryForId(BukkitUUIDUtils.toBytes(player));

    Location location = player.getLocation();
    plugin.getPlayerReportLocationStorage()
          .create(new PlayerReportLocationData(report, playerData, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw()));
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
