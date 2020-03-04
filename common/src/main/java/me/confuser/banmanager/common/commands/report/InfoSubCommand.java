package me.confuser.banmanager.common.commands.report;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class InfoSubCommand extends CommonSubCommand {

  public InfoSubCommand(BanManagerPlugin plugin) {
    super(plugin, "info");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (sender.isConsole()) return false;
    if (parser.getArgs().length != 1) return false;

    final int id;

    try {
      id = Integer.parseInt(parser.getArgs()[0]);
    } catch (NumberFormatException e) {
      Message.get("report.info.error.invalidId").set("id", parser.getArgs()[0]).sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerReportData data;

      try {
        data = getPlugin().getPlayerReportStorage().queryForId(id);
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }

      if (data == null) {
        Message.get("report.info.error.notFound").sendTo(sender);
        return;
      }

      String dateTimeFormat = Message.getString("report.info.dateTimeFormat");

      Message.get("report.info.notify.report")
          .set("id", data.getId())
          .set("player", data.getPlayer().getName())
          .set("actor", data.getActor().getName())
          .set("reason", data.getReason())
          .set("created", DateUtils.format(dateTimeFormat, data.getCreated()))
          .sendTo(sender);

      PlayerReportLocationData location;

      try {
        location = getPlugin().getPlayerReportLocationStorage().getByReportId(id);
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }

      if (location == null) return;

      Message.get("report.info.notify.location")
          .set("world", location.getWorld())
          .set("x", location.getX())
          .set("y", location.getY())
          .set("z", location.getZ())
          .sendTo(sender);

    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<id>";
  }

  @Override
  public String getPermission() {
    return "command.reports.info";
  }
}
