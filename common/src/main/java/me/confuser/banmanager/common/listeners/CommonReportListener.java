package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.event.player.PlayerReportDeletedEvent;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class CommonReportListener {
  private final BanManagerPlugin plugin;

  public CommonReportListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    plugin.getEventBus().subscribe(PlayerReportedEvent.class, e -> notifyOnReport(e.report()));
    plugin.getEventBus().subscribe(PlayerReportDeletedEvent.class, e -> deleteReferences(e.report().id()));
  }

  public void notifyOnReport(PlayerReport data) {
    Message message = Message.get("report.notify");

    message.set("player", data.player().name())
        .set("playerId", data.player().uuid().toString())
        .set("actor", data.actor().name())
        .set("reason", data.reason())
        .set("id", data.id());

    plugin.getServer().broadcast(message, "bm.notify.report");

    CommonPlayer player = plugin.getServer().getPlayer(data.actor().uuid());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (!player.hasPermission("bm.notify.report")) {
      message.sendTo(player);
    }
  }

  public void deleteReferences(int reportId) {
    try {
      DeleteBuilder location = plugin.getPlayerReportLocationStorage().deleteBuilder();
      location.where().eq("report_id", reportId);
      location.delete();

      DeleteBuilder commands = plugin.getPlayerReportCommandStorage().deleteBuilder();
      commands.where().eq("report_id", reportId);
      commands.delete();

      DeleteBuilder comments = plugin.getPlayerReportCommentStorage().deleteBuilder();
      comments.where().eq("report_id", reportId);
      comments.delete();
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to process report", e);
    }
  }
}
