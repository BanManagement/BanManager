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
import me.confuser.banmanager.util.CommandUtils;

import java.sql.SQLException;
import java.util.List;

public class UnassignSubCommand extends SubCommand<String> {

  public UnassignSubCommand(LocaleManager locale) {
    super(CommandSpec.REPORTS_UNASSIGN.localize(locale), "unassign", CommandPermission.REPORTS_UNASSIGN, Predicates.alwaysFalse());
  }

  //command.reports.unassign

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, String s, List<String> args, String label) {
    if (args.size() != 1) return CommandResult.INVALID_ARGS;

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, "reports unassign", args);
      return CommandResult.SUCCESS;
    }

    final int id;

    try {
      id = Integer.parseInt(args.get(0));
    } catch (NumberFormatException e) {
      Message.REPORT_TP_ERROR_INVALIDID.send(sender, "id", args.get(0));
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
        Message.REPORT_TP_ERROR_NOTFOUND.send(sender);
        return;
      }

      data.setAssignee(null);

      try {
        data.setState(plugin.getReportStateStorage().queryForId(1));
        plugin.getPlayerReportStorage().update(data);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      Message.REPORT_UNASSIGN_PLAYER.send(sender, "id", data.getId());
    });

    return CommandResult.SUCCESS;
  }

  @Override
  public void sendUsage(Sender sender, String label) {
    sender.sendMessage("<id>");
  }

}
