package me.confuser.banmanager.sponge.listeners;

import com.j256.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.banmanager.sponge.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerReportedEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;
import java.util.Optional;

public class ReportListener {

  private BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
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

    if (player == null || !player.isOnline()) {
      return;
    }

    if (!player.hasPermission("bm.notify.report")) {
      message.sendTo(player);
    }
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void storeLocation(PlayerReportedEvent event) {
    PlayerReportData report = event.getReport();

    Optional<Player> player = Sponge.getServer().getPlayer(report.getPlayer().getUUID());
    Optional<Player> actor = Sponge.getServer().getPlayer(report.getActor().getUUID());

    try {
      if (player.isPresent())  createLocation(report, player.get());
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      if (actor.isPresent()) createLocation(report, actor.get());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void createLocation(PlayerReportData report, Player player) throws SQLException {
    if (player == null) return;

    PlayerData playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player.getUniqueId()));
    Location<World> loc = player.getLocation();

    plugin.getPlayerReportLocationStorage()
          .create(new PlayerReportLocationData(report, playerData, player.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), 0, 0));
  }

  @Listener
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
