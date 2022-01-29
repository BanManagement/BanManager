package me.confuser.banmanager.common.commands.report;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.data.ReportState;
import me.confuser.banmanager.common.ormlite.stmt.SelectArg;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.ReportList;

import java.sql.SQLException;
import java.util.List;

public class ListSubCommand extends CommonSubCommand {

  public ListSubCommand(BanManagerPlugin plugin) {
    super(plugin, "list");
  }

  @Override
  public boolean onCommand(final CommonSender sender, final CommandParser parser) {
    getPlugin().getScheduler().runAsync(() -> {
      int page = 1;
      Integer state = null;

      if (parser.getArgs().length >= 1) {
        try {
          page = Integer.parseInt(parser.getArgs()[0]);
        } catch (NumberFormatException e) {
          Message.get("report.list.error.invalidPage").set("page", parser.getArgs()[0]).sendTo(sender);
          return;
        }
      }

      if (parser.getArgs().length == 2) {
        try {
          List<ReportState> states = getPlugin().getReportStateStorage()
                                                .queryForEq("name", new SelectArg(parser.getArgs()[1]));

          if (states.size() == 0) {
            Message.get("report.list.error.invalidState").set("state", parser.getArgs()[1]).sendTo(sender);
            return;
          }

          state = states.get(0).getId();
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }
      }

      ReportList reports;

      try {
        if (sender.isConsole() || sender.hasPermission("bm.command.reports.list.others")) {
          reports = getPlugin().getPlayerReportStorage().getReports(page, state, null);
        } else {
          reports = getPlugin().getPlayerReportStorage()
                               .getReports(page, state, sender.getData().getUUID());
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (reports == null || reports.getList().size() == 0) {
        Message.get("report.list.noResults").sendTo(sender);
        return;
      }

      reports.send(sender, page);
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "[page] [state]";
  }

  @Override
  public String getPermission() {
    return "command.reports.list";
  }
}
