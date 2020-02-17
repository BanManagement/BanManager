package me.confuser.banmanager.common.listeners;

import com.j256.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class CommonReportListener {
  private BanManagerPlugin plugin;

  public CommonReportListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void notifyOnReport(PlayerReportData data) {
    Message message = Message.get("report.notify");

    message.set("player", data.getPlayer().getName())
        .set("playerId", data.getPlayer().getUUID().toString())
        .set("actor", data.getActor().getName())
        .set("reason", data.getReason())
        .set("id", data.getId());

    plugin.getServer().broadcast(message.toString(), "bm.notify.report");

    // Check if the sender is online and does not have the
    // broadcastPermission
    CommonPlayer player = plugin.getServer().getPlayer(data.getActor().getUUID());

    if (player == null || !player.isOnline()) {
      return;
    }

    if (!player.hasPermission("bm.notify.report")) {
      message.sendTo(player);
    }
  }

  public void deleteReferences(PlayerReportData data) {
    int id = data.getId();

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
