package me.confuser.banmanager.common.commands.report;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportCommandData;
import me.confuser.banmanager.common.data.PlayerReportCommentData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.StringUtils;

import java.sql.SQLException;

public class CloseSubCommand extends CommonSubCommand {

  public CloseSubCommand(BanManagerPlugin plugin) {
    super(plugin, "close");
  }

  @Override
  public boolean onCommand(final CommonSender sender, final CommandParser parser) {
    if (parser.getArgs().length == 0) return false;

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

      try {
        data.setState(getPlugin().getReportStateStorage().queryForId(4));
        getPlugin().getPlayerReportStorage().update(data);
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }

      if (parser.getArgs().length == 1) {
        String message = Message.get("report.close.notify.closed")
                                .set("actor", sender.getName())
                                .set("id", data.getId())
                                .toString();

        getPlugin().getServer().broadcast(message, "bm.notify.report.closed", sender);
        return;
      }

      PlayerData actor = sender.getData();

      if (parser.getArgs()[1].startsWith("/")) {
        PlayerReportCommandData commandData = new PlayerReportCommandData(data, actor, parser.getArgs()[1]
                .substring(1), StringUtils.join(parser.getArgs(), " ", 2, parser.getArgs().length));

        final String command = StringUtils.join(parser.getArgs(), " ", 1, parser.getArgs().length);

        // Run command as actor
        getPlugin().getScheduler().runSync(() -> {
          Message.get("report.close.dispatch").set("command", command).sendTo(sender);
          getPlugin().getServer().dispatchCommand(sender, command.substring(1));
        });

        try {
          getPlugin().getPlayerReportCommandStorage().create(commandData);
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        String message = Message.get("report.close.notify.command")
                                .set("actor", sender.getName())
                                .set("id", data.getId())
                                .set("command", command)
                                .toString();

        getPlugin().getServer().broadcast(message, "bm.notify.report.closed", sender);
      } else {
        if(parser.isInvalidReason()) {
          sender.sendMessage(Message.getString("ban.error.invalidReason"));
          return;
        }
        String comment = parser.getReason(1).getMessage();
        PlayerReportCommentData commentData = new PlayerReportCommentData(data, actor, comment);

        try {
          getPlugin().getPlayerReportCommentStorage().create(commentData);
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        String message = Message.get("report.close.notify.comment")
                                .set("actor", sender.getName())
                                .set("id", data.getId())
                                .set("comment", comment)
                                .toString();

        getPlugin().getServer().broadcast(message, "bm.notify.report.closed", sender);
      }

    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<id> [/command || comment]";
  }

  @Override
  public String getPermission() {
    return "command.reports.close";
  }
}
