package me.confuser.banmanager.commands.report;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.data.ReportState;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class ListSubCommand extends SubCommand<BanManager> {

  public ListSubCommand() {
    super("list");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final String[] args) {
    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

      @Override
      public void run() {
        int page = 1;
        Integer state = null;

        if (args.length >= 1) {
          try {
            page = Integer.parseInt(args[0]);
          } catch (NumberFormatException e) {
            Message.get("report.list.error.invalidPage").set("page", args[0]).sendTo(sender);
            return;
          }
        }

        if (args.length == 2) {
          try {
            List<ReportState> states = plugin.getReportStateStorage().queryForEq("name", args[1]);

            if (states.size() == 0) {
              Message.get("report.list.error.invalidState").set("state", args[1]).sendTo(sender);
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
          if (!(sender instanceof Player) || sender.hasPermission("bm.command.report.list.others")) {
            reports = plugin.getPlayerReportStorage().getReports(page, state, null);
          } else {
            reports = plugin.getPlayerReportStorage()
                            .getReports(page, state, ((Player) sender).getUniqueId());
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

        CommandUtils.sendReportList(reports, sender, page);
      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "[page] [state]";
  }

  @Override
  public String getPermission() {
    return "command.report.list";
  }
}
