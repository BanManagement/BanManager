package me.confuser.banmanager.commands.report;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportCommandData;
import me.confuser.banmanager.data.PlayerReportCommentData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class CloseSubCommand extends SubCommand<BanManager> {

  public CloseSubCommand() {
    super("close");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final String[] args) {
    if (args.length == 0) return false;

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, "reports close", args);
      return true;
    }

    final int id;

    try {
      id = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      Message.get("report.tp.error.invalidId").set("id", args[0]).sendTo(sender);
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerReportData data;

        try {
          data = plugin.getPlayerReportStorage().queryForId(id);
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
          data.setState(plugin.getReportStateStorage().queryForId(4));
          plugin.getPlayerReportStorage().update(data);
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        if (args.length == 1) {
          String message = Message.get("report.close.notify.closed")
                                  .set("actor", data.getActor().getName())
                                  .set("id", data.getId())
                                  .toString();

          CommandUtils.broadcast(message, "bm.notify.report.closed", sender);
          return;
        }

        PlayerData actor = CommandUtils.getActor(sender);

        if (args[1].startsWith("/")) {
          PlayerReportCommandData commandData = new PlayerReportCommandData(data, actor, args[1]
                  .substring(1), StringUtils.join(args, " ", 2, args.length));

          final String command = StringUtils.join(args, " ", 1, args.length);

          // Run command as actor
          plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

            @Override
            public void run() {
              Message.get("report.close.dispatch").set("command", command).sendTo(sender);
              plugin.getServer().dispatchCommand(sender, command.substring(1));
            }
          });

          try {
            plugin.getPlayerReportCommandStorage().create(commandData);
          } catch (SQLException e) {
            sender.sendMessage(Message.getString("sender.error.exception"));
            e.printStackTrace();
            return;
          }

          String message = Message.get("report.close.notify.command")
                                  .set("actor", data.getActor().getName())
                                  .set("id", data.getId())
                                  .set("command", command)
                                  .toString();

          CommandUtils.broadcast(message, "bm.notify.report.closed", sender);
        } else {
          String comment = CommandUtils.getReason(1, args).getMessage();
          PlayerReportCommentData commentData = new PlayerReportCommentData(data, actor, comment);

          try {
            plugin.getPlayerReportCommentStorage().create(commentData);
          } catch (SQLException e) {
            sender.sendMessage(Message.getString("sender.error.exception"));
            e.printStackTrace();
            return;
          }

          String message = Message.get("report.close.notify.comment")
                                  .set("actor", data.getActor().getName())
                                  .set("id", data.getId())
                                  .set("comment", comment)
                                  .toString();

          CommandUtils.broadcast(message, "bm.notify.report.closed", sender);
        }

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
    return "command.report.close";
  }
}
