package me.confuser.banmanager.common.commands.report;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class UnassignSubCommand extends CommonSubCommand {

  public UnassignSubCommand(BanManagerPlugin plugin) {
    super(plugin, "unassign");
  }

  @Override
  public boolean onCommand(final CommonSender sender, final CommandParser parser) {
    if (parser.getArgs().length != 1) return false;

    final int id;

    try {
      id = Integer.parseInt(parser.getArgs()[0]);
    } catch (NumberFormatException e) {
      Message.get("report.tp.error.invalidId").set("id", parser.getArgs()[0]).sendTo(sender);
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
        sender.sendMessage(Message.getString("report.tp.error.notFound"));
        return;
      }

      data.setAssignee(null);

      try {
        data.setState(getPlugin().getReportStateStorage().queryForId(1));
        getPlugin().getPlayerReportStorage().update(data);
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }

      Message.get("report.unassign.player")
             .set("id", data.getId())
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
    return "command.reports.unassign";
  }
}
