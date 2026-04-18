package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.ReportList;

import java.sql.SQLException;

public class DashboardSubCommand extends CommonSubCommand {

  private static final String COUNT_TOKEN = "count";

  public DashboardSubCommand(BanManagerPlugin plugin) {
    super(plugin, "dashboard");
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser args) {
    BanManagerPlugin plugin = getPlugin();

    int activeBans = plugin.getPlayerBanStorage().getBans().size();
    int activeMutes = plugin.getPlayerMuteStorage().getMutes().size();
    int openReports;

    try {
      ReportList reports = plugin.getPlayerReportStorage().getReports(1, 1);
      openReports = reports != null ? (int) reports.getCount() : 0;
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to fetch report count for dashboard", e);
      openReports = 0;
    }

    Message.get("dashboard.header").sendTo(sender);

    Message.get("dashboard.activeBans")
        .set(COUNT_TOKEN, activeBans)
        .sendTo(sender);

    Message.get("dashboard.activeMutes")
        .set(COUNT_TOKEN, activeMutes)
        .sendTo(sender);

    Message.get("dashboard.openReports")
        .set(COUNT_TOKEN, openReports)
        .sendTo(sender);

    Message.get("dashboard.footer").sendTo(sender);

    return true;
  }

  @Override
  public String getHelp() {
    return "- View staff dashboard summary";
  }

  @Override
  public String getPermission() {
    return "command.bm.dashboard";
  }
}
