package me.confuser.banmanager.common.commands.report;

import com.j256.ormlite.stmt.SelectArg;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SubCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.ReportState;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;

public class ListSubCommand extends SubCommand<String> {

  public ListSubCommand(LocaleManager locale) {
    super(CommandSpec.REPORTS_LIST.localize(locale), "list", CommandPermission.REPORTS_LIST, Predicates.alwaysFalse());
  }

  //command.reports.list

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, String s, List<String> args, String label) {
    plugin.getBootstrap().getScheduler().executeSync(() -> {
      int page = 1;
      Integer state = null;

      if (args.size() >= 1) {
        try {
          page = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
          //Message.get("report.list.error.invalidPage").set("page", args[0]).sendTo(sender);//TODO not in config!

          return;
        }
      }

      if (args.size() == 2) {
        try {
          List<ReportState> states = plugin.getReportStateStorage().queryForEq("name", new SelectArg(args.get(1)));

          if (states.size() == 0) {
            //Message.get("report.list.error.invalidState").set("state", args[1]).sendTo(sender);//TODO not in config!
            return;
          }

          state = states.get(0).getId();
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      }

      ReportList reports;

      try {
        if (sender.isConsole() || sender.hasPermission("bm.command.reports.list.others")) {
          reports = plugin.getPlayerReportStorage().getReports(page, state, null);
        } else {
          reports = plugin.getPlayerReportStorage().getReports(page, state, UUIDUtils.getUUID(sender));
        }
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (reports == null || reports.getList().size() == 0) {
        Message.REPORT_LIST_NORESULTS.send(sender);
        return;
      }

      CommandUtils.sendReportList(reports, sender, page);
    });

    return CommandResult.SUCCESS;
  }

  @Override
  public void sendUsage(Sender sender, String label) {
    sender.sendMessage("[page] [state]");
  }


}
