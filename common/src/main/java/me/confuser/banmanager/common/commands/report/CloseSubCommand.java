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
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportCommandData;
import me.confuser.banmanager.data.PlayerReportCommentData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.CommandUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class CloseSubCommand extends SubCommand<String> {

  public CloseSubCommand(LocaleManager locale) {
    super(CommandSpec.REPORTS_CLOSE.localize(locale), "close", CommandPermission.REPORTS_CLOSE, Predicates.alwaysFalse());
  }

  //command.reports.close

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, String s, List<String> args, String label) {
    if (args.size() == 0) return CommandResult.INVALID_ARGS;

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, "reports close", args);
      return CommandResult.SUCCESS;
    }

    final int id;

    try {
      id = Integer.parseInt(args.get(0));
    } catch (NumberFormatException e) {
      Message.REPORT_TP_ERROR_INVALIDID.send(sender, "id", args.get(0));
      return CommandResult.INVALID_ARGS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(plugin, new Runnable() {

      @Override
      public void run() {
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

        try {
          data.setState(plugin.getReportStateStorage().queryForId(4));
          plugin.getPlayerReportStorage().update(data);
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }

        if (args.size() == 1) {
          String message = Message.REPORT_CLOSE_NOTIFY_CLOSED.asString(plugin.getLocaleManager(),
                                  "actor", data.getActor().getName(),
                                  "id", data.getId());

          CommandUtils.broadcast(message, "bm.notify.report.closed", sender);
          return;
        }

        PlayerData actor = CommandUtils.getActor(sender);

        if (args[1].startsWith("/")) {
          PlayerReportCommandData commandData = new PlayerReportCommandData(data, actor, args.get(1)
                  .substring(1), StringUtils.join(args, " ", 2, args.size()));

          final String command = StringUtils.join(args, " ", 1, args.size());

          // Run command as actor
          plugin.getBootstrap().getScheduler().executeSync(() -> {
            Message.REPORT_CLOSE_DISPATCH.send(sender, "command", command);
            plugin.getServer().dispatchCommand(sender, command.substring(1));
          });

          try {
            plugin.getPlayerReportCommandStorage().create(commandData);
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
            e.printStackTrace();
            return;
          }

          String message = Message.REPORT_CLOSE_NOTIFY_COMMAND.asString(plugin.getLocaleManager(),
                                  "actor", data.getActor().getName(),
                                  "id", data.getId(),
                                  "command", command);


          CommandUtils.broadcast(message, "bm.notify.report.closed", sender);
        } else {
          String comment = CommandUtils.getReason(1, args).getMessage();
          PlayerReportCommentData commentData = new PlayerReportCommentData(data, actor, comment);

          try {
            plugin.getPlayerReportCommentStorage().create(commentData);
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
            e.printStackTrace();
            return;
          }

          String message = Message.REPORT_CLOSE_NOTIFY_COMMENT.asString(plugin.getLocaleManager(),
                                  "actor", data.getActor().getName(),
                                  "id", data.getId(),
                                  "comment", comment);

          CommandUtils.broadcast(message, "bm.notify.report.closed", sender);
        }

      }
    });

    return CommandResult.SUCCESS;
  }

  @Override
  public void sendUsage(Sender sender, String label) {
    sender.sendMessage("<id> [/command || comment]");
  }


}
