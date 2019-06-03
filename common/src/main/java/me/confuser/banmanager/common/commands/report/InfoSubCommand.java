package me.confuser.banmanager.common.commands.report;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SubCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.data.PlayerReportLocationData;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.SQLException;
import java.util.List;

public class InfoSubCommand extends SubCommand<String> {

  //command.reports.info

  public InfoSubCommand(LocaleManager locale) {
    super(CommandSpec.REPORTS_INFO.localize(locale), "info", CommandPermission.REPORTS_INFO, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, String s, List<String> args, String label) {
    if (args.size() != 1) return CommandResult.INVALID_ARGS;

    final int id;

    try {
      id = Integer.parseInt(args.get(0));
    } catch (NumberFormatException e) {
      Message.REPORT_INFO_ERROR_INVALIDID.send(sender, "id", args.get(0));
      return CommandResult.INVALID_ARGS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerReportData data;

      try {
        data = plugin.getPlayerReportStorage().queryForId(id);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (data == null) {
        Message.REPORT_INFO_ERROR_NOTFOUND.send(sender);
        return;
      }

      String dateTimeFormat = Message.REPORT_INFO_DATETIMEFORMAT.getMessage();
      FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

      Message.REPORT_INFO_NOTIFY_REPORT.send(sender,
             "id", data.getId(),
             "player", data.getPlayer().getName(),
             "actor", data.getActor().getName(),
             "reason", data.getReason(),
             "created", dateFormatter.format(data.getCreated() * 1000L));

      PlayerReportLocationData location;

      try {
        location = plugin.getPlayerReportLocationStorage().getByReportId(id);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (location == null) return;

      Message.REPORT_INFO_NOTIFY_LOCATION.send(sender,
             "world", location.getWorld(),
             "x", location.getX(),
             "y", location.getY(),
             "z", location.getZ());

    });

    return CommandResult.SUCCESS;
  }

  @Override
  public void sendUsage(Sender sender, String label) {
    sender.sendMessage("<id>");
  }

}
